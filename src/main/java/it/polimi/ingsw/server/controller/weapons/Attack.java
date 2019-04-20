package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.battlefield.Board;
import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.view.Interviewer;

import java.util.*;

/**
 * This class schematizes an attack, which is the effect that a weapon has on one or more targets
 */
public class Attack {

    protected final String name;
    private final List<ActionConfig> actionConfigs;
    protected final Board board;
    protected boolean basicFirst;
    protected final List<Coin> cost;

    public Attack(String name, List<ActionConfig> actionConfigs, Board board, List<Coin> cost) {
        this.name = name;
        this.actionConfigs = Collections.unmodifiableList(actionConfigs);
        this.board = board;
        this.basicFirst = false;
        this.cost = cost;
    }

    public Attack(Attack toCopy, boolean basicFirst) {
        this(toCopy.name, toCopy.actionConfigs, toCopy.board, toCopy.cost);
        this.basicFirst = basicFirst;
    }

    /**
     * This method returns a String representing the name of the Attack
     * The name is a Unique Identifier for the attack and must be used to implement hashCode and equals in classes which implement this interface
     *
     * @return the name of the attack
     */
    public String getName() {
        return this.name;
    }

    public final void execute(Interviewer interviewer, BasicWeapon weapon) {

        for (ActionConfig actionConfig : actionConfigs) {
            Set<Block> potentialStartingPoints = getPotentialStartingPoints(actionConfig, weapon);
            Block startingPoint;
            if (potentialStartingPoints.size() > 1) {
                startingPoint = interviewer.select(potentialStartingPoints);
            } else {
                startingPoint = potentialStartingPoints.iterator().next();
            }
            Set<Set<Player>> potentialTargets = getPotentialTargets(startingPoint, actionConfig, weapon);
            Optional<Set<Player>> chosenSet;
            if (potentialTargets.isEmpty() && actionConfig.isSkippable()) {
                chosenSet = Optional.empty();
            } else if (potentialTargets.size() == 1 && !actionConfig.isSkippable()) {
                chosenSet = Optional.of(potentialTargets.iterator().next());
            } else if (potentialTargets.isEmpty()) {
                throw new IllegalStateException("No players to hit");
            } else if (actionConfig.isSkippable()) {
                chosenSet = interviewer.selectOptional(potentialTargets);
            } else {
                chosenSet = Optional.of(interviewer.select(potentialTargets));
            }
            if (!chosenSet.isPresent()) {
                //TODO: attack is over
            } else {
                actionConfig.execute(chosenSet.get(), interviewer, weapon);
                weapon.addHitTargets(chosenSet.get(), this);
            }
        }
    }

    protected Set<Block> getPotentialStartingPoints(ActionConfig actionConfig, BasicWeapon weapon) {
        return actionConfig.updateStartingPoint(weapon);
    }

    protected Set<Set<Player>> getPotentialTargets(Block startingPoint, ActionConfig actionConfig, BasicWeapon weapon) {
        Set<Player> computedTargets = null;
        Optional<TargetCalculator> calculator = actionConfig.getCalculator();
        if (calculator.isPresent()) {
            computedTargets = calculator.get().computeTargets(startingPoint);
        }
        if (computedTargets != null) {
            Set<Player> availableTargets = actionConfig.getTargetsToChooseFrom(weapon);
            computedTargets.removeIf(player -> !availableTargets.contains(player));
        } else {
            computedTargets = actionConfig.getBonusTargets(weapon.getAllTargets(), weapon.getCurrentShooter());
        }
        Set<Set<Player>> potentialTargets = actionConfig.adaptToScope(computedTargets);
        potentialTargets = actionConfig.addToEach(potentialTargets, weapon);
        return actionConfig.applyVeto(potentialTargets, weapon.getAllTargets());
    }


    public Set<Player> getLastHit() {
        return new HashSet<>();
    }

    public List<Coin> getCost() {
        return cost;
    }

    List<ActionConfig> getActionConfigs() {
        return this.actionConfigs;
    }
}

