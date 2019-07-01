package it.polimi.ingsw.client.io.listeners;

import it.polimi.ingsw.shared.events.networkevents.*;

/**
 * Interface shared between client and server of a class that will react to the status changes of a player
 */
public interface PlayerListener {
    /**
     * This method is called when a player dies
     * @param e the event corresponding to the player's death
     */
    void onPlayerDied(PlayerEvent e);

    /**
     * This method is called when a player is brought back to life
     * @param e the event corresponding to the player's rebirth
     */
    void onPlayerReborn(PlayerEvent e);

    /**
     * This method is called when a player changes their wallet
     * @param e the event corresponding to the player's wallet changing
     */
    void onPlayerWalletChanged(PlayerWalletChanged e);

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
     * This method is called when a player's health changes
     * @param e the event corresponding to the player's health changing
     */
    void onPlayerHealthChanged(PlayerHealthChanged e);

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
    void onWeaponPicked(PlayerWeaponExchanged e);

    /**
     * This method is called when a player drops a weapon
     * @param e the event corresponding to the player dropping a weapon
     */
    void onWeaponDropped(PlayerWeaponExchanged e);

    /**
     * This method is called when a player reconnects to the game
     * @param e the event corresponding to the player's reconnection
     */
    void onPlayerReconnected(PlayerEvent e);

    /**
     * This method is called when a player spawns
     * @param e the event corresponding to the player spawning
     */
    void onPlayerSpawned(PlayerSpawned e);

    /**
     * This method is called when a player is overkilled
     * @param e the event corresponding to the player being overkilled
     */
    void onPlayerOverkilled(PlayerEvent e);

    /**
     * This method is called when it's a new player's turn
     * @param e the event corresponding to the change of turn
     */
    void onActivePlayerChanged(PlayerEvent e);
}
