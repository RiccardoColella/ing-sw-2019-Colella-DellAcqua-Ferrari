package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.shared.viewmodels.Player;

public class WeaponEvent extends PlayerEvent {

    private final String weaponName;

    public WeaponEvent(Player owner, String weaponName) {
        super(owner);
        this.weaponName = weaponName;
    }

    public String getWeaponName() {
        return weaponName;
    }
}
