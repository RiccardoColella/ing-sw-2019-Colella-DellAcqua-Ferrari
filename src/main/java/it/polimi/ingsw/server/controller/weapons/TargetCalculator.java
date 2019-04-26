package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.player.Player;

import java.util.List;
import java.util.Set;

/**
 * This interface schematizes all the possible ways to calculate which damageable entities are actual targets
 */
public interface TargetCalculator {

    /**
     * This method returns the groups of Damageable that can be targeted by the Attack solely based on their position relative to the starting point
     * @param startingPoint the Block relative to which the targets should be
     * @return a list of the available groups of targets, which will be empty if none are available
     */
    Set<Player> computeTargets(Block startingPoint, BasicWeapon weapon);

    boolean contains(TargetCalculator calculator);

    List<TargetCalculator> getSubCalculators();
}
