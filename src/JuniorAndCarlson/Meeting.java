package JuniorAndCarlson;
import java.util.Date;

public class Meeting implements Comparable<Meeting> {
    public class LateException extends RuntimeException {
        public LateException() {
            super("Встреча " + meetingName + " была отменена из-за опоздания");
        }
    }

    private Date meetingTime;
    private String meetingName;

    public Meeting(String meetingName, Date meetingTime) {
        this.meetingName = meetingName;
        this.meetingTime = meetingTime;
    }

    public void held(Date heldTime) {
        if (heldTime.after(meetingTime)) {
            throw new LateException();
        }
        System.out.println("Встреча " + meetingName + " вовремя");
    }

    public String getName() {
        return meetingName;
    }

    public Date getTime() {
        return (Date) meetingTime.clone();
    }

    @Override
    public String toString() {
        return "Meeting \"" + meetingName + "\" at " + meetingTime;
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
        return meetingTime.compareTo(other.meetingTime);
    }

}

