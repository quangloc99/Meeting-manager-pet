package ru.ifmo.se.s267880.lab56.client;

import ru.ifmo.se.s267880.lab56.shared.CommandHandlersWithMeeting;
import ru.ifmo.se.s267880.lab56.shared.Helper;
import ru.ifmo.se.s267880.lab56.shared.Meeting;
import ru.ifmo.se.s267880.lab56.shared.QueryToServer;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

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

    void defaultCommandHandler() throws IOException {
        SocketChannel sc = createChannel();
        Serializable[] castedParams = new Serializable[currentCommandParams.length];
        for (int i = 0; i < currentCommandParams.length; ++i) {
            assert(currentCommandParams[i] instanceof  Serializable);
            castedParams[i] = (Serializable) currentCommandParams[i];
        }

        ByteBuffer bf = ByteBuffer.wrap(Helper.serializableToByteArray(new QueryToServer(
                currentCommandName, castedParams
        )));
        sc.write(bf);
        sc.close();
    }

    /**
     * Add all data from another file into the current collection.
     * @param path the path to the file.
     */
    public void doImport(String path) throws IOException {
        defaultCommandHandler();
    }

    /**
     * Add meeting into the collection
     * @param meeting the meeting wanted to be add.
     */
    public void add(Meeting meeting) throws IOException {
        defaultCommandHandler();
    }

    /**
     * List all the meetings.
     */
    public void show() throws IOException {
        defaultCommandHandler();
    }

    /**
     * Remove a meeting from the collection by value.
     * @param meeting the meeting wanted to be removed.
     */
    public void remove(Meeting meeting) throws IOException {
        defaultCommandHandler();
    }

    /**
     * Remove a meeting from the collection by index.
     * @param num the index (base 1) of the element.
     */
    public void remove(int num) throws IOException {
        defaultCommandHandler();
    }

    /**
     * Add new meeting into the collection if it's date is before every other meeting in the collection.
     * @param meeting the meeting wanted to be added.
     */
    public void addIfMin(Meeting meeting) throws IOException {
        defaultCommandHandler();
    }

    /**
     * show file name, number of meeting and the time the file first open during this session.
     */
    public void info() throws IOException {
        defaultCommandHandler();
    }

    /**
     * Replace the current collection with the ones in another file. Also change the current working file to that file.
     * @param path the path to the file.
     */
    public void open(String path) throws Exception {
        defaultCommandHandler();
    }

    /**
     * Just change the current working file. The data of that file will be replaced.
     * @param path that path to the file.
     */
    public void saveAs(String path) throws IOException {
        defaultCommandHandler();
    }

    /**
     * Sort all the meeting ascending by their date.
     */
    public void sortByDate() throws IOException {
        defaultCommandHandler();
    }

    public void sortBytime() throws IOException {
        defaultCommandHandler();
    }

    /**
     * Reverse the order of the meetings.
     */
    public void reverse() throws IOException {
        defaultCommandHandler();
    }

    /**
     * Swap 2 meeting.
     * @param a the index of the first meeting.
     * @param b the index of the second meeting.
     */
    public void swap(int a, int b) throws IOException {
        defaultCommandHandler();
    }

    /**
     * Clear the collection.
     */
    public void clear() throws IOException {
        defaultCommandHandler();
    }
}
