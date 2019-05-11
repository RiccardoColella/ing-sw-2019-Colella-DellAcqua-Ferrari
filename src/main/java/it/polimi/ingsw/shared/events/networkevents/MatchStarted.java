package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.shared.viewmodels.Player;

import java.util.List;

public class MatchStarted extends NetworkEvent {


    private final BoardFactory.Preset preset;
    private final List<Player> opponents;
    private final Player self;

    public MatchStarted(BoardFactory.Preset preset, Player self, List<Player> opponents) {
        this.preset = preset;
        this.opponents = opponents;
        this.self = self;
    }

    public BoardFactory.Preset getPreset() {
        return preset;
    }

    public List<Player> getOpponents() {
        return opponents;
    }

    public Player getSelf() {
        return self;
    }
}
