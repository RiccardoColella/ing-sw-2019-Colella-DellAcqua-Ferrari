package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.battlefield.Board;
import it.polimi.ingsw.server.model.weapons.WeaponTile;

import java.util.EventObject;

public class NewWeaponAvailable extends BoardEvent {

    private final Block block;
    private final WeaponTile weapon;

    public NewWeaponAvailable(Board board, WeaponTile weapon, Block block){
        super(board);
        this.weapon = weapon;
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }

    public WeaponTile getWeapon() {
        return weapon;
    }
}