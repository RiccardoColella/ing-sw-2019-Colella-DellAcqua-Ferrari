package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.battlefield.Board;
import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.view.Interviewer;
import it.polimi.ingsw.shared.messages.ClientApi;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class schematizes an attack, which is the effect that a weapon has on one or more targets
 *
 * @author Adriana Ferrari
 */
public class Attack {

    /**
     * The name of the attack represented with a {@code String}
     */
    protected final String name;

    /**
     * The configurations of this attack
     */
    private final List<ActionConfig> actionConfigs;

    /**
     * The {@code Board} this attack can be executed on
     */
    protected final Board board;

    /**
     * {@code true} if this attack needs the basic attack to be executed before it, {@code false} otherwise
     */
    private boolean basicFirst;

    /**
     * The cost of this attack in {@code Coin}
     */
    protected final List<Coin> cost;

    /**
     * Creates a new {@code Attack}
     *
     * @param name {@code String} representing the name of the attack
     * @param actionConfigs the list of the configurations for this attack
     * @param board the {@code Board} this attack can be used on
     * @param cost the price to pay to use this attack
     */
    public Attack(String name, List<ActionConfig> actionConfigs, Board board, List<Coin> cost) {
        this.name = name;
        this.actionConfigs = Collections.unmodifiableList(actionConfigs);
        this.board = board;
        this.basicFirst = false;
        this.cost = cost;
    }

    /**
     * Copies an existing {@code Attack}, allowing to change {@code basicFirst}
     *
     * @param toCopy the {@code Attack} to copy
     * @param basicFirst whether the attack requires that the basic attack has already been executed
     */
    public Attack(Attack toCopy, boolean basicFirst) {
        this(toCopy.name, toCopy.actionConfigs, toCopy.board, toCopy.cost);
        this.basicFirst = basicFirst;
    }

    /**
     * This method returns a {@code String} representing the name of the {@code Attack}
     * The name is a Unique Identifier for the attack whithin a {@code Weapon}
     *
     * @return the name of the attack
     */
    public String getName() {
        return this.name;
    }

    /**
     * Executes the attack
     *
     * @param interviewer the {@code Interviewer} that will be asked when there is a choice to be made
     * @param weapon the {@code Weapon} used to shoot
     */
    public final void execute(Interviewer interviewer, Weapon weapon) {

        for (ActionConfig actionConfig : actionConfigs) {
            Block startingPoint = findStartingPoint(interviewer, actionConfig, weapon);
            weapon.setStartingBlock(startingPoint);
            Set<Set<Player>> potentialTargets = getPotentialTargets(startingPoint, actionConfig, weapon);
            Optional<Set<Player>> chosenSet = chooseSet(interviewer, actionConfig, potentialTargets);
            if (!chosenSet.isPresent()) {
                int next = actionConfigs.indexOf(actionConfig) + 1;
                if (next >= actionConfigs.size() || !actionConfigs.get(next).isSkippable() && actionConfig.isSkippable()) {
                    break;
                } else if (!actionConfig.isSkippable()) {
                    throw new IllegalStateException("Action was not skippable");
                }
            } else if (chosenSet.get().isEmpty()) {
                throw new IllegalStateException("Empty target selection");
            } else {
                actionConfig.execute(chosenSet.get(), interviewer, weapon);
                weapon.addHitTargets(chosenSet.get(), this);
            }
        }
    }

    /**
     * Finds the starting point for the given action
     *
     * @param interviewer the {@code Interviewer} that will be asked if there is a choice to be made
     * @param actionConfig the current action
     * @param weapon the {@code Weapon} used to shoot
     * @return the {@code Block} corresponding to the starting point
     */
    private Block findStartingPoint(Interviewer interviewer, ActionConfig actionConfig, Weapon weapon) {
        Set<Block> potentialStartingPoints = getPotentialStartingPoints(actionConfig, weapon);
        Block startingPoint;
        if (potentialStartingPoints.size() > 1) {
            Set<Point> points = potentialStartingPoints.stream().map(block -> new Point(block.getColumn(), block.getRow())).collect(Collectors.toSet());
            Point chosenPoint = interviewer.select("Select the starting point for your attack", points, ClientApi.BLOCK_QUESTION);
            startingPoint = board.getBlock(chosenPoint.y, chosenPoint.x).orElseThrow(() -> new IllegalStateException("Block at row: " + chosenPoint.y + " column: " + chosenPoint.x + " does not exist"));
        } else {
            startingPoint = potentialStartingPoints.iterator().next();
        }
        return startingPoint;
    }

    /**
     * Asks for the actual targets for the current action
     *
     * @param interviewer the {@code Interviewer} that will be asked if there is a choice to be made
     * @param actionConfig the current action
     * @param potentialTargets the previously calculated targets
     * @return an {@code Optional} containing the chosen set of targets
     */
    private Optional<Set<Player>> chooseSet(Interviewer interviewer, ActionConfig actionConfig, Set<Set<Player>> potentialTargets) {
        Optional<Set<Player>> chosenSet;
        if (potentialTargets.isEmpty() && actionConfig.isSkippable()) {
            chosenSet = Optional.empty();
        } else if (potentialTargets.size() == 1 && !actionConfig.isSkippable()) {
            chosenSet = Optional.of(potentialTargets.iterator().next());
        } else if (potentialTargets.isEmpty()) {
            throw new IllegalStateException("No players to hit");
        } else if (actionConfig.isSkippable()) {
            Set<Set<String>> nicknames = mapPlayerToNickName(potentialTargets);
            Optional<Set<String>> chosenNicknames = interviewer.selectOptional("Select the group of targets you want to hit", nicknames, ClientApi.TARGET_SET_QUESTION);
            chosenSet = chosenNicknames.map(strings -> findTargetByNickname(potentialTargets, strings));
        } else {
            Set<Set<String>> nicknames = mapPlayerToNickName(potentialTargets);
            Set<String> chosenNicknames = interviewer.select("Select the group of targets you want to hit", nicknames, ClientApi.TARGET_SET_QUESTION);
            chosenSet = Optional.of(findTargetByNickname(potentialTargets, chosenNicknames));
        }
        return chosenSet;
    }

    /**
     * Maps a set of sets of players into a set of sets of strings representing their nicknames
     *
     * @param potentialTargets the set of sets of players to map
     * @return the set of set of strings corresponding to the nicknames
     */
    private Set<Set<String>> mapPlayerToNickName(Set<Set<Player>> potentialTargets) {
        return  potentialTargets
                .stream()
                .map(set -> set
                        .stream()
                        .map(player -> player.getPlayerInfo().getNickname())
                        .collect(Collectors.toSet()))
                .collect(Collectors.toSet());
    }

    /**
     * Finds a set of players given their nicknames
     *
     * @param potentialTargets the set of sets of players in which the desired set is contained
     * @param chosenNicknames the nicknames corresponding to the desired players
     * @return the desired set of players
     */
    private Set<Player> findTargetByNickname(Set<Set<Player>> potentialTargets, Set<String> chosenNicknames) {
        for (Set<Player> potentialSet : potentialTargets) {
            Set<String> setNicknames = potentialSet.stream().map(player -> player.getPlayerInfo().getNickname()).collect(Collectors.toSet());
            if (setNicknames.equals(chosenNicknames)) {
                return potentialSet;
            }
        }
        throw new IllegalStateException("No set was selected");
    }

    /**
     * Gets the potential starting points for an action
     *
     * @param actionConfig the current action
     * @param weapon the {@code Weapon} used to shoot
     * @return the potential starting points, in a {@code Set}
     */
    protected Set<Block> getPotentialStartingPoints(ActionConfig actionConfig, Weapon weapon) {
        return actionConfig.updateStartingPoint(weapon);
    }

    /**
     * Computes the potential targets of an attack
     *
     * @param startingPoint the {@code Block} from which the attack will start
     * @param actionConfig the current action
     * @param weapon the {@code Weapon} used to shoot
     * @return a set containing the groups of potential targets
     */
    protected Set<Set<Player>> getPotentialTargets(Block startingPoint, ActionConfig actionConfig, Weapon weapon) {
        return actionConfig.computePotentialTargets(startingPoint, weapon);
    }

    /**
     * Determines whether this attack is executable by the given weapon in the current situation
     *
     * @param weapon the {@code Weapon} that would be used to shoot
     * @return {@code true} if the attack is executable, {@code false} if it is not
     */
    protected boolean isExecutable(Weapon weapon) {
        if (weapon.getExecutedAttacks().contains(weapon.basicAttack) || !this.basicFirst) {
            boolean executable = false;
            for (Block sp : this.getPotentialStartingPoints(actionConfigs.get(0), weapon)) {
                executable |= isValidStartingPoint(sp, weapon);
            }
            return executable;
        }
        return false;
    }

    /**
     * Determines whether a starting point is valid for the attack - it is valid if it can hit one or more targets
     *
     * @param sp the potential starting point
     * @param weapon the {@code Weapon} that would be used to shoot
     * @return {@code true} if the starting point is valid, {@code false} if it is not
     */
    protected boolean isValidStartingPoint(Block sp, Weapon weapon) {
        return !this.getPotentialTargets(sp, actionConfigs.get(0), weapon).isEmpty();
    }

    /**
     * Gets the cost of the attack
     *
     * @return a list containing the coins needed to execute this attack
     */
    public List<Coin> getCost() {
        return cost;
    }

    /**
     * Gets the action configurations
     *
     * @return the action configurations
     */
    List<ActionConfig> getActionConfigs() {
        return this.actionConfigs;
    }

    /**
     * States whether the basic attack of the weapon must be done before this attack
     *
     * @return {@code true} if the basic attack must be executed first, {@code false} otherwise
     */
    protected boolean basicMustBeDoneFirst() {
        return basicFirst;
    }
}

