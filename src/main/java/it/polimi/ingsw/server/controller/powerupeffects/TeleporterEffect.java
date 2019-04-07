package it.polimi.ingsw.server.controller.powerupeffects;

import it.polimi.ingsw.server.model.*;
import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.exceptions.MissingOwnershipException;
import it.polimi.ingsw.server.model.exceptions.TypeMismatchException;
import it.polimi.ingsw.server.model.player.Player;

public class TeleporterEffect extends PowerupEffect {
    /**
     * This constructor needs to be passed the context of the game
     *
     * @param match        the Match the player is associated with
     * @param sourcePlayer the player who is using this effect
     * @param powerupTile  the associated powerup type
     * @throws MissingOwnershipException thrown if the player didn't have the correct PowerupTile for this effect
     * @throws TypeMismatchException     thrown if the powerup tile does not correspond to the effect
     */
    public TeleporterEffect(Match match, Player sourcePlayer, PowerupTile powerupTile) throws MissingOwnershipException, TypeMismatchException {
        super(match, sourcePlayer, powerupTile, PowerupTile.Type.TELEPORTER);
    }

    /**
     * Teleport the sourcePlayer to another block in the board
     * @param destination the destination block in the board to teleport the player to
     */
    public void teleport(Block destination) {
        this.match.getBoard().teleportPlayer(sourcePlayer, destination);
    }
}
