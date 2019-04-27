package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.weapons.Weapon;
import it.polimi.ingsw.server.view.Interviewer;
import it.polimi.ingsw.shared.Direction;
import it.polimi.ingsw.utils.Tuple;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class represents the most basic type of weapon of the game, which only has a basic attack
 *
 * @author Adriana Ferrari
 */
public class BasicWeapon {
    /**
     * The Basic Attack, which is the {@code Attack} all weapons must have
     */
    protected final Attack basicAttack;

    /**
     * The {@code Attack} that is currently being executed
     */
    protected Attack activeAttack;

    /**
     * The weapon's name as a {@code String}. It is a unique identifier of the weapon
     */
    private final String name;

    /**
     * The targets that have been damaged during the current shoot action. Each damaged set of targets
     * is associated with the {@code Attack} that was used to deal the damage
     */
    protected List<Tuple<Set<Player>, Attack>> previouslyHit;

    /**
     * The {@code Block} from which {@code Attacks} executed by this weapon should start
     */
    private Block startingBlock;

    /**
     * The {@code Player} that is currently using this weapon
     */
    protected Player currentShooter;

    /**
     * A list of the attacks that can be executed by this weapon in the current situation
     */
    protected List<Attack> availableAttacks;

    /**
     * The attacks that have already been executed by the weapon during the current shoot
     */
    protected List<Attack> executedAttacks;

    /**
     * The {@code Direction} that this weapon can target in the current situation, or {@code null} all directions are available
     */
    protected Direction fixedDirection;

    /**
     * This constructor prepares the weapon so that it can be used
     *
     * @param name the name of the weapon as a {@code String}
     * @param basicAttack the basic attack of the weapon
     */
    public BasicWeapon(String name, Attack basicAttack) {
        this.basicAttack = basicAttack;
        this.name = name;
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

    /**
     * This method will execute the shooting action
     *
     * @param interviewer the {@code Interviewer} that should make the decisions when multiple choices are available
     * @param activePlayer the {@code Player} that is shooting the weapon
     */
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

    /**
     * Adds {@code targets} to the targets hit by this weapon
     *
     * @param targets the targets to add
     * @param attack the {@code Attack} that was used to hit the targets
     */
    protected void addHitTargets(Set<Player> targets, Attack attack) {
        previouslyHit.add(new Tuple<>(new HashSet<>(targets), attack));
    }

    /**
     * Handles the payment of the selected attack
     *
     * @param interviewer the {@code Interviewer} that should make the decisions when multiple choices are available
     * @param chosenAttack the {@code Attack} that is being "bought"
     * @param activePlayer the {@code Player} that is shooting this weapon
     */
    protected void handlePayment(Interviewer interviewer, Attack chosenAttack, Player activePlayer) {
        if (canAffordAttack(chosenAttack)) {
            //TODO: integrate payment handling
        } else throw new IllegalStateException("Unaffordable attacks cannot be chosen");
    }

    protected Player getCurrentShooter() {
        return currentShooter;
    }

    protected Optional<Block> getStartingPoint() {
        return Optional.ofNullable(this.startingBlock);
    }

    protected List<Player> getAllTargets() {
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
