package it.polimi.ingsw.server.model;

import java.util.LinkedList;
import java.util.List;

/**
 * This class represents the most basic type of weapon of the game, which only has a basic attack
 */
public class Weapon {
    /**
     * This property represents the Basic Attack, which is something all weapons have
     */
    private final Attack basicAttack;

    /**
     * This property stores the Attack that is currently being executed
     */
    protected Attack activeAttack;

    /**
     * This property represents the weapon's name as a String
     */
    private final String name;

    /**
     * This property stores whether the weapon is loaded, and can therefore be used
     */
    private boolean isLoaded;

    /**
     * This property stores the targets that have been damaged during the current shoot action
     */
    protected List<Damageable> damagedTargets;

    /**
     * This property represents the cost in coin that shall be paid to reload the weapon
     */
    private final List<Coin> reloadCost;

    /**
     * This property represents the cost in coin that shall be paid to purchase the weapon
     */
    private final List<Coin> acquisitionCost;

    /**
     * This constructor assignes all the final values to the weapon, making it ready to be bought
     * @param basicAttack the basic attack of the weapon
     * @param name a string with the name of the weapon
     * @param reloadCost a list of coin equal to the reload cost of the weapon
     * @param acquisitionCost a list of coin equal to the acquisition cost of the weapon
     */
    public Weapon(Attack basicAttack, String name, List<Coin> reloadCost, List<Coin> acquisitionCost) {
        this.basicAttack = basicAttack;
        this.name = name;
        this.isLoaded = false;
        this.activeAttack = null;
        this.damagedTargets = new LinkedList<>();
        this.reloadCost = reloadCost;
        this.acquisitionCost = acquisitionCost;
    }

    /**
     * This method executes the attack that is currently stored as the active attack of the weapon
     */
    public void executeActiveAttack() {

    }

    /**
     * This method sets the chosen attack as active
     * @param attack the attack chosen by the player
     */
    public void chooseAttack(Attack attack) {

    }

    /**
     * This method gets the reload cost of the weapon
     * @return coin list equal to the reload cost
     */
    public List<Coin> getReloadCost() {
        return this.reloadCost;
    }

    /**
     * This method gets the acquisition cost of the weapon
     * @return coin list equal to the acquisition cost
     */
    public List<Coin> getAcquisitionCost() {
        return this.acquisitionCost;
    }

    /**
     * This method gets the basic attack of the weapon
     * @return the basic attack of the weapon
     */
    public Attack getBasicAttack() {
        return this.basicAttack;
    }

    /**
     * This method gets the name of the weapon
     * @return the name of the weapon as a String
     */
    public String getName() {
        return this.name;
    }

    /**
     * This method tells whether the weapon is loaded
     * @return true if the weapon is loaded, otherwise false
     */
    public boolean isLoaded() {
        return this.isLoaded;
    }

    /**
     * This method updates the loaded status of the weapon
     * @param isLoaded boolean which sould be true if the weapon is loaded, otherwise false
     */
    public void setLoaded(boolean isLoaded) {
        this.isLoaded = isLoaded;
    }

}
