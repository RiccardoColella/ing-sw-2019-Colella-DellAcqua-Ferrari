package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.player.Player;

import java.util.EventObject;

public class PlayerWalletChanged extends EventObject {

    /**
     *
     * @param walletOwner the source object
     */
    public PlayerWalletChanged(Player walletOwner) {
        super(walletOwner);
    }
}
