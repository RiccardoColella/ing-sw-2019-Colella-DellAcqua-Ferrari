package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.player.Player;

public class PowerupExchange extends PowerupEvent {

    private final Player player;

    public PowerupExchange(PowerupTile powerup, Player player){
        super(powerup);
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}
