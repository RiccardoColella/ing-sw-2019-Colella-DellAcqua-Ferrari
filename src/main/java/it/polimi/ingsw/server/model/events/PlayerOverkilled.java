package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.player.Player;

/**
 * Event fired when a player is overkilled
 */
public class PlayerOverkilled extends PlayerEvent {

    /**
     * The killer
     */
    private final Player killer;

    /**
     * Constructs a player overkilled event
     *
     * @param victim the source object
     * @param killer the player who stroke the deadshot
     */
    public PlayerOverkilled(Player victim, Player killer) {
        super(victim);
        this.killer = killer;
    }

    /**
     * @return the victim
     */
    public Player getVictim() {
        return getPlayer();
    }

    /**
     * @return the killer
     */
    public Player getKiller() {
        return killer;
    }
}
