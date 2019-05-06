package ru.ifmo.se.s267880.lab56.shared.commandsController.helper;

import ru.ifmo.se.s267880.lab56.shared.functional.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandHandler;
import ru.ifmo.se.s267880.lab56.shared.commandsController.IncorrectInputException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * An utility class that use reflection and annotation to generate commands for {@link CommandController} more easily
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
 *          CommandController cc = new CommandController();  // use CLIWithJsonCommandController in order to display all the commands.
 *          ReflectionCommandHandlerGenerator.generate(MyCommands.class, new MyCommands(), new JsonBasicInputPreprocessor())
 *              .forEach(cc::addCommand);
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
     * Generate command handlers.
     *
     * @param metaDataClass the class that has methods to be handlers, marked with annotation {@link Command}
     * @param commandHandlers the object that is an instance of metaDataClass
     * @param preprocessor an object for preprocess the input.
     */
    public static Map<String, CommandHandler> generate(Class metaDataClass, CommandHandlers commandHandlers, InputPreprocessor preprocessor) {
        Map<String, CommandHandler> res = new LinkedHashMap<>();
        filterCommands(metaDataClass).forEach((commandName, methods) ->
            res.put(commandName, CommandHandler.join(methods.stream()
                    .map(m -> generateHandlerFromMethod(commandName, m, commandHandlers, preprocessor))
                    .toArray(CommandHandler[]::new)
            ))
        );
        return res;
    }

    /**
     * Generate command handlers. This method calls {@link #generate(Class, CommandHandlers, InputPreprocessor)} with
     * metaDataClass is the direct class of commandHandlers.
     *
     * @param commandHandlers the object that is an instance of metaDataClass
     * @param preprocessor an object for preprocess the input.
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
        Map<String, List<Method>> res = new LinkedHashMap<>();
        for (Method method : cls.getDeclaredMethods()) {
            res.computeIfAbsent(getCommandNameFromMethod(method), k -> new ArrayList<>()).add(method);
        }
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
                                o -> commandHandlers.setCommandInformation(null),
                                e -> commandHandlers.setCommandInformation(null)
                        ).andThen(callback);
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
            public String getUsage(String format, String commandName) {
                int paramCount = med.getParameterCount() - (hasHandlerCallbackParam(med) ? 1 : 0);
                Usage usage = med.getAnnotation(Usage.class);
                Command command = med.getAnnotation(Command.class);
                assert(command != null);
                String [] params = new String[paramCount];
                for (int i = 0; i < paramCount; ++i) {
                    params[i] = "<" + med.getParameters()[i].getName() + ">";
                }
                String res = commandName + " " + String.join(" ", params);
                String usageStr = usage == null ? "This command has no usage." : usage.value();
                String[] lines = usageStr.split("\n");
                lines[0] = String.format(format, res, lines[0]);
                for (int i = 1; i < lines.length; ++i) {
                    lines[i] = String.format(format, " ", lines[i]);
                }
                return String.join("\n", lines);
            }
        };
    }
}

