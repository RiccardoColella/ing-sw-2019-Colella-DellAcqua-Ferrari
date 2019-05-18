package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.shared.datatransferobjects.Player;

public class PlayerEvent extends NetworkEvent {


    private final Player player;

    public PlayerEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}
