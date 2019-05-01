package ru.ifmo.se.s267880.lab56.server.commandHandlers;

import ru.ifmo.se.s267880.lab56.server.Services;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.CommandHandlers;

import java.util.function.Supplier;

abstract public class ServerCommandHandlers implements CommandHandlers {
    protected final Services services;

    public ServerCommandHandlers(Services services) {
        this.services = services;
    }
}
