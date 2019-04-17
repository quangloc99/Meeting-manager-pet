package ru.ifmo.se.s267880.lab56.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is a modification version from this answer https://stackoverflow.com/a/26376532.
 */
public class SimpleArgumentsParser extends HashMap<String, List<String>> {
    public SimpleArgumentsParser(String[] args) {
        super();
        String currentKey = "";
        put("", new ArrayList<>());
        for (String i: args) {
            if (i.startsWith("-")) {
                currentKey = i.substring(1);
                putIfAbsent(currentKey, new ArrayList<>());
            } else {
                get(currentKey).add(i);
            }
        }
    }

    public String[] toArgsArray() {
        return this.entrySet().stream().flatMap(kv -> {
            LinkedList<String> targs = new LinkedList<>(kv.getValue());
            targs.addFirst("-" + kv.getKey());
            return targs.stream();
        }).toArray(String[]::new);
    }

    public String getFirst(String key) {
        if (!containsKey(key)) return null;
        if (get(key).size() == 0) return null;
        return get(key).get(0);
    }
}
