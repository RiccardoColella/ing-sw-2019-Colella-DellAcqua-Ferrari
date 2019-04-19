package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.currency.AmmoCube;
import it.polimi.ingsw.server.model.player.Damageable;
import it.polimi.ingsw.server.model.exceptions.MissingOwnershipException;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.weapons.Weapon;

import java.util.*;

/**
 * This class represents the most basic name of weapon of the game, which only has a basic attack
 */
public class BasicWeapon {

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
    private final Weapon.Name name;

    /**
     * This property stores the targets that have been damaged during the current shoot action
     */
    protected List<Player> previouslyHit;


    protected Block startingBlock;

    protected Player currentShooter;


    /**
     * This constructor assignes all the final values to the weapon, making it ready to be bought
     *
     * @param name the name of the weapon
     * @param basicAttack the basic attack of the weapon
     */
    public BasicWeapon(Weapon.Name name, Attack basicAttack) {
        this.basicAttack = basicAttack;
        this.name = name;
        this.activeAttack = null;
        this.previouslyHit = new LinkedList<>();
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
        currentShooter = activePlayer;
        activeAttack = basicAttack;
        handlePayment(communicator, activeAttack, currentShooter);
        basicAttack.execute(communicator, this);
    }

    public void addHitTargets(Set<Player> targets) {
        previouslyHit.addAll(targets);
    }

    protected void handlePayment(Communicator communicator, Attack chosenAttack, Player activePlayer) {

    }

    protected Block determineStartingBlock(Communicator communicator, Player activePlayer) {
        return activePlayer.getMatch().getBoard().findPlayer(activePlayer).orElseThrow(() -> new IllegalStateException("Player is not in the board"));
    }

    protected void initAttack() {

    }

    public Player getCurrentShooter() {
        return currentShooter;
    }

    public List<Attack> getExecutedAttacks() {
        return new LinkedList<>();
    }

    public Optional<Block> getStartingPoint() {
        return Optional.ofNullable(this.startingBlock);
    }

    public List<Player> getAllTargets() {
        return previouslyHit;
    }
}
