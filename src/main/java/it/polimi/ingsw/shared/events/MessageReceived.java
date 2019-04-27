package it.polimi.ingsw.shared.events;

import it.polimi.ingsw.shared.messages.Message;

import java.util.EventObject;

/**
 * Event fired when a message is received
 *
 * @author Carlo Dell'Acqua
 */
public class MessageReceived extends EventObject {

    /**
     * The received message
     */
    private Message message;

    /**
     * Constructs this class
     *
     * @param source the object on which the Event initially occurred.
     * @param message the received message
     */
    public MessageReceived(Object source, Message message) {
        super(source);
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
