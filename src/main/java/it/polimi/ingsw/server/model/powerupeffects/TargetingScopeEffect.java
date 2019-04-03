package it.polimi.ingsw.server.model.powerupeffects;

import it.polimi.ingsw.server.model.*;
import it.polimi.ingsw.server.model.exceptions.MissingOwnershipException;
import it.polimi.ingsw.server.model.exceptions.TypeMismatchException;

public class TargetingScopeEffect extends PowerupEffect {

    /**
     * This constructor needs to be passed the context of the game
     *
     * @param match        the Match the player is associated with
     * @param sourcePlayer the player who is using this effect
     * @param powerupTile  the associated powerup type
     * @throws MissingOwnershipException thrown if the player didn't have the correct PowerupTile for this effect
     * @throws TypeMismatchException     thrown if the powerup tile does not correspond to the effect
     */
    public TargetingScopeEffect(Match match, Player sourcePlayer, PowerupTile powerupTile) throws MissingOwnershipException, TypeMismatchException {
        super(match, sourcePlayer, powerupTile, PowerupType.TARGETING_SCOPE);
    }

    /**
     * Activates the effect making the sourcePlayer pay for it
     * @param coin the cost of this effect the player must pay
     * @throws MissingOwnershipException thrown if the player cannot afford this effect
     */
    public void activate(Coin coin) throws MissingOwnershipException {
        // TODO: determine the concrete type of "coin" to remove it from the associated list in the player
        throw new MissingOwnershipException("The player " + sourcePlayer + " cannot afford this effect " + this);
    }
}
