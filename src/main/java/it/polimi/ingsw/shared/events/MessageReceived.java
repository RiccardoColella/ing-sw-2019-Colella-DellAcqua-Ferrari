package it.polimi.ingsw.shared.events;

import it.polimi.ingsw.shared.messages.Message;

import java.util.EventObject;

public class MessageReceived extends EventObject {

    private Message message;

    /**
     * Constructs MessageReceived
     *
     * @param source The object on which the Event initially occurred.
     * @param message The received message
     */
    public MessageReceived(Object source, Message message) {
        super(source);
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
