package it.polimi.ingsw.server.model.currency;

import it.polimi.ingsw.server.model.currency.AmmoCube;
import it.polimi.ingsw.server.model.currency.CurrencyColor;

/**
 * Factory class to create ammoCubes for the match
 */
public final class AmmoCubeFactory {

    /**
     * Private empty constructor because this class should not have instances
     */
    private AmmoCubeFactory() {

    }

    /**
     * Creates a new AmmoCube, given the color
     * @param color color of the AmmoCube to be created
     * @return the new AmmoCube
     */
    public static AmmoCube create(CurrencyColor color) {
        return new AmmoCube(color);
    }
}