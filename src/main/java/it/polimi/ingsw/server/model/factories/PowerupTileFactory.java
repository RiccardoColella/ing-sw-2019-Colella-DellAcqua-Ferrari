package it.polimi.ingsw.server.model.factories;

import it.polimi.ingsw.server.model.collections.Deck;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.currency.PowerupTile;

import java.util.LinkedList;
import java.util.List;

/**
 * This class creates all the powerup tiles needed in the game
 */
public final class PowerupTileFactory {

    /**
     * Private empty constructor because this class should not have instances
     */
    private PowerupTileFactory() {

    }

    /**
     * This method creates a new powerup tile given its type and its color
     * @param type a value of the Name enum
     * @param color a value of the CurrencyColor enum
     * @return a new powerup tile with the given characteristics
     */
    public static PowerupTile create(PowerupTile.Type type, CurrencyColor color) {
        return new PowerupTile(color, type);
    }

    public static Deck<PowerupTile> createDeck() {
        List<PowerupTile> powerupCards = new LinkedList<>();
        for (PowerupTile.Type type : PowerupTile.Type.values()) {
            for (CurrencyColor color : CurrencyColor.values()) {
                for (int i = 0; i < 2; i++) {
                    powerupCards.add(PowerupTileFactory.create(type, color));
                }
            }
        }

        return new Deck<>(powerupCards, true);
    }
}
