package it.polimi.ingsw.server.model.currency;

/**
 * This class represents the actual powerup tile, which can be in the Deck or owned by a Player
 */
public class PowerupTile implements Coin {

    /**
     * This property represents the color of the powerup tile
     */
    private final CurrencyColor color;

    /**
     * This property represents the type of the powerup tile
     */
    private final String name;

    /**
     * This constructor creates a powerup tile given its color, name and name
     * @param color a value of CurrencyColor representing the color
     * @param name a value representing the name
     */
    public PowerupTile(CurrencyColor color, String name) {
        this.color = color;
        this.name = name;
    }

    /**
     * Copy constructor
     * @param copy the powerup tile to copy
     */
    public PowerupTile(PowerupTile copy) {
        this(copy.color, copy.name);
    }

    /**
     * This method returns the color of the powerup tile
     *
     * @return the color of the powerup tile
     */
    @Override
    public CurrencyColor getColor() {
        return this.color;
    }

    /**
     * This method checks whether two coins are the same
     *
     * @param that the Coin to compare
     * @return true if the two coins are the same, false otherwise
     */
    @Override
    public boolean equalsTo(Coin that) {
        return
                that instanceof PowerupTile &&
                this.color == that.getColor() &&
                this.getName().equals(((PowerupTile) that).getName());
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

    /**
     * This method returns the name of the powerup tile
     * @return the string representing the name
     */
    public String getName() {
        return this.name;
    }
}
