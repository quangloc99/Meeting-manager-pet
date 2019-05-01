package ru.ifmo.se.s267880.lab56.shared;

import java.util.Comparator;

public enum MeetingSortOrder {
    ASCENDING_TIME("asc-time", Comparator.comparing(Meeting::getTime)),
    DESCENDING_TIME("des-time", Comparator.comparing(Meeting::getTime).reversed()),
    ASCENDING_NAME("asc-name", Comparator.comparing(Meeting::getName)),
    DESCENDING_NAME("des-name", Comparator.comparing(Meeting::getName).reversed());

    String shortHand;
    Comparator<Meeting> meetingComparator;

    MeetingSortOrder(String shortHand, Comparator<Meeting> meetingComparator) {
        this.shortHand = shortHand;
        this.meetingComparator = meetingComparator;
    }

    @Override
    public String toString() {
        return shortHand;
    }

    public Comparator<Meeting> getMeetingComparator() {
        return meetingComparator;
    }

    public static MeetingSortOrder getByShorthand(String shortHand) {
        for (MeetingSortOrder i : MeetingSortOrder.values()) {
            if (i.toString().equals(shortHand)) {
                return i;
            }
        }
        return null;  // return default order
    }
}
