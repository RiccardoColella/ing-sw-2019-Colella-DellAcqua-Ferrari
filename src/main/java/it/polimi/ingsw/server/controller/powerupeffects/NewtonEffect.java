package it.polimi.ingsw.server.controller.powerupeffects;

import it.polimi.ingsw.server.model.battlefield.Direction;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.player.Player;

public class NewtonEffect extends PowerupEffect {


    /**
     * This constructor needs to be passed the context of the game
     *
     * @param match        the Match the player is associated with
     * @param sourcePlayer the player who is using this effect
     * @param powerupTile  the associated powerup type
     */
    public NewtonEffect(Match match, Player sourcePlayer, PowerupTile powerupTile) {
        super(match, sourcePlayer, powerupTile, PowerupTile.Type.NEWTON);
    }

    /**
     * Moves the target player by a number of steps in the specified direction
     * @param target the target player
     * @param direction the direction
     * @param steps the amount of blocks in that direction
     */
    public void move(Player target, Direction direction, int steps) {
        for (int i = 0; i < steps; i++) {
            match.getBoard().movePlayer(target, direction);
        }
    }
}
