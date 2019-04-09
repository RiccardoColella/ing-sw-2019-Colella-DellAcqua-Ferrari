package it.polimi.ingsw.server.controller.powerupeffects;

import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.player.Player;

import java.util.Collections;

public class TargetingScopeEffect extends PowerupEffect {

    /**
     * This constructor needs to be passed the context of the game
     *
     * @param match        the Match the player is associated with
     * @param sourcePlayer the player who is using this effect
     * @param powerupTile  the associated powerup type
     */
    public TargetingScopeEffect(Match match, Player sourcePlayer, PowerupTile powerupTile) {
        super(match, sourcePlayer, powerupTile, PowerupTile.Type.TARGETING_SCOPE);
    }

    /**
     * Activates the effect making the sourcePlayer pay for it
     * @param coin the cost of this effect the player must pay
     */
    public void activate(Coin coin) {


        sourcePlayer.pay(Collections.singletonList(coin));

    }
}
