package ru.ifmo.se.s267880.lab56.shared.sharedCommandHandlers;

import ru.ifmo.se.s267880.lab56.shared.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandHandler;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.*;
import ru.ifmo.se.s267880.lab56.shared.communication.FileTransferRequest;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public interface StoringAndRestoringCommandHandlers extends CommandHandlers {
    /**
     * Add all data from another file into the current collection.
     */
    @Command("import")
    @Usage("Add all data from the file given by the arg into the current collection.\nNote that the file name must be quoted")
    void doImport(File csv_file_name, HandlerCallback callback);

    @Command
    @Usage("Export/download the current collection and save it with csv format.")
    void export(String csv_file_name, HandlerCallback<FileTransferRequest> callback);

    // TODO: add more detailed usage.
    @Command
    @Usage("Open a collection with name.")
    void open(String collection_name, HandlerCallback callback);

    @Command
    @Usage("Save the collection into the database.")
    void save(HandlerCallback callback);

    @Command
    @Usage("Save the collection into the database with the give name.")
    void save(String collection_name, HandlerCallback callback);

    /**
     * Replace the current collection with the ones in another file. Also change the current working file to that file.
     */
//    @Command
    @Usage("load a file with name given by arg. The content of the collection will be replaced.\n" +
            "Note that if the file name contains special characters (e.g \".\", \",\", \" \", \"\\\", ...), then it must be quoted." )
    void loadFile(String path_to_file, HandlerCallback callback);

    //    @Command
    @Usage("save current collection. Note that initially there is no file name yet, so please use `save-as {String}` first.")
    @Deprecated
    void saveFile(HandlerCallback callback) ;

    /**
     * Just change the current working file. The data of that file will be replaced.
     */
//    @Command
    @Usage(
            "change the current working file.\n" +
                    "Note that if the file name contains special characters (e.g \".\", \",\", \" \", \"\\\", ...), then it must be quoted.\n" +
                    "After executing this command, the current file name changed."
    )
    @Deprecated
    void saveFile(String path_to_file, HandlerCallback callback);

    @Command("list-collections")
    @Usage("List all saved collections with users email.")
    void listCollections(HandlerCallback<HashMap<String, String>> callback);

    @Override
    default Map<String, CommandHandler> generateHandlers(InputPreprocessor preprocessor) {
        return ReflectionCommandHandlerGenerator.generate(
                StoringAndRestoringCommandHandlers.class, this, preprocessor
        );
    }
}
