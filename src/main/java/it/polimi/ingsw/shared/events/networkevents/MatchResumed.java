package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.player.PlayerColor;
import it.polimi.ingsw.shared.datatransferobjects.BonusTile;
import it.polimi.ingsw.shared.datatransferobjects.Player;
import it.polimi.ingsw.utils.Tuple;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MatchResumed extends MatchStarted {


    private final Match.Mode matchMode;
    private final List<Tuple<PlayerColor, Boolean>> killshots;
    private final Map<PlayerColor, Point> playerLocations;

    public MatchResumed(
            int skulls,
            BoardFactory.Preset preset,
            Player self,
            List<Player> opponents,
            List<String> weaponTop,
            List<String> weaponRight,
            List<String> weaponLeft,
            Player currentActivePlayer,
            Set<BonusTile> turretBonusTiles,
            Match.Mode matchMode,
            List<Tuple<PlayerColor, Boolean>> killshots,
            Map<PlayerColor, Point> playerLocations) {
        super(skulls, preset, self, opponents, weaponTop, weaponRight, weaponLeft, currentActivePlayer, turretBonusTiles);
        this.matchMode = matchMode;
        this.killshots = killshots;
        this.playerLocations = playerLocations;
    }

    public Match.Mode getMatchMode() {
        return matchMode;
    }

    public List<Tuple<PlayerColor, Boolean>> getKillshots() {
        return killshots;
    }

    public Map<PlayerColor, Point> getPlayerLocations() {
        return playerLocations;
    }
}
