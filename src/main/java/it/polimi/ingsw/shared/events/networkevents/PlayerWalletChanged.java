package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.shared.datatransferobjects.Player;

/**
 * Network event carrying information about a player's wallet
 *
 * @author Carlo Dell'Acqua
 */
public class PlayerWalletChanged extends PlayerEvent {

    /**
     * The message about the wallet
     */
    private final String message;

    /**
     * Constructs a player's wallet event
     *
     * @param player the player who owns the wallet
     * @param message the message to show to the view
     */
    public PlayerWalletChanged(Player player, String message) {
        super(player);
        this.message = message;
    }

    /**
     * @return the message to show to the view
     */
    public String getMessage() {
        return message;
    }
}
