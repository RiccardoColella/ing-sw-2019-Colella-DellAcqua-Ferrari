package it.polimi.ingsw.server.model.currency;

/**
 * This class represents the actual powerup tile, which can be in the Deck or owned by a Player
 */
public class PowerupTile implements Coin {

    /**
     * This enum differentiates the 4 possible powerup options
     */
    public enum Type {
        NEWTON,
        TAGBACK_GRENADE,
        TARGETING_SCOPE,
        TELEPORTER
    }


    /**
     * This property represents the color of the powerup tile
     */
    private final CurrencyColor color;

    /**
     * This property represents the name of the powerup tile
     */
    private final String name;

    /**
     * This property represents the type of the powerup tile
     */
    private final Type type;

    /**
     * This constructor creates a powerup tile given its color, name and type
     * @param color a value of CurrencyColor representing the color
     * @param name a string representing the name
     * @param type a value of Type representing the type
     */
    public PowerupTile(CurrencyColor color, String name, Type type) {
        this.color = color;
        this.name = name;
        this.type = type;
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
                this.getType() == ((PowerupTile) that).getType();
    }

    /**
     * This method checks whether two coins have the same value
     *
     * @param coin the Coin to compare
     * @return true if the two coins have the same value, false otherwise
     */
    @Override
    public boolean hasSameValueAs(Coin coin) {
        return false;
    }

    /**
     * This method returns the name of the powerup tile
     * @return a string representing the name of the powerup
     */
    public String getName() {
        return this.name;
    }

    /**
     * This method returns the type of the powerup tile
     * @return the enum representing the type
     */
    public Type getType() {
        return this.type;
    }
}
