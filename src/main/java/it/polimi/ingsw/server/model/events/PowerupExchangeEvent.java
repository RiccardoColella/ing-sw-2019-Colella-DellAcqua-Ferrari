package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.player.Player;

public class PowerupExchangeEvent extends PowerupEvent{
    private Player player;

    public PowerupExchangeEvent(String powerup, Player player){
        super(powerup);
        this.player = player;
    }
}
