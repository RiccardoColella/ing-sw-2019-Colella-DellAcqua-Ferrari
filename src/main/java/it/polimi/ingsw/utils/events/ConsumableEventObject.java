package it.polimi.ingsw.utils.events;

import java.util.EventObject;

/**
 * A generic event that can be consumed during its dispatchment
 *
 * @author Carlo Dell'Acqua
 */
public abstract class ConsumableEventObject extends EventObject {

    /**
     * True if the event has been consumed by a listener
     */
    private boolean consumed = false;

    /**
     * Constructs a consumable event based on the standard EventObject
     *
     * @param source the source object that creates the event
     */
    public ConsumableEventObject(Object source) {
        super(source);
    }

    /**
     * Sets the consumed flag to true so that the source of this event can handle the situation
     */
    public void consume() {
        consumed = true;
    }

    /**
     * @return true if the event has been consumed, else otherwise
     */
    public boolean isConsumed() {
        return consumed;
    }
}
