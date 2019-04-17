package ru.ifmo.se.s267880.lab56.shared;
import java.io.Serializable;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Tran Quang Loc
 * A class to check if a meeting is held late or not.
 */
public class Meeting implements Comparable<Meeting>, Serializable {
    /**
     * This exception will be thrown when a meeting was held late.
     */
    public class LateException extends RuntimeException {
        public LateException() {
            super("Встреча " + meetingName + " была отменена из-за опоздания");
        }
    }

    private String meetingName;         // replacement for object's name
    private Duration duration;          // replacement for size
    private BuildingLocation location;  // replacement for location
    private ZonedDateTime meetingTime;           // replacement for object's creation's time

    /**
     * @param meetingName The name of the meeting.
     * @param meetingTime The meeting time.
     */
    public Meeting(String meetingName, Duration duration, BuildingLocation location, ZonedDateTime meetingTime) {
        this.meetingName = meetingName;
        this.duration = duration;
        this.location = location;
        this.meetingTime = meetingTime;
    }

    /**
     * Held the meeting.
     * @throws LateException
     */
    public void held(ZonedDateTime heldTime) {
        if (heldTime.isAfter(meetingTime)) {
            throw new LateException();
        }
        System.out.println("Встреча " + meetingName + " вовремя");
    }

    public String getName() {
        return meetingName;
    }

    public Duration getDuration() { return duration; }

    public BuildingLocation getLocation() { return location; }

    public ZonedDateTime getTime() {
        return meetingTime;  // because it is immutable.
    }

    public Meeting withName(String name) { return new Meeting(name, duration, location, meetingTime); }
    public Meeting withDuration(Duration dur) { return new Meeting(meetingName, dur, location, meetingTime); }
    public Meeting withLocation(BuildingLocation loc) { return new Meeting(meetingName, duration, location, meetingTime); }
    public Meeting withTime(ZonedDateTime time) { return new Meeting(meetingName, duration, location, time); }

    @Override
    public String toString() {
        return toString(Helper.meetingDateFormat);
    }

    /**
     * Generate string representation with custom date format.
     * @return
     */
    public String toString(DateTimeFormatter formatter) {
        return String.format("%s: at %s for %s minute(s) on %s",
                meetingName,
                formatter.format(meetingTime),
                duration.toMinutes(),
                location
        );
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (other.getClass() != this.getClass()) return false;

        Meeting o = (Meeting) other;
        return o.meetingTime.equals(this.meetingTime) && o.meetingName.equals(this.meetingName);
    }

    @Override
    public int compareTo(Meeting other) {
        int res = meetingTime.compareTo(other.meetingTime);
        if (res == 0) res = duration.compareTo(other.duration);
        return res;
    }

}

