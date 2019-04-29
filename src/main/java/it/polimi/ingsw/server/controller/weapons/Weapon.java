package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.player.Player;
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
public class Weapon {
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
     * The {@code Interviewer} that will be asked when there is a choice to make during the shooting session
     */
    protected Interviewer interviewer;

    /**
     * This constructor prepares the weapon so that it can be used
     *
     * @param name the name of the weapon as a {@code String}
     * @param basicAttack the basic attack of the weapon
     */
    public Weapon(String name, Attack basicAttack) {
        this.basicAttack = basicAttack;
        this.name = name;
        this.activeAttack = null;
        this.previouslyHit = new LinkedList<>();
        startingBlock = null;
        availableAttacks = new LinkedList<>();
        executedAttacks = new LinkedList<>();
        fixedDirection = null;
        interviewer = null;
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
    public final void shoot(Interviewer interviewer, Player activePlayer) {
        resetStatus(activePlayer, interviewer);
        do {
            attackSelection();
            if (activeAttack != null) {
                handlePayment(interviewer, activeAttack);
                attackExecution();
            }
        } while (activeAttack != null);
    }

    /**
     * Selects an available attack so that it can be executed
     */
    protected void attackSelection() {
        if (executedAttacks.contains(basicAttack)) {
            activeAttack = null;
        } else {
            activeAttack = basicAttack;
        }
    }

    /**
     * Executes the active attack
     */
    protected final void attackExecution() {
        executedAttacks.add(activeAttack);
        activeAttack.execute(interviewer, this);
    }

    /**
     * Prepares the weapon for a new shooting session
     *
     * @param activePlayer the {@code Player} that is going to use this weapon
     */
    protected void resetStatus(Player activePlayer, Interviewer interviewer) {
        currentShooter = activePlayer;
        this.interviewer = interviewer;
        executedAttacks.clear();
        previouslyHit.clear();
        availableAttacks.clear();
        fixedDirection = null;
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
     */
    protected final void handlePayment(Interviewer interviewer, Attack chosenAttack) {
        if (canAffordAttack(chosenAttack)) {
            //TODO: integrate payment handling
        } else throw new IllegalStateException("Unaffordable attacks cannot be chosen");
    }

    /**
     * Returns the {@code Player} that is currently shooting the weapon, if present - this method should only be used
     * within a shooting action
     *
     * @return the {@code Player} shooting the weapon
     * @throws IllegalStateException if no one is shooting the weapon
     */
    protected Player getCurrentShooter() {
        if (currentShooter != null) {
            return currentShooter;
        } else throw new IllegalStateException("No one is shooting this weapon");
    }

    /**
     * Returns an {@code Optional} wrapping the {@code Block} that represents the starting point of the attacks of this weapon
     *
     * @return the value of {@code startingBlock} wrapped in an {@code Optional}
     */
    protected Optional<Block> getStartingPoint() {
        return Optional.ofNullable(this.startingBlock);
    }

    /**
     * Returns a list of all the targets that were hit by this weapon during the current shooting action. The same target
     * will appear in the list as many time as he was hit
     *
     * @return a {@code List<Player>} with all the players hit by the weapon
     */
    protected List<Player> getAllTargets() {
        List<Player> hitTargets = new ArrayList<>();
        previouslyHit.forEach(tuple -> hitTargets.addAll(tuple.getItem1()));
        return hitTargets;
    }

    /**
     * Determines whether the {@code currentShooter} can or cannot afford an attack
     *
     * @param attack the {@code Attack} that should be evaluated
     * @return {@code true} if the {@code Attack} is affordable, {@code false} if it isn't
     */
    protected final boolean canAffordAttack(Attack attack) {
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

    /**
     * Determines whether the given {@code activePlayer} can execute any {@code Attack} in the current situation
     *
     * @param activePlayer the {@code Player} that wants to shoot this weapon
     * @return {@code true} if {@code activePlayer} can execute at least one {@code Attack}, false otherwise
     */
    public boolean hasAvailableAttacks(Player activePlayer) {
        this.currentShooter = activePlayer;
        if (!canAffordAttack(basicAttack)) {
            return false;
        }
        return canExecuteAttack(basicAttack);
    }

    /**
     * Returns the attacks that have already been executed in the current shooting session
     *
     * @return a {@code List<Attack>} containing the executed attacks
     */
    protected List<Attack> getExecutedAttacks() {
        return this.executedAttacks;
    }

    /**
     * Determines whether an {@code Attack} can be executed
     *
     * @param attack the {@code Attack} to analyze
     * @return {@code true} if {@code attack} can be executed, {@code false} if it cannot
     */
    protected final boolean canExecuteAttack(Attack attack) {
        this.activeAttack = attack;
        Block oldSp = this.startingBlock;
        boolean executable = attack.isExecutable(this);
        this.startingBlock = oldSp;
        return executable;
    }

    /**
     * Returns the {@code Set<Player>} that were hit by {@code attack}. Even if {@code attack} hit a player more than once,
     * he will only appear once in the result (as it is a {@code Set})
     *
     * @param attack the {@code Attack} you want to know the victims of
     * @return a {@code Set<Player>} containing the victime of {@code attack}
     */
    public final Set<Player> wasHitBy(Attack attack) {
        return this.previouslyHit.stream()
                .filter(execution -> execution.getItem2() == attack)
                .flatMap(execution ->
                        execution.getItem1().stream())
                .collect(Collectors.toSet());
    }

    /**
     * Sets the property {@code startingBlock} to the desired value
     *
     * @param block the new starting block
     */
    protected void setStartingBlock(Block block) {
        this.startingBlock = block;
    }

    /**
     * Gets the {@code Attack} that is currently being executed, if any
     *
     * @return the current active {@code Attack}
     * @throws IllegalStateException if no {@code Attack} is being executed
     */
    protected Attack getActiveAttack() {
        if (this.activeAttack != null) {
            return this.activeAttack;
        } else throw new IllegalStateException("No attack is being executed");
    }
}
