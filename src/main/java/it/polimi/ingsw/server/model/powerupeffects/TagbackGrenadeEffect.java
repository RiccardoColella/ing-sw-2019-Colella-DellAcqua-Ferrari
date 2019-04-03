package it.polimi.ingsw.server.model.powerupeffects;

import it.polimi.ingsw.server.model.Match;
import it.polimi.ingsw.server.model.Player;
import it.polimi.ingsw.server.model.PowerupTile;
import it.polimi.ingsw.server.model.PowerupType;
import it.polimi.ingsw.server.model.exceptions.MissingOwnershipException;
import it.polimi.ingsw.server.model.exceptions.TypeMismatchException;

public class TagbackGrenadeEffect extends PowerupEffect {

    /**
     * This constructor needs to be passed the context of the game
     *
     * @param match        the Match the player is associated with
     * @param sourcePlayer the player who is using this effect
     * @param powerupTile  the associated powerup type
     * @throws MissingOwnershipException thrown if the player didn't have the correct PowerupTile for this effect
     * @throws TypeMismatchException     thrown if the powerup tile does not correspond to the effect
     */
    public TagbackGrenadeEffect(Match match, Player sourcePlayer, PowerupTile powerupTile) throws MissingOwnershipException, TypeMismatchException {
        super(match, sourcePlayer, powerupTile, PowerupType.TAGBACK_GRENADE);
    }

    /**
     * Activates the powerup assigning a mark DamageToken to the current active player
     */
    public void activate() {

    }
}
