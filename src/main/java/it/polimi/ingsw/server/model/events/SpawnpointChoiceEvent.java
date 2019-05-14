package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.battlefield.SpawnpointBlock;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.player.Player;

import java.util.EventObject;

public class SpawnpointChoiceEvent extends PlayerEvent {

    private final SpawnpointBlock destination;

    public SpawnpointChoiceEvent(CurrencyColor currencyColor, Player player){
        super(player);
        this.destination = player.getMatch().getBoard().getSpawnpoint(currencyColor);
    }

    public SpawnpointBlock getDestination() {
        return destination;
    }

}
