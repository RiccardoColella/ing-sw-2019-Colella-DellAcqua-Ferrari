package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.player.Player;

public class PlayerOverkilled extends PlayerEvent {

    private final Player killer;

    /**
     * @param victim the source object
     * @param killer the player who stroke the deadshot
     */
    public PlayerOverkilled(Player victim, Player killer) {
        super(victim);
        this.killer = killer;
    }

    public Player getVictim() {
        return getPlayer();
    }

    public Player getKiller() {
        return killer;
    }
}
