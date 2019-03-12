package ru.ifmo.se.s267880.lab56.commandControllerHelper;

/**
 * Preprocess the input enter by the user into the desired type.
 * Used with {@link ru.ifmo.se.s267880.lab56.CommandController CommandController} and {@link ReflectionCommandAdder}.
 *
 * @author Tran Quang Loc
 * @see ru.ifmo.se.s267880.lab56.CommandController
 * @see ReflectionCommandAdder
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

