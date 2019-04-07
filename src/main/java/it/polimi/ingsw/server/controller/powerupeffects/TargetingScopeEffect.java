package it.polimi.ingsw.server.controller.powerupeffects;

import it.polimi.ingsw.server.model.*;
import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.exceptions.MissingOwnershipException;
import it.polimi.ingsw.server.model.exceptions.TypeMismatchException;
import it.polimi.ingsw.server.model.player.Player;

import java.util.Collections;

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
        super(match, sourcePlayer, powerupTile, PowerupTile.Type.TARGETING_SCOPE);
    }

    /**
     * Activates the effect making the sourcePlayer pay for it
     * @param coin the cost of this effect the player must pay
     */
    public void activate(Coin coin) {

        try {
            sourcePlayer.pay(Collections.singletonList(coin));
        } catch (MissingOwnershipException e) {
            throw new MissingOwnershipException("The player " + sourcePlayer + " cannot afford this effect " + this);
        }
    }
}
