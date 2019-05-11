package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.server.model.player.PlayerColor;
import it.polimi.ingsw.shared.viewmodels.Player;
import it.polimi.ingsw.shared.viewmodels.Wallet;

import java.util.List;

public class PlayerHealthChanged extends PlayerEvent {

    private final List<PlayerColor> damages;
    private final List<PlayerColor> marks;
    private final int skulls;

    public PlayerHealthChanged(Player player, List<PlayerColor> damages, List<PlayerColor> marks, int skulls) {
        super(player);
        this.damages = damages;
        this.marks = marks;
        this.skulls = skulls;
    }

    public List<PlayerColor> getDamages() {
        return damages;
    }

    public List<PlayerColor> getMarks() {
        return marks;
    }

    public int getSkulls() {
        return skulls;
    }
}
