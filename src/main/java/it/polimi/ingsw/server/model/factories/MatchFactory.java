package it.polimi.ingsw.server.model.factories;

import it.polimi.ingsw.server.model.Match;
import it.polimi.ingsw.server.model.Player;
import it.polimi.ingsw.server.model.PlayerInfo;

import java.util.List;
import java.util.stream.Collectors;

public class MatchFactory {
    /**
     *
     * @param playersInfo the PlayerInfo storing basic info about the players
     * @param preset the board preset that was chosen for the match
     * @param skulls an int representing the number of skulls
     * @param mode the initial match mode
     * @return an awesome match
     */
    public static Match create(List<PlayerInfo> playersInfo, BoardFactory.Preset preset, int skulls, Match.Mode mode) {
        Match match = new Match(
                playersInfo.stream().map(Player::new).collect(Collectors.toList()),
                BoardFactory.create(preset),
                skulls,
                mode
        );
        match.getPlayers().forEach(player -> {
            player.addPlayerDiedListener(match);
            player.setMatch(match);
            match.addMatchModeChangedListener(player);
        });
        return match;
    }
}
