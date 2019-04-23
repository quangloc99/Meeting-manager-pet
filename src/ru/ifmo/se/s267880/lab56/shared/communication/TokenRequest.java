package ru.ifmo.se.s267880.lab56.shared.communication;

public class TokenRequest implements Message<MessageType> {
    @Override
    public MessageType getType() { return MessageType.REQUEST; }

    private String displayingMessage;
    public TokenRequest(String displayingMessage) {
        this.displayingMessage = displayingMessage;
    }

    public String getDisplayingMessage() {
        return displayingMessage;
    }
}
