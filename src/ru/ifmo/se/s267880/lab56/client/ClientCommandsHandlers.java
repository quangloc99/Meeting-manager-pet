package ru.ifmo.se.s267880.lab56.client;

import ru.ifmo.se.s267880.lab56.shared.*;
import ru.ifmo.se.s267880.lab56.shared.communication.CommandExecuteRequest;
import ru.ifmo.se.s267880.lab56.shared.communication.CommunicationIOException;
import ru.ifmo.se.s267880.lab56.shared.communication.CommandExecuteRespond;
import ru.ifmo.se.s267880.lab56.shared.communication.CommandExecuteRespondStatus;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

abstract public class ClientCommandsHandlers implements SharedCommandHandlers {
    private String currentCommandName = null;
    private Object[] currentCommandParams;
    private boolean isQuite = false;


    abstract public SocketChannel getChannel() throws IOException;

    private class CommandExecutor {
        public CommandExecutor() {}

        public CommandExecuteRespond run() throws Exception {
            SocketChannel channel = getChannel();
            sendData(channel, generateQuery());
            CommandExecuteRespond res = receiveData(channel);
            processResult(res);
            return res;
        }

        protected CommandExecuteRequest generateQuery() throws Exception {
            Serializable[] castedParams = new Serializable[currentCommandParams.length];
            for (int i = 0; i < currentCommandParams.length; ++i) {
                assert (currentCommandParams[i] instanceof Serializable);
                castedParams[i] = (Serializable) currentCommandParams[i];
            }
            return new CommandExecuteRequest(currentCommandName, castedParams);
        }

        protected void sendData(SocketChannel channel, CommandExecuteRequest qr) throws Exception {
            try {
                ByteBuffer bf = ByteBuffer.wrap(Helper.serializableToByteArray(qr));
                channel.write(bf);
            } catch (IOException e) {
                throw new CommunicationIOException("Data cannot be sent to server.", e);
            }
        }

        protected CommandExecuteRespond receiveData(SocketChannel channel) throws Exception {
            try {
                ObjectInputStream in = new ObjectInputStream(Channels.newInputStream(channel));
                return (CommandExecuteRespond) in.readObject();
            } catch (IOException e) {
                throw new CommunicationIOException("Cannot read data sent from server.", e);
            } catch(ClassNotFoundException e) {
                throw new CommunicationIOException("Data sent from server broken/may not be fully sent.", e);
            }
        }

        protected void processResult(CommandExecuteRespond res) throws Exception {
            if (res.getStatus() == CommandExecuteRespondStatus.FAIL) {
                throw res.<Exception>getResult();
            }
            System.out.println("Command " + currentCommandName + " successfully executed.");
            if (isQuite) return;
            System.out.println("# Meeting list (sorted by name):");
            int i = 0;
            for (Meeting meeting: res.getCollection()) {
                System.out.printf("%3d) %s\n", ++i, meeting);
            }
            System.out.println("To get the original order (the real order), please use command `show`.");
        }
    }

    /**
     * Set current command information.
     * @param name - name of the command.
     * @param args
     */
    public void setCommandInformation(String name, Object[] args) {
        currentCommandName = name;
        currentCommandParams = args;
    }

    public void toggleQuite() {
        isQuite = !isQuite;
    }

    /**
     * Add all data from another file into the current collection.
     * @param inputStream the input stream that the data will be imported from.
     */
    @Override
    public void doImport(InputStream inputStream) throws Exception {
        assert(inputStream instanceof FileInputStream);
        new CommandExecutor() {
            @Override
            protected CommandExecuteRequest generateQuery() throws Exception {
                // because there is a least 1 element we cannot sent InputStream to server.
                // the ServerInputPreprocessor will get the data from Socket and pass to the
                // command's handler on server.
                return new CommandExecuteRequest(currentCommandName, new Serializable[]{
                        ((FileInputStream) inputStream).getChannel().size()
                });
            }

            @Override
            protected void sendData(SocketChannel channel, CommandExecuteRequest qr) throws Exception {
                super.sendData(channel, qr);
                ByteBuffer dataBuffer = ByteBuffer.wrap(new byte[1024]);
                byte[] data = new byte[1024];
                int numRead;
                while ((numRead = inputStream.read(data)) != -1) {
                    dataBuffer.clear();
                    dataBuffer.put(data, 0, numRead);
                    dataBuffer.flip();
                    try {
                        channel.write(dataBuffer);
                    } catch (IOException e) {
                        throw new CommunicationIOException("Cannot sent data to server.", e);
                    }
                }
            }
        }.run();
    }

    /**
     * Replace the current collection with the ones in another file. Also change the current working file to that file.
     * @param path the path to the file.
     */
    @Override
    public void load(String path) throws Exception {
        new CommandExecutor().run();
    }

    @Override
    public void save() throws Exception {
        new CommandExecutor().run();
    }

    @Override
    public void saveAs(String path) throws Exception {
        new CommandExecutor().run();
    }

    /**
     * Add meeting into the collection
     * @param meeting the meeting wanted to be add.
     */
    @Override
    public void add(Meeting meeting) throws Exception {
        new CommandExecutor().run();
    }

    /**
     * List all the meetings.
     */
    @Override
    public List<Meeting> show() throws Exception {
        return new CommandExecutor() {
            @Override
            protected void processResult(CommandExecuteRespond res) throws Exception {
                boolean currentQuiteState = isQuite;
                isQuite = true;
                super.processResult(res);
                isQuite = currentQuiteState;

                assert(res.getStatus() == CommandExecuteRespondStatus.SUCCESS);
                List<Meeting> meetings = res.getResult();
                System.out.println("# Meeting list (original order):");
                Iterator<Integer> counter = IntStream.rangeClosed(1, meetings.size()).iterator();
                meetings.stream()
                    .map(meeting -> String.format("%3d) %s", counter.next(), meeting))
                    .forEachOrdered(System.out::println);
            }
        }.run().getResult();
    }

    /**
     * Remove a meeting from the collection by value.
     * @param meeting the meeting wanted to be removed.
     */
    @Override
    public void remove(Meeting meeting) throws Exception {
        new CommandExecutor().run();
    }

    /**
     * Remove a meeting from the collection by index.
     * @param num the index (base 1) of the element.
     */
    @Override
    public void remove(int num) throws Exception {
        new CommandExecutor().run();
    }

    /**
     * Add new meeting into the collection if it's date is before every other meeting in the collection.
     * @param meeting the meeting wanted to be added.
     */
    @Override
    public void addIfMin(Meeting meeting) throws Exception {
        new CommandExecutor().run();
    }

    /**
     * show file name, number of meeting and the time the file first load during this session.
     */
    @Override
    public Map<String, String> info() throws Exception {
        return new CommandExecutor() {
            @Override
            protected void processResult(CommandExecuteRespond res) throws Exception {
                boolean currentQuiteState = isQuite;
                isQuite = true;
                super.processResult(res);
                isQuite = currentQuiteState;

                assert(res.getStatus() == CommandExecuteRespondStatus.SUCCESS);
                Map<String, String> result = res.getResult();
                System.out.println("# Information");
                System.out.println("File name: " + (result.get("file") == null ? "<<no name>>" : result.get("file")));
                System.out.println("Time zone: UTC" + result.get("time-zone"));
                System.out.println("Number of meeting: " + result.get("meeting-count"));
                System.out.println("File load since: " + result.get("since"));
                System.out.println("Is quite: " + currentQuiteState);
            }
        }.run().getResult();
    }

    /**
     * Sort all the meeting ascending by their date.
     */
    @Override
    public void sortByDate() throws Exception {
        new CommandExecutor().run();
    }

    @Override
    public void sortBytime() throws Exception {
        new CommandExecutor().run();
    }

    /**
     * Reverse the order of the meetings.
     */
    @Override
    public void reverse() throws Exception {
        new CommandExecutor().run();
    }

    /**
     * Swap 2 meeting.
     * @param a the index of the first meeting.
     * @param b the index of the second meeting.
     */
    @Override
    public void swap(int a, int b) throws Exception {
        new CommandExecutor().run();
    }

    /**
     * Clear the collection.
     */
    @Override
    public void clear() throws Exception {
        new CommandExecutor().run();
    }

    @Override
    public Map<Integer, ZoneId> listTimeZones(int offset) throws Exception {
        return (new CommandExecutor() {
            @Override
            protected void processResult(CommandExecuteRespond res) throws Exception {
                ZoneUtils.printZonesByZoneOffset(res.getResult());
            }
        }).run().getResult();
    }

    @Override
    public void setTimeZone(int timeZoneKey) throws Exception {
        new CommandExecutor().run();
    }
}
