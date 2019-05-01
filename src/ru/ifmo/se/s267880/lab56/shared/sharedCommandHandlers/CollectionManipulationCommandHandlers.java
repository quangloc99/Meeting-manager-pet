package ru.ifmo.se.s267880.lab56.shared.sharedCommandHandlers;

import ru.ifmo.se.s267880.lab56.shared.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.Meeting;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandHandler;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.*;

import java.util.List;
import java.util.Map;

public interface CollectionManipulationCommandHandlers extends CommandHandlers {
    /**
     * Add meeting into the collection
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
     */
    @Command
    @Usage("remove the meeting correspond to the argument.")
    void remove(Meeting meetingJson, HandlerCallback callback);

    /**
     * Remove a meeting from the collection by index.
     */
    @Command
    @Usage("remove the meeting with index.")
    void remove(int meetingId, HandlerCallback callback);

    /**
     * Add new meeting into the collection if it's date is before every other meeting in the collection.
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

    @Override
    default Map<String, CommandHandler> generateHandlers(InputPreprocessor preprocessor) {
        return ReflectionCommandHandlerGenerator.generate(CollectionManipulationCommandHandlers.class, this, preprocessor);
    }
}
