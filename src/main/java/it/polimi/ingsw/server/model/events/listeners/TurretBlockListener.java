package it.polimi.ingsw.server.model.events.listeners;

import it.polimi.ingsw.server.model.events.BonusTileEvent;

import java.util.EventListener;

/**
 * Interface of a class that will react to the status changes of a turret block
 */
public interface TurretBlockListener extends EventListener {

    /**
     * This method is called when a bonus tile is dropped in the turret
     * @param e the event corresponding to the bonus tile being dropped
     */
    void onBonusTileDropped(BonusTileEvent e);

    /**
     * This method is called when a bonus tile is grabbed from the turret
     * @param e the event corresponding to the bonus tile being grabbed
     */
    void onBonusTileGrabbed(BonusTileEvent e);
}
