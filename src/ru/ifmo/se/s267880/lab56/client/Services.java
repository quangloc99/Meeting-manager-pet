package ru.ifmo.se.s267880.lab56.client;

import ru.ifmo.se.s267880.lab56.client.commandHandlers.CommandToServerExecutor;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;
import ru.ifmo.se.s267880.lab56.shared.communication.Broadcaster;
import ru.ifmo.se.s267880.lab56.shared.communication.MessageType;
import ru.ifmo.se.s267880.lab56.shared.communication.Receiver;
import ru.ifmo.se.s267880.lab56.shared.communication.Sender;

public class Services {
    CommandController commandController;
    Broadcaster<MessageType> messageFromServerBroadcaster;
    Sender messageToServerSender;

    public static class Builder {
        private CommandController commandController;
        private Broadcaster<MessageType> messageFromServerBroadcaster;
        private Sender messageToServerSender;

        public Builder() {}

        public void setCommandController(CommandController commandController) {
            this.commandController = commandController;
        }

        public void setMessageFromServerBroadcaster(Broadcaster<MessageType> messageFromServerBroadcaster) {
            this.messageFromServerBroadcaster = messageFromServerBroadcaster;
        }

        public void setMessageToServerSender(Sender messageToServerSender) {
            this.messageToServerSender = messageToServerSender;
        }

        public Services build() {
            Services res = new Services();
            res.commandController = commandController;
            res.messageFromServerBroadcaster = messageFromServerBroadcaster;
            res.messageToServerSender = messageToServerSender;
            return res;
        }
    }

    private Services() {}

    public CommandToServerExecutor createCommandToServerExecutor() {
        return new CommandToServerExecutor(messageFromServerBroadcaster, messageToServerSender);
    }

    public CommandController getCommandController() {
        return commandController;
    }

    public Broadcaster<MessageType> getMessageFromServerBroadcaster() {
        return messageFromServerBroadcaster;
    }

    public Sender getMessageToServerSender() {
        return messageToServerSender;
    }
}
