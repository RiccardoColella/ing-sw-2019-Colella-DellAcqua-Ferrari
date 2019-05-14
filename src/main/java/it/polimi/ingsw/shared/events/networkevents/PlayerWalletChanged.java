package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.shared.viewmodels.Player;
import it.polimi.ingsw.shared.viewmodels.Wallet;

public class PlayerWalletChanged extends PlayerEvent {

    private final Wallet wallet;
    private final String message;

    public PlayerWalletChanged(Player player, Wallet wallet, String message) {
        super(player);
        this.wallet = wallet;
        this.message = message;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public String getMessage() {
        return message;
    }
}
