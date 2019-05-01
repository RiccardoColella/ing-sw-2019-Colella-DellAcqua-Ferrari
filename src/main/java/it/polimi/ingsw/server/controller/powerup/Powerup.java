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
     * Powerup name
     */
    private final String name;
    /**
     * Powerup trigger
     */
    private final Trigger trigger;
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
     * @param cost the cost of the effect that the powerup owner should pay before the activation
     * @param effect the consumer that will manage the powerup effect
     */
    public Powerup(String name, Trigger trigger, int cost, TriConsumer<Player, Player, Interviewer> effect) {

        this.name = name;
        this.trigger = trigger;
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
}
