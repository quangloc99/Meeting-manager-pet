package ru.ifmo.se.s267880.lab56.server;

import ru.ifmo.se.s267880.lab56.shared.Helper;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.CannotPreprocessInputException;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.InputPreprocessor;

public class CastToTypeInputPreprocessor implements InputPreprocessor {
    @Override
    public Object preprocess(Object obj, Class cls) throws CannotPreprocessInputException {
        if (cls.isPrimitive()) cls = Helper.toWrapper(cls);
        if (!cls.isInstance(obj)) {
            throw new CannotPreprocessInputException("" + obj + " is not a " + cls.getCanonicalName() + ".");
        }
        return cls.cast(obj);
    }
}
