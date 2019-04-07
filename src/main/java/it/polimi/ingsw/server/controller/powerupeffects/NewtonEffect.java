package it.polimi.ingsw.server.controller.powerupeffects;

import it.polimi.ingsw.server.model.*;
import it.polimi.ingsw.server.model.battlefield.Direction;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.exceptions.MissingOwnershipException;
import it.polimi.ingsw.server.model.exceptions.TypeMismatchException;
import it.polimi.ingsw.server.model.player.Player;

public class NewtonEffect extends PowerupEffect {


    /**
     * This constructor needs to be passed the context of the game
     *
     * @param match        the Match the player is associated with
     * @param sourcePlayer the player who is using this effect
     * @param powerupTile  the associated powerup type
     * @throws MissingOwnershipException thrown if the player didn't have the correct PowerupTile for this effect
     * @throws TypeMismatchException     thrown if the powerup tile does not correspond to the effect
     */
    public NewtonEffect(Match match, Player sourcePlayer, PowerupTile powerupTile) throws MissingOwnershipException, TypeMismatchException {
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
