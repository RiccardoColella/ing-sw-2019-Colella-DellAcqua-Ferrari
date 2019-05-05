package it.polimi.ingsw.server.model.events.listeners;

import it.polimi.ingsw.server.model.events.PlayerDamaged;

import java.util.EventListener;

public interface PlayerDamagedListener extends EventListener {

    /**
     * This method is called when a player is damaged
     * @param e this parameter contains info about the attacker and the damaged player
     */
    void onPlayerDamaged(PlayerDamaged e);
}
