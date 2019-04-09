package it.polimi.ingsw.server.controller.powerupeffects;

import it.polimi.ingsw.server.model.player.DamageToken;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.currency.PowerupTile;

public class TagbackGrenadeEffect extends PowerupEffect {

    /**
     * This constructor needs to be passed the context of the game
     *
     * @param match        the Match the player is associated with
     * @param sourcePlayer the player who is using this effect
     * @param powerupTile  the associated powerup type
     */
    public TagbackGrenadeEffect(Match match, Player sourcePlayer, PowerupTile powerupTile) {
        super(match, sourcePlayer, powerupTile, PowerupTile.Type.TAGBACK_GRENADE);
    }

    /**
     * Activates the powerup assigning a mark DamageToken to the current active player
     */
    public void activate() {
        match.getActivePlayer().addMark(new DamageToken(sourcePlayer));
    }
}
