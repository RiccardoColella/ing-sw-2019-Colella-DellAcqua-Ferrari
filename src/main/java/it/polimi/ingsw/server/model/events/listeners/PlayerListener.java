package it.polimi.ingsw.server.model.events.listeners;

import it.polimi.ingsw.server.model.events.*;

import java.util.EventListener;

/**
 * Interface of a class that will react to the status changes of a player
 */
public interface PlayerListener extends EventListener {

    /**
     * This method is called when a player dies
     * @param e the event corresponding to the player's death
     */
    void onPlayerDied(PlayerDied e);

    /**
     * This method is called when a player is damaged
     * @param e this parameter contains info about the attacker and the damaged player
     */
    void onPlayerDamaged(PlayerDamaged e);

    /**
     * This method is called when a player dies
     * @param e the event corresponding to the player's death
     */
    void onPlayerOverkilled(PlayerOverkilled e);

    /**
     * This method is called when a player is brought back to life
     * @param e the event corresponding to the player's rebirth
     */
    void onPlayerReborn(PlayerEvent e);

    /**
     * This method is called when a player's board is flipped
     * @param e the event corresponding to the player's board flipping
     */
    void onPlayerBoardFlipped(PlayerEvent e);

    /**
     * This method is called when a player's tile is flipped
     * @param e the event corresponding to the player's tile flipping
     */
    void onPlayerTileFlipped(PlayerEvent e);

    /**
     * This method is called when a player reloads a weapon
     * @param e the event corresponding to the player reloading a weapon
     */
    void onWeaponReloaded(PlayerWeaponEvent e);

    /**
     * This method is called when a player unloads a weapon
     * @param e the event corresponding to the player unloading a weapon
     */
    void onWeaponUnloaded(PlayerWeaponEvent e);

    /**
     * This method is called when a player picks up a weapon
     * @param e the event corresponding to the player picking up a weapon
     */
    void onWeaponPicked(WeaponExchanged e);

    /**
     * This method is called when a player drops a weapon
     * @param e the event corresponding to the player dropping a weapon
     */
    void onWeaponDropped(WeaponExchanged e);

    /**
     * This method is called when a player's wallet changes
     * @param e the event corresponding to the player's wallet changing
     */
    void onWalletChanged(PlayerWalletChanged e);

    /**
     * This method is called when a player's health changes
     * @param e the event corresponding to the player's health changing
     */
    void onHealthChanged(PlayerEvent e);

    /**
     * This method is called when a player discards a powerup
     * @param e the event corresponding to the player discarding a powerup
     */
    void onPowerupDiscarded(PowerupExchange e);

    /**
     * This method is called when a player grabs a powerup
     * @param e the event corresponding to the player grabbing a powerup
     */
    void onPowerupGrabbed(PowerupExchange e);

    /**
     * This method is called when a player chooses a spawnpoint
     * @param e the event corresponding to the player choosing a spawnpoint
     */
    void onSpawnpointChosen(SpawnpointChoiceEvent e);
}
