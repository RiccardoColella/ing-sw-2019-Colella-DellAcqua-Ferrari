package it.polimi.ingsw.server.model.events;


import it.polimi.ingsw.server.model.currency.PowerupTile;

import java.util.EventObject;

/**
 * Event related to a powerup
 */
public class PowerupEvent extends EventObject {

    /**
     * The powerup involved in this event
     */
    private final PowerupTile powerupTile;

    /**
     * Constructs a powerup event
     *
     * @param powerup the powerup involved in this event
     */
    public PowerupEvent(PowerupTile powerup){
        super(powerup);
        powerupTile = powerup;
    }

    /**
     * @return the powerup involved in this event
     */
    public PowerupTile getPowerupTile() {
        return powerupTile;
    }
}
