package ru.ifmo.se.s267880.lab56.client;

import ru.ifmo.se.s267880.lab56.shared.*;
import ru.ifmo.se.s267880.lab56.shared.communication.*;

import java.io.*;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class ClientCommandsHandlers implements SharedCommandHandlers {
    private String currentCommandName = null;
    private Object[] currentCommandParams;
    private boolean isQuite = false;
    private Broadcaster<MessageType> messageFromServerBroadcaster;
    private Sender messageToServerSender;

    public ClientCommandsHandlers(Broadcaster<MessageType> messageFromServerBroadcaster, Sender messageToServerSender) {
        this.messageFromServerBroadcaster = messageFromServerBroadcaster;
        this.messageToServerSender = messageToServerSender;
    }

    private CommandExecuteRequest generateDefaultRequest() {
        Serializable[] castedParams = new Serializable[currentCommandParams.length];
        for (int i = 0; i < currentCommandParams.length; ++i) {
            assert (currentCommandParams[i] instanceof Serializable);
            castedParams[i] = (Serializable) currentCommandParams[i];
        }
        return new CommandExecuteRequest(currentCommandName, castedParams);
    }

    private <T extends Serializable> HandlerCallback<CommandExecuteRespond> defaultCallbackWrapper(HandlerCallback<T> callback) {
        return new HandlerCallback<>(
                res -> {
                    System.out.println("Command " + currentCommandName + " successfully executed.");
                    if (!isQuite) {
                        System.out.println("# Meeting list (sorted by name):");
                        int i = 0;
                        for (Meeting meeting : res.getCollection()) {
                            System.out.printf("%3d) %s\n", ++i, meeting);
                        }
                        System.out.println("To get the original order (the real order), please use command `show`.");
                    }
                    callback.onSuccess(res.getResult());
                }, callback::onError
        );
    }

    private class CommandExecutor {
        HandlerCallback<CommandExecuteRespond> callback;
        Message<MessageType> request = null;
        Consumer<Exception> errorHandler = messageFromServerBroadcaster.onError.listen(e -> {
            removeHandlers();
            callback.onError(new CommunicationIOException());
        });
        Consumer<Message<MessageType>> respondFailHandler = messageFromServerBroadcaster
                .whenReceive(MessageType.RESPOND_FAIL).listen(m -> {
                    if (!(m instanceof CommandExecuteRespond)) return;
                    removeHandlers();
                    callback.onError(((CommandExecuteRespond) m).getResult());
                });
        Consumer<Message<MessageType>> respondSuccessHandler = messageFromServerBroadcaster
                .whenReceive(MessageType.RESPOND_SUCCESS).listen(m -> {
                    if (!(m instanceof CommandExecuteRespond)) return;
                    removeHandlers();
                    callback.onSuccess((CommandExecuteRespond) m);
                });

        public CommandExecutor setRequest(Message<MessageType> request) {
            this.request = request;
            return this;
        }

        CommandExecutor(HandlerCallback<CommandExecuteRespond> callback) {
            this.callback = callback;
        }

        public void run() {
            if (!messageFromServerBroadcaster.isConnecting()) {
                callback.onError(new CommunicationIOException());
                return ;
            }
            try {
                if (request == null) request = generateDefaultRequest();
                messageToServerSender.send(request);
            } catch (IOException e) {
                removeHandlers();
                callback.onError(new CommunicationIOException("Error when communicate with server", e));
            }
        }
        void removeHandlers() {
            messageFromServerBroadcaster.onError.removeListener(errorHandler);
            messageFromServerBroadcaster.whenReceive(MessageType.RESPOND_SUCCESS).removeListener(respondSuccessHandler);
            messageFromServerBroadcaster.whenReceive(MessageType.RESPOND_FAIL).removeListener(respondFailHandler);
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
     * @param file the file that the data will be imported from.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void doImport(File file, HandlerCallback callback) {
        try {
            FileTransferRequest res = new FileTransferRequest(file);
            new CommandExecutor(defaultCallbackWrapper(callback))
                    .setRequest(new CommandExecuteRequest(currentCommandName, res))
                    .run();
        } catch (FileNotFoundException e) {
            callback.onError(e);
        }
    }

    @Override
    public void export(String name, HandlerCallback<FileTransferRequest> callback) {
        new CommandExecutor(defaultCallbackWrapper(callback)).run();
    }

    /**
     * Replace the current collection with the ones in another file. Also change the current working file to that file.
     * @param path the path to the file.
     */
    @Override
    public void load(String path, HandlerCallback callback){
        new CommandExecutor(defaultCallbackWrapper(callback)).run();
    }

    @Override
    public void save(HandlerCallback callback) {
        new CommandExecutor(defaultCallbackWrapper(callback)).run();
    }

    @Override
    public void save(String path, HandlerCallback callback) {
        new CommandExecutor(defaultCallbackWrapper(callback)).run();
    }

    /**
     * Add meeting into the collection
     * @param meeting the meeting wanted to be add.
     */
    @Override
    public void add(Meeting meeting, HandlerCallback callback) {
        new CommandExecutor(defaultCallbackWrapper(callback)).run();
    }

    /**
     * List all the meetings.
     */
    @Override
    public void show(HandlerCallback<List<Meeting>> callback) {
        new CommandExecutor(new HandlerCallback<>(res -> {
            List<Meeting> meetings = res.getResult();
            System.out.println("# Meeting list (original order):");
            Iterator<Integer> counter = IntStream.rangeClosed(1, meetings.size()).iterator();
            meetings.stream()
                    .map(meeting -> String.format("%3d) %s", counter.next(), meeting))
                    .forEachOrdered(System.out::println);
            callback.onSuccess(meetings);
        }, callback::onError)).run();
    }

    /**
     * Remove a meeting from the collection by value.
     * @param meeting the meeting wanted to be removed.
     */
    @Override
    public void remove(Meeting meeting, HandlerCallback callback) {
        new CommandExecutor(defaultCallbackWrapper(callback)).run();
    }

    /**
     * Remove a meeting from the collection by index.
     * @param num the index (base 1) of the element.
     */
    @Override
    public void remove(int num, HandlerCallback callback) {
        new CommandExecutor(defaultCallbackWrapper(callback)).run();
    }

    /**
     * Add new meeting into the collection if it's date is before every other meeting in the collection.
     * @param meeting the meeting wanted to be added.
     */
    @Override
    public void addIfMin(Meeting meeting, HandlerCallback callback) {
        new CommandExecutor(defaultCallbackWrapper(callback)).run();
    }

    /**
     * show file name, number of meeting and the time the file first load during this session.
     */
    @Override
    public void info(HandlerCallback<Map<String, String>> callback) {
        new CommandExecutor(new HandlerCallback<>(res -> {
            Map<String, String> result = res.getResult();
            System.out.println("# Information");
            System.out.println("File name: " + (result.get("file") == null ? "<<no name>>" : result.get("file")));
            System.out.println("Time zone: UTC" + result.get("time-zone"));
            System.out.println("Number of meeting: " + result.get("meeting-count"));
            System.out.println("File load since: " + result.get("since"));
            System.out.println("Is quite: " + isQuite);
            callback.onSuccess(result);
        }, callback::onError)).run();
    }

    /**
     * Sort all the meeting ascending by their date.
     */
    @Override
    public void sortByDate(HandlerCallback callback) {
        new CommandExecutor(defaultCallbackWrapper(callback)).run();
    }

    @Override
    public void sortBytime(HandlerCallback callback) {
        new CommandExecutor(defaultCallbackWrapper(callback)).run();
    }

    /**
     * Reverse the order of the meetings.
     */
    @Override
    public void reverse(HandlerCallback callback) {
        new CommandExecutor(defaultCallbackWrapper(callback)).run();
    }

    /**
     * Swap 2 meeting.
     * @param a the index of the first meeting.
     * @param b the index of the second meeting.
     */
    @Override
    public void swap(int a, int b, HandlerCallback callback) {
        new CommandExecutor(defaultCallbackWrapper(callback)).run();
    }

    /**
     * Clear the collection.
     */
    @Override
    public void clear(HandlerCallback callback) {
        new CommandExecutor(defaultCallbackWrapper(callback)).run();
    }

    @Override
    public void listTimeZones(int offset, HandlerCallback<Map<Integer, ZoneId>> callback) {
        new CommandExecutor(
                new HandlerCallback<>(res -> ZoneUtils.printZonesByZoneOffset(res.getResult()), callback::onError)
        ).run();
    }

    @Override
    public void setTimeZone(int timeZoneKey, HandlerCallback callback) {
        new CommandExecutor(defaultCallbackWrapper(callback)).run();
    }
}
