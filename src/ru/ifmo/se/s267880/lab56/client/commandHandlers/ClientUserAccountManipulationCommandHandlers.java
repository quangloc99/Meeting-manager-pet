package ru.ifmo.se.s267880.lab56.client.commandHandlers;

import ru.ifmo.se.s267880.lab56.client.ConsoleWrapper;
import ru.ifmo.se.s267880.lab56.client.Services;
import ru.ifmo.se.s267880.lab56.client.UserInputHelper;
import ru.ifmo.se.s267880.lab56.shared.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.sharedCommandHandlers.UserAccountManipulationCommandHandlers;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

public class ClientUserAccountManipulationCommandHandlers extends ClientCommandsHandlers
    implements UserAccountManipulationCommandHandlers
{
    public ClientUserAccountManipulationCommandHandlers(Services services) {
        super(services);
    }

    @Override
    public void register(Map.Entry<InternetAddress, char[]> userEmailAndPassword, HandlerCallback<Boolean> callback) {
        try {
            userEmailAndPassword.getKey().validate();
            char[] pass = UserInputHelper.getCheckedPassword();
            if (pass == null) {
                callback.onSuccess(true);
                return ;
            }
            userEmailAndPassword.setValue(pass);
            // well this is now the callback hell, so...
            // TODO: change the way to set user default information.
            buildCommandExecutor().run(generateRequest(), new HandlerCallback<>(res -> {
                if (res.<Boolean>getResult()) {
                    services.getCommandController().execute("set-timezone", Collections.emptyList(),
                            new HandlerCallback<>(newRes -> {
                                    ConsoleWrapper.console.println("Registration completed. You are now login.");
                                    callback.onSuccess(true);
                            }, callback::onError)
                    );
                } else {
                    ConsoleWrapper.console.println("Registration aborted.");
                    callback.onSuccess(res.getResult());
                }
            }, callback::onError));
            Arrays.fill(pass, '\0');
        } catch (AddressException e) {
            callback.onError(e);
        }
    }

    @Override
    public void login(Map.Entry<InternetAddress, char[]> userEmailAndPassword, HandlerCallback<Boolean> callback) {
        char[] pass = ConsoleWrapper.console.readPassword("Enter your password: ");
        if (pass == null) {
            callback.onSuccess(true);
            return;
        }
        userEmailAndPassword.setValue(pass);
        buildCommandExecutor().run(generateRequest(), new HandlerCallback<>(res -> {
            System.out.println("Your are logged in.");
            callback.onSuccess(res.getResult());
        }, callback::onError));
        Arrays.fill(pass, '\0');
    }

    @Override
    public void logout(HandlerCallback callback) {
        buildCommandExecutor().run(generateRequest(), new HandlerCallback<>(res -> {
            ConsoleWrapper.console.println("You are logged out. Your progress is till be save and can be access in the next login.");
            callback.onSuccess(null);
        }, callback::onError));
    }

    @Override
    public void listUsers(HandlerCallback<String[]> callback) {
        buildCommandExecutor().run(generateRequest(), new HandlerCallback<>(res -> {
            String[] userEmails = res.getResult();
            ConsoleWrapper.console.println("# List of users:");
            Arrays.stream(userEmails).forEach(ConsoleWrapper.console::println);
            callback.onSuccess(userEmails);
        }, callback::onError));
    }
}
