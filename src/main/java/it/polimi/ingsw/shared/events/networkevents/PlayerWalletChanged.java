package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.shared.datatransferobjects.Player;
import it.polimi.ingsw.shared.datatransferobjects.Wallet;

public class PlayerWalletChanged extends PlayerEvent {

    private final String message;

    public PlayerWalletChanged(Player player, String message) {
        super(player);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
