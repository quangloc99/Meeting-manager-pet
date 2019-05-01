package ru.ifmo.se.s267880.lab56.client.commandHandlers;

import ru.ifmo.se.s267880.lab56.shared.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.Meeting;
import ru.ifmo.se.s267880.lab56.shared.MeetingSortOrder;
import ru.ifmo.se.s267880.lab56.shared.sharedCommandHandlers.CollectionManipulationCommandHandlers;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class ClientCollectionManipulationCommandHandlers extends ClientCommandsHandlers
        implements CollectionManipulationCommandHandlers
{
    public ClientCollectionManipulationCommandHandlers(Supplier<CommandToServerExecutor> commandExecutorSupplier) {
        super(commandExecutorSupplier);
    }

    /**
     * Add meeting into the collection
     * @param meeting the meeting wanted to be add.
     */
    @Override
    public void add(Meeting meeting, HandlerCallback callback) {
        buildCommandExecutor().run(generateRequest(), callback);
    }

    /**
     * List all the meetings.
     */
    @Override
    public void show(HandlerCallback<List<Meeting>> callback) {
        buildCommandExecutor().run(generateRequest(), new HandlerCallback<>(res -> {
            List<Meeting> meetings = res.getResult();
            System.out.println("# Meeting list (original order):");
            Iterator<Integer> counter = IntStream.rangeClosed(1, meetings.size()).iterator();
            meetings.stream()
                    .map(meeting -> String.format("%3d) %s", counter.next(), meeting))
                    .forEachOrdered(System.out::println);
            callback.onSuccess(meetings);
        }, callback::onError));
    }

    /**
     * Remove a meeting from the collection by value.
     * @param meeting the meeting wanted to be removed.
     */
    @Override
    public void remove(Meeting meeting, HandlerCallback callback) {
        buildCommandExecutor().run(generateRequest(), callback);
    }

    /**
     * Remove a meeting from the collection by index.
     * @param num the index (base 1) of the element.
     */
    @Override
    public void remove(int num, HandlerCallback callback) {
        buildCommandExecutor().run(generateRequest(), callback);
    }

    /**
     * Add new meeting into the collection if it's date is before every other meeting in the collection.
     * @param meeting the meeting wanted to be added.
     */
    @Override
    public void addIfMin(Meeting meeting, HandlerCallback callback) {
        buildCommandExecutor().run(generateRequest(), callback);
    }

    /**
     * Clear the collection.
     */
    @Override
    public void clear(HandlerCallback callback) {
        buildCommandExecutor().run(generateRequest(), callback);
    }

    @Override
    public void setOrder(MeetingSortOrder sort_order, HandlerCallback callback) {
        buildCommandExecutor().run(generateRequest(), callback);
    }
}
