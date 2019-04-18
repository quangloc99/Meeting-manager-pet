package ru.ifmo.se.s267880.lab56.shared.commandsController.helper;

import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
     * This class introduce metaDataClass inorder to get more freedom: the annotations can be added in the super class
     * and the subclass does not need to add.
     *
     * @param cc the command controller that is
     * @param metaDataClass the class that has methods with annotation {@link Command}
     * @param commandHandlers the object that is an instance of metaDataClass
     * @param preprocessor an object for preprocess the input entered by the user. Note that this class can be extends to be used with the other types.
     */
    public static void addCommand(CommandController cc, Class metaDataClass, CommandHandlers commandHandlers, InputPreprocessor preprocessor) {
        filterCommands(metaDataClass).forEach((commandName, methodList) -> {
            cc.addCommand(
                    commandName,
                    generateUsage(methodList),
                    generateHandler(commandName, commandHandlers, methodList, preprocessor)
            );
        });

    }
    /**
     * The only method in this class, that add all commands represented by commandHandlers's methods (with annotation {@link Command}),
     * which has input preprocessed by preprocessor.
     *
     * This method called {@link #addCommand(CommandController, Class, CommandHandlers, InputPreprocessor)} with metaDataClass
     * is commandHandlers.getClass().
     *
     * @param cc the command controller that is
     * @param commandHandlers the object that has methods with annotation {@link Command}
     * @param preprocessor an object for preprocess the input entered by the user. Note that this class can be extends to be used with the other types.
     */
    public static void addCommand(CommandController cc, CommandHandlers commandHandlers, InputPreprocessor preprocessor) {
        addCommand(cc, commandHandlers.getClass(), commandHandlers, preprocessor);
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

    private static CommandController.Handler generateHandler(
            String commandName,
            CommandHandlers commandHandlers,
            List<Method> methodList,
            InputPreprocessor preprocessor
    ) {
        return args ->  {
            for (Method med : methodList) {
                if (med.getParameterCount() != args.length) {
                    continue;
                }
                try {
                    Object[] preprocessedArgs = preprocessor.preprocess(args, med.getParameterTypes());
                    commandHandlers.setCommandInformation(commandName, preprocessedArgs);
                    med.setAccessible(true);
                    return med.invoke(commandHandlers, preprocessedArgs);
                } catch (CannotPreprocessInputException | IllegalAccessException e) {
                    // ignore
                } catch (InvocationTargetException e) {
                    throw (Exception)e.getTargetException();
                } finally {
                    commandHandlers.setCommandInformation(null);
                }
            }
            throw new CommandController.IncorrectInputException();
        };
    }

    private static String generateUsage(List<Method> methods) {
        List<String> usageLines = new LinkedList<>();
        for (Method med: methods) {
            Usage usage = med.getAnnotation(Usage.class);
            Command command = med.getAnnotation(Command.class);
            if (usage == null) continue;
            String args = usage.params().length != 0
                    ? String.join(",", usage.params())
                    : Arrays.stream(med.getParameterTypes())
                         .map(Class::getSimpleName).collect(Collectors.joining(","));
            args = args.isEmpty() ? "If there is no argument" : "If the argumentes are {" + args + "}";
            usageLines.add(String.format("%s, then %s %s",
                    args, usage.value(), command.additional() ? "[Additional]" : ""
            ));
        }
        return usageLines.stream().collect(Collectors.joining("\n"));
    }
}

