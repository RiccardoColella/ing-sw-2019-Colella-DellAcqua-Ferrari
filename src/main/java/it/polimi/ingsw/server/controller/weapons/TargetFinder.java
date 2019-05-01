package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.player.Player;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This interface contains a set of methods used to find targets for an action
 *
 * @author Adriana Ferrari
 */
public interface TargetFinder {

    /**
     * Gets the {@code TargetCalculator} that is used
     *
     * @return an {@code Optional} containing the target calculator if present, or {@code Optional.empty()} if it is not
     */
    Optional<TargetCalculator> getCalculator();

    /**
     * Finds the bonus targets - those targets that the weapon will hit no matter where they are on the board
     *
     * @param previouslyHit the targets previously hit by the weapon
     * @param activePlayer the current shooter of the weapon
     * @return a set with the new targets
     */
    Set<Player> getBonusTargets(List<Player> previouslyHit, Player activePlayer);

    /**
     * Limits the targets available to this weapon
     *
     * @param currentWeapon the {@code Weapon} that is used to shoot
     * @return the set of targets that are potentially available, based on their status
     */
    Set<Player> getTargetsToChooseFrom(Weapon currentWeapon);

    /**
     * Adapts the potential targets to the actual scope of the weapon
     *
     * @param potentialTargets the potential targets of this weapon
     * @return a set of groups of targets, adapted to the field of action of the weapon
     */
    Set<Set<Player>> adaptToScope(Set<Player> potentialTargets);

    /**
     * Adds targets to each group of targets, based on the status of the weapon
     *
     * @param potentialTargets the pre-computed potential groups of targets
     * @param weapon the {@code Weapon} used to shoot
     * @return the updated potential groups of targets
     */
    Set<Set<Player>> addToEach(Set<Set<Player>> potentialTargets, Weapon weapon);

    /**
     * Removes targets that cannot be attacked based on the status of the weapon
     *
     * @param potentialTargets the pre-computed potential groups of targets
     * @param weapon the {@code Weapon} used to shoot
     * @return the updated potential groups of targets
     */
    Set<Set<Player>> applyVeto(Set<Set<Player>> potentialTargets, Weapon weapon);

}
