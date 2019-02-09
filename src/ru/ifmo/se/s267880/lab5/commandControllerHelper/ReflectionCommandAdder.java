package ru.ifmo.se.s267880.lab5.commandControllerHelper;

import ru.ifmo.se.s267880.lab5.CommandController;

import java.lang.reflect.Method;

public class ReflectionCommandAdder {
    public static void addCommand(CommandController cc, Object obj) {
        Class cls = obj.getClass();
        for (Method med: cls.getDeclaredMethods()) {
            Command anno = med.getAnnotation(Command.class);
            System.out.println(med.getName());
            if (anno == null) continue;
            med.setAccessible(true);           // This line is necessary, even when the method is public.
            if (med.getParameterCount() > 0) {
                cc.addCommandWithJson(med.getName(), anno.usage(), elm -> {
                    try {
                        med.invoke(obj, elm);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                cc.addCommand(med.getName(), anno.usage(), () -> {
                    try {
                        med.invoke(obj);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }
}
