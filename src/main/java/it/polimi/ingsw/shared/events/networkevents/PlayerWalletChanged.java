package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.shared.viewmodels.Player;
import it.polimi.ingsw.shared.viewmodels.Wallet;

public class PlayerWalletChanged extends PlayerEvent {

    private final Wallet wallet;

    public PlayerWalletChanged(Player player, Wallet wallet) {
        super(player);
        this.wallet = wallet;
    }

    public Wallet getWallet() {
        return wallet;
    }
}
