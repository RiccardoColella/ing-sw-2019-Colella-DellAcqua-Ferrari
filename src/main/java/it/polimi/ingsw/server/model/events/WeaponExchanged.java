package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.weapons.WeaponTile;

public class WeaponExchanged extends WeaponEvent {

    private final Block block;

    public WeaponExchanged(Player player, WeaponTile weaponTile, Block block) {
        super(player, weaponTile);
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }
}
