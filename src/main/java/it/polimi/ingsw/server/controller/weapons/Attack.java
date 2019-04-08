package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.player.Damageable;
import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.player.Player;

import java.util.List;
import java.util.Map;

/**
 * This interface schematizes an attack, which is the effect that a weapon has on one or more targets
 */
public interface Attack {

    /**
     * This enum differentiates the possible type of targets of an attack
     */
    enum TargetType {
        TYPE_1,
        TYPE_2
    }


    /**
     * This method returns a list of list of Damageable that the attack can target in one shot
     *
     * @param type the type of target which is to be attacked
     * @return a list of the groups of targets that can be attacked, which will be empty if none are available
     */
    List<List<Damageable>> getTargets(TargetType type);

    /**
     * This method returns a String representing the name of the Attack
     * The name is a Unique Identifier for the attack and must be used to implement hashCode and equals in classes which implement this interface
     *
     * @return the name of the attack
     */
    String getName();

    /**
     * This method returns the cost of the attack
     * @return the cost that shall be paid to use this attack, the list will be empty if the attack is free
     */
    List<Coin> getCost();

    /**
     * This method strikes the attack and returns the damageables this attack was dealt on
     *
     * @param attacker the player who is executing the attack
     * @param targets a Map associating target types to a list of damageable needed to determine the effect
     * @return the list of Damageable affected by this attack, the list will be empty if no one was affected
     */
    List<Damageable> execute(Player attacker, Map<TargetType, List<Damageable>> targets);

    TargetType getSupportedTargetTypes();

}
