package ru.ifmo.se.s267880.lab56.shared.commandsController.helper;

import ru.ifmo.se.s267880.lab56.shared.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandHandler;
import ru.ifmo.se.s267880.lab56.shared.commandsController.IncorrectInputException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

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
 *          ReflectionCommandHandlerGenerator.addCommand(cc, new MyCommands(), new JsonBasicInputPreprocessor());
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
public class ReflectionCommandHandlerGenerator {

    /**
     * The only method in this class, that add all commands represented by commandHandlers's methods (with annotation {@link Command}),
     * which has input preprocessed by preprocessor.
     * This class introduce metaDataClass inorder to get more freedom: the annotations can be added in the super class
     * and the subclass does not need to add.
     *
     * @param metaDataClass the class that has methods with annotation {@link Command}
     * @param commandHandlers the object that is an instance of metaDataClass
     * @param preprocessor an object for preprocess the input entered by the user. Note that this class can be extends to be used with the other types.
     */
    public static Map<String, CommandHandler> generate(Class metaDataClass, CommandHandlers commandHandlers, InputPreprocessor preprocessor) {
        Map<String, CommandHandler> res = new HashMap<>();
        for (Map.Entry<String, List<Method>> e : filterCommands(metaDataClass).entrySet()) {
            res.put(e.getKey(), CommandHandler.join(e.getValue().stream()
                    .map(m -> generateHandlerFromMethod(e.getKey(), m, commandHandlers, preprocessor))
                    .toArray(CommandHandler[]::new)
            ));
        }
        return res;
    }

    /**
     * The only method in this class, that add all commands represented by commandHandlers's methods (with annotation {@link Command}),
     * which has input preprocessed by preprocessor.
     *
     * This method called {@link #generate(Class, CommandHandlers, InputPreprocessor)} with metaDataClass
     * is commandHandlers.getClass().
     *
     * @param commandHandlers the object that has methods with annotation {@link Command}
     * @param preprocessor an object for preprocess the input entered by the user. Note that this class can be extends to be used with the other types.
     */
    public static Map<String, CommandHandler> generate(CommandHandlers commandHandlers, InputPreprocessor preprocessor) {
        return generate(commandHandlers.getClass(), commandHandlers, preprocessor);
    }

    private static String getCommandNameFromMethod(Method med) {
        Command anno = med.getAnnotation(Command.class);
        if (anno == null) return "";
        return anno.value().isEmpty() ? med.getName() : anno.value();
    }

    private static Map<String, List<Method>> filterCommands(Class cls) {
        Map<String, List<Method>> res = Arrays.stream(cls.getDeclaredMethods())
                .collect(Collectors.groupingBy(ReflectionCommandHandlerGenerator::getCommandNameFromMethod));
        res.remove("");
        return res;
    }

    private static boolean hasHandlerCallbackParam(Method med) {
        if (med.getParameterCount() == 0) return false;
        return HandlerCallback.class.isAssignableFrom(med.getParameterTypes()[med.getParameterCount() - 1]);
    }

    private static CommandHandler generateHandlerFromMethod(
            String commandName,
            Method med,
            CommandHandlers commandHandlers,
            InputPreprocessor preprocessor
    ) {
        return new CommandHandler() {
            @Override
            public void process(Object[] args, HandlerCallback<Object> callback) {
                boolean hasCallback = hasHandlerCallbackParam(med);
                int paramCount = med.getParameterCount() - (hasCallback ? 1 : 0);
                if (paramCount != args.length) {
                    callback.onError(new IncorrectInputException());
                    return ;
                }
                try {
                    Object[] preprocessedArgs = new Object[med.getParameterCount()];
                    System.arraycopy(
                            preprocessor.preprocess(args, Arrays.copyOfRange(med.getParameterTypes(), 0, paramCount)),
                            0, preprocessedArgs, 0, paramCount
                    );
                    if (hasCallback) {
                        preprocessedArgs[paramCount] = new HandlerCallback<>(
                                o -> {
                                    commandHandlers.setCommandInformation(null);
                                    callback.onSuccess(o);
                                },
                                e -> {
                                    commandHandlers.setCommandInformation(null);
                                    callback.onError(e);
                                }
                        );
                    }
                    commandHandlers.setCommandInformation(commandName, Arrays.copyOfRange(preprocessedArgs, 0, paramCount));
                    Object res = med.invoke(commandHandlers, preprocessedArgs);
                    if (!hasCallback) callback.onSuccess(res);
                } catch (CannotPreprocessInputException e) {
                    callback.onError(new IncorrectInputException());
                } catch (IllegalAccessException e) {
                    callback.onError(new Exception("Some how IllegalEaccessException occur. If this error show up, please contact the creator.", e));
                } catch (InvocationTargetException e) {
                    callback.onError((Exception)e.getTargetException());
                } finally {
                    commandHandlers.setCommandInformation(null);
                }

            }

            @Override
            public String getUsage(String commandName) {
                int paramCount = med.getParameterCount() - (hasHandlerCallbackParam(med) ? 1 : 0);
                Usage usage = med.getAnnotation(Usage.class);
                Command command = med.getAnnotation(Command.class);
                assert(command != null);
                String [] params = new String[paramCount];
                for (int i = 0; i < paramCount; ++i) {
                    params[i] = "<" + med.getParameters()[i].getName() + ">";
                }
                String res = commandName + " " + String.join(" ", params);
                if (usage == null) return res + "\n\tThis command has no usage";
                return res + "\n\t" + usage.value();
            }
        };
    }
}

