package it.polimi.ingsw.server.model.events;


import it.polimi.ingsw.server.model.currency.PowerupTile;

import java.util.EventObject;

public class PowerupEvent extends EventObject {

    private final PowerupTile powerupTile;

    public PowerupEvent(PowerupTile powerup){
        super(powerup);
        powerupTile = powerup;
    }

    public PowerupTile getPowerupTile() {
        return powerupTile;
    }
}
