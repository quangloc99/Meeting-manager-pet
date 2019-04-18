package ru.ifmo.se.s267880.lab56.server;

import ru.ifmo.se.s267880.lab56.shared.communication.QueryToServer;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;

import java.io.Serializable;
import java.util.Iterator;
import java.util.function.Consumer;

public class ServerCommandController extends CommandController {
    private QueryToServer query = null;
    private Iterator<Serializable> inputIterator = null;

    // No override
    public synchronized void execute(QueryToServer query, Consumer<Object> onSuccessfulExecuted, Consumer<Exception> onError) {
        this.query = query;
        setNInputLimit(query.getParameters().size());
        inputIterator = query.getParameters().iterator();
        try {
            super.execute(onSuccessfulExecuted, onError);
        } finally {
            this.query = null;
            inputIterator = null;
        }
    }

    @Override
    protected String getUserCommand() {
        return query.getName();
    }

    @Override
    protected Object getUserInput() {
        assert(inputIterator != null);
        assert(inputIterator.hasNext());
        return inputIterator.next();
    }
}
