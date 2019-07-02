package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.battlefield.SpawnpointBlock;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.player.Player;

/**
 * Event fired at the beginning of a match when a player choose his spawnpoint
 */
public class SpawnpointChoiceEvent extends PlayerEvent {

    /**
     * Where the player chose to spawn
     */
    private final SpawnpointBlock destination;

    /**
     * Constructs a spawnpoint choice event
     *
     * @param currencyColor the color of the spawnpoint
     * @param player the player which chosen the spawnpoint
     */
    public SpawnpointChoiceEvent(CurrencyColor currencyColor, Player player){
        super(player);
        this.destination = player.getMatch().getBoard().getSpawnpoint(currencyColor);
    }

    /**
     * @return where the player chose to spawn
     */
    public SpawnpointBlock getDestination() {
        return destination;
    }

}
