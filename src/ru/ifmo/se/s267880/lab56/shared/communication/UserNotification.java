package ru.ifmo.se.s267880.lab56.shared.communication;

public class UserNotification implements Message<MessageType> {
    @Override
    public MessageType getType() { return MessageType.NOTIFICATION; }

    private String userName;
    private String userAction;

    public UserNotification(String userName, String userAction) {
        this.userName = userName;
        this.userAction = userAction;
    }

    public String getUserAction() {
        return userAction;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public String toString() { return userName + ": " + userAction; }
}
