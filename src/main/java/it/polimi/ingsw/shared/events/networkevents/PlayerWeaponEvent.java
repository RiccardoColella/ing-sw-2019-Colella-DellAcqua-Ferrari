package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.shared.datatransferobjects.Player;

public class PlayerWeaponEvent extends PlayerEvent {

    private final String weaponName;

    public PlayerWeaponEvent(Player owner, String weaponName) {
        super(owner);
        this.weaponName = weaponName;
    }

    public String getWeaponName() {
        return weaponName;
    }
}
