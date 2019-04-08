package it.polimi.ingsw.server.model.currency;

/**
 * This class represents the ammoCubes, which are the main currency of the game
 */
public class AmmoCube implements Coin {

    /**
     * This property stores the color of the ammo
     */
    private CurrencyColor color;

    /**
     * Class constructor given the color
     * @param color AmmoCube's color
     */
    public AmmoCube(CurrencyColor color) {
        this.color = color;
    }

    /**
     * Check if the AmmoCube's color
     * @param that color to be compared
     * @return true if the color is the same
     */
    @Override
    public boolean equalsTo(Coin that) {
        return that instanceof AmmoCube && this.hasSameValueAs(that);
    }

    /**
     * This method returns the color of the ammo
     *
     * @return the CurrencyColor of the ammo
     */
    @Override
    public CurrencyColor getColor() {
        return this.color;
    }

    /**
     * This method checks whether two coins have the same value
     *
     * @param that the Coin to compare
     * @return true if the two coins have the same value, false otherwise
     */
    @Override
    public boolean hasSameValueAs(Coin that) {
        return this.color == that.getColor();
    }
}
