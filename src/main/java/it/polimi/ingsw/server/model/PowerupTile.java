package it.polimi.ingsw.server.model;

/**
 * This class represents the actual powerup tile, which can be in the Deck or owned by a Player
 */
public class PowerupTile implements Coin{

    /**
     * This property represents the color of the powerup tile
     */
    private final CoinColor color;

    /**
     * This property represents the name of the powerup tile
     */
    private final String name;

    /**
     * This property represents the type of the powerup tile
     */
    private final PowerupType type;

    /**
     * This constructor creates a powerup tile given its color, name and type
     * @param color a value of CoinColor representing the color
     * @param name a string representing the name
     * @param type a value of PowerupType representing the type
     */
    public PowerupTile(CoinColor color, String name, PowerupType type) {
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
    public CoinColor getColor() {
        return this.color;
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
    public PowerupType getType() {
        return this.type;
    }
}
