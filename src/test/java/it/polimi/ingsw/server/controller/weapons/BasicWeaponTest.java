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

        //teleporting away t3 to test the weapon with t1 and t2 as the only targets
        Block b21 = board.getBlock(2, 1).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        board.teleportPlayer(t3, b21);

        machineGun.shoot(new MockInterviewer(0), activePlayer);

        //basic attack hits 2 targets as before
        hitByBasic = machineGun.wasHitBy(machineGun.basicAttack);
        assertEquals(2, hitByBasic.size(), "Wrong number of targets");

        //focus shot hits one target as before
        hitByFocusShot = machineGun.wasHitBy(((WeaponWithMultipleEffects) machineGun).getPoweredAttacks().get(0));
        assertEquals(1, hitByFocusShot.size(), "Wrong number of targets");
        assertTrue(hitByBasic.containsAll(hitByFocusShot));

        //turret tripod can only hit the other target hit by basic
        hitByTurretTripod = machineGun.wasHitBy(((WeaponWithMultipleEffects) machineGun).getPoweredAttacks().get(1));
        assertEquals(1, hitByTurretTripod.size(), "Wrong number of targets");
        assertFalse(hitByTurretTripod.containsAll(hitByFocusShot)); //the target hit by focus shot must not be hit again
        assertTrue(hitByBasic.containsAll(hitByTurretTripod));

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

    /**
     * Testing the weapon Vortex Cannon:
     * - Basic effect: choose a square you can see, but not your own: the vortex. Choose a target on the vortex or 1 move away, put it on the vortex and give it 2 damage
     * - Black hole: choose up to 2 other targets on the vortex or 1 move away from it, move them onto the vortex and give them each 1 damage
     */
    @Test
    void vortexCannon() {

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
        b01.addPlayer(t2);
        b01.addPlayer(t3);

        //once the vortex is set, all targets can be moved to it and then hit using the two attacks

        BasicWeapon vortexCannon = WeaponFactory.create(Weapon.Name.VORTEX_CANNON, board);
        vortexCannon.shoot(new MockInterviewer(0), activePlayer);

        Block vortex = t1.getBlock();
        assertTrue(vortex.containsPlayer(t2), "Player should be on the vortex");
        assertTrue(vortex.containsPlayer(t3), "Player should be on the vortex");
        assertTrue(board.getVisibleBlocks(b00).contains(vortex), "Active player should see the vortex");
        assertNotEquals(b00, vortex, "Vortex should not be the same block of the active player");

        Set<Player> hitByBasic = vortexCannon.wasHitBy(vortexCannon.basicAttack);
        assertEquals(1, hitByBasic.size(), "Wrong number of targets");
        for (Player target : hitByBasic) {
            assertEquals(2, target.getDamageTokens().size(), "Player should have 1 damage");
        }

        Set<Player> hitByBlackHole = vortexCannon.wasHitBy(((WeaponWithMultipleEffects) vortexCannon).getPoweredAttacks().get(0));
        assertEquals(2, hitByBlackHole.size(), "Wrong number of targets");
        for (Player target : hitByBlackHole) {
           assertEquals(1, target.getDamageTokens().size(), "Player should have 1 damage");
        }
    }

    /**
     * Testing the weapon T.H.O.R.:
     * - Basic effect: deal 2 damage to 1 target you can see
     * - Chain reaction: deal 1 damage to a second target your first target can see
     * - High voltage: deal 2 damage to a third target that your second target can see
     */
    @Test
    void thor() {

        //adding players to the board

        Board board = match.getBoard();
        Player activePlayer = match.getActivePlayer();
        Player t1 = match.getPlayers().get(1);
        Player t2 = match.getPlayers().get(2);
        Player t3 = match.getPlayers().get(3);
        Block b00 = board.getBlock(0, 0).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b00.addPlayer(activePlayer);
        Block b11 = board.getBlock(1, 1).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        Block b22 = board.getBlock(2, 2).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        Block b23 = board.getBlock(2, 3).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b11.addPlayer(t1);
        b22.addPlayer(t2);
        b23.addPlayer(t3);

        //in this configuration, there are 3 players this weapon can hit

        BasicWeapon thor = WeaponFactory.create(Weapon.Name.THOR, board);
        thor.shoot(new MockInterviewer(0), activePlayer);

        Set<Player> hitByBasic = thor.wasHitBy(thor.basicAttack);
        Set<Player> hitByChainReaction = thor.wasHitBy(((WeaponWithMultipleEffects) thor).poweredAttacks.get(0));
        Set<Player> hitByHighVoltage = thor.wasHitBy(((WeaponWithMultipleEffects) thor).poweredAttacks.get(1));

        //each attack hits only one target, which is different for every attack

        assertEquals(1, hitByBasic.size(), "Only one target is allowed for this attack");
        assertEquals(1, hitByChainReaction.size(), "Only one target is allowed for this attack");
        assertEquals(1, hitByHighVoltage.size(), "Only one target is allowed for this attack");
        assertNotEquals(hitByBasic, hitByChainReaction, "Targets should be different");
        assertNotEquals(hitByBasic, hitByHighVoltage, "Targets should be different");
        assertNotEquals(hitByChainReaction, hitByHighVoltage, "Targets should be different");
        assertEquals(2, t1.getDamageTokens().size());
        assertEquals(1, t2.getDamageTokens().size());
        assertEquals(2, t3.getDamageTokens().size());

    }

    /**
     * Testing the weapon Furnace:
     * - Basic mode: choose a room you can see, but not the room you are in. Deal 1 damage to everyone in that room
     * - Cozy fire mode: choose a square exactly 1 move away. Deal 1 damage and 1 mark to everyone on that square
     */
    @Test
    void furnace() {

        //adding players to the board

        Board board = match.getBoard();
        Player activePlayer = match.getActivePlayer();
        Player t1 = match.getPlayers().get(1);
        Player t2 = match.getPlayers().get(2);
        Player t3 = match.getPlayers().get(3);
        Player t4 = match.getPlayers().get(4);
        Block b00 = board.getBlock(0, 0).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b00.addPlayer(activePlayer);
        Block b11 = board.getBlock(1, 1).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        Block b12 = board.getBlock(1, 2).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b11.addPlayer(t1);
        b12.addPlayer(t2);
        b12.addPlayer(t3);
        b00.addPlayer(t4);
        BasicWeapon furnace = WeaponFactory.create(Weapon.Name.FURNACE, board);
        furnace.shoot(new MockInterviewer(0), activePlayer);

        //t1, t2 and t3 are hit: they are all in the same room which is visible by the active player, but not his own

        Set<Player> hitByBasic = furnace.wasHitBy(furnace.basicAttack);
        assertEquals(3, hitByBasic.size(), "Wrong amount of targets");
        for (Player target : hitByBasic) {
            assertEquals(1, target.getDamageTokens().size(), "Targets should have 1 damage");
        }

        //preparing the board for the alternative attack

        Block b01 = board.getBlock(0, 1).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        board.teleportPlayer(t1, b01);
        board.teleportPlayer(t2, b01);

        //t1 and t2 are on the same block which is 1 move away from the active player, they are both hit
        furnace.shoot(new MockInterviewer(1), activePlayer);
        Set<Player> hitByCozyFire = furnace.wasHitBy(((WeaponWithAlternative) furnace).alternativeAttack);
        assertEquals(2, hitByCozyFire.size(), "Wrong amount of hit targets");
        for (Player target : hitByCozyFire) {
            //2 damage because they were hit by basic in the previous shooting
            assertEquals(2, target.getDamageTokens().size(), "Targets should have 2 damage");
            assertEquals(1, target.getMarks().size(), "Targets should have 1 mark");
        }
    }
}