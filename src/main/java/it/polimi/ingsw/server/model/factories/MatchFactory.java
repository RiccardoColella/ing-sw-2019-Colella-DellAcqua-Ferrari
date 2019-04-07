package it.polimi.ingsw.server.model.factories;

import it.polimi.ingsw.server.model.Match;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.player.PlayerInfo;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

public class MatchFactory {


    private MatchFactory() {

    }

    /**
     *
     * @param playersInfo the PlayerInfo storing basic info about the players
     * @param preset the board preset that was chosen for the match
     * @param skulls an int representing the number of skulls
     * @param mode the initial match mode
     * @return an awesome match
     * @throws FileNotFoundException thrown if the needed configuration files are not found
     */
    public static Match create(List<PlayerInfo> playersInfo, BoardFactory.Preset preset, int skulls, Match.Mode mode) throws FileNotFoundException {
        Match match = new Match(
                playersInfo.stream().map(Player::new).collect(Collectors.toList()),
                BoardFactory.create(preset),
                skulls,
                mode
        );
        match.getPlayers().forEach(player -> {
            player.addPlayerDiedListener(match);
            player.addPlayerOverkilledListener(match);
            player.setMatch(match);
            match.addMatchModeChangedListener(player);
        });
        return match;
    }
}
