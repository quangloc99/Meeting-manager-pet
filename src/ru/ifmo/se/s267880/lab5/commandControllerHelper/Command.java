package ru.ifmo.se.s267880.lab5.commandControllerHelper;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    String usage();
}
