package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.currency.BonusTile;

import java.util.EventObject;

/**
 * Event related to a bonus tile on the board
 */
public class BonusTileBoardEvent extends EventObject {

    /**
     * The bonus tile involved in this event
     */
    private final BonusTile bonusTile;
    /**
     * The location of the bonus tile on the board
     */
    private final Block location;

    /**
     * Constructs a bonus tile board event
     *
     * @param source the source of this event
     * @param bonusTile the bonus tile involved in this event
     * @param location the location of the bonus tile on the board
     */
    public BonusTileBoardEvent(Object source, BonusTile bonusTile, Block location) {
        super(source);
        this.location = location;
        this.bonusTile = bonusTile;
    }

    /**
     * @return the bonus tile involved in this event
     */
    public BonusTile getBonusTile() {
        return bonusTile;
    }


    /**
     * @return the location of the bonus tile on the board
     */
    public Block getLocation() {
        return location;
    }
}
