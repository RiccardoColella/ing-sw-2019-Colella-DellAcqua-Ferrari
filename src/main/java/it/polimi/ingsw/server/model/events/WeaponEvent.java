package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.weapons.WeaponTile;

import java.util.EventObject;

/**
 * A generic weapon event
 */
public class WeaponEvent extends EventObject {

    /**
     * The weapon involved in this event
     */
    private final WeaponTile weaponTile;

    /**
     * Constructs a weapon event
     *
     * @param source the source of the event
     * @param weaponTile the weapon involved in this event
     */
    public WeaponEvent(Object source, WeaponTile weaponTile) {
        super(source);
        this.weaponTile = weaponTile;
    }

    /**
     * @return the weapon involved in this event
     */
    public WeaponTile getWeaponTile() {
        return weaponTile;
    }
}
