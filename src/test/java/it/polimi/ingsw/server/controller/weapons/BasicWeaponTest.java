package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.battlefield.Board;
import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.currency.AmmoCube;
import it.polimi.ingsw.server.model.currency.AmmoCubeFactory;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.match.MatchFactory;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.player.PlayerColor;
import it.polimi.ingsw.server.model.player.PlayerInfo;
import it.polimi.ingsw.server.model.weapons.Weapon;
import it.polimi.ingsw.server.view.Interviewer;
import it.polimi.ingsw.shared.commands.ClientApi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class BasicWeaponTest {

    private Match match;

    private class MockInterviewer implements Interviewer {

        @Override
        public <T> T select(String questionText, Collection<T> options, ClientApi commandName) {
            return options.iterator().next();
        }

        @Override
        public <T> Optional<T> selectOptional(String questionText, Collection<T> options, ClientApi commandName) {
            return Optional.ofNullable(options.iterator().next());
        }

        @Override
        public <T> T select(Collection<T> options) {
            return null;
        }

        @Override
        public <T> Optional<T> selectOptional(Collection<T> options) {
            return Optional.empty();
        }
    }
    @BeforeEach
    void setUp() {
        List<PlayerInfo> playerInfos = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            playerInfos.add(new PlayerInfo("Player" + i, PlayerColor.values()[i]));
        }
        match = MatchFactory.create(playerInfos, BoardFactory.Preset.BOARD_1, 5, Match.Mode.STANDARD);
        List<AmmoCube> fullWallet = new ArrayList<>();
        for (CurrencyColor color : CurrencyColor.values()) {
            for (int i = 0; i < match.getPlayers().get(0).getConstraints().getMaxAmmoCubesOfAColor(); i++) {
                fullWallet.add(AmmoCubeFactory.create(color));
            }
        }
        match.getPlayers().forEach(player -> player.grabAmmoCubes(fullWallet));
    }

    @AfterEach
    void tearDown() {
    }

    /**
     * Testing the weapon Lock Rifle:
     * - Basic effect: deal 2 damage and 1 mark to 1 target you can see
     * - With second lock: deal 1 mark to a different target you can see
     */
    @Test
    void LockRifle() {
        Board board = match.getBoard();
        Player activePlayer = match.getActivePlayer();
        Player t1 = match.getPlayers().get(1);
        Player t2 = match.getPlayers().get(2);
        Block b00 = board.getBlock(0, 0).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b00.addPlayer(activePlayer);
        Block b01 = board.getBlock(0, 1).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b01.addPlayer(t1);
        Block b02 = board.getBlock(0, 2).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b02.addPlayer(t2);
        BasicWeapon lockRifle = WeaponFactory.create(Weapon.Name.LOCK_RIFLE, board);
        lockRifle.shoot(new MockInterviewer(), activePlayer);
        List<Player> targets = lockRifle.getAllTargets();
        assertEquals(1, lockRifle.wasHitBy(lockRifle.basicAttack).size());
        t1 = lockRifle.wasHitBy(lockRifle.basicAttack).iterator().next();
        assertEquals(2, t1.getDamageTokens().size());
        assertEquals(1, t1.getMarks().size());
        Attack powered = ((WeaponWithMultipleEffects) lockRifle).poweredAttacks.get(0);
        t2 = lockRifle.wasHitBy(powered).iterator().next();
        assertEquals(0, t2.getDamageTokens().size());
        assertEquals(1, t2.getMarks().size());
    }

    @Test
    void Electroscythe() {
        Board board = match.getBoard();
        Player activePlayer = match.getActivePlayer();
        Player t1 = match.getPlayers().get(1);
        Player t2 = match.getPlayers().get(2);
        Block b00 = board.getBlock(0, 0).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b00.addPlayer(activePlayer);
        b00.addPlayer(t1);
        b00.addPlayer(t2);
        BasicWeapon electroscythe = WeaponFactory.create(Weapon.Name.ELECTROSCYTHE, board);
        electroscythe.shoot(new MockInterviewer(), activePlayer);
        List<Player> targets = electroscythe.getAllTargets();
        System.out.println(targets.size());
        assertFalse(targets.contains(activePlayer));
    }
}