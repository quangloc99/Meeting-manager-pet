package ru.ifmo.se.s267880.lab56.client.commandHandlers;

import ru.ifmo.se.s267880.lab56.client.ConsoleWrapper;
import ru.ifmo.se.s267880.lab56.client.Services;
import ru.ifmo.se.s267880.lab56.shared.functional.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.communication.CommandExecuteRequest;
import ru.ifmo.se.s267880.lab56.shared.communication.FileTransferRequest;
import ru.ifmo.se.s267880.lab56.shared.sharedCommandHandlers.StoringAndRestoringCommandHandlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

public class ClientStoringAndRestoringCommandHandlers extends ClientCommandsHandlers
    implements StoringAndRestoringCommandHandlers
{
    public ClientStoringAndRestoringCommandHandlers(Services services) {
        super(services);
    }

    /**
     * Add all data from another file into the current collection.
     * @param file the file that the data will be imported from.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void doImport(File file, HandlerCallback callback) {
        try {
            buildCommandExecutor().run(
                    new CommandExecuteRequest(getCommandName(), new FileTransferRequest(file)),
                    callback
            );
        } catch (FileNotFoundException e) {
            callback.onError(e);
        }
    }

    @Override
    public void export(String name, HandlerCallback<FileTransferRequest> callback) {
        buildCommandExecutor().run(generateRequest(), new HandlerCallback<>(o -> callback.onSuccess(null), callback::onError));
    }

    @Override
    public void open(String collectionName, HandlerCallback callback) {
        buildCommandExecutor().run(generateRequest(), callback);
    }

    @Override
    public void save(HandlerCallback callback) {
        buildCommandExecutor().run(generateRequest(), callback);
    }

    @Override
    public void save(String name, HandlerCallback callback) {
        buildCommandExecutor().run(generateRequest(), callback);
    }

    /**
     * Replace the current collection with the ones in another file. Also change the current working file to that file.
     * @param path the path to the file.
     */
    @Override
    @Deprecated
    public void loadFile(String path, HandlerCallback callback){
        buildCommandExecutor().run(generateRequest(), callback);
    }

    @Override
    @Deprecated
    public void saveFile(HandlerCallback callback) {
        buildCommandExecutor().run(generateRequest(), callback);
    }

    @Override
    @Deprecated
    public void saveFile(String path, HandlerCallback callback) {
        buildCommandExecutor().run(generateRequest(), callback);
    }

    @Override
    public void listCollections(HandlerCallback<HashMap<String, String>> callback) {
        buildCommandExecutor().run(generateRequest(), new HandlerCallback<>(res -> {
            HashMap<String, String> collections = res.getResult();
            final String format = "| %-30s | %-30s |\n";
            ConsoleWrapper.console.println("# Collections lists:");
            ConsoleWrapper.console.printf(format, "Collection name", "User email");
            ConsoleWrapper.console.printf(String.format(format, " ", " ").replace(" ", "-"));
            collections.forEach((collectionName, userEmail) -> ConsoleWrapper.console.printf(format, collectionName, userEmail));
            callback.onSuccess(collections);
        }, callback::onError));
    }
}
