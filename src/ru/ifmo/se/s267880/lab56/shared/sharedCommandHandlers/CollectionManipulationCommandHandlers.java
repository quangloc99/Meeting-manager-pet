package ru.ifmo.se.s267880.lab56.shared.sharedCommandHandlers;

import ru.ifmo.se.s267880.lab56.shared.functional.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.Meeting;
import ru.ifmo.se.s267880.lab56.shared.MeetingSortOrder;
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
    void add(Meeting meeting_json, HandlerCallback callback);

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
    void remove(Meeting meeting_json, HandlerCallback callback);

    /**
     * Remove a meeting from the collection by index.
     */
    @Command
    @Usage("remove the meeting with index.")
    void remove(int meeting_id, HandlerCallback callback);

    /**
     * Add new meeting into the collection if it's date is before every other meeting in the collection.
     */
    @Command("add_if_min")
    @Usage("add new meeting into the collection if it's date is before every other meeting in the collection.")
    void addIfMin(Meeting meeting_json, HandlerCallback callback);

    /**
     * Clear the collection.
     */
    @Command(additional = true)
    @Usage("delete all the elements from the collection")
    void clear(HandlerCallback callback);

    @Command(value="set-order", additional = true)
    @Usage("set sort order. The accepted values are:\n" +
            "- `asc-time`: ascending by time\n" +
            "- `des-time`: descending by time\n" +
            "- `asc-name`: ascending by name\n" +
            "- `des-time`: descending by name")
    void setOrder(MeetingSortOrder sort_order, HandlerCallback callback);

    @Override
    default Map<String, CommandHandler> generateHandlers(InputPreprocessor preprocessor) {
        return ReflectionCommandHandlerGenerator.generate(CollectionManipulationCommandHandlers.class, this, preprocessor);
    }
}
