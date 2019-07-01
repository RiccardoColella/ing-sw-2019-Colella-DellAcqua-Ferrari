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

/**
 * Network event carrying information about a match being resumed
 *
 * @author Carlo Dell'Acqua
 */
public class MatchResumed extends MatchStarted {

    /**
     * The match mode
     */
    private final Match.Mode matchMode;
    /**
     * The current killshot track status
     */
    private final List<Tuple<PlayerColor, Boolean>> killshots;
    /**
     * The player positions on the board
     */
    private final Map<PlayerColor, Point> playerLocations;

    /**
     * Constructs the match resumed event
     *
     * @param skulls the number of skulls
     * @param preset the board preset
     * @param self the "self" relative to the player which will receive the event
     * @param opponents the opponents of the "self"
     * @param weaponTop the weapon on the spawnpoint located on the top of the board
     * @param weaponRight the weapon on the spawnpoint located on the right of the board
     * @param weaponLeft the weapon on the spawnpoint located on the left of the board
     * @param currentActivePlayer the active player for the first turn
     * @param turretBonusTiles the bonus tiles on the various turrets
     * @param matchMode the current match mode
     * @param killshots the killshots track status
     * @param playerLocations the players' positions on the board
     */
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

    /**
     * @return the current match mode
     */
    public Match.Mode getMatchMode() {
        return matchMode;
    }

    /**
     * @return the killshot track
     */
    public List<Tuple<PlayerColor, Boolean>> getKillshots() {
        return killshots;
    }

    /**
     * @return the players' locations
     */
    public Map<PlayerColor, Point> getPlayerLocations() {
        return playerLocations;
    }
}
