package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.shared.datatransferobjects.Player;

/**
 * Network event carrying information about a player
 *
 * @author Carlo Dell'Acqua
 */
public class PlayerEvent extends NetworkEvent {

    /**
     * The player
     */
    private final Player player;

    /**
     * Constructs a player event
     *
     * @param player the player who caused this event
     */
    public PlayerEvent(Player player) {
        this.player = player;
    }

    /**
     * @return the player who caused this event
     */
    public Player getPlayer() {
        return player;
    }
}
