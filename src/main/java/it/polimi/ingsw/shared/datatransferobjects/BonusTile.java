package it.polimi.ingsw.shared.datatransferobjects;

import it.polimi.ingsw.server.model.currency.CurrencyColor;

import java.awt.*;
import java.util.List;

/**
 * Represents a BonusTile for the Client View
 *
 * @author Carlo Dell'Acqua
 */
public class BonusTile {

    /**
     * Bonus tile location
     */
    private Point location;

    /**
     * Bonus tile coins color
     */
    private final List<CurrencyColor> ammoCubes;

    /**
     * Constructs a BonusTile view model
     *
     * @param ammoCubes the currency color of the powerup
     */
    public BonusTile(List<CurrencyColor> ammoCubes, Point location) {
        this.ammoCubes = ammoCubes;
        this.location = location;
    }

    public List<CurrencyColor> getAmmoCubes() {
        return ammoCubes;
    }

    @Override
    public int hashCode() {
        return ammoCubes.hashCode() + location.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof BonusTile)) {
            return false;
        } else {
            return this.ammoCubes.equals(((BonusTile) other).ammoCubes) && this.location.equals(((BonusTile) other).location);
        }
    }
}
