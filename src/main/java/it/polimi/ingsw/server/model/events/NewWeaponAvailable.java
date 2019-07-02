package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.battlefield.Board;
import it.polimi.ingsw.server.model.weapons.WeaponTile;

/**
 * Event fired when a new weapon is available
 */
public class NewWeaponAvailable extends BoardEvent {

    /**
     * The block on which the weapon is available
     */
    private final Block block;
    /**
     * The available weapon
     */
    private final WeaponTile weapon;

    /**
     * Constructs this event
     *
     * @param board the board on which the weapon has become available
     * @param weapon the weapon that has become available
     * @param block the location where the weapon has become available
     */
    public NewWeaponAvailable(Board board, WeaponTile weapon, Block block){
        super(board);
        this.weapon = weapon;
        this.block = block;
    }

    /**
     * @return the block on which the weapon is
     */
    public Block getBlock() {
        return block;
    }

    /**
     * @return the weapon that has become available
     */
    public WeaponTile getWeapon() {
        return weapon;
    }
}