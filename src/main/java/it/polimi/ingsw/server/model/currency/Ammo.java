package it.polimi.ingsw.server.model.currency;

/**
 * This class represents the ammos, which are the main currency of the game
 */
public class Ammo implements Coin {

    /**
     * This property stores the color of the ammo
     */
    private CurrencyColor color;

    /**
     * Class constructor given the color
     * @param color Ammo's color
     */
    public Ammo(CurrencyColor color) {
        this.color = color;
    }

    /**
     * Check if the Ammo's color
     * @param that color to be compared
     * @return true if the color is the same
     */
    @Override
    public boolean equalsTo(Coin that) {
        return that instanceof Ammo && this.hasSameValueAs(that);
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
