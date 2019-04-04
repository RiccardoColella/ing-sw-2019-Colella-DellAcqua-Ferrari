package it.polimi.ingsw.server.model;

/**
 * This interface schematizes the currency used in the game, which is distinguished by its color
 */
public interface Coin {

    /**
     * This method returns the color of the coin
     * @return the color of the Coin
     */
    CoinColor getColor();

    /**
     * This method checks whether two coins are the same
     * @param that the Coin to compare
     * @return true if the two coins are the same, false otherwise
     */
    boolean equalsTo(Coin that);

    /**
     * This method checks whether two coins have the same value
     * @param that the Coin to compare
     * @return true if the two coins have the same value, false otherwise
     */
    boolean hasSameValueAs(Coin that);
}
