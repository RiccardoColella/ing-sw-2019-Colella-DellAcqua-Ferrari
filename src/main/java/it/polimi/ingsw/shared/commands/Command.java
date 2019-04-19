package it.polimi.ingsw.shared.commands;

public interface Command {
    String getName();
    <T> T getPayload();
}
