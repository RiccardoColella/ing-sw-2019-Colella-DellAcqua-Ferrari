package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.currency.AmmoCube;
import it.polimi.ingsw.server.model.player.Damageable;
import it.polimi.ingsw.server.model.exceptions.MissingOwnershipException;
import it.polimi.ingsw.server.model.player.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents the most basic name of weapon of the game, which only has a basic attack
 */
public class BasicWeapon {

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
         *
         * @param humanReadableName a human readable name for the weapon
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
    protected final Attack basicAttack;

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
    private final List<AmmoCube> reloadCost;

    /**
     * This property represents the cost in coin that shall be paid to purchase the weapon
     */
    private final List<AmmoCube> acquisitionCost;

    protected Block startingBlock;


    /**
     * This constructor assignes all the final values to the weapon, making it ready to be bought
     *
     * @param name the name of the weapon
     * @param basicAttack the basic attack of the weapon
     * @param acquisitionCost a list of coin equal to the acquisition cost of the weapon
     * @param reloadCost a list of coin equal to the reload cost of the weapon
     */
    public BasicWeapon(Name name, Attack basicAttack, List<AmmoCube> acquisitionCost, List<AmmoCube> reloadCost) {
        this.basicAttack = basicAttack;
        this.name = name;
        this.isLoaded = false;
        this.activeAttack = null;
        this.damagedTargets = new LinkedList<>();
        this.reloadCost = reloadCost;
        this.acquisitionCost = acquisitionCost;
        startingBlock = null;
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
     * This method gets the name of the weapon
     * @return the name of the weapon as a String
     */

    public String getName() {
        return this.name.toString();
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

    public void askForTargets() {
        //TODO: ask view for targets and set them as the current target
    }

    public void shoot(Communicator communicator, Player activePlayer) {
        //TODO: implement shooting handling
        //this method should be overridden by children
        activeAttack = basicAttack;
        handlePayment(communicator, activeAttack, activePlayer);
        basicAttack.execute(communicator, activePlayer, HashSet::new, HashMap::new);
    }

    protected void handlePayment(Communicator communicator, Attack chosenAttack, Player activePlayer) {

    }

    protected Block determineStartingBlock(Communicator communicator, Player activePlayer) {
        return activePlayer.getMatch().getBoard().findPlayer(activePlayer).orElseThrow(() -> new IllegalStateException("Player is not in the board"));
    }

    protected void initAttack() {

    }
}
