package ru.ifmo.se.s267880.lab56.shared.commandsController.helper;

import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;

/**
 * Preprocess the input enter by the user into the desired type.
 * Used with {@link ReflectionCommandHandlerGenerator} and {@link CommandHandlers}
 *
 * @author Tran Quang Loc
 */
public interface InputPreprocessor {
    /**
     * Transform the object into the desired type.
     * @param obj the object needed to be transform.
     * @param cls the class of the desired type
     * @return transformed object
     * @throws CannotPreprocessInputException when cannot transform or there is another exception during the transformation process.
     */
    Object preprocess(Object obj, Class cls) throws CannotPreprocessInputException;

    /**
     * Transform an array objects into the corresponding type.
     * @param objs the array of objects that need to be transform.
     * @param classList the array of desired type.
     * @return an array of transformed objects.
     * @throws CannotPreprocessInputException when cannot transform or there is another exception during the transformation process.
     */
    default Object[] preprocess(Object[] objs, Class[] classList) throws CannotPreprocessInputException {
        assert (objs.length == classList.length);
        Object[] ans = new Object[objs.length];
        for (int i = 0; i < ans.length; ++i) {
            ans[i] = preprocess(objs[i], classList[i]);
        }
        return ans;
    }
}

