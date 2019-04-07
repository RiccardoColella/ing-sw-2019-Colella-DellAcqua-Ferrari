package it.polimi.ingsw.server.model;

import it.polimi.ingsw.server.model.factories.BoardFactory;
import it.polimi.ingsw.server.model.factories.MatchFactory;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.player.PlayerColor;
import it.polimi.ingsw.server.model.player.PlayerInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class MatchTest {

    private Match match;

    @BeforeEach
    void setUp() throws FileNotFoundException {
        List<PlayerInfo> playerInfos = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            playerInfos.add(new PlayerInfo("Player" + i, PlayerColor.values()[i]));
        }
        this.match = MatchFactory.create(playerInfos, BoardFactory.Preset.BOARD_10, 5, Match.Mode.STANDARD);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void changeTurn() {

        for (int i = 0; i < match.getPlayers().size() + 1; i++) {
            assertSame(match.getActivePlayer(), match.getPlayers().get(i % match.getPlayers().size()), "Active player mismatch");
            match.changeTurn();
        }
    }


    @Test
    void endTurn() {
        match.getPlayers().forEach(player -> player
                .addDamageTokens(IntStream.range(0, 10)
                        .boxed()
                        .map(x -> new DamageToken(match.getPlayers().get(1)))
                        .collect(Collectors.toList()))
        );

        match.getPlayers().forEach(player -> player.addDamageToken(new DamageToken(match.getPlayers().get(1))));
        List<Player> deadPlayers = match.endTurn();
        assertEquals(match.getPlayers().size(), deadPlayers.size(), "endTurn didn't provide the correct amount of dead players");
        deadPlayers.forEach(player -> player.bringBackToLife());


        match.getPlayers().get(1).addDamageTokens(
                IntStream.range(0, 10)
                        .boxed()
                        .map(x -> new DamageToken(match.getPlayers().get(1)))
                        .collect(Collectors.toList())
        );
        match.getPlayers().get(1).addDamageToken(new DamageToken(match.getPlayers().get(1)));
        deadPlayers = match.endTurn();
        assertEquals(1, deadPlayers.size(), "endTurn didn't provide the correct amount of dead players");
        deadPlayers.forEach(player -> player.bringBackToLife());
    }

    @Test
    void onPlayerDied() {
        match.getPlayers().forEach(player -> player
                .addDamageTokens(IntStream.range(0, 10)
                .boxed()
                .map(x -> new DamageToken(match.getPlayers().get(1)))
                .collect(Collectors.toList()))
        );

        int matchSkulls = match.getRemainingSkulls();
        int victimSkulls = match.getPlayers().get(1).getSkulls();
        match.getPlayers().get(1).addDamageToken(new DamageToken(match.getActivePlayer()));
        match.endTurn();
        match.getPlayers().get(1).bringBackToLife();
        matchSkulls--;
        victimSkulls++;
        assertEquals(matchSkulls, match.getRemainingSkulls(), "Skull not subtracted from the match");
        assertEquals(victimSkulls, match.getPlayers().get(1).getSkulls(), "Skull not added to the victim");
        match.changeTurn();

        int killerMarks = match.getActivePlayer().getMarks().size();
        victimSkulls = match.getPlayers().get(0).getSkulls();
        match.getPlayers().get(0).addDamageTokens(Arrays.asList(new DamageToken(match.getActivePlayer()), new DamageToken(match.getActivePlayer())));
        match.endTurn();
        match.getPlayers().get(0).bringBackToLife();
        matchSkulls--;
        victimSkulls++;
        killerMarks++;
        assertEquals(matchSkulls, match.getRemainingSkulls(), "Skull not subtracted from the match");
        assertEquals(victimSkulls, match.getPlayers().get(0).getSkulls(), "Skull not added to the victim");
        assertEquals(killerMarks, match.getActivePlayer().getMarks().size(), "No mark assigned to the player who did an overkill");
        match.changeTurn();
    }
}