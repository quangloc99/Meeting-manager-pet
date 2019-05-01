package ru.ifmo.se.s267880.lab56.server.commandHandlers;

import ru.ifmo.se.s267880.lab56.server.services.Services;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.CommandHandlers;

abstract public class ServerCommandHandlers implements CommandHandlers {
    protected final Services services;

    public ServerCommandHandlers(Services services) {
        this.services = services;
    }
}
