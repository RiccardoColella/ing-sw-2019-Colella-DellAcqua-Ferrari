package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.battlefield.Board;
import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.view.Interviewer;
import it.polimi.ingsw.shared.messages.ClientApi;

import java.awt.*;
import java.util.List;
import java.util.*;
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

    public final void execute(Interviewer interviewer, Weapon weapon) {

        for (ActionConfig actionConfig : actionConfigs) {
            Set<Block> potentialStartingPoints = getPotentialStartingPoints(actionConfig, weapon);
            Block startingPoint;
            if (potentialStartingPoints.size() > 1) {
                Set<Point> points = potentialStartingPoints.stream().map(block -> new Point(block.getColumn(), block.getRow())).collect(Collectors.toSet());
                Point chosenPoint = interviewer.select("Select the starting point for your attack", points, ClientApi.BLOCK_QUESTION);
                startingPoint = board.getBlock(chosenPoint.y, chosenPoint.x).orElseThrow(() -> new IllegalStateException("Block at row: " + chosenPoint.y + " column: " + chosenPoint.x + " does not exist"));
            } else {
                startingPoint = potentialStartingPoints.iterator().next();
            }
            weapon.setStartingBlock(startingPoint);
            Set<Set<Player>> potentialTargets = getPotentialTargets(startingPoint, actionConfig, weapon);
            Optional<Set<Player>> chosenSet;
            if (potentialTargets.isEmpty() && actionConfig.isSkippable()) {
                chosenSet = Optional.empty();
            } else if (potentialTargets.size() == 1 && !actionConfig.isSkippable()) {
                chosenSet = Optional.of(potentialTargets.iterator().next());
            } else if (potentialTargets.isEmpty()) {
                throw new IllegalStateException("No players to hit");
            } else if (actionConfig.isSkippable()) {
                Set<Set<String>> nicknames = mapPlayerToNickName(potentialTargets);
                Optional<Set<String>> chosenNicknames = interviewer.selectOptional("Select the group of targets you want to hit", nicknames, ClientApi.TARGET_QUESTION);
                chosenSet = chosenNicknames.map(strings -> findTargetByNickname(potentialTargets, strings));
            } else {
                Set<Set<String>> nicknames = mapPlayerToNickName(potentialTargets);
                Set<String> chosenNicknames = interviewer.select("Select the group of targets you want to hit", nicknames, ClientApi.TARGET_QUESTION);
                chosenSet = Optional.of(findTargetByNickname(potentialTargets, chosenNicknames));
            }
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

    private Set<Set<String>> mapPlayerToNickName(Set<Set<Player>> potentialTargets) {
        return  potentialTargets
                .stream()
                .map(set -> set
                        .stream()
                        .map(player -> player.getPlayerInfo().getNickname())
                        .collect(Collectors.toSet()))
                .collect(Collectors.toSet());
    }
    private Set<Player> findTargetByNickname(Set<Set<Player>> potentialTargets, Set<String> chosenNicknames) {
        for (Set<Player> potentialSet : potentialTargets) {
            Set<String> setNicknames = potentialSet.stream().map(player -> player.getPlayerInfo().getNickname()).collect(Collectors.toSet());
            if (setNicknames.equals(chosenNicknames)) {
                return potentialSet;
            }
        }
        throw new IllegalStateException("No set was selected");
    }

    protected Set<Block> getPotentialStartingPoints(ActionConfig actionConfig, Weapon weapon) {
        return actionConfig.updateStartingPoint(weapon);
    }

    protected Set<Set<Player>> getPotentialTargets(Block startingPoint, ActionConfig actionConfig, Weapon weapon) {
        Set<Player> computedTargets = null;
        Optional<TargetCalculator> calculator = actionConfig.getCalculator();
        if (calculator.isPresent()) {
            computedTargets = calculator.get().computeTargets(startingPoint, weapon);
        }
        if (computedTargets != null) {
            Set<Player> availableTargets = actionConfig.getTargetsToChooseFrom(weapon);
            computedTargets.removeIf(player -> !availableTargets.contains(player));
        } else {
            computedTargets = actionConfig.getBonusTargets(weapon.getAllTargets(), weapon.getCurrentShooter());
        }
        Set<Set<Player>> potentialTargets = actionConfig.adaptToScope(computedTargets);
        potentialTargets = actionConfig.addToEach(potentialTargets, weapon);
        return actionConfig.applyVeto(potentialTargets, weapon);
    }

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

    protected boolean isValidStartingPoint(Block sp, Weapon weapon) {
        return !this.getPotentialTargets(sp, actionConfigs.get(0), weapon).isEmpty();
    }

    public List<Coin> getCost() {
        return cost;
    }

    List<ActionConfig> getActionConfigs() {
        return this.actionConfigs;
    }

    public boolean basicMustBeDoneFirst() {
        return basicFirst;
    }
}

