package ru.ifmo.se.s267880.lab56.server;

import ru.ifmo.se.s267880.lab56.shared.BoundedInputStream;
import ru.ifmo.se.s267880.lab56.shared.Helper;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.CannotPreprocessInputException;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.InputPreprocessor;
import ru.ifmo.se.s267880.lab56.shared.communication.FileTransferRequest;

import java.io.File;
import java.io.IOException;

public class ServerInputPreprocessor implements InputPreprocessor {
    @Override
    public Object preprocess(Object obj, Class cls) throws CannotPreprocessInputException {
        if (cls == File.class) {
            if (!(obj instanceof FileTransferRequest))
                throw new CannotPreprocessInputException("uploaded object must be a FileTransferRequest");
            try {
                return ((FileTransferRequest) obj).getDestinationFile();
            } catch (IOException e) {
                throw new CannotPreprocessInputException("Error while getting file.");
            }
        }

        return castToTypePreprocessor(obj, cls);
    }

    public Object castToTypePreprocessor(Object obj, Class cls) throws CannotPreprocessInputException {
        if (cls.isPrimitive()) cls = Helper.toWrapper(cls);
        if (!cls.isInstance(obj)) {
            throw new CannotPreprocessInputException("" + obj + " is not a " + cls.getCanonicalName() + ".");
        }
        return cls.cast(obj);
    }
}
