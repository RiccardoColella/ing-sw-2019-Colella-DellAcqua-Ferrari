package it.polimi.ingsw.client.io.listeners;

import it.polimi.ingsw.shared.events.networkevents.BonusTileEvent;
import it.polimi.ingsw.shared.events.networkevents.PlayerMoved;
import it.polimi.ingsw.shared.events.networkevents.WeaponEvent;

/**
 * Interface shared between client and server of a class that will react to the status changes of the board
 */
public interface BoardListener {
    /**
     * This method is called when a player moves
     * @param e the event corresponding to a player moving
     */
    void onPlayerMoved(PlayerMoved e);

    /**
     * This method is called when a player teleports
     * @param e the event corresponding to a player teleporting
     */
    void onPlayerTeleported(PlayerMoved e);

    /**
     * This method is called when a new weapon is available
     * @param e the event corresponding to a new weapon being available
     */
    void onNewWeaponAvailable(WeaponEvent e);

    /**
     * This method is called when a bonus tile is grabbed
     * @param e the event corresponding to a bonus tile being grabbed
     */
    void onBonusTileGrabbed(BonusTileEvent e);

    /**
     * This method is called when a bonus tile is dropped
     * @param e the event corresponding to a bonus tile being dropped
     */
    void onBonusTileDropped(BonusTileEvent e);
}
