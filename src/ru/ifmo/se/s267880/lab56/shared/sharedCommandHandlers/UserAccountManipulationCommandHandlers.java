package ru.ifmo.se.s267880.lab56.shared.sharedCommandHandlers;

import ru.ifmo.se.s267880.lab56.shared.functional.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandHandler;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.*;

import javax.mail.internet.InternetAddress;
import java.util.Map;

public interface UserAccountManipulationCommandHandlers extends CommandHandlers {
    @Command
    @Usage("Do the registration with email. The password will be asked after entering this command.")
    void register(Map.Entry<InternetAddress, char[]> email, HandlerCallback<Boolean> callback);

    @Command
    @Usage("Login")
    void login(Map.Entry<InternetAddress, char[]> email, HandlerCallback<Boolean> callback);

    @Command
    @Usage("Just logout.")
    void logout(HandlerCallback callback);


    @Command("list-users")
    @Usage("Print all user's email.")
    void listUsers(HandlerCallback<String[]> callback);

    @Override
    default Map<String, CommandHandler> generateHandlers(InputPreprocessor preprocessor) {
        return ReflectionCommandHandlerGenerator.generate(
                UserAccountManipulationCommandHandlers.class, this, preprocessor
        );
    }
}
