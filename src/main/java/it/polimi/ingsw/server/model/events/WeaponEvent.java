package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.weapons.WeaponTile;

import java.util.EventObject;

public class WeaponEvent extends EventObject {

    private final WeaponTile weaponTile;

    public WeaponEvent(Object source, WeaponTile weaponTile) {
        super(source);
        this.weaponTile = weaponTile;
    }

    public WeaponTile getWeaponTile() {
        return weaponTile;
    }
}
