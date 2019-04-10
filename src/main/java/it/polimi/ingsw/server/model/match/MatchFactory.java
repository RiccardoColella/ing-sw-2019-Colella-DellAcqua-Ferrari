package it.polimi.ingsw.server.model.match;

import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.player.PlayerFactory;
import it.polimi.ingsw.server.model.player.PlayerInfo;

import java.util.List;
import java.util.function.BiFunction;

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
     */
    public static Match create(List<PlayerInfo> playersInfo, BoardFactory.Preset preset, int skulls, Match.Mode mode) {
        return create(playersInfo, preset, skulls, mode, PlayerFactory::create);
    }

    /**
     *
     * @param playersInfo the PlayerInfo storing basic info about the players
     * @param preset the board preset that was chosen for the match
     * @param skulls an int representing the number of skulls
     * @param mode the initial match mode
     * @param playerSupplier a bi-function which provides a Player instance given this match and a PlayerInfo object
     * @return an awesome match
     */
    public static Match create(List<PlayerInfo> playersInfo, BoardFactory.Preset preset, int skulls, Match.Mode mode, BiFunction<Match, PlayerInfo, Player> playerSupplier) {
        Match match = new Match(
                playersInfo,
                BoardFactory.create(preset),
                skulls,
                mode,
                playerSupplier
        );
        match.getPlayers().forEach(player -> {
            player.addPlayerDiedListener(match);
            player.addPlayerOverkilledListener(match);
            match.addMatchModeChangedListener(player);
        });
        return match;
    }
}
