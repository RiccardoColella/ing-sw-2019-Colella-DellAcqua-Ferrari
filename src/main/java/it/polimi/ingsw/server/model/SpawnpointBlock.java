package it.polimi.ingsw.server.model;

import java.util.List;

/**
 * This class represents all blocks of the spawnpoint type, which allow a player to grab and put back weapons and also they are the starting blocks of the match
 */
public class SpawnpointBlock extends Block {

    /**
     * This property represents the CoinColor associated to the spawnpoint
     */
    private final CoinColor color;

    /**
     * This property stores the weapons that are currently available in the spawnpoint
     */
    private List<Weapon> weapons;

    /**
     * Class constructor given the position in the board, every BoarderType, the color of the spawnpoint and the weapons it contains
     * @param row an int representing the row of the block in the board
     * @param column an int representing the column of the block in the board
     * @param borderNorth BorderType of the northern border
     * @param borderEast BorderType of the eastern border
     * @param borderSouth BorderType of the southern border
     * @param borderWest BorderType of the western border
     * @param color the color associated to the spawnpoint
     * @param weapons the weapons contained in the spawnpoint
     */
    public SpawnpointBlock(int row, int column, BorderType borderNorth, BorderType borderEast, BorderType borderSouth, BorderType borderWest, CoinColor color, List<Weapon> weapons) {
        super(row, column, borderNorth, borderEast, borderSouth, borderWest);
        this.color = color;
        this.weapons = weapons;
    }

    /**
     * This method gets the weapons that are available in the spawnpoint
     * @return a list of the available weapons
     */
    public List<Weapon> getWeapons() {
        return this.weapons;
    }

    /**
     * This method allows for a weapon to be grabbed from the spawnpoint
     * @param weapon the weapon to grab
     */
    public void grabWeapon(Weapon weapon) {
        //TODO: make sure weapon is not null or check and throw exception
        this.weapons.remove(weapon);
    }

    /**
     * This method allows for a weapon to be put back in the spawnpoint
     * @param weapon the weapon to drop
     */
    public void dropWeapon(Weapon weapon) {
        //TODO: throw exception if there are already 3 weapons
        this.weapons.add(weapon);
    }

    /**
     * This method gets the color of the spawnpoint
     * @return the CoinColor corresponding to the color of the spawnpoint
     */
    public CoinColor getColor() {
        return this.color;
    }
}
