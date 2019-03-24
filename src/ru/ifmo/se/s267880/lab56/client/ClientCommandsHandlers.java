package ru.ifmo.se.s267880.lab56.client;

import ru.ifmo.se.s267880.lab56.shared.*;

import javax.xml.transform.Result;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;

interface DataWriterToSocket {
    void writeData(SocketChannel channel) throws IOException;
}

abstract public class ClientCommandsHandlers implements CommandHandlersWithMeeting {
    private String currentCommandName = null;
    private Object[] currentCommandParams;

    /**
     * Set current command information.
     * @param name - name of the command.
     * @param args
     */
    public void setCommandInformation(String name, Object[] args) {
        currentCommandName = name;
        currentCommandParams = args;
    }

    abstract public SocketChannel createChannel() throws IOException;

    ResultToClient queryToServer(DataWriterToSocket dataWriter) throws IOException {
        try (SocketChannel sc = createChannel()) {
            dataWriter.writeData(sc);
            ObjectInputStream in = new ObjectInputStream(Channels.newInputStream(sc));
            ResultToClient res = (ResultToClient) in.readObject();
//            System.out.println(res.getStatus());  // testing
            return res;
        } catch (EOFException | ClassNotFoundException e) {
            throw new IOException("Result sent from server has wrong format or there is a problem with connection.", e);
        }
    }

    private QueryToServer generateQuery() {
        Serializable[] castedParams = new Serializable[currentCommandParams.length];
        for (int i = 0; i < currentCommandParams.length; ++i) {
            assert (currentCommandParams[i] instanceof Serializable);
            castedParams[i] = (Serializable) currentCommandParams[i];
        }

        return new QueryToServer(currentCommandName, castedParams);
    }

    ResultToClient queryToServer() throws IOException {
        return queryToServer(socketChannel -> {
            ObjectOutputStream out = new ObjectOutputStream(Channels.newOutputStream(socketChannel));

//            ByteBuffer bf = ByteBuffer.wrap(Helper.serializableToByteArray(qr));
//            sc.write(bf);
            out.writeObject(generateQuery());
            socketChannel.shutdownOutput();
        });
    }

    /**
     * Add all data from another file into the current collection.
     * @param inputStream the input stream that the data will be imported from.
     */
    @Override
    public void doImport(InputStream inputStream) throws IOException {
        queryToServer(channel -> {
            ByteBuffer queryBuffer = ByteBuffer.wrap(Helper.serializableToByteArray(
                    new QueryToServer(currentCommandName, new Serializable[]{
                            null    // because there is a least 1 element we cannot sent InputStream to server.
                                    // the ServerInputPreprocessor will get the data from Socket and pass to the
                                    // command's handler on server.
                    })
            ));
            channel.write(queryBuffer);
            ByteBuffer dataBuffer = ByteBuffer.wrap(new byte[1024]);
            byte[] data = new byte[1024];
            int numRead;
            while ((numRead = inputStream.read(data)) != -1) {
                dataBuffer.clear();
                dataBuffer.put(data, 0, numRead);
                dataBuffer.flip();
                channel.write(dataBuffer);
            }
            channel.shutdownOutput();
        });
    }

    /**
     * Add meeting into the collection
     * @param meeting the meeting wanted to be add.
     */
    @Override
    public void add(Meeting meeting) throws IOException {
        queryToServer();
    }

    /**
     * List all the meetings.
     */
    @Override
    public List<Meeting> show() throws IOException {
        List<Meeting> meetings = (List<Meeting>) queryToServer().getResult();
        System.out.println("# Meeting list:");
        Iterator<Integer> counter = IntStream.rangeClosed(1, meetings.size()).iterator();
        meetings.stream()
                .map(meeting -> String.format("%3d) %s", counter.next(), meeting))
                .forEachOrdered(System.out::println);
        return meetings;
    }

    /**
     * Remove a meeting from the collection by value.
     * @param meeting the meeting wanted to be removed.
     */
    @Override
    public void remove(Meeting meeting) throws IOException {
        queryToServer();
    }

    /**
     * Remove a meeting from the collection by index.
     * @param num the index (base 1) of the element.
     */
    @Override
    public void remove(int num) throws IOException {
        queryToServer();
    }

    /**
     * Add new meeting into the collection if it's date is before every other meeting in the collection.
     * @param meeting the meeting wanted to be added.
     */
    @Override
    public void addIfMin(Meeting meeting) throws IOException {
        queryToServer();
    }

    /**
     * show file name, number of meeting and the time the file first load during this session.
     */
    @Override
    public Map<String, String> info() throws IOException {
        Map<String, String> result = (Map<String, String>) queryToServer().getResult();
        System.out.println("# Information");
        System.out.println("File name: " + result.get("file"));
        System.out.println("Number of meeting: " + result.get("meeting-count"));
        System.out.println("File load since: " + result.get("since"));
        return result;
    }

    /**
     * Replace the current collection with the ones in another file. Also change the current working file to that file.
     * @param path the path to the file.
     */
    @Override
    public void load(String path) throws Exception {
        queryToServer();
    }

    /**
     * Just change the current working file. The data of that file will be replaced.
     * @param path that path to the file.
     */
    @Override
    public void saveAs(String path) throws IOException {
        queryToServer();
    }

    /**
     * Sort all the meeting ascending by their date.
     */
    @Override
    public void sortByDate() throws IOException {
        queryToServer();
    }

    @Override
    public void sortBytime() throws IOException {
        queryToServer();
    }

    /**
     * Reverse the order of the meetings.
     */
    @Override
    public void reverse() throws IOException {
        queryToServer();
    }

    /**
     * Swap 2 meeting.
     * @param a the index of the first meeting.
     * @param b the index of the second meeting.
     */
    @Override
    public void swap(int a, int b) throws IOException {
        queryToServer();
    }

    /**
     * Clear the collection.
     */
    @Override
    public void clear() throws IOException {
        queryToServer();
    }
}
