package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.weapons.WeaponTile;

import java.util.EventObject;

public class WeaponDropped extends EventObject {

    private final Block block;

     public WeaponDropped(WeaponTile weapon, Block block){
        super(weapon);
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }
}