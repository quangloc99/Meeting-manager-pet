package ru.ifmo.se.s267880.lab56.shared.sharedCommandHandlers;

import ru.ifmo.se.s267880.lab56.shared.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.Meeting;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.Command;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.CommandHandlers;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.Usage;

import java.util.List;

public interface CollectionManipulationCommandHandlers extends CommandHandlers {
    /**
     * Add meeting into the collection
     * @param meeting the meeting wanted to be add.
     */
    @Command
    @Usage("add new meeting into the collection.")
    void add(Meeting meetingJson, HandlerCallback callback);

    /**
     * List all the meetings.
     */
    @Command
    @Usage("List all the meetings.")
    void show(HandlerCallback<List<Meeting>> callback);

    /**
     * Remove a meeting from the collection by value.
     * @param meeting the meeting wanted to be removed.
     */
    @Command
    @Usage("remove the meeting correspond to the argument.")
    void remove(Meeting meetingJson, HandlerCallback callback);

    /**
     * Remove a meeting from the collection by index.
     * @param num the index (base 1) of the element.
     */
    @Command
    @Usage("remove the meeting with index.")
    void remove(int meetingId, HandlerCallback callback);

    /**
     * Add new meeting into the collection if it's date is before every other meeting in the collection.
     * @param meeting the meeting wanted to be added.
     */
    @Command("add_if_min")
    @Usage("add new meeting into the collection if it's date is before every other meeting in the collection.")
    void addIfMin(Meeting meetingJson, HandlerCallback callback);

    /**
     * Clear the collection.
     */
    @Command(additional = true)
    @Usage("delete all the elements from the collection")
    void clear(HandlerCallback callback);
}
