package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.server.model.player.PlayerColor;
import it.polimi.ingsw.shared.datatransferobjects.Player;

import java.util.List;

public class PlayerHealthChanged extends PlayerEvent {


    public PlayerHealthChanged(Player player) {
        super(player);
    }

    public List<PlayerColor> getDamages() {
        return getPlayer().getDamage();
    }

    public List<PlayerColor> getMarks() {
        return getPlayer().getMarks();
    }

    public int getSkulls() {
        return getPlayer().getSkulls();
    }
}
