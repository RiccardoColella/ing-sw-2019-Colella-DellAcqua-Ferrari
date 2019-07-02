package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.currency.BonusTile;

import java.util.EventObject;

/**
 * Event related to a bonus tile
 */
public class BonusTileEvent extends EventObject {

    /**
     * The bonus tile that is involved in this event
     */
    private final BonusTile bonusTile;

    /**
     * Constructs a bonus tile event
     *
     * @param source the source of this event
     * @param bonusTile the bonus tile involved in this event
     */
    public BonusTileEvent(Object source, BonusTile bonusTile) {
        super(source);
        this.bonusTile = bonusTile;
    }

    /**
     * @return the bonus tile involved in this event
     */
    public BonusTile getBonusTile() {
        return bonusTile;
    }
}
