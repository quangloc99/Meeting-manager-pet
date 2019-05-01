package ru.ifmo.se.s267880.lab56.server.services;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Properties;

public class MailSender {
    private String from;
    private String username;
    private String password;
    private Session messageSession;

    public static MailSender fromJson(JsonElement elm) {
        JsonObject obj = elm.getAsJsonObject();
        MailSender res = new MailSender();
        res.setFrom(obj.get("email-address").getAsString());
        res.setUsername(obj.get("username").getAsString());
        res.setPassword(obj.get("password").getAsString());
        Properties props = System.getProperties();
        for (Map.Entry<String, JsonElement> entry : obj.getAsJsonObject("properties").entrySet()) {
            props.put(entry.getKey(), entry.getValue().getAsString());
        }
        res.setMessageSession(Session.getDefaultInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(res.username, res.password);
            }
        }));
        return res;
    }

    public synchronized void sendHTMLMail(String to, String subject, String body) throws MessagingException {
        MimeMessage msg = new MimeMessage(messageSession);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
        msg.setSubject(subject, "UTF-8");
        msg.setText(body, "UTF-8");
        Transport.send(msg);
    }

    public synchronized void setFrom(String from) {
        this.from = from;
    }

    public synchronized void setMessageSession(Session messageSession) {
        this.messageSession = messageSession;
    }

    public synchronized void setPassword(String password) {
        this.password = password;
    }

    public synchronized void setUsername(String username) {
        this.username = username;
    }
}
