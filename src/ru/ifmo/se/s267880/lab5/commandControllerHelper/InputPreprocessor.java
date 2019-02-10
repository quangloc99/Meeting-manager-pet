package ru.ifmo.se.s267880.lab5.commandControllerHelper;

import com.google.gson.JsonElement;
import ru.ifmo.se.s267880.lab5.CommandController;

/**
 * A helper class that transforms json input by the user into other type before passing it into another method.
 *
 * The main use of this class is for the class {@link ReflectionCommandAdder}
 *
 * Right now this class can only transform Integer and String, but other types can be transformed too with the helps
 * of inheritance.
 *
 * @author Tran Quang Loc
 * @see ReflectionCommandAdder
 */
public class InputPreprocessor {
    /**
     * The only function that used to transform user input.
     *
     * This method is used without generic, so it can extends easily, and beside it is used only with
     * {@link ReflectionCommandAdder#addCommand(CommandController, Object, InputPreprocessor)}
     *
     * @param elm the json that typed by the user.
     * @param inputType the desired class of the output.
     * @return null when the elm cannot be transform into the given class by inputType.
     */
    public Object preprocess(JsonElement elm, Class inputType) {
        if (inputType == Integer.class || inputType == Integer.TYPE) {
            return elm.getAsInt();
        }
        if (inputType == String.class) {
            return elm.getAsString();
        }
        return null;
    }
}
