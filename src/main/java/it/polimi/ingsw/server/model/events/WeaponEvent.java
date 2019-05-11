package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.weapons.WeaponTile;

public class WeaponEvent extends PlayerEvent {

    private final WeaponTile weaponTile;

    public WeaponEvent(Player player, WeaponTile weaponTile) {
        super(player);
        this.weaponTile = weaponTile;
    }

    public WeaponTile getWeaponTile() {
        return weaponTile;
    }
}
