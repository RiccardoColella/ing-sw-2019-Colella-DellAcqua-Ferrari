package it.polimi.ingsw.server.controller;

import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.currency.AmmoCube;
import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.match.MatchFactory;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.player.PlayerColor;
import it.polimi.ingsw.server.model.player.PlayerInfo;
import it.polimi.ingsw.server.view.Interviewer;
import it.polimi.ingsw.server.view.exceptions.ViewDisconnectedException;
import it.polimi.ingsw.shared.messages.ClientApi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class PaymentHandlerTest {

    private class MockView implements Interviewer {
        int index;

        private MockView(int index) {
            this.index = index;
        }

        @Override
        public <T> T select(String questionText, Collection<T> options, ClientApi messageName) {
            List<T> optionsList = new ArrayList<>(options);
            int i = 0;
            while (i < index && i < optionsList.size() - 1){ i++; }
            return optionsList.get(i);
        }

        @Override
        public <T> Optional<T> selectOptional(String questionText, Collection<T> options, ClientApi messageName) {
            List<T> optionsList = new ArrayList<>(options);
            int i = 0;
            while (i < index && i < optionsList.size() - 1){ i++; }
            return Optional.of(optionsList.get(i));
        }
    }

    private Match match;
    private List<PlayerInfo> playerInfos = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // Populating a list of players which will join the match
        for (int i = 0; i < 5; i++) {
            playerInfos.add(new PlayerInfo("Player" + i, PlayerColor.values()[i]));
        }
        match = MatchFactory.create(
                playerInfos.stream().map(PlayerInfo::getNickname).collect(Collectors.toList()),
                BoardFactory.Preset.BOARD_1,
                8,
                Match.Mode.STANDARD);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void canAfford() {
        Player player0 = match.getPlayers().get(0);
        List<AmmoCube> ammoCubes = new LinkedList<>();
        for (int i = 0; i < 2; i++) {
            ammoCubes.add(new AmmoCube(CurrencyColor.YELLOW));
        }
        //Some assertion to check initialization...
        assertEquals(3, player0.getAmmoCubes().size());
        assertEquals(0, player0.getPowerups().size());
        assertEquals(2, ammoCubes.size());
        assertFalse(PaymentHandler.canAfford(ammoCubes, player0));
        //Here we grab some Ammo Cubes for some specific tests
        player0.grabAmmoCubes(ammoCubes);
        assertTrue(PaymentHandler.canAfford(ammoCubes, player0));
        ammoCubes.add(new AmmoCube((CurrencyColor.YELLOW)));
        assertTrue(PaymentHandler.canAfford(ammoCubes, player0));
        ammoCubes.add(new AmmoCube(CurrencyColor.BLUE));
        assertTrue(PaymentHandler.canAfford(ammoCubes, player0));

        assertTrue(PaymentHandler.canAfford(player0.getAmmoCubes().size() + player0.getPowerups().size(), player0));
        assertFalse(PaymentHandler.canAfford(player0.getAmmoCubes().size() + player0.getPowerups().size() + 1, player0));
    }

    @Test
    void collectCoins() {
        Interviewer mockView = new MockView(0);
        Player player1 = match.getPlayers().get(1);
        LinkedList<Coin> mockDebt = new LinkedList<>();
        //checking starting configuration
        assertEquals(3, player1.getAmmoCubes().size() + player1.getPowerups().size());

        //Checking collectCoins doesn't have side effects
        PaymentHandler.collectCoins(2, player1, mockView);
        assertEquals(3, player1.getAmmoCubes().size() + player1.getPowerups().size());
        mockDebt.add(new AmmoCube(CurrencyColor.YELLOW));
        PaymentHandler.collectCoins(mockDebt, player1, mockView);
        assertEquals(3, player1.getAmmoCubes().size() + player1.getPowerups().size());

        //Testing result dimensions
        List<Coin> collected = PaymentHandler.collectCoins(3, player1, mockView);
        assertEquals(3, collected.size());
        collected = PaymentHandler.collectCoins(collected, player1, mockView);
        assertEquals(3, collected.size());
        mockDebt = new LinkedList<>();
        mockDebt.add(new AmmoCube(CurrencyColor.RED));
        mockDebt.add(new AmmoCube(CurrencyColor.BLUE));
        collected = PaymentHandler.collectCoins(mockDebt, player1, mockView);
        assertEquals(2, collected.size());

        //Testing result type
        mockDebt.pop();
        collected = PaymentHandler.collectCoins(mockDebt, player1, mockView);
        assertEquals(1, mockDebt.size());
        assertEquals(1, collected.size());
        assertEquals(CurrencyColor.BLUE, mockDebt.get(0).getColor());
        assertEquals(CurrencyColor.BLUE, collected.get(0).getColor());
    }

    @Test
    void pay() {
        Interviewer mockView = new MockView(1);
        Player player2 = match.getPlayers().get(2);
        List<AmmoCube> ammoCubesToBeGrabbed = new LinkedList<>();
        List<Coin> mockDebt = new LinkedList<>();
        //checking starting configuration
        assertEquals(3, player2.getAmmoCubes().size() + player2.getPowerups().size());

        //Testing result dimensions
        PaymentHandler.pay(2, player2, mockView);
        assertEquals(1, player2.getAmmoCubes().size() + player2.getPowerups().size());
        PaymentHandler.pay(1, player2, mockView);
        assertEquals(0, player2.getAmmoCubes().size() + player2.getPowerups().size());
        mockDebt.add(new AmmoCube(CurrencyColor.YELLOW));
        ammoCubesToBeGrabbed.clear();
        ammoCubesToBeGrabbed.add(new AmmoCube(CurrencyColor.YELLOW));
        player2.grabAmmoCubes(ammoCubesToBeGrabbed);
        assertEquals(1, player2.getAmmoCubes().size() + player2.getPowerups().size());
        PaymentHandler.pay(mockDebt, player2, mockView);
        assertEquals(0, player2.getAmmoCubes().size() + player2.getPowerups().size());

        //Testing result dimensions with powerups
        player2.grabPowerup(new PowerupTile(CurrencyColor.YELLOW, "MockPowerup"));
        player2.grabPowerup(new PowerupTile(CurrencyColor.YELLOW, "MockPowerup"));
        PaymentHandler.pay(2, player2, mockView);
        player2.grabPowerup(new PowerupTile(CurrencyColor.YELLOW, "MockPowerup"));
        player2.grabPowerup(new PowerupTile(CurrencyColor.YELLOW, "MockPowerup"));
        mockDebt.clear();
        mockDebt.add(new PowerupTile(CurrencyColor.YELLOW, "MockPowerup"));
        mockDebt.add(new PowerupTile(CurrencyColor.YELLOW, "MockPowerup"));
        PaymentHandler.pay(mockDebt, player2, mockView);
        assertEquals(0, player2.getPowerups().size() + player2.getAmmoCubes().size());

        //Testing result type
        player2.grabPowerup(new PowerupTile(CurrencyColor.YELLOW, "MockPowerup"));
        player2.grabPowerup(new PowerupTile(CurrencyColor.YELLOW, "MockPowerup"));
        player2.grabPowerup(new PowerupTile(CurrencyColor.BLUE, "MockPowerup"));
        mockDebt.clear();
        mockDebt.add(new PowerupTile(CurrencyColor.BLUE, "MockPowerup"));
        mockDebt.add(new PowerupTile(CurrencyColor.YELLOW, "MockPowerup"));
        PaymentHandler.pay(mockDebt, player2, mockView);
        assertEquals(1, player2.getPowerups().size() + player2.getAmmoCubes().size());
        assertEquals(CurrencyColor.YELLOW, player2.getPowerups().get(0).getColor());
        PaymentHandler.pay(1, player2, mockView);
        assertEquals(0, player2.getAmmoCubes().size() + player2.getPowerups().size());

        ammoCubesToBeGrabbed.clear();
        ammoCubesToBeGrabbed.add(new AmmoCube(CurrencyColor.YELLOW));
        mockDebt.clear();
        mockDebt.add(new PowerupTile(CurrencyColor.BLUE, "MockPowerup"));
        player2.grabAmmoCubes(ammoCubesToBeGrabbed);
        assertEquals(1, player2.getPowerups().size() + player2.getAmmoCubes().size());
        assertThrows(ViewDisconnectedException.class, () -> PaymentHandler.pay(mockDebt, player2, mockView));
    }
}