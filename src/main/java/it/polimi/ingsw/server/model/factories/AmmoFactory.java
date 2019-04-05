package it.polimi.ingsw.server.model.factories;

import it.polimi.ingsw.server.model.currency.Ammo;
import it.polimi.ingsw.server.model.currency.CurrencyColor;

/**
 * Factory class to create ammos for the match
 */
public final class AmmoFactory {

    /**
     * Private empty constructor because this class should not have instances
     */
    private AmmoFactory() {

    }

    /**
     * Creates a new Ammo, given the color
     * @param color color of the Ammo to be created
     * @return the new Ammo
     */
    public static Ammo create(CurrencyColor color) {
        return new Ammo(color);
    }
}