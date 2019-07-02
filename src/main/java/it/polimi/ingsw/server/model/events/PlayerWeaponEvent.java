package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.weapons.WeaponTile;

/**
 * Event related to a player and a weapon
 */
public class PlayerWeaponEvent extends PlayerEvent {

    /**
     * The weapon of this event
     */
    private final WeaponTile weaponTile;

    /**
     * Constructs a player weapon event
     *
     * @param player the player involved in this event
     * @param weaponTile the weapon involved in this event
     */
    public PlayerWeaponEvent(Player player, WeaponTile weaponTile) {
        super(player);
        this.weaponTile = weaponTile;
    }

    /**
     * @return the weapon of this event
     */
    public WeaponTile getWeaponTile() {
        return weaponTile;
    }
}
