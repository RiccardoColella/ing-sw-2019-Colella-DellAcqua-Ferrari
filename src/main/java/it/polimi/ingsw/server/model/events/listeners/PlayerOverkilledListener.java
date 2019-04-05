package it.polimi.ingsw.server.model.events.listeners;

import it.polimi.ingsw.server.model.events.PlayerOverkilled;

import java.util.EventListener;

/**
 * This interface is implemented by all classes that need to know when a player was killed
 */
public interface PlayerOverkilledListener extends EventListener {

    /**
     * This method is called when a player dies
     * @param event the event corresponding to the player's death
     */
    void onPlayerOverkilled(PlayerOverkilled event);

}
