package it.polimi.ingsw.server.controller.powerupeffects;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.player.Player;

public class TeleporterEffect extends PowerupEffect {
    /**
     * This constructor needs to be passed the context of the game
     *
     * @param match        the Match the player is associated with
     * @param sourcePlayer the player who is using this effect
     * @param powerupTile  the associated powerup type
     */
    public TeleporterEffect(Match match, Player sourcePlayer, PowerupTile powerupTile) {
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
