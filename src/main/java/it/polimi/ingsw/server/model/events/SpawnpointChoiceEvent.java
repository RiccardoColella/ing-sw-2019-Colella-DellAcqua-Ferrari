package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.player.Player;

import java.util.EventObject;

public class SpawnpointChoiceEvent extends EventObject {
    private final Player player;
    public SpawnpointChoiceEvent(CurrencyColor currencyColor, Player player){
        super(currencyColor);
        this.player = player;
    }
}
