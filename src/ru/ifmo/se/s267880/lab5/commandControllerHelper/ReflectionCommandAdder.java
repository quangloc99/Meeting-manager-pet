package ru.ifmo.se.s267880.lab5.commandControllerHelper;

import ru.ifmo.se.s267880.lab5.CommandController;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ReflectionCommandAdder {
    public static void addCommand(CommandController cc, Object obj, InputPreprocessor preprocessor) {
        Class cls = obj.getClass();
        Map<String, LinkedList<Method>> commandList = new HashMap<>();
        for (Method med: cls.getDeclaredMethods()) {
            Command anno = med.getAnnotation(Command.class);
            if (anno == null) continue;

            med.setAccessible(true);           // This line is necessary, even when the method is public.

            String commandName = anno.commandName();
            if (commandName.equals("")) {
                commandName = med.getName();
            }

            if (!commandList.containsKey(commandName)) {
                commandList.put(commandName, new LinkedList<>());
            }

            commandList.get(commandName).add(med);
        }

        commandList.forEach((commandName, methodList) -> {
            boolean hasOtherParam = false;
            List<String> commandWithParamUsage = new LinkedList<>();
            for (Method med: methodList) {
                Command anno = med.getAnnotation(Command.class);
                if (med.getParameterCount() > 0) {
                    hasOtherParam = true;
                    commandWithParamUsage.add(anno.usage());
                    continue;
                }
                cc.addCommand(commandName, anno.usage(), () -> {
                    med.invoke(obj);
                });
            }
            if (!hasOtherParam) return;
            cc.addCommandWithJson(commandName, String.join("\n", commandWithParamUsage), elm -> {
                for (Method med: methodList) {
                    Class inputType = med.getParameterTypes()[0];
                    Object inp = preprocessor.preprocess(elm, inputType);
                    if (inp == null) continue;
                    med.invoke(obj, inp);
                    break;
                }
            });
        });
    }
}
