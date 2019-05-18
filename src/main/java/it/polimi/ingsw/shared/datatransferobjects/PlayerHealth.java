package it.polimi.ingsw.shared.datatransferobjects;

import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.player.PlayerColor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlayerHealth {
    private int skulls;
    private List<PlayerColor> damages;
    private List<PlayerColor> marks;

    public PlayerHealth(int skulls, List<PlayerColor> damages, List<PlayerColor> marks) {
        this.skulls = skulls;
        this.damages = damages;
        this.marks = marks;
    }

    public int getSkulls() {
        return skulls;
    }

    public List<PlayerColor> getDamages() {
        return damages;
    }

    public List<PlayerColor> getMarks() {
        return marks;
    }

    @Override
    public int hashCode() {
        return Stream.concat(marks.stream().map(x -> (Object)x), damages.stream().map(x -> (Object)x))
            .collect(Collectors.toList())
            .hashCode() + Integer.hashCode(skulls);
    }

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
