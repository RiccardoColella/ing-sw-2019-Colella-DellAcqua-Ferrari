package it.polimi.ingsw.server.model.events.listeners;

import it.polimi.ingsw.server.model.events.BonusTileBoardEvent;
import it.polimi.ingsw.server.model.events.NewWeaponAvailable;
import it.polimi.ingsw.server.model.events.PlayerMoved;

import java.util.EventListener;

/**
 * Interface of a class that will react to the status changes of the board
 */
public interface BoardListener extends EventListener {

    /**
     * This method is called when a player teleports
     * @param e the event corresponding to the player teleporting
     */
    void onPlayerTeleported(PlayerMoved e);

    /**
     * This method is called when a player moves
     * @param e the event corresponding to the player moving
     */
    void onPlayerMoved(PlayerMoved e);

    /**
     * This method is called when a new weapon is available
     * @param e the event corresponding to a new weapon being available
     */
    void onNewWeaponAvailable(NewWeaponAvailable e);

    /**
     * This method is called when a bonus tile is grabbed
     * @param e the event corresponding to a bonus tile being grabbed
     */
    void onBonusTileGrabbed(BonusTileBoardEvent e);

    /**
     * This method is called when a bonus tile is dropped
     * @param e the event corresponding to a bonus tile being dropped
     */
    void onBonusTileDropped(BonusTileBoardEvent e);
}
