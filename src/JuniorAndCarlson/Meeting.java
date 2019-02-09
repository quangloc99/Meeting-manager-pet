package JuniorAndCarlson;
import java.util.Date;

public class Meeting {
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
}

