package ru.ifmo.se.s267880.lab5.commandControllerHelper;

import ru.ifmo.se.s267880.lab5.CommandController;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * An utility class that use reflection and annotation to add commands for {@link CommandController} more easily
 * and nicely.
 * Example: <pre>
 *      class MyCommands {
 *         {@code @Usage} (usage = "say hi to {arg}")
 *         {@code
 *         public void hi(String name) {
 *             System.out.println("Hi " + name);
 *         }
 *         }
 *
 *         private int i = 0;
 *         {@code @Usage} (commandName = "print-next", usage = "first it prints 1. Then after that each call it will increase the printed number.")
 *         public void printNext() {
 *             System.out.println(++i);
 *         }
 *
 *      }
 *
 *      ....
 *
 *      {@code
 *      public static void main(String[] args) {
 *          CommandController cc = new CommandController();
 *          ReflectionCommandAdder.addCommand(cc, new MyCommands(), new InputPreprocessor());
 *          cc.addCommand("exit", "just exit", () -> System.exit(0));  // beside using this adder, we can combined it with the traditional ones.
 *          while (1) {
 *              cc.prompt();
 *          }
 *      }
 *      }
 * </pre>
 *
 * @author Tran Quang Loc
 * @see Usage
 * @see InputPreprocessor
 */
public class ReflectionCommandAdder {
    /**
     * The only method in this class, that add all commands represented by obj's methods (with annotation {@link Usage}),
     * which has input preprocessed by preprocessor.
     *
     * @param cc the command controller that is
     * @param obj the object that has methods with annotation {@link Usage}
     * @param preprocessor the InputPreprocessor. Note that this class can be extends to be used with the other types.
     */
    public static void addCommand(CommandController cc, Object obj, InputPreprocessor preprocessor) {
        Class cls = obj.getClass();
        Map<String, LinkedList<Method>> commandList = new HashMap<>();
        for (Method med: cls.getDeclaredMethods()) {
            Command anno = med.getAnnotation(Command.class);
            if (anno == null) continue;

            med.setAccessible(true);           // This line is necessary, even when the method is public.

            String commandName = med.getName();
            if (anno.value().length() > 0) {
                commandName = anno.value();
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
                Usage usageAnno = med.getAnnotation(Usage.class);
                String usage = usageAnno == null ? "" : usageAnno.value();
                if (med.getParameterCount() > 0) {
                    hasOtherParam = true;
                    commandWithParamUsage.add(usage);
                    continue;
                }
                cc.addCommand(commandName, usage, () -> {
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
