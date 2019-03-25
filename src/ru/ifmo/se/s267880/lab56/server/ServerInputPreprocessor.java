package ru.ifmo.se.s267880.lab56.server;

import ru.ifmo.se.s267880.lab56.shared.BoundedInputStream;
import ru.ifmo.se.s267880.lab56.shared.Helper;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.CannotPreprocessInputException;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.InputPreprocessor;

import java.io.InputStream;

public class ServerInputPreprocessor implements InputPreprocessor {
    @Override
    public Object preprocess(Object obj, Class cls) throws CannotPreprocessInputException {
        if (cls == InputStream.class) {
            // a little hacking here.
            if (!(obj instanceof Number)) throw new CannotPreprocessInputException("" + obj + " must be a number.");
            QueryHandlerThread thread = (QueryHandlerThread) Thread.currentThread();
            return new BoundedInputStream(thread.getInputStream(), ((Number) obj).longValue());
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
