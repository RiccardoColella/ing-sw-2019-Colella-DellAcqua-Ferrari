package it.polimi.ingsw.server.controller.powerupeffects;

import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.exceptions.MissingOwnershipException;
import it.polimi.ingsw.server.model.exceptions.TypeMismatchException;

import java.util.Optional;


/**
 * This class represents the basic PowerupEffects and it's used to collect common properties and functionalities
 */
public abstract class PowerupEffect {

    protected Match match;
    protected Player sourcePlayer;

    /**
     * This constructor needs to be passed the context of the game
     * @param match the Match the player is associated with
     * @param sourcePlayer the player who is using this effect
     * @param powerupTile the associated powerup tile
     * @param powerupType the powerup type associated with the tile
     */
    public PowerupEffect(Match match, Player sourcePlayer, PowerupTile powerupTile, PowerupTile.Type powerupType) {

        this.match = match;
        this.sourcePlayer = sourcePlayer;

        if (powerupTile.getType() != powerupType) {
            throw new TypeMismatchException("The powerup tile " + powerupTile + " cannot activate this effect " + this);
        }

        // Removes the activated powerup from the list in the player and re-inserts it into the deck
        Optional<PowerupTile> playerTile = sourcePlayer
                .getPowerups()
                .stream()
                .filter(x -> x.equalsTo(powerupTile))
                .findAny();

        if (playerTile.isPresent()) {
            sourcePlayer.getPowerups().remove(playerTile.get());
            match.getPowerupDeck().discard(playerTile.get());
        } else {
            throw new MissingOwnershipException("PowerupTile " + powerupTile + " does not belong to player " +  sourcePlayer);
        }
    }
}
