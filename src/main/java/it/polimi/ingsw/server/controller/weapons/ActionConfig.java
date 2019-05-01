package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.view.Interviewer;
import it.polimi.ingsw.utils.TriConsumer;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * This class stores the configuration of every single action belonging to an {@code Attack}
 *
 * @author Adriana Ferrari
 */
public class ActionConfig {

    /**
     * {@code TriConsumer} that will execute the action
     */
    private final TriConsumer<Set<Player>, Interviewer, Weapon> executor;

    /**
     * {@code true} if the action can be skipped, {@code false} if it cannot
     */
    private final boolean skippable;

    /**
     * {@code Function} that computes the new possible starting points for the given {@code Weapon}
     */
    private final Function<Weapon, Set<Block>> startingPointUpdater;

    /**
     * {@code TargetFinder} containing all the necessary methods and data to compute the potential targets
     */
    private final TargetFinder targetFinder;

    /**
     * Standard constructor
     *
     * @param targetFinder the {@code TargetFinder} used to find targets
     * @param skippable {@code true} if this action is skippable, {@code false} if it must be done
     * @param startingPointUpdater {@code Function} that will compute the potential starting points
     * @param executor {@code Triconsumer} that will execute the action
     */
    public ActionConfig(
            TargetFinder targetFinder,
            boolean skippable,
            Function<Weapon, Set<Block>> startingPointUpdater,
            TriConsumer<Set<Player>, Interviewer, Weapon> executor
    ) {
        this.targetFinder = targetFinder;
        this.executor = executor;
        this.skippable = skippable;
        this.startingPointUpdater = startingPointUpdater;
    }

    /**
     * Gets the target calculator included in this configuration
     *
     * @return an {@code Optional} containing the target calculator if present, or {@code Optional.empty()} if it is not
     */
    public Optional<TargetCalculator> getCalculator() {
        return targetFinder.getCalculator();
    }

    /**
     * Allows to execute the action represented by this configuration
     *
     * @param targets the targets that will receive the effects of this action
     * @param interviewer the {@code Interviewer} to ask when a choice is to be made
     * @param weapon the weapon that is shooting
     */
    public void execute(Set<Player> targets, Interviewer interviewer, Weapon weapon) {
        executor.apply(targets, interviewer, weapon);
    }

    /**
     * States if this action can be skipped
     *
     * @return {@code true} if this action can be skipped, {@code false} if it cannot
     */
    public boolean isSkippable() {
        return skippable;
    }

    /**
     * Gives possible new values for the starting point of the weapon
     *
     * @param weapon the {@code Weapon} that is being used
     * @return a {@code Set} with the possible starting points
     */
    public Set<Block> updateStartingPoint(Weapon weapon) {
        return startingPointUpdater.apply(weapon);
    }

    /**
     * Computes the potential targets for this action
     *
     * @param startingPoint the starting point
     * @param weapon the weapon that will be used to shoot
     * @return a {@code Set} containing the groups of potential targets
     */
    public Set<Set<Player>> computePotentialTargets(Block startingPoint, Weapon weapon) {
        Set<Player> computedTargets = null;
        Optional<TargetCalculator> calculator = targetFinder.getCalculator();
        if (calculator.isPresent()) {
            computedTargets = calculator.get().computeTargets(startingPoint, weapon);
        }
        if (computedTargets != null) {
            Set<Player> availableTargets = targetFinder.getTargetsToChooseFrom(weapon);
            computedTargets.removeIf(player -> !availableTargets.contains(player));
        } else {
            computedTargets = targetFinder.getBonusTargets(weapon.getAllTargets(), weapon.getCurrentShooter());
        }
        Set<Set<Player>> potentialTargets = targetFinder.adaptToScope(computedTargets);
        potentialTargets = targetFinder.addToEach(potentialTargets, weapon);
        return targetFinder.applyVeto(potentialTargets, weapon);
    }
}
