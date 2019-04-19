package it.polimi.ingsw.shared.events;

import it.polimi.ingsw.shared.commands.Command;

import java.util.EventObject;

public class CommandReceived extends EventObject {

    private Command command;

    /**
     * Constructs CommandReceived
     *
     * @param source The object on which the Event initially occurred.
     * @param command The received command
     */
    public CommandReceived(Object source, Command command) {
        super(source);
        this.command = command;
    }

    public Command getCommand() {
        return command;
    }
}
