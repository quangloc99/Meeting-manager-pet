package ru.ifmo.se.s267880.lab56.shared.sharedCommandHandlers;

import ru.ifmo.se.s267880.lab56.shared.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.Command;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.CommandHandlers;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.Usage;
import ru.ifmo.se.s267880.lab56.shared.communication.FileTransferRequest;

import java.io.File;
import java.util.HashMap;

public interface StoringAndRestoringCommandHandlers extends CommandHandlers {
    /**
     * Add all data from another file into the current collection.
     * @param file the file that the data will be imported from.
     */
    @Command("import")
    @Usage("Add all data from the file given by the arg into the current collection.\nNote that the file name must be quoted")
    void doImport(File fileName, HandlerCallback callback);

    @Command
    @Usage("Export/download the current collection and save it with csv format.")
    void export(String fileName, HandlerCallback<FileTransferRequest> callback);

    // TODO: add more detailed usage.
    @Command
    @Usage("Open a collection with name.")
    void open(String collectionName, HandlerCallback callback);

    @Command
    @Usage("Save the collection into the database.")
    void save(HandlerCallback callback);

    @Command
    @Usage("Save the collection into the database with the give name.")
    void save(String collectionName, HandlerCallback callback);

    /**
     * Replace the current collection with the ones in another file. Also change the current working file to that file.
     * @param path the path to the file.
     */
//    @Command
    @Usage("load a file with name given by arg. The content of the collection will be replaced.\n" +
            "Note that if the file name contains special characters (e.g \".\", \",\", \" \", \"\\\", ...), then it must be quoted." )
    void loadFile(String pathToFile, HandlerCallback callback);

    //    @Command
    @Usage("save current collection. Note that initially there is no file name yet, so please use `save-as {String}` first.")
    @Deprecated
    void saveFile(HandlerCallback callback) ;

    /**
     * Just change the current working file. The data of that file will be replaced.
     * @param path that path to the file.
     */
//    @Command
    @Usage(
            "change the current working file.\n" +
                    "Note that if the file name contains special characters (e.g \".\", \",\", \" \", \"\\\", ...), then it must be quoted.\n" +
                    "After executing this command, the current file name changed."
    )
    @Deprecated
    void saveFile(String pathToFile, HandlerCallback callback);

    @Command("list-collections")
    @Usage("List all saved collections with users email.")
    void listCollections(HandlerCallback<HashMap<String, String>> callback);
}
