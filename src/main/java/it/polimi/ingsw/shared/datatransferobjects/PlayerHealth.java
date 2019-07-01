package it.polimi.ingsw.shared.datatransferobjects;

import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.player.PlayerColor;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class describing the player health
 *
 * @author Carlo Dell'Acqua
 */
public class PlayerHealth implements Serializable {
    /**
     * The number of skulls assigned to the player
     */
    private int skulls;
    /**
     * The damage tokens assigned to the player
     */
    private List<PlayerColor> damages;
    /**
     * The marks assigned to the player
     */
    private List<PlayerColor> marks;

    /**
     * Constructs a player health object
     *
     * @param skulls the number of skulls assigned to the player
     * @param damages the damage tokens assigned to the player
     * @param marks the marks assigned to the player
     */
    public PlayerHealth(int skulls, List<PlayerColor> damages, List<PlayerColor> marks) {
        this.skulls = skulls;
        this.damages = damages;
        this.marks = marks;
    }

    /**
     * @return the number of skulls assigned to the player
     */
    public int getSkulls() {
        return skulls;
    }

    /**
     * @return the damage tokens assigned to the player
     */
    public List<PlayerColor> getDamages() {
        return damages;
    }

    /**
     * @return the marks assigned to the player
     */
    public List<PlayerColor> getMarks() {
        return marks;
    }

    /**
     * Overrides the default hashCode method
     *
     * @return the hash code of this object
     */
    @Override
    public int hashCode() {
        return Stream.concat(marks.stream().map(x -> (Object)x), damages.stream().map(x -> (Object)x))
            .collect(Collectors.toList())
            .hashCode() + Integer.hashCode(skulls);
    }

    /**
     * Overrides the default equals method
     *
     * @return true if this object is equal to the one passed as a parameter
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PlayerHealth)) {
            return false;
        } else {
            return skulls == ((PlayerHealth) other).skulls
                    && marks.equals(((PlayerHealth) other).marks)
                    && damages.equals(((PlayerHealth) other).damages);
        }
    }
}
