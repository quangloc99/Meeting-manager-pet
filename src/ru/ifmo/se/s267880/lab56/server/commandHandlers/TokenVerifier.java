package ru.ifmo.se.s267880.lab56.server.commandHandlers;

import ru.ifmo.se.s267880.lab56.server.services.MailSender;
import ru.ifmo.se.s267880.lab56.server.Main;
import ru.ifmo.se.s267880.lab56.shared.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.communication.*;

import javax.mail.MessagingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TokenVerifier {
    private static String mailTemplate;
    static {
        InputStream mailTemplateFile = Main.class.getResourceAsStream("res/email-template.html");
        mailTemplate = new BufferedReader(new InputStreamReader(mailTemplateFile)).lines().collect(Collectors.joining("\n"));
    }

    private Broadcaster<MessageType> messageFromClientBroadcaster;
    private Sender messageToClientSender;
    private MailSender mailSender;

    public TokenVerifier(Broadcaster<MessageType> messageFromClientBroadcaster, Sender messageToClientSender, MailSender mailSender) {
        this.messageFromClientBroadcaster = messageFromClientBroadcaster;
        this.messageToClientSender = messageToClientSender;
        this.mailSender = mailSender;
    }

    private static String generateToken() {
        Random random = new Random();
        byte[] res = new byte[18];
        random.nextBytes(res);
        return Base64.getEncoder().encodeToString(res);
    }

    public void verify(String emailAddress, String subject, long timeOut, HandlerCallback<Boolean> callback) {
        try {
            String token = generateToken();
            String mail = MessageFormat.format(mailTemplate, subject, token);
            mailSender.sendHTMLMail(emailAddress, "Token for registration", mail);

            long currentMillis = System.currentTimeMillis();
            Consumer<Message<MessageType>> onReceivingToken = new Consumer<Message<MessageType>>() {
                @Override
                public void accept(Message<MessageType> msg) {
                    long deltaTime = System.currentTimeMillis() - currentMillis;
                    if (deltaTime > timeOut) {
                        callback.onError(new TimeoutException("Your token has been expired."));
                        removeListener();
                        return ;
                    }
                    if (!(msg instanceof Respond)) return;
                    String res = ((Respond) msg).getResult();
                    if (!res.startsWith("Token:")) return;
                    res = res.substring(res.indexOf(":") + 1);
                    if (res.equals(token)) {
                        removeListener();
                        callback.onSuccess(true);
                    } else if (res.equals("\\abort")) {
                        callback.onSuccess(false);
                        removeListener();
                    } else try {
                        messageToClientSender.send(new TokenRequest(
                                "INCORRECT token! " +
                                        "Enter it again. " +
                                        "(Token expires in " + (timeOut - deltaTime) / 1000 + "s)"
                        ));
                    } catch (IOException e) {
                        callback.onError(e);
                    }
                }

                private void removeListener() {
                    messageFromClientBroadcaster.whenReceive(MessageType.RESPOND_SUCCESS).removeListener(this);
                }
            };
            messageFromClientBroadcaster.whenReceive(MessageType.RESPOND_SUCCESS).listen(onReceivingToken);
            messageToClientSender.send(new TokenRequest(
                    "A token has been send to your mail box. " +
                            "Enter the token to complete the operation. " +
                            "(Token expires in " + timeOut / 1000 + " s)."
            ));
        } catch (MessagingException e) {
            callback.onError(new Exception("Token cannot be sent!", e));
        } catch (IOException e) {
            callback.onError(e);
        }

    }
}
