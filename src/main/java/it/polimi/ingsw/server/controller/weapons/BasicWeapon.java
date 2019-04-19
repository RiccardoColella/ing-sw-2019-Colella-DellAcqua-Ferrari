package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.currency.AmmoCube;
import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.player.Damageable;
import it.polimi.ingsw.server.model.exceptions.MissingOwnershipException;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.weapons.Weapon;

import java.util.*;
import java.util.stream.Collectors;

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

    protected List<Attack> availableAttacks;
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
        availableAttacks = new LinkedList<>();
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

    public void shoot(Communicator communicator, Player activePlayer) {
        currentShooter = activePlayer;
        handlePayment(communicator, activeAttack, currentShooter);
        activeAttack = basicAttack;
        basicAttack.execute(communicator, this);
        previouslyHit.clear();
        activeAttack = null;
    }

    public void addHitTargets(Set<Player> targets) {
        previouslyHit.addAll(targets);
    }

    protected void handlePayment(Communicator communicator, Attack chosenAttack, Player activePlayer) {
        if (canAffordAttack(chosenAttack)) {
            //TODO: integrate payment handling
        } else throw new IllegalStateException("Unaffordable attacks cannot be chosen");
    }

    protected Block determineStartingBlock(Communicator communicator, Player activePlayer) {
        return activePlayer.getMatch().getBoard().findPlayer(activePlayer).orElseThrow(() -> new IllegalStateException("Player is not in the board"));
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

    protected boolean canAffordAttack(Attack attack) {
        List<Coin> activePlayerWallet = this.currentShooter.getAmmoCubes().stream().map(ammoCube -> (Coin) ammoCube).collect(Collectors.toCollection(LinkedList::new));
        activePlayerWallet.addAll(this.currentShooter.getPowerups().stream().map(powerupTile -> (Coin) powerupTile).collect(Collectors.toList()));
        for (Coin coin : attack.getCost()) {
            if (activePlayerWallet.contains(coin)) {
                activePlayerWallet.remove(coin);
            } else {
                return false;
            }
        }
        return true;
    }

    public boolean hasAvailableAttacks(Player activePlayer) {
        this.currentShooter = activePlayer;
        if (!canAffordAttack(basicAttack)) {
            return false;
        }
        return canDoFirstAction(basicAttack);
    }

    protected boolean canDoFirstAction(Attack attack) {
        Set<Block> startingPoints = attack.getPotentialStartingPoints(attack.getActionConfigs().get(0), this);
        for (Block startingPoint : startingPoints) {
            if (!attack.getPotentialTargets(startingPoint, attack.getActionConfigs().get(0), this).isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
