package ru.ifmo.se.s267880.lab5;

import JuniorAndCarlson.Meeting;
import ru.ifmo.se.s267880.lab5.commandControllerHelper.Command;
import ru.ifmo.se.s267880.lab5.csv.CsvReader;
import ru.ifmo.se.s267880.lab5.csv.CsvRowWithNamesWriter;

import java.io.*;
import java.text.ParseException;
import java.util.*;

public class MeetingManager {
    private List<Meeting> collection = new LinkedList<>();
    private String currentFileName;
    public MeetingManager(String path) {
        open(path);
    }

    @Command(usage = "[Additional] Open a file with name given by arg. The content of the collection will be replaced.")
    public void open(String path) {
        collection.clear();
        try {
            collection = getMeetingsFromFile(path);
        } catch (ParseException | IOException e) {
            System.err.println(e);
            System.err.println("The collection is initialized empty and still be saved into " + path);
        }
        currentFileName = path;
    }

    @Command(usage = "Add all data from the file given by the arg into the current collection.")
    public void doImport(String path) {
        try {
            collection.addAll(getMeetingsFromFile(path));
        } catch (ParseException | IOException e) {
            System.err.println("File " + path + " does not exist or is corrupted. No data is imported");
        }
    }

    private List<Meeting> getMeetingsFromFile(String path) throws ParseException, IOException {
        currentFileName = path;
        CsvReader reader = new CsvReader(new BufferedInputStream(new FileInputStream(path)), true);

        List<Meeting> newData = new LinkedList<>();
        while (true) {
            Map<String, String> row = reader.getNextRowWithNames();
            if (row == null) break;
            Meeting meeting = new Meeting(row.get("meeting name"), Helper.meetingDateFormat.parse(row.get("meeting time")));
            newData.add(meeting);
        }
        return newData;
    }

    public void save() {
        try {
            CsvRowWithNamesWriter writer = new CsvRowWithNamesWriter(
                    new FileOutputStream(currentFileName),
                    List.of("meeting name", "meeting time")
            );
            for (Meeting meeting: collection) {
                Map<String, String> row = new HashMap<>();
                row.put("meeting time", Helper.meetingDateFormat.format(meeting.getTime()));
                row.put("meeting name", meeting.getName());
                writer.writeRow(row);
            }
            writer.close();
        } catch (IOException e) {
            System.err.println("Unable to write data into file " + currentFileName);
            System.err.println(e);
        }
    }

    @Command(usage = "Add new meeting into the collection.")
    public void add(Meeting meeting) {
        collection.add(meeting);
    }

    @Command(usage = "list all the meetings.")
    public void show() {
        System.out.println("# Meeting list:");
        int i = 1;
        for (Meeting meeting: collection) {
            System.out.printf("%d) %s\n", i++, meeting);
        }
    }

    @Command(usage = "If arg is an object then the correspond meeting in the collection will be removed.")
    public void remove(Meeting meeting) {
        collection.remove(meeting);
    }

    @Command(usage = "If arg is a number then the meeting with the given index (1-base indexed) will be removed.")
    public void remove(int num) {
        collection.remove(num - 1);
    }

    @Command(
        commandName = "add_if_min",
        usage = "add new meeting into the collection if it's date is before every other meeting in the collection."
    )
    public void addIfMin(Meeting meeting) {
        if (meeting.compareTo(Collections.min(collection)) >= 0) return;
        add(meeting);
    }

    @Command(usage = "show some information.")
    public void info() {
        System.out.println("# Information");
        System.out.println("File name: " + currentFileName);
        System.out.println("Number of meeting: " + collection.size());
        System.out.println("Last time modified: " + Helper.meetingDateFormat.format(new Date(new File(currentFileName).lastModified())));
    }
}
