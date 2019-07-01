package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.shared.datatransferobjects.BonusTile;

/**
 * Network event carrying information about a bonus tile
 *
 * @author Carlo Dell'Acqua
 */
public class BonusTileEvent extends NetworkEvent {

    /**
     * The bonus tile that caused the event
     */
    private final BonusTile bonusTile;

    /**
     * Constructs the event
     *
     * @param bonusTile the bonus tile that caused the event
     */
    public BonusTileEvent(BonusTile bonusTile) {
        this.bonusTile = bonusTile;
    }

    /**
     * @return the bonus tile that caused the event
     */
    public BonusTile getBonusTile() {
        return bonusTile;
    }
}
