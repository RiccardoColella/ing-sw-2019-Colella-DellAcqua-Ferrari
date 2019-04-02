package it.polimi.ingsw.server.model;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Attack {

    /**
     * This method returns the list of Damageable that it can target based on their position and potential further restrictions
     * @return a list of the groups of targets that can be attacked, which will be empty if none are available
     */
    List<List<Damageable>> getTargets();

    /**
     * This method returns a String representing the name of the Attack
     * @return the name of the attack
     */
    String getName();

    /**
     * This method will deal to the target the kind of damage planned for its type
     * @param target the Damageable chosen for this attack
     * @param targetType the kind of damage that should be applied among the ones available
     */
    void selectTarget(@NotNull Damageable target, @NotNull TargetType targetType);

    /**
     * This method returns the cost of the attack
     * @return the cost that shall be paid to use this attack, the list will be empty if the attack is free
     */
    List<Coin> getCost();

    /**
     * This method returns the damageables this attack was dealt on
     * @return the list of Damageable affected by this attack, the list will be empty if no one was affected
     */
    List<Damageable> execute();

}
