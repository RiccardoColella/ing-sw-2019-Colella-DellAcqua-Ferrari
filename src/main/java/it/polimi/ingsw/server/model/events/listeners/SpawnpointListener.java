package it.polimi.ingsw.server.model.events.listeners;

import it.polimi.ingsw.server.model.events.WeaponEvent;

import java.util.EventListener;

/**
 * Interface of a class that will react to the status changes of a spawnpoint
 */
public interface SpawnpointListener extends EventListener {

    /**
     * This method is called when a weapon is dropped in the spawnpoint
     * @param e the event corresponding to the weapon being dropped
     */
    void onWeaponDropped(WeaponEvent e);
}
