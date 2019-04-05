package it.polimi.ingsw.server.model.events.listeners;

import it.polimi.ingsw.server.model.events.PlayerReborn;

import java.util.EventListener;

/**
 * This interface is implemented by all classes that need to know when a player was killed
 */
public interface PlayerRebornListener extends EventListener {

    /**
     * This method is called when a player is brought back to life
     * @param event the event corresponding to the player's rebirth
     */
    void onPlayerReborn(PlayerReborn event);

}
