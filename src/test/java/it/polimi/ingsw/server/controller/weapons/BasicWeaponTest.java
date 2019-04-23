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
    void lockRifle() {

        //adding players to the board

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

        //in this configuration, both b01 e b02 are visible from b00, so the active player can see both t1 and t2
        //one target will be hit by the basic attack, the other by the powered attack

        BasicWeapon lockRifle = WeaponFactory.create(Weapon.Name.LOCK_RIFLE, board);
        lockRifle.shoot(new MockInterviewer(0), activePlayer);
        assertEquals(1, lockRifle.wasHitBy(lockRifle.basicAttack).size());
        t1 = lockRifle.wasHitBy(lockRifle.basicAttack).iterator().next();
        assertEquals(2, t1.getDamageTokens().size());
        assertEquals(1, t1.getMarks().size());
        Attack powered = ((WeaponWithMultipleEffects) lockRifle).poweredAttacks.get(0);
        t2 = lockRifle.wasHitBy(powered).iterator().next();
        assertEquals(0, t2.getDamageTokens().size());
        assertEquals(1, t2.getMarks().size());
    }

    /**
     * Testing the weapon Electroscythe:
     * - Basic mode: deal 1 damage to every other player on your square
     * - Reaper mode: deal 2 damage to every other player on your square
     */
    @Test
    void electroscythe() {

        //adding players to the board

        Board board = match.getBoard();
        Player activePlayer = match.getActivePlayer();
        Player t1 = match.getPlayers().get(1);
        Player t2 = match.getPlayers().get(2);
        Block b00 = board.getBlock(0, 0).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b00.addPlayer(activePlayer);
        b00.addPlayer(t1);
        b00.addPlayer(t2);
        BasicWeapon electroscythe = WeaponFactory.create(Weapon.Name.ELECTROSCYTHE, board);

        //in this configuration, t1 and t2 are both on b00: they will both be hit by basic attack

        electroscythe.shoot(new MockInterviewer(0), activePlayer);
        List<Player> targets = electroscythe.getAllTargets();
        assertFalse(targets.contains(activePlayer));
        Set<Player> hit = electroscythe.wasHitBy(electroscythe.basicAttack);
        for (Player target : hit) {
            assertEquals(1, target.getDamageTokens().size(), "Wrong number of damage tokens");
        }
        assertEquals(2, hit.size(), "Wrong number of targets");

        //adding an extra player, but to another block, so he should not be hit

        Player notATarget = match.getPlayers().get(4);
        Block b01 = board.getBlock(0, 1).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b01.addPlayer(notATarget);

        //shooting again, but this time alternative attack will be used

        electroscythe.shoot(new MockInterviewer(1), activePlayer);
        hit = electroscythe.wasHitBy(((WeaponWithAlternative) electroscythe).alternativeAttack);
        for (Player target : hit) {
            //3 damage expected because they are the same targets hit by the test in basic mode
            assertEquals(3, target.getDamageTokens().size(), "Wrong number of damage tokens");
        }
        assertEquals(2, hit.size(), "Wrong number of targets");
    }

    /**
     * Testing the weapon Machine Gun:
     * - Basic effect: choose 1 or 2 targets you can see and deal 1 damage to each
     * - Focus shot: deal 1 additional damage to one of those targets
     * - Turret tripod: deal 1 additional damage to the other of those targets and/or deal 1 damage to a different target you can see
     */
    @Test
    void machineGun() {

        //adding players to the board

        Board board = match.getBoard();
        Player activePlayer = match.getActivePlayer();
        Player t1 = match.getPlayers().get(1);
        Player t2 = match.getPlayers().get(2);
        Player t3 = match.getPlayers().get(3);
        Block b00 = board.getBlock(0, 0).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b00.addPlayer(activePlayer);
        Block b01 = board.getBlock(0, 1).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b01.addPlayer(t1);
        Block b02 = board.getBlock(0, 2).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b02.addPlayer(t2);
        b00.addPlayer(t3);
        BasicWeapon machineGun = WeaponFactory.create(Weapon.Name.MACHINE_GUN, board);

        //in this configuration, all 3 targets are visible from b00
        //two targets will receive 1 damage from the basic attack, then the alternative attacks will give an extra
        //damage to each and also a damage to the third target

        machineGun.shoot(new MockInterviewer(0), activePlayer);

        Set<Player> hitByBasic = machineGun.wasHitBy(machineGun.basicAttack);
        for (Player target : hitByBasic) {
            //2 damages expected because they were both hit twice
            assertEquals(2, target.getDamageTokens().size(), "Wrong number of damage tokens");
        }
        assertEquals(2, hitByBasic.size(), "Wrong number of targets");

        Set<Player> hitByFocusShot = machineGun.wasHitBy(((WeaponWithMultipleEffects) machineGun).getPoweredAttacks().get(0));
        for (Player target : hitByFocusShot) {
            //2 damages expected because the target has already been hit by basic
            assertEquals(2, target.getDamageTokens().size(), "Wrong number of damage tokens");
        }
        assertEquals(1, hitByFocusShot.size(), "Wrong number of targets");
        assertTrue(hitByBasic.containsAll(hitByFocusShot));

        Set<Player> hitByTurretTripod = machineGun.wasHitBy(((WeaponWithMultipleEffects) machineGun).getPoweredAttacks().get(1));
        assertEquals(2, hitByTurretTripod.size(), "Wrong number of targets");
        assertFalse(hitByTurretTripod.containsAll(hitByFocusShot)); //the target hit by focus shot must not be hit again
        hitByTurretTripod.retainAll(hitByBasic); //intersecting the targets hit by basic and those hit by turret tripod
        assertEquals(1, hitByTurretTripod.size()); //only one target is in common
    }

    /**
     * Testing the weapon Tractor Beam:
     * - Basic mode: move a target 0, 1 or 2 squares to a square you can see and give it 1 damage
     * - Punisher mode: choose a target 0, 1 or 2 moves away from you, move it to your square and deal 3 damage to it
     */
    @Test
    void tractorBeam() {

        //adding players to the board

        Board board = match.getBoard();
        Player activePlayer = match.getActivePlayer();
        Player t1 = match.getPlayers().get(1);
        Block b00 = board.getBlock(0, 0).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b00.addPlayer(activePlayer);
        Block b21 = board.getBlock(2, 1).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b21.addPlayer(t1);
        BasicWeapon tractorBeam = WeaponFactory.create(Weapon.Name.TRACTOR_BEAM, board);
        tractorBeam.shoot(new MockInterviewer(0), activePlayer);

        //in this configuration, t1 is the only target on the board and hittable by the weapon
        //the weapon can move it of 0, 1 or 2 steps from where it was
        assertTrue(board.getReachableBlocks(b21, new Range(0, 2)).contains(t1.getBlock()));
        //the final block must be visible by the player
        assertTrue(board.getVisibleBlocks(b00).contains(t1.getBlock()));
        assertEquals(1, t1.getDamageTokens().size());

        //moving t1 out of the way
        board.teleportPlayer(t1, b21);

        //putting t2 2 moves away from the active player
        Block b02 = board.getBlock(0, 2).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        Player t2 = match.getPlayers().get(2);
        b02.addPlayer(t2);
        tractorBeam.shoot(new MockInterviewer(1), activePlayer); //shooting in alternative mode

        assertEquals(3, t2.getDamageTokens().size());
        assertTrue(board.getReachableBlocks(b00, new Range(0, 2)).contains(t2.getBlock())); //t2 is from 0 to 2 moves away from the active player
    }
}