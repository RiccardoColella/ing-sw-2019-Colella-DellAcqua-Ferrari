package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.exceptions.MissingOwnershipException;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.weapons.Weapon;
import it.polimi.ingsw.server.view.Interviewer;
import it.polimi.ingsw.shared.Direction;
import it.polimi.ingsw.utils.Tuple;

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
     * This property represents the weapon's name as a String. It is a unique identifier of the weapon
     */
    private final String name;

    /**
     * This property stores the targets that have been damaged during the current shoot action. Each damaged set of targets
     * is associated with
     */
    protected List<Tuple<Set<Player>, Attack>> previouslyHit;

    protected Block startingBlock;

    protected Player currentShooter;

    protected List<Attack> availableAttacks;

    protected List<Attack> executedAttacks;

    protected Direction fixedDirection;

    /**
     * This constructor assignes all the final values to the weapon, making it ready to be bought
     *
     * @param name the name of the weapon
     * @param basicAttack the basic attack of the weapon
     */
    public BasicWeapon(Weapon.Name name, Attack basicAttack) {
        this.basicAttack = basicAttack;
        this.name = name.toString();
        this.activeAttack = null;
        this.previouslyHit = new LinkedList<>();
        startingBlock = null;
        availableAttacks = new LinkedList<>();
        executedAttacks = new LinkedList<>();
        fixedDirection = null;
    }

    /**
     * This method gets the name of the weapon
     * @return the name of the weapon as a String
     */

    public String getName() {
        return this.name;
    }

    public void shoot(Interviewer interviewer, Player activePlayer) {
        currentShooter = activePlayer;
        executedAttacks.clear();
        previouslyHit.clear();
        fixedDirection = null;
        activeAttack = basicAttack;
        handlePayment(interviewer, activeAttack, currentShooter);
        executedAttacks.add(basicAttack);
        basicAttack.execute(interviewer, this);
        activeAttack = null;
    }

    protected void addHitTargets(Set<Player> targets, Attack attack) {
        previouslyHit.add(new Tuple<>(new HashSet<>(targets), attack));
    }

    protected void handlePayment(Interviewer interviewer, Attack chosenAttack, Player activePlayer) {
        if (canAffordAttack(chosenAttack)) {
            //TODO: integrate payment handling
        } else throw new IllegalStateException("Unaffordable attacks cannot be chosen");
    }

    public Player getCurrentShooter() {
        return currentShooter;
    }

    public Optional<Block> getStartingPoint() {
        return Optional.ofNullable(this.startingBlock);
    }

    public List<Player> getAllTargets() {
        List<Player> hitTargets = new ArrayList<>();
        previouslyHit.forEach(tuple -> hitTargets.addAll(tuple.getItem1()));
        return hitTargets;
    }

    protected boolean canAffordAttack(Attack attack) {
        List<Coin> activePlayerWallet = this.currentShooter.getAmmoCubes().stream().map(ammoCube -> (Coin) ammoCube).collect(Collectors.toCollection(LinkedList::new));
        activePlayerWallet.addAll(this.currentShooter.getPowerups().stream().map(powerupTile -> (Coin) powerupTile).collect(Collectors.toList()));
        for (Coin coin : attack.getCost()) {
            if (activePlayerWallet.stream().anyMatch(playerCoin -> playerCoin.hasSameValueAs(coin))) {
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

    public List<Attack> getExecutedAttacks() {
        return this.executedAttacks;
    }

    protected boolean canDoFirstAction(Attack attack) {
        this.activeAttack = attack;
        Block oldSp = this.startingBlock;
        if (this.getExecutedAttacks().contains(basicAttack) || !attack.basicMustBeDoneFirst()) {
            Set<Block> startingPoints = attack.getPotentialStartingPoints(attack.getActionConfigs().get(0), this);
            for (Block startingPoint : startingPoints) {
                if (validStartingPoint(attack, startingPoint)) {
                    return true;
                }
            }
        }
        this.startingBlock = oldSp;
        return false;
    }

    protected boolean validStartingPoint(Attack attack, Block startingPoint) {
        Block oldSp = this.startingBlock;
        this.startingBlock = startingPoint;
        this.activeAttack = attack;
        if (!attack.getPotentialTargets(startingPoint, attack.getActionConfigs().get(0), this).isEmpty()) {
            this.startingBlock = oldSp;
            return true;
        }
        this.startingBlock = oldSp;
        return false;
    }

    public Set<Player> wasHitBy(Attack attack) {
        return this.previouslyHit.stream()
                .filter(execution -> execution.getItem2() == attack)
                .flatMap(execution ->
                        execution.getItem1().stream())
                .collect(Collectors.toSet());
    }

    protected void setStartingBlock(Block block) {
        this.startingBlock = block;
    }

    protected Attack getActiveAttack() {
        return this.activeAttack;
    }
}
