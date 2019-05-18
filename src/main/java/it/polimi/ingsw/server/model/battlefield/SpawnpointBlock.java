package it.polimi.ingsw.server.model.battlefield;

import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.events.WeaponEvent;
import it.polimi.ingsw.server.model.events.listeners.SpawnpointListener;
import it.polimi.ingsw.server.model.weapons.WeaponTile;
import it.polimi.ingsw.shared.Direction;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This class represents all blocks of the spawnpoint type, which allow a player to grab and put back weapons and also they are the starting blocks of the match
 */
public class SpawnpointBlock extends Block {

    private final int maxWeapons;
    /**
     * This property represents the CurrencyColor associated to the spawnpoint
     */
    private final CurrencyColor color;

    private Set<SpawnpointListener> listeners = new HashSet<>();

    /**
     * This property stores the weapons that are currently available in the spawnpoint
     */
    private final List<WeaponTile> weapons;

    /**
     * Class constructor given the position in the board, every BoarderType, the color of the spawnpoint and the weapons it contains
     * @param row an int representing the row of the block in the board
     * @param column an int representing the column of the block in the board
     * @param borderNorth BorderType of the northern border
     * @param borderEast BorderType of the eastern border
     * @param borderSouth BorderType of the southern border
     * @param borderWest BorderType of the western border
     * @param color the color associated to the spawnpoint
     * @param maxWeapons the maximum amount of weapons this block can contain
     */
    public SpawnpointBlock(int row, int column, BorderType borderNorth, BorderType borderEast, BorderType borderSouth, BorderType borderWest, CurrencyColor color, int maxWeapons) {
        super(row, column, borderNorth, borderEast, borderSouth, borderWest);
        this.color = color;
        this.weapons = new LinkedList<>();
        this.maxWeapons = maxWeapons;
    }

    /**
     * This method gets the weapons that are available in the spawnpoint
     * @return a list of the available weapons
     */
    public List<WeaponTile> getWeapons() {
        return this.weapons;
    }

    /**
     * This method allows for a weapon to be grabbed from the spawnpoint
     * @param weapon the weapon to grab
     */
    public void grabWeapon(WeaponTile weapon) {
        if (!this.weapons.remove(weapon)) {
            throw new IllegalArgumentException("Grabbing was not possible, the weapon is not in this spawnpoint");
        }
    }

    /**
     * This method gets the color of the spawnpoint
     * @return the CurrencyColor corresponding to the color of the spawnpoint
     */
    public CurrencyColor getColor() {
        return this.color;
    }

    /**
     * This method allows for a weapon to be put back in the spawnpoint
     * @param weapon the weapon to drop
     */
    @Override
    public void drop(WeaponTile weapon) {
        if (this.getWeapons().size() < maxWeapons){
            this.weapons.add(weapon);
            notifyWeaponDropped(weapon);
        } else throw new IllegalStateException("Dropping was not possible, the spawnpoint is full");
    }

    /**
     * Copies the current block creating a new one constructed with the same initial parameters
     *
     * @return the copy
     */
    @Override
    public Block copy() {
        return new SpawnpointBlock(
                this.getRow(),
                this.getColumn(),
                this.getBorderType(Direction.NORTH),
                this.getBorderType(Direction.EAST),
                this.getBorderType(Direction.SOUTH),
                this.getBorderType(Direction.WEST),
                this.color,
                this.maxWeapons
        );
    }

    public int getMaxWeapons() {
        return maxWeapons;
    }

    public void addSpawnpointListener(SpawnpointListener l) {
        listeners.add(l);
    }
    public void removeSpawnpointListener(SpawnpointListener l) {
        listeners.remove(l);
    }
    public void notifyWeaponDropped(WeaponTile weapon) {
        WeaponEvent e = new WeaponEvent(this, weapon);
        listeners.forEach(l -> l.onWeaponDropped(e));
    }
}
