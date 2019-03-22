package ru.ifmo.se.s267880.lab56.shared.commandsController.helper;

import ru.ifmo.se.s267880.lab56.CLIWithJSONCommandController;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;

import java.lang.reflect.InvocationTargetException;
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
 *         {@code @Command}
 *         {@code @Usage} ("say hi to {arg}")
 *         {@code
 *         public void hi(String name) {
 *             System.out.println("Hi " + name);
 *         }
 *         }
 *
 *         private int i = 0;
 *         {@code @Command("print-next")}
 *         {@code @Usage} ("first it prints 1. Then after that each call it will increase the printed number.")
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
 *          CommandController cc = new CommandController();  // use CLIWithJsonCommandController inorder to display all the commands.
 *          ReflectionCommandAdder.addCommand(cc, new MyCommands(), new JsonBasicInputPreprocessor());
 *          cc.addCommand("exit", "just exit", () -> {
     *        System.exit(0);
 *            return CommandController.SUCESS;
 *          });  // beside using this adder, we can combined it with the traditional ones.
 *          while (1) {
 *              cc.execute();
 *          }
 *      }
 *      }
 * </pre>
 *
 * @author Tran Quang Loc
 * @see Usage
 * @see JsonBasicInputPreprocessor
 */
public class ReflectionCommandAdder {
    /**
     * The only method in this class, that add all commands represented by obj's methods (with annotation {@link Command}),
     * which has input preprocessed by preprocessor.
     *
     * @param cc the command controller that is
     * @param obj the object that has methods with annotation {@link Command}
     * @param preprocessor an object for preprocess the input entered by the user. Note that this class can be extends to be used with the other types.
     */
    public static void addCommand(CommandController cc, Object obj, InputPreprocessor preprocessor) {
        filterCommands(obj.getClass()).forEach((commandName, methodList) -> {
            cc.addCommand(commandName, generateHandler(obj, methodList, preprocessor));
        });
    }

    private static Map<String, List<Method>> filterCommands(Class cls) {
        Map<String, List<Method>> commandList = new HashMap<>();
        for (Method med: cls.getDeclaredMethods()) {
            Command anno = med.getAnnotation(Command.class);
            if (anno == null) continue;
            String commandName = anno.value().isEmpty() ? med.getName() : anno.value();
            commandList.computeIfAbsent(commandName, k -> new LinkedList<>()).add(med);
        }
        return commandList;
    }

    private static CommandController.Handler generateHandler(Object obj, List<Method> methodList, InputPreprocessor preprocessor) {
        final Map<Integer, LinkedList<Method>> methodMap = new HashMap<>();
        final String usage = generateUsage(methodList);
        int maxNElement_ = 0;
        for (Method med: methodList) {
            med.setAccessible(true);           // This line is necessary, even when the method is public.
            int nElm = med.getParameterCount();
            methodMap.computeIfAbsent(nElm, k -> new LinkedList<>()).add(med);
            maxNElement_ = Math.max(maxNElement_, nElm);
        }

        final int maxNElement = maxNElement_;
        return new CLIWithJSONCommandController.HandlerWithUsage() {
            @Override
            public String getUsage() {
                return usage;
            }

            @Override
            public int process(Object[] args) throws Exception {
                if (methodMap.containsKey(args.length)) {
                    for (Method med : methodMap.get(args.length)) {
                        Object[] preprocessedArgs;
                        try {
                            preprocessedArgs = preprocessor.preprocess(args,med.getParameterTypes());
                        } catch (CannotPreprocessInputException e) {
                            continue;
                        }

                        try {
                            med.invoke(obj, preprocessedArgs);
                            return CommandController.SUCCESS;
                        } catch (IllegalAccessException e) {
                            continue;
                        } catch (InvocationTargetException e) {
                            throw (Exception)e.getTargetException();
                        }
                    }
                }

                return (args.length < maxNElement) ? CommandController.NEED_MORE_INPUT : CommandController.FAIL;
            }
        };
    }

    private static String generateUsage(List<Method> methodList) {
        StringBuilder usageBuilder = new StringBuilder();
        for (Method med : methodList) {
            Usage cmdUsage = med.getAnnotation(Usage.class);
            if (cmdUsage == null) continue;
            if (usageBuilder.length() != 0) {
                usageBuilder.append("\n");
            }
            List<String> clsnames = new LinkedList<>();
            for (Class cls: med.getParameterTypes()) {
                clsnames.add(cls.getSimpleName());
            }
            String usage = "";
            if (clsnames.isEmpty()) {
                usage = cmdUsage.value();
                if (methodList.size() > 1)
                    usage = "If there is no argument, then " + usage;
            } else {
                usage = String.format("If the arguments are {%s}, then %s", String.join(", ", clsnames), cmdUsage.value());
            }
            if (med.getAnnotation(Command.class).additional()) {
                usageBuilder.append("[Additional] ");
            }
            usageBuilder.append(usage);
        }
        return usageBuilder.toString();
    }
}

