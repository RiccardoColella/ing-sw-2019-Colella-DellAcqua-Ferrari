package it.polimi.ingsw.shared.commands;

public interface CommandCommunicator {
    Command receive();
    <T> T receive(String command);
    void send(Command command);
}
