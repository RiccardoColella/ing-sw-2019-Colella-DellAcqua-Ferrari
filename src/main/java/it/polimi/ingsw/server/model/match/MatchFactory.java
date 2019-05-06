package it.polimi.ingsw.server.model.match;

import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.player.PlayerColor;
import it.polimi.ingsw.server.model.player.PlayerFactory;
import it.polimi.ingsw.server.model.player.PlayerInfo;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class MatchFactory {


    private MatchFactory() {

    }

    /**
     *
     * @param playerNicknames the unique identifiers of the players
     * @param preset the board preset that was chosen for the match
     * @param skulls an int representing the number of skulls
     * @param mode the initial match mode
     * @return an awesome match
     */
    public static Match create(List<String> playerNicknames, BoardFactory.Preset preset, int skulls, Match.Mode mode) {

        LinkedList<PlayerColor> colors = new LinkedList<>(Arrays.asList(PlayerColor.values()));
        Collections.shuffle(colors);

        return create(
                playerNicknames
                        .stream()
                        .map(nick -> new PlayerInfo(nick, colors.pop()))
                        .collect(Collectors.toList()),
                preset,
                skulls,
                mode,
                PlayerFactory::create
        );
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
            player.addPlayerListener(match);
            player.addPlayerListener(match);
            match.addMatchListener(player);
        });
        return match;
    }
}
