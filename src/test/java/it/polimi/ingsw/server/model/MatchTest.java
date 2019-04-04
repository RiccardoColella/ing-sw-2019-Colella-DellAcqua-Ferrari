package it.polimi.ingsw.server.model;

import it.polimi.ingsw.server.model.events.PlayerDied;
import it.polimi.ingsw.server.model.exceptions.UnknownEnumException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MatchTest {

    private Match match;
    private List<PlayerInfo> playerInfos;

    @BeforeEach
    void setUp() {
        this.playerInfos = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            this.playerInfos.add(new PlayerInfo("Player" + i, PlayerColor.values()[i]));
        }
        try {
            this.match = new Match(playerInfos, BoardPreset.BOARD_10, 5, MatchMode.STANDARD);
        } catch (UnknownEnumException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void changeTurn() {
        match.changeTurn();
        assert match.getActivePlayer().getNickname().equals(playerInfos.get(1).getNickname());
        match.changeTurn();
        assert match.getActivePlayer().getNickname().equals(playerInfos.get(2).getNickname());
        match.changeTurn();
        assert match.getActivePlayer().getNickname().equals(playerInfos.get(3).getNickname());
        match.changeTurn();
        assert match.getActivePlayer().getNickname().equals(playerInfos.get(4).getNickname());
        match.changeTurn();
        assert match.getActivePlayer().getNickname().equals(playerInfos.get(0).getNickname());
    }

    @Test
    void onPlayerDied() {
        Player victim = match.getPlayers().get(3);
        int oldSkulls = victim.getSkulls();
        int matchOldSkulls = match.getRemainingSkulls();
        match.onPlayerDied(new PlayerDied(victim, match.getActivePlayer(), false));
        assert victim.getSkulls() == oldSkulls + 1;
        assert match.getRemainingSkulls() == matchOldSkulls - 1;
        victim = match.getPlayers().get(4);
        int oldMarks = match.getActivePlayer().getMarks().size();
        match.onPlayerDied(new PlayerDied(victim, match.getActivePlayer(), true));
        assert oldMarks + 1 == match.getActivePlayer().getMarks().size();
    }
}