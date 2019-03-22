package ru.ifmo.se.s267880.lab56.shared.commandsController.helper;

import ru.ifmo.se.s267880.lab56.CLIWithJSONCommandController;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;
import static ru.ifmo.se.s267880.lab56.shared.Helper.makePair;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
     * The only method in this class, that add all commands represented by commandHandlers's methods (with annotation {@link Command}),
     * which has input preprocessed by preprocessor.
     *
     * @param cc the command controller that is
     * @param commandHandlers the object that has methods with annotation {@link Command}
     * @param preprocessor an object for preprocess the input entered by the user. Note that this class can be extends to be used with the other types.
     */
    public static void addCommand(CommandController cc, Object commandHandlers, InputPreprocessor preprocessor) {
        filterCommands(commandHandlers.getClass()).forEach((commandName, methodList) -> {
            cc.addCommand(commandName, generateHandler(commandHandlers, methodList, preprocessor));
        });
    }

    public static void addCommand(CLIWithJSONCommandController cc, Object commandHandlers, InputPreprocessor preprocessor) {
        filterCommands(commandHandlers.getClass()).forEach((commandName, methodList) -> {
            cc.addCommand(commandName, generateUsage(methodList), generateHandler(commandHandlers, methodList, preprocessor));
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

    private static CommandController.Handler generateHandler(Object commandHandlers, List<Method> methodList, InputPreprocessor preprocessor) {
        final int maxNElement = methodList.stream().mapToInt(Method::getParameterCount).max().getAsInt();
        return args ->  {
            for (Method med : methodList) {
                if (med.getParameterCount() != args.length) {
                    continue;
                }
                try {
                    Object[] preprocessedArgs = preprocessor.preprocess(args,med.getParameterTypes());
                    med.setAccessible(true);
                    med.invoke(commandHandlers, preprocessedArgs);
                    return CommandController.SUCCESS;
                } catch (CannotPreprocessInputException | IllegalAccessException e) {
                    // ignore
                } catch (InvocationTargetException e) {
                    throw (Exception)e.getTargetException();
                }
            }
            return (args.length < maxNElement) ? CommandController.NEED_MORE_INPUT : CommandController.FAIL;
        };
    }

    private static String generateUsage(List<Method> methods) {
        methods = methods.stream().filter(med -> med.isAnnotationPresent(Usage.class)).collect(Collectors.toList());
        List<String> params = methods.stream()
                .map(med -> Arrays.stream(med.getParameterTypes()))
                .map(p -> p.map(Class::getSimpleName))
                .map(p -> p.collect(Collectors.joining(", ", "{", "}")))
                .map(p -> p.equals("{}") ? "If there is no argument" : "If arguments are " + p)
                .collect(Collectors.toList());
        List<String> usages = methods.stream()
                .map(med -> med.getAnnotation(Usage.class).value())
                .collect(Collectors.toList());
        List<String> additional = methods.stream()
                .map(med -> med.getAnnotation(Command.class).additional() ? " [Additional]" : "")
                .collect(Collectors.toList());
        return IntStream.range(0, params.size())
                .mapToObj(i -> String.format("%s, then %s%s", params.get(i), usages.get(i), additional.get(i)))
                .collect(Collectors.joining("\n"));
    }
}

