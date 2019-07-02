package it.polimi.ingsw.shared.datatransferobjects;

import it.polimi.ingsw.server.model.player.PlayerColor;

import java.io.Serializable;
import java.util.List;

/**
 * This class represents the data that describes a player
 *
 * @author Carlo Dell'Acqua
 */
public class Player implements Serializable {

    /**
     * The nickname
     */
    private final String nickname;
    /**
     * The character color
     */
    private final PlayerColor color;
    /**
     * The wallet
     */
    private final Wallet wallet;
    /**
     * His health
     */
    private final PlayerHealth health;
    /**
     * Whether or not his tile is flipped
     */
    private boolean isTileFlipped;
    /**
     * Whether or not his board is flipped
     */
    private boolean isBoardFlipped;

    /**
     * Constructs a Player object
     *
     * @param nickname his nickname
     * @param color his color
     * @param wallet his wallet
     * @param health his health
     * @param isTileFlipped whether or not his tile is flipped
     * @param isBoardFlipped whether or not his board is flipped
     */
    public Player(String nickname, PlayerColor color, Wallet wallet, PlayerHealth health, boolean isTileFlipped, boolean isBoardFlipped) {
        this.nickname = nickname;
        this.color = color;
        this.wallet = wallet;
        this.health = health;
        this.isTileFlipped = isTileFlipped;
        this.isBoardFlipped = isBoardFlipped;
    }

    /**
     * @return his nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * @return his color
     */
    public PlayerColor getColor() {
        return color;
    }

    /**
     * @return his wallet
     */
    public Wallet getWallet() {
        return wallet;
    }

    /**
     * @return the assigned skulls
     */
    public int getSkulls() {
        return health.getSkulls();
    }

    /**
     * @return his damage tokens
     */
    public List<PlayerColor> getDamage() {
        return health.getDamages();
    }

    /**
     * @return his marks
     */
    public List<PlayerColor> getMarks() {
        return health.getMarks();
    }

    /**
     * @return his health
     */
    public PlayerHealth getHealth() {
        return health;
    }

    /**
     * @return whether or not his tile is flipped
     */
    public boolean isTileFlipped() {
        return isTileFlipped;
    }

    /**
     * @return whether or not his board is flipped
     */
    public boolean isBoardFlipped() {
        return isBoardFlipped;
    }

    /**
     * Overrides the default hashCode method
     *
     * @return the hash code of this object
     */
    @Override
    public int hashCode() {
        return nickname.hashCode();
    }

    /**
     * Overrides the default equals method
     *
     * @return true if this object is equal to the one passed as a parameter
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Player)) {
            return false;
        } else {
            return this.nickname.equals(((Player) other).nickname);
        }
    }
}
