package ru.ifmo.se.s267880.lab5.commandControllerHelper;

import com.google.gson.JsonElement;

public class InputPreprocessor {
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
