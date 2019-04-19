package ru.ifmo.se.s267880.lab56.shared.commandsController.helper;

import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;

/**
 * Preprocess the input enter by the user into the desired type.
 * Used with {@link CommandController CommandController} and {@link ReflectionCommandHandlerGenerator}.
 *
 * @author Tran Quang Loc
 * @see CommandController
 * @see ReflectionCommandHandlerGenerator
 */
public interface InputPreprocessor {
    Object preprocess(Object obj, Class cls) throws CannotPreprocessInputException;
    default Object[] preprocess(Object[] objs, Class[] classList) throws CannotPreprocessInputException {
        assert (objs.length == classList.length);
        Object[] ans = new Object[objs.length];
        for (int i = 0; i < ans.length; ++i) {
            ans[i] = preprocess(objs[i], classList[i]);
        }
        return ans;
    }
}

