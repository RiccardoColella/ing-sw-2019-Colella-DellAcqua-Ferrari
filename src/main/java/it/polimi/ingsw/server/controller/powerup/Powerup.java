package it.polimi.ingsw.server.controller.powerup;

import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.view.Interviewer;
import it.polimi.ingsw.utils.TriConsumer;


/**
 * This class represents a controller-side Powerup and it's used to generalize all the effects
 *
 * @author Carlo Dell'Acqua
 */
public class Powerup {

    /**
     * Enum containing the different name of triggers that the controller should use to determine when to ask the player
     * if he wants to activate his powerup
     */
    public enum Trigger {
        IN_BETWEEN_ACTIONS,
        ON_DAMAGE_RECEIVED,
        ON_DAMAGE_GIVEN
    }

    /**
     * Enum containing the different target types that the controller should use to determine which players can be affected by this powerup
     */
    public enum Target {
        OTHERS,
        ATTACKER,
        SELF,
        DAMAGED
    }

    /**
     * Enum containing the different constraints a target must satisfy to be affected by the powerup effect
     */
    public enum TargetConstraint {
        NONE,
        VISIBLE
    }

    /**
     * Powerup name
     */
    private final String name;
    /**
     * Powerup trigger
     */
    private final Trigger trigger;
    /**
     * Powerup target
     */
    private final Target target;
    /**
     * Powerup target constraint
     */
    private final TargetConstraint targetConstraint;
    /**
     * A powerup can have a cost, expressed as an integer because any Coin color is valid
     */
    private final int cost;
    /**
     * The actual effect that this powerup should cause
     */
    private TriConsumer<Player, Player, Interviewer> effect;

    /**
     * Constructs a controller-side powerup configured with the desired effect
     *
     * @param name the powerup name associated with this powerup
     * @param trigger the kind of trigger that makes this powerup available for the player
     * @param target the kind of target this powerup effect can affect
     * @param targetConstraint the kind of constraint a target must satisfy
     * @param cost the cost of the effect that the powerup owner should pay before the activation
     * @param effect the consumer that will manage the powerup effect
     */
    public Powerup(String name, Trigger trigger, Target target, TargetConstraint targetConstraint, int cost, TriConsumer<Player, Player, Interviewer> effect) {

        this.name = name;
        this.trigger = trigger;
        this.target = target;
        this.targetConstraint = targetConstraint;
        this.cost = cost;
        this.effect = effect;
    }

    /**
     * Activates this powerup
     *
     * @param owner the player who is using this powerup
     * @param target the target of the powerup effect
     * @param interviewer the interviewer needed for the communication
     */
    public void activate(Player owner, Player target, Interviewer interviewer) {

        effect.accept(owner, target, interviewer);
    }

    /**
     * @return the name of this powerup
     */
    public String getName() {
        return name;
    }

    /**
     * @return the cost of this powerup (any coin color is valid)
     */
    public int getCost() {
        return cost;
    }

    /**
     * @return the trigger that makes this powerup available
     */
    public Trigger getTrigger() {
        return trigger;
    }

    /**
     * @return the target that can be affected by this powerup
     */
    public Target getTarget() {
        return target;
    }

    /**
     * @return the constraint the target must satisfy to be affected by the powerup effect
     */
    public TargetConstraint getTargetConstraint() {
        return targetConstraint;
    }
}
