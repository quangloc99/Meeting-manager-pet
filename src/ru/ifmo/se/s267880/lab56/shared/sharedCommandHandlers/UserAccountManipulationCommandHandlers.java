package ru.ifmo.se.s267880.lab56.shared.sharedCommandHandlers;

import ru.ifmo.se.s267880.lab56.shared.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.Command;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.CommandHandlers;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.Usage;

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
}
