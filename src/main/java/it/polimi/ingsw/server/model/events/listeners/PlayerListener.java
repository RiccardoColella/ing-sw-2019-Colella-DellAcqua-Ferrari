package it.polimi.ingsw.server.model.events.listeners;

import it.polimi.ingsw.server.model.events.PlayerDamaged;
import it.polimi.ingsw.server.model.events.PlayerDied;
import it.polimi.ingsw.server.model.events.PlayerOverkilled;
import it.polimi.ingsw.server.model.events.PlayerReborn;

import java.util.EventListener;

public interface PlayerListener extends EventListener {

    /**
     * This method is called when a player dies
     * @param event the event corresponding to the player's death
     */
    void onPlayerDied(PlayerDied event);

    /**
     * This method is called when a player is damaged
     * @param e this parameter contains info about the attacker and the damaged player
     */
    void onPlayerDamaged(PlayerDamaged e);

    /**
     * This method is called when a player dies
     * @param event the event corresponding to the player's death
     */
    void onPlayerOverkilled(PlayerOverkilled event);

    /**
     * This method is called when a player is brought back to life
     * @param event the event corresponding to the player's rebirth
     */
    void onPlayerReborn(PlayerReborn event);
}
