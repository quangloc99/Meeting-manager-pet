package ru.ifmo.se.s267880.lab56.client;

import ru.ifmo.se.s267880.lab56.shared.CommandHandlersWithMeeting;
import ru.ifmo.se.s267880.lab56.shared.Helper;
import ru.ifmo.se.s267880.lab56.shared.Meeting;

public class ClientCommandsHandlers implements CommandHandlersWithMeeting {
    private String currentCommandName = null;
    private Object[] currentCommandParams;

    void defaultCommandHandler() {
        System.out.printf("Command \"%s\" is invoked with parameters:\n\t%s\n", currentCommandName, Helper.join("\n\t", currentCommandParams));
    }

    /**
     * Add all data from another file into the current collection.
     * @param path the path to the file.
     */
    public void doImport(String path) {
        defaultCommandHandler();
    }

    /**
     * Add meeting into the collection
     * @param meeting the meeting wanted to be add.
     */
    public void add(Meeting meeting) {
        defaultCommandHandler();
    }

    /**
     * List all the meetings.
     */
    public void show() {
        defaultCommandHandler();
    }

    /**
     * Remove a meeting from the collection by value.
     * @param meeting the meeting wanted to be removed.
     */
    public void remove(Meeting meeting) {
        defaultCommandHandler();
    }

    /**
     * Remove a meeting from the collection by index.
     * @param num the index (base 1) of the element.
     */
    public void remove(int num) {
        defaultCommandHandler();
    }

    /**
     * Add new meeting into the collection if it's date is before every other meeting in the collection.
     * @param meeting the meeting wanted to be added.
     */
    public void addIfMin(Meeting meeting) {
        defaultCommandHandler();
    }

    /**
     * show file name, number of meeting and the time the file first open during this session.
     */
    public void info() {
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
    public void saveAs(String path) {
        defaultCommandHandler();
    }

    /**
     * Sort all the meeting ascending by their date.
     */
    public void sortByDate() {
        defaultCommandHandler();
    }

    public void sortBytime() {
        defaultCommandHandler();
    }

    /**
     * Reverse the order of the meetings.
     */
    public void reverse() {
        defaultCommandHandler();
    }

    /**
     * Swap 2 meeting.
     * @param a the index of the first meeting.
     * @param b the index of the second meeting.
     */
    public void swap(int a, int b) {
        defaultCommandHandler();
    }

    /**
     * Clear the collection.
     */
    public void clear() {
        defaultCommandHandler();
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
}
