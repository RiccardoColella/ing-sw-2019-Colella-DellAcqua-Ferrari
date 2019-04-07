package it.polimi.ingsw.server.model.weapons;

import it.polimi.ingsw.server.model.Damageable;
import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.exceptions.MissingOwnershipException;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents the most basic name of weapon of the game, which only has a basic attack
 */
public class Weapon {

    /**
     * This enum cathegorizes the 21 different weapons
     */
    public enum Name {
        LOCK_RIFLE("Lock Rifle"),
        ELECTROSCYTHE("Electroscythe"),
        MACHINE_GUN("Machine Gun"),
        TRACTOR_BEAM("Tractor Beam"),
        THOR("T.H.O.R."),
        VORTEX_CANNON("Vortex Cannon"),
        FURNACE("Furnace"),
        PLASMA_GUN("Plasma Gun"),
        HEATSEEKER("Heatseeker"),
        WHISPER("Whisper"),
        HELLION("Hellion"),
        FLAMETHROWER("Flamethrower"),
        TWO_X_TWO("2x-2"),
        GRENADE_LAUNCHER("Grenade Launcher"),
        SHOTGUN("Shotgun"),
        ROCKET_LAUNCHER("Rocket Launcher"),
        POWER_GLOVE("Power Glove"),
        RAILGUN("Railgun"),
        SHOCKWAVE("Shockwave"),
        CYBERBLADE("Cyberblade"),
        SLEDGEHAMMER("Sledgehammer");

        private String humanReadableName;

        /**
         * Constructs the enum associated with a human readable name
         *
         * @param humanReadableName
         */
        Name(String humanReadableName) {
            this.humanReadableName = humanReadableName;
        }

        @Override
        public String toString() {
            return humanReadableName;
        }
    }

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
    private final Name name;

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
     *
     * @param name the name of the weapon
     * @param basicAttack the basic attack of the weapon
     * @param acquisitionCost a list of coin equal to the acquisition cost of the weapon
     * @param reloadCost a list of coin equal to the reload cost of the weapon
     */
    public Weapon(Name name, Attack basicAttack, List<Coin> acquisitionCost, List<Coin> reloadCost) {
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
        // TODO: attack execution will be implemented after weapons
    }

    /**
     * This method sets the chosen attack as active
     *
     * @param attack the attack chosen by the player
     */
    public void chooseAttack(Attack attack) {
        if (hasAttack(attack)) {
            this.activeAttack = attack;
        } else {
            throw new MissingOwnershipException("Selected attack does not belong to this weapon");
        }
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
    public Name getName() {
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

    /**
     * This method verifies whether or not the attack passed as an argument belongs to this weapon
     *
     * @param attack the attack
     * @return true if the attack belongs to the weapon
     */
    protected boolean hasAttack(Attack attack) {
        return basicAttack.equals(attack);
    }
}
