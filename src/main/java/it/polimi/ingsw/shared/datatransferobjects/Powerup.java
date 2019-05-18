package it.polimi.ingsw.shared.datatransferobjects;

import it.polimi.ingsw.server.model.currency.CurrencyColor;

/**
 * Represents a Powerup for the Client View
 *
 * @author Carlo Dell'Acqua
 */
public class Powerup {

    /**
     * Powerup name
     */
    private String name;

    /**
     * Powerup color
     */
    private CurrencyColor color;

    /**
     * Constructs a Powerup view model
     *
     * @param name the name of the powerup
     * @param color the currency color of the powerup
     */
    public Powerup(String name, CurrencyColor color) {
        this.name = name;
        this.color = color;
    }

    /**
     * @return the name of the powerup
     */
    public String getName() {
        return name;
    }

    /**
     * @return the currency color of the powerup
     */
    public CurrencyColor getColor() {
        return color;
    }


    @Override
    public int hashCode() {
        return (name + color.toString()).hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Powerup)) {
            return false;
        } else {
            return this.name.equals(((Powerup) other).name) && this.color.equals(((Powerup) other).color);
        }
    }
}
