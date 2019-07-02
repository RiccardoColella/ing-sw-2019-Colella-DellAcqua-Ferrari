package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.player.Player;

/**
 * Event fired when a player's wallet changes
 */
public class PlayerWalletChanged extends PlayerEvent {

    /**
     * Constructs a player wallet changed event
     *
     * @param walletOwner the owner of the wallet
     */
    public PlayerWalletChanged(Player walletOwner) {
        super(walletOwner);
    }

    /**
     * @return the player who owns the wallet
     */
    public Player getPlayer() {
        return (Player)source;
    }
}
