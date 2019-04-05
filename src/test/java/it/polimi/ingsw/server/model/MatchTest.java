package it.polimi.ingsw.server.model;

import it.polimi.ingsw.server.model.events.PlayerDied;
import it.polimi.ingsw.server.model.factories.BoardFactory;
import it.polimi.ingsw.server.model.factories.MatchFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class MatchTest {

    private Match match;
    private List<PlayerInfo> playerInfos;

    @BeforeEach
    void setUp() {
        this.playerInfos = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            this.playerInfos.add(new PlayerInfo("Player" + i, PlayerColor.values()[i]));
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
    void onPlayerDied() {
        Player attacker = match.getPlayers().get(3);
        int expectedSkulls = attacker.getSkulls();
        int matchExpectedSkulls = match.getRemainingSkulls();
        match.onPlayerDied(new PlayerDied(match.getActivePlayer(), attacker, false));
        expectedSkulls++;
        matchExpectedSkulls--;
        assertEquals(expectedSkulls, match.getActivePlayer().getSkulls(), "Player did not receive its skull");
        assertEquals(matchExpectedSkulls, match.getRemainingSkulls(), "Match didn't decrease the skull number");
        attacker = match.getPlayers().get(4);
        int expectedMarks = attacker.getMarks().size();

        match.onPlayerDied(new PlayerDied(match.getActivePlayer(), attacker, true));
        expectedMarks++;
        matchExpectedSkulls--;
        assertEquals(expectedMarks, attacker.getMarks().size(), "Player did not receive its mark");

        match.onPlayerDied(new PlayerDied(match.getActivePlayer(), attacker, true));
        expectedMarks++;
        matchExpectedSkulls--;
        assertEquals(expectedMarks, attacker.getMarks().size(), "Player did not receive its mark");

        match.onPlayerDied(new PlayerDied(match.getActivePlayer(), attacker, true));
        expectedMarks++;
        matchExpectedSkulls--;
        assertEquals(expectedMarks, attacker.getMarks().size(), "Player did not receive its mark");

        match.onPlayerDied(new PlayerDied(match.getActivePlayer(), attacker, true));
        // expectedMarks++; // Reached the maximum number of marks from the same player, the expected number should stop at 3
        matchExpectedSkulls--;
        assertEquals(expectedMarks, attacker.getMarks().size(), "Player did not receive its mark");

        assertEquals(matchExpectedSkulls, match.getRemainingSkulls(), "Remaining skulls should be 0");
        assertEquals(Match.Mode.FINAL_FRENZY, match.getMode(), "0 Skulls should imply FINAL_FRENZY mode");

        match.changeTurn();

        match.onPlayerDied(new PlayerDied(match.getActivePlayer(), attacker, true));
        // Each player can give up to 3 marks, so the expected number should increase due to the fact that the active player has changed
        expectedMarks++;
        assertEquals(expectedMarks, attacker.getMarks().size(), "Player did not receive its mark");
    }
}