package it.polimi.ingsw.shared.datatransferobjects;

import it.polimi.ingsw.server.model.currency.CurrencyColor;

import java.awt.*;
import java.io.Serializable;
import java.util.List;

/**
 * Represents a BonusTile for the Client View
 *
 * @author Carlo Dell'Acqua
 */
public class BonusTile implements Serializable {
    /**
     * Bonus tile location
     */
    private final Point location;

    /**
     * Bonus tile coins color
     */
    private final List<CurrencyColor> ammoCubes;

    /**
     * Constructs a BonusTile view model
     *
     * @param ammoCubes the colors of the ammos
     * @param location the location of the tile
     */
    public BonusTile(List<CurrencyColor> ammoCubes, Point location) {
        this.ammoCubes = ammoCubes;
        this.location = location;
    }

    /**
     * Gets the list of currency colors representing the ammo cubes
     *
     * @return a list of currency colors representing the ammo cubes
     */
    public List<CurrencyColor> getAmmoCubes() {
        return ammoCubes;
    }

    /**
     * Gets the location of the tile
     *
     * @return the location of the tile
     */
    public Point getLocation() {
        return location;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return ammoCubes.hashCode() + location.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof BonusTile)) {
            return false;
        } else {
            return this.ammoCubes.equals(((BonusTile) other).ammoCubes) && this.location.equals(((BonusTile) other).location);
        }
    }
}
