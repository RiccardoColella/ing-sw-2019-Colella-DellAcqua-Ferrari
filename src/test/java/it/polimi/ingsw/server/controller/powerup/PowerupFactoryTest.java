package it.polimi.ingsw.server.controller.powerup;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.match.MatchFactory;
import it.polimi.ingsw.server.model.player.PlayerColor;
import it.polimi.ingsw.server.model.player.PlayerInfo;
import it.polimi.ingsw.server.view.Interviewer;
import it.polimi.ingsw.shared.messages.ClientApi;
import it.polimi.ingsw.utils.Range;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class PowerupFactoryTest {

    private Match match;

    private class MockInterviewer implements Interviewer {

        private int index;

        MockInterviewer(int i) {
            index = i;
        }
        @Override
        public <T> T select(String questionText, Collection<T> options, ClientApi commandName) {
            List<T> optionList = new ArrayList<>(options);
            int i = 0;
            while (i < index && i < optionList.size() - 1) {
                i++;
            }
            return optionList.get(i);
        }

        @Override
        public <T> Optional<T> selectOptional(String questionText, Collection<T> options, ClientApi commandName) {
            List<T> optionList = new ArrayList<>(options);
            int i = 0;
            while (i < index && i < optionList.size() - 1) {
                i++;
            }
            return Optional.of(optionList.get(i));
        }
    }

    @BeforeEach
    void setUp() {
        match = MatchFactory.create(
                IntStream
                    .range(0, 5)
                    .boxed()
                    .map(i -> "Player" + i)
                    .collect(Collectors.toList()),
                BoardFactory.Preset.BOARD_1,
                5,
                Match.Mode.STANDARD
        );
        this.match.getPlayers().forEach(p -> this.match.getBoard().getSpawnpoint(CurrencyColor.BLUE).addPlayer(p));
    }

    @Test
    void getAll() {

        Interviewer interviewer = new MockInterviewer(0);
        Block oldBlock;
        for (Powerup powerup : PowerupFactory.getAll()) {
            switch (powerup.getName()) {
                case "Newton":
                    oldBlock = match.getPlayers().get(0).getBlock();
                    powerup.activate(match.getPlayers().get(0), match.getPlayers().get(0), interviewer);
                    assertNotEquals(oldBlock, match.getPlayers().get(0).getBlock(), "Newton didn't move");
                    assertTrue(match.getBoard().getReachableBlocks(oldBlock, new Range(1, 2)).contains(match.getPlayers().get(0).getBlock()), "Newton didn't move to a reachable block");
                    break;
                case "Tagback Grenade":
                    int oldMarkCount = match.getPlayers().get(1).getMarks().size();
                    powerup.activate(match.getPlayers().get(0), match.getPlayers().get(1), interviewer);
                    assertEquals(oldMarkCount + 1, match.getPlayers().get(1).getMarks().size(), "No mark added");
                    assertEquals(
                            match.getPlayers().get(0),
                            match.getPlayers().get(1)
                                    .getMarks()
                                    .get(match.getPlayers().get(1).getMarks().size() - 1)
                                    .getAttacker(),
                            "Given mark does not belong to the expected player"
                    );
                    break;
                case "Teleporter":
                    powerup.activate(match.getPlayers().get(0), match.getPlayers().get(0), interviewer);
                    assertTrue(match.getBoard().getBlocks().contains(match.getPlayers().get(0).getBlock()));
                    break;
                case "Targeting Scope":
                    int oldDamageCount = match.getPlayers().get(1).getMarks().size();
                    powerup.activate(match.getPlayers().get(0), match.getPlayers().get(1), interviewer);
                    assertEquals(oldDamageCount + 1, match.getPlayers().get(1).getDamageTokens().size(), "No damage added");
                    assertEquals(
                            match.getPlayers().get(0),
                            match.getPlayers().get(1)
                                    .getDamageTokens()
                                    .get(match.getPlayers().get(1).getDamageTokens().size() - 1)
                                    .getAttacker(),
                            "Given damage does not belong to the expected player"
                    );
                    break;
            }
        }
    }
}