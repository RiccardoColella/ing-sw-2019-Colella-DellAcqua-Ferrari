package it.polimi.ingsw.server.model.events;


import java.util.EventObject;

public class PowerupEvent extends EventObject {

    public PowerupEvent(String powerup){super(powerup);}
}
