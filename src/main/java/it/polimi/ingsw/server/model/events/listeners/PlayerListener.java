package it.polimi.ingsw.server.model.events.listeners;

import it.polimi.ingsw.server.model.events.*;

import java.util.EventListener;

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

    void onPlayerBoardFlipped(PlayerEvent e);

    void onWeaponReloaded(PlayerWeaponEvent e);

    void onWeaponUnloaded(PlayerWeaponEvent e);

    void onWeaponPicked(WeaponExchanged e);

    void onWeaponDropped(WeaponExchanged e);

    void onWalletChanged(PlayerWalletChanged e);

    void onHealthChanged(PlayerEvent e);

    void onPowerupDiscarded(PowerupExchange e);

    void onPowerupGrabbed(PowerupExchange e);

    void onSpawnpointChosen(SpawnpointChoiceEvent e);
}
