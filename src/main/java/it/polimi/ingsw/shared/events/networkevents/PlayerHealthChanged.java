package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.server.model.player.PlayerColor;
import it.polimi.ingsw.shared.datatransferobjects.Player;

import java.util.List;

/**
 * Network event carrying information about a player's health
 *
 * @author Carlo Dell'Acqua
 */
public class PlayerHealthChanged extends PlayerEvent {

    /**
     * Constructs a player health event
     *
     * @param player the player the health has changed
     */
    public PlayerHealthChanged(Player player) {
        super(player);
    }

    /**
     * @return the current damage tokens
     */
    public List<PlayerColor> getDamages() {
        return getPlayer().getDamage();
    }

    /**
     * @return the current marks
     */
    public List<PlayerColor> getMarks() {
        return getPlayer().getMarks();
    }

    /**
     * @return the current skulls
     */
    public int getSkulls() {
        return getPlayer().getSkulls();
    }
}
