package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.player.Player;

/**
 * Event fired when a powerup is exchanged
 */
public class PowerupExchange extends PowerupEvent {

    /**
     * The player which exchanged the powerup
     */
    private final Player player;

    /**
     * Constructs a powerup exchange event
     *
     * @param powerup the powerup involved in this event
     * @param player the player involved in this event
     */
    public PowerupExchange(PowerupTile powerup, Player player){
        super(powerup);
        this.player = player;
    }

    /**
     * @return the player involved in this event
     */
    public Player getPlayer() {
        return player;
    }
}
