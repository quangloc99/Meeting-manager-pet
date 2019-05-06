package ru.ifmo.se.s267880.lab56.shared.commandsController.helper;

import com.google.gson.JsonElement;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;

/**
 * A basic input preprocessor that transform Json into some primitive types.
 * @author Tran Quang Loc
 * @see ReflectionCommandHandlerGenerator
 */
public class JsonBasicInputPreprocessor implements InputPreprocessor {
    /**
     * Transform the object into the desired type.
     * @param obj the object needed to be transform.
     * @param inputType the class of the desired type
     * @return transformed object
     * @throws CannotPreprocessInputException when cannot transform or there is another exception during the transformation process.
     */
    public Object preprocess(Object obj, Class inputType) throws CannotPreprocessInputException {
        if (!(obj instanceof  JsonElement)) throw new CannotPreprocessInputException("obj must be JsonElement");
        return preprocessJson((JsonElement)obj, inputType);
    }

    /**
     * Transform an JsonElement into desired class.
     * @param elm the json element that entered by the user.
     * @param inputType the desired class of the output.
     * @throws CannotPreprocessInputException when cannot transform or there is another exception during the transformation process.
     */
    protected Object preprocessJson(JsonElement elm, Class inputType) throws CannotPreprocessInputException {
        try {
            if (inputType == Integer.class || inputType == Integer.TYPE) {
                return elm.getAsInt();
            }
            if (inputType == Long.class || inputType == Long.TYPE) {
                return elm.getAsLong();
            }
            if (inputType == Boolean.class || inputType == Boolean.TYPE) {
                return elm.getAsBoolean();
            }
            if (inputType == String.class) {
                return elm.getAsString();
            }
        } catch (Exception e) {
            // if it is failed to parse the value;
            throw new CannotPreprocessInputException(e.getMessage());
        }
        throw new CannotPreprocessInputException("No match type for input " + elm);
    }
}
