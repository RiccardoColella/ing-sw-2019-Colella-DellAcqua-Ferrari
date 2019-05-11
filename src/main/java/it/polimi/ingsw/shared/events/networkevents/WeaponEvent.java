package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.shared.viewmodels.Player;

import java.util.List;

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
