package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.shared.datatransferobjects.Player;

import java.util.List;

public class MatchResumed extends MatchStarted {


    private final Match.Mode matchMode;

    public MatchResumed(int skulls, BoardFactory.Preset preset, Player self, List<Player> opponents, List<String> weaponTop, List<String> weaponRight, List<String> weaponLeft, Player currentActivePlayer, Match.Mode matchMode) {
        super(skulls, preset, self, opponents, weaponTop, weaponRight, weaponLeft, currentActivePlayer);
        this.matchMode = matchMode;
    }

    public Match.Mode getMatchMode() {
        return matchMode;
    }
}
