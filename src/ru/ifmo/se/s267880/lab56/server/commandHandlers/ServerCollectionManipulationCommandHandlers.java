package ru.ifmo.se.s267880.lab56.server.commandHandlers;

import ru.ifmo.se.s267880.lab56.server.services.Services;
import ru.ifmo.se.s267880.lab56.shared.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.Meeting;
import ru.ifmo.se.s267880.lab56.shared.MeetingSortOrder;
import ru.ifmo.se.s267880.lab56.shared.sharedCommandHandlers.CollectionManipulationCommandHandlers;

import java.util.Collections;
import java.util.List;

public class ServerCollectionManipulationCommandHandlers extends ServerCommandHandlers
    implements CollectionManipulationCommandHandlers
{
    public ServerCollectionManipulationCommandHandlers(Services services) {
        super(services);
    }

    /**
     * Add meeting into the collection
     * Note: Because the meeting is from the client so this method will transform meeting's time to have the same zone
     * on the server but with same <b>local</b>.
     * @param meeting the meeting wanted to be add.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void add(Meeting meeting, HandlerCallback callback) {
        services.getUserState().add(meeting);
        callback.onSuccess(null);
    }

    /**
     * List all the meetings.
     * Note: Because the method will pass a list of object to the client so every meeting's must be transformed
     * to the current zone with same <b>instant</b>.
     */
    @Override
    public void show(HandlerCallback<List<Meeting>> callback) {
        callback.onSuccess(services.getUserState().getMeetingsCollection());
    }

    /**
     * Remove a meeting from the collection by value.
     * Note: Because the meeting is from the client so this method will transform meeting's time to have the same zone
     * on the server but with same <b>local</b>.
     * @param meeting the meeting wanted to be removed.
     */
    @Override
    @SuppressWarnings("unchecked")
    public synchronized void remove(Meeting meeting, HandlerCallback callback) {
        int num = services.getUserState().findMeeting(meeting);
        if (num == -1) callback.onSuccess(null);
        else services.getUserState().remove(num);
    }

    /**
     * Remove a meeting from the collection by index.
     * @param num the index (base 1) of the element.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void remove(int num, HandlerCallback callback) {
        try {
            services.getUserState().remove(num);
            callback.onSuccess(null);
        } catch (IndexOutOfBoundsException e) {
            callback.onError(e);
        }
    }

    /**
     * Add new meeting into the collection if it's date is before every other meeting in the collection.
     * Note: Because the meeting is from the client so this method will transform meeting's time to have the same zone
     * on the server but with same <b>local</b>.
     * @param meeting the meeting wanted to be added.
     */
    @Override
    public synchronized void addIfMin(Meeting meeting, HandlerCallback callback) {
        meeting = services.getUserState().transformMeetingTimeSameLocal(meeting);
        synchronized (services.getUserState().getMeetingsCollection()) {
            if (meeting.compareTo(Collections.min(services.getUserState().getMeetingsCollection())) < 0)
                services.getUserState().add(meeting);
            callback.onSuccess(null);
        }
    }

    /**
     * Clear the collection.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void clear(HandlerCallback callback) {
        services.getUserState().clear();
        callback.onSuccess(null);
    }

    @Override
    public void setOrder(MeetingSortOrder sort_order, HandlerCallback callback) {
        services.getUserState().setMeetingSortOrder(sort_order);
        callback.onSuccess(null);
    }
}
