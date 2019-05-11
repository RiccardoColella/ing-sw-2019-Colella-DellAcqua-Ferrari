package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.player.Player;

import java.util.EventObject;

public class PlayerWalletChanged extends PlayerEvent {

    public PlayerWalletChanged(Player walletOwner) {
        super(walletOwner);
    }

    public Player getPlayer() {
        return (Player)source;
    }
}
