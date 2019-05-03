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
import it.polimi.ingsw.server.view.Interviewer;
import it.polimi.ingsw.shared.messages.ClientApi;
import it.polimi.ingsw.utils.Range;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class WeaponTest {

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

        Weapon lockRifle = WeaponFactory.create("Lock Rifle", board);
        lockRifle.shoot(new MockInterviewer(0), activePlayer);
        assertEquals(1, lockRifle.wasHitBy(lockRifle.basicAttack).size());
        t1 = lockRifle.wasHitBy(lockRifle.basicAttack).iterator().next();
        assertEquals(2, t1.getDamageTokens().size());
        assertEquals(1, t1.getMarks().size());
        Attack powered = ((WeaponWithMultipleEffects) lockRifle).poweredAttacks.get(0);
        t2 = lockRifle.wasHitBy(powered).iterator().next();
        assertEquals(0, t2.getDamageTokens().size());
        assertEquals(1, t2.getMarks().size());

        int paid = lockRifle.allAttacks.stream()
                .mapToInt(attack -> attack.getCost().size())
                .sum();
        assertEquals(9, activePlayer.getAmmoCubes().size() + paid, "Wrong payment");
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
        Weapon electroscythe = WeaponFactory.create("Electroscythe", board);

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

        int paid = electroscythe.allAttacks.stream()
                .mapToInt(attack -> attack.getCost().size())
                .sum();
        assertEquals(9, activePlayer.getAmmoCubes().size() + paid, "Wrong payment");
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
        Weapon machineGun = WeaponFactory.create("Machine Gun", board);

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

        int paid = machineGun.allAttacks.stream()
                .mapToInt(attack -> attack.getCost().size())
                .sum();
        assertEquals(9, activePlayer.getAmmoCubes().size() + paid, "Wrong payment");

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

        paid += machineGun.allAttacks.stream()
                .mapToInt(attack -> attack.getCost().size())
                .sum();
        assertEquals(9, activePlayer.getAmmoCubes().size() + paid, "Wrong payment");
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
        Weapon tractorBeam = WeaponFactory.create("Tractor Beam", board);
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

        int paid = tractorBeam.allAttacks.stream()
                .mapToInt(attack -> attack.getCost().size())
                .sum();
        assertEquals(9, activePlayer.getAmmoCubes().size() + paid, "Wrong payment");
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

        Weapon vortexCannon = WeaponFactory.create("Vortex Cannon", board);
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

        int paid = vortexCannon.allAttacks.stream()
                .mapToInt(attack -> attack.getCost().size())
                .sum();
        assertEquals(9, activePlayer.getAmmoCubes().size() + paid, "Wrong payment");
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

        Weapon thor = WeaponFactory.create("T.H.O.R.", board);
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

        int paid = thor.allAttacks.stream()
                .mapToInt(attack -> attack.getCost().size())
                .sum();
        assertEquals(9, activePlayer.getAmmoCubes().size() + paid, "Wrong payment");

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
        Weapon furnace = WeaponFactory.create("Furnace", board);
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

        int paid = furnace.allAttacks.stream()
                .mapToInt(attack -> attack.getCost().size())
                .sum();
        assertEquals(9, activePlayer.getAmmoCubes().size() + paid, "Wrong payment");
    }

    /**
     * Testing the weapon Plasma Gun:
     * - Basic effect: deal 2 damage to 1 target you can see
     * - Phase glide: Move 1 or 2 squares
     * - Charged shot: Deal 1 additional damage to your target
     */
    @Test
    void plasmaGun() {

        //adding players to the board

        Board board = match.getBoard();
        Player activePlayer = match.getActivePlayer();
        Player t1 = match.getPlayers().get(1);
        Player t2 = match.getPlayers().get(2);
        Block b00 = board.getBlock(0, 0).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b00.addPlayer(activePlayer);
        Block b11 = board.getBlock(1, 1).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b11.addPlayer(t1);
        b11.addPlayer(t2);

        //t1 or t2 will be the only target for this weapon (besides the active player, who will be allowed to move)

        Weapon plasmaGun = WeaponFactory.create("Plasma Gun", board);
        plasmaGun.shoot(new MockInterviewer(0), activePlayer);

        Set<Player> hitByBasic = plasmaGun.wasHitBy(plasmaGun.basicAttack);
        Set<Player> hitByPhaseGlide = plasmaGun.wasHitBy(((WeaponWithMultipleEffects) plasmaGun).getPoweredAttacks().get(0));
        Set<Player> hitByChargedShot = plasmaGun.wasHitBy(((WeaponWithMultipleEffects) plasmaGun).getPoweredAttacks().get(1));

        //all attacks hit only one target

        assertEquals(1, hitByBasic.size(), "Wrong amount of targets");
        assertEquals(1, hitByPhaseGlide.size(), "Wrong amount of targets");
        assertEquals(1, hitByChargedShot.size(), "Wrong amount of targets");

        //the target hit by the basic attack is the same hit by charged shot, and it is not the active player
        //the active player is hit by phase glide instead

        assertEquals(hitByBasic, hitByChargedShot, "Targets should be the same");
        assertTrue(hitByPhaseGlide.contains(activePlayer), "Phase glide should only hit the active player");
        assertFalse(hitByBasic.contains(activePlayer), "Wrong target for basic attack");

        //the active player was moved of one or 2 steps - the range is from 0 to 2 because the player could have taken
        //one step and then another to go back to his initial position

        assertTrue(board.getReachableBlocks(b00, new Range(0, 2)).contains(activePlayer.getBlock()), "Wrong active player position: " + activePlayer.getBlock().getRow() + " : " + activePlayer.getBlock().getColumn());

        //the target has 3 damage total (2 given by basic, 1 given by charged shot)

        for (Player target : hitByBasic) {
            assertEquals(3, target.getDamageTokens().size(), "Wrong damage amount");
        }

        int paid = plasmaGun.allAttacks.stream()
                .mapToInt(attack -> attack.getCost().size())
                .sum();
        assertEquals(9, activePlayer.getAmmoCubes().size() + paid, "Wrong payment");
    }

    /**
     * Testing the weapon Heatseeker:
     * - effect: choose a target you cannot see and deal 3 damage to it
     */
    @Test
    void heatseeker() {

        //adding players to the board

        Board board = match.getBoard();
        Player activePlayer = match.getActivePlayer();
        Player t1 = match.getPlayers().get(1);
        Player t2 = match.getPlayers().get(2);
        Block b00 = board.getBlock(0, 0).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b00.addPlayer(activePlayer);
        Block b23 = board.getBlock(2, 3).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b00.addPlayer(t1);
        b23.addPlayer(t2);

        //only t2 is a valid target for this weapon

        Weapon heatseeker = WeaponFactory.create("Heatseeker", board);
        heatseeker.shoot(new MockInterviewer(0), activePlayer);

        Set<Player> hitByBasic = heatseeker.wasHitBy(heatseeker.basicAttack);

        //there is only one attack and only one target: t2, who received 3 damage

        assertEquals(1, hitByBasic.size(), "Wrong amount of targets");
        assertTrue(hitByBasic.contains(t2), "Wrong target");
        assertEquals(3, t2.getDamageTokens().size(), "Wrong damage");

        int paid = heatseeker.allAttacks.stream()
                .mapToInt(attack -> attack.getCost().size())
                .sum();
        assertEquals(9, activePlayer.getAmmoCubes().size() + paid, "Wrong payment");
    }

    /**
     * Testing the weapon Whisper:
     * - effect: Deal 3 damage and 1 mark to 1 target you can see that is at least 2 moves away from you.
     */
    @Test
    void whisper() {

        //adding players to the board

        Board board = match.getBoard();
        Player activePlayer = match.getActivePlayer();
        Player t1 = match.getPlayers().get(1);
        Player t2 = match.getPlayers().get(2);
        Block b00 = board.getBlock(0, 0).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b00.addPlayer(activePlayer);
        Block b11 = board.getBlock(1, 1).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        Block b21 = board.getBlock(2, 1).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b11.addPlayer(t1);
        b21.addPlayer(t2);

        //both targets are at least 2 moves away from the active player, but only t1 is visible: t1 is the only available target

        Weapon whisper = WeaponFactory.create("Whisper", board);
        whisper.shoot(new MockInterviewer(0), activePlayer);

        Set<Player> hitByBasic = whisper.wasHitBy(whisper.basicAttack);

        //t1 is the only target and has received 3 damage

        assertTrue(hitByBasic.contains(t1), "Wrong target");
        assertEquals(1, hitByBasic.size(), "Wrong amount of targets");
        assertEquals(3, t1.getDamageTokens().size(), "Wrong damage");

        int paid = whisper.allAttacks.stream()
                .mapToInt(attack -> attack.getCost().size())
                .sum();
        assertEquals(9, activePlayer.getAmmoCubes().size() + paid, "Wrong payment");
    }

    /**
     * Testing the weapon Hellion:
     * - Basic mode: Deal 1 damage to 1 target you can see at least 1 move away. Then give 1 mark to that target and everyone else on that square.
     * - Nano tracer mode: Deal 1 damage to 1 target you can see at least 1 move away. Then give 2 marks to that target and everyone else on that square.
     */
    @Test
    void hellion() {

        //adding players to the board

        Board board = match.getBoard();
        Player activePlayer = match.getActivePlayer();
        Player t1 = match.getPlayers().get(1);
        Player t2 = match.getPlayers().get(2);
        Player t3 = match.getPlayers().get(3);
        Block b00 = board.getBlock(0, 0).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b00.addPlayer(activePlayer);
        Block b11 = board.getBlock(1, 1).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b11.addPlayer(t1);
        b11.addPlayer(t2);
        b11.addPlayer(t3);

        //t1, t2 and t3 are all on the same square, so they will all be hit, two of them will only receive the marks and the other also the damage

        Weapon hellion = WeaponFactory.create("Hellion", board);
        hellion.shoot(new MockInterviewer(0), activePlayer);

        Set<Player> hitByBasic = hellion.wasHitBy(hellion.basicAttack);

        //3 targets are hit by the attack, they all have a mark and one of them also has a damage

        assertEquals(3, hitByBasic.size(), "Wrong amount of targets");
        assertTrue(hitByBasic.contains(t1), "t1 should be a target");
        assertTrue(hitByBasic.contains(t2), "t2 should be a target");
        assertTrue(hitByBasic.contains(t3), "t3 should be a target");
        for (Player target : hitByBasic) {
            assertEquals(1, target.getMarks().size(), "Wrong marks");
        }
        assertEquals(1, hitByBasic.stream().filter(t -> t.getDamageTokens().size() == 1).count(), "Wrong amount of damaged players");
        assertEquals(2, hitByBasic.stream().filter(t -> t.getDamageTokens().isEmpty()).count(), "Wrong amount of damaged players");

        //shooting again, the targets should all receive 2 more marks and one of them will also receive a damage

        hellion.shoot(new MockInterviewer(1), activePlayer);

        Set<Player> hitByNanoTracer = hellion.wasHitBy(((WeaponWithAlternative) hellion).alternativeAttack);

        //the targets are the same as the first attack, damage and marks are not tested because it depends on the order
        //used to hit the players (marks turn into damage after receiving a new damage)
        assertEquals(3, hitByNanoTracer.size(), "Wrong amount of targets");
        assertTrue(hitByNanoTracer.containsAll(hitByBasic), "Wrong targets");

        int paid = hellion.allAttacks.stream()
                .mapToInt(attack -> attack.getCost().size())
                .sum();
        assertEquals(9, activePlayer.getAmmoCubes().size() + paid, "Wrong payment");
    }

    /**
     * Testing the weapon Flamethrower:
     * - Basic mode: Choose a square 1 move away and possibly a second square 1 more move away in the same direction. On each square, you may choose 1 target and give it 1 damage.
     * - Barbecue mode: Choose 2 squares as above. Deal 2 damage to everyone on the first square and 1 damage to everyone on the second square.
     */
    @Test
    void flamethrower() {

        //adding players to the board

        Board board = match.getBoard();
        Player activePlayer = match.getActivePlayer();
        Player t1 = match.getPlayers().get(1);
        Player t2 = match.getPlayers().get(2);
        Player t3 = match.getPlayers().get(3);
        Player t4 = match.getPlayers().get(4);
        Block b11 = board.getBlock(1, 1).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b11.addPlayer(activePlayer);
        Block b12 = board.getBlock(1, 2).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        Block b13 = board.getBlock(1, 3).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b12.addPlayer(t1);
        b12.addPlayer(t2);
        b13.addPlayer(t3);
        b13.addPlayer(t4);

        //two players for the two squares, which are in the same direction and 1 and 2 moves away from the active player
        //in basic mode, this means that 1 target for each square will be damaged

        Weapon flamethrower = WeaponFactory.create("Flamethrower", board);
        flamethrower.shoot(new MockInterviewer(0), activePlayer);

        Set<Player> hitByBasic = flamethrower.wasHitBy(flamethrower.basicAttack);

        //only two targets are hit, and they are on different squares
        assertEquals(2, hitByBasic.size(), "Wrong target amount");
        for (Player target : hitByBasic) {
            assertEquals(1, target.getDamageTokens().size(), "Wrong amount of damage");
        }
        assertTrue(hitByBasic.contains(t1) != hitByBasic.contains(t2), "t1 and t2 can't be both targets");
        assertTrue(hitByBasic.contains(t3) != hitByBasic.contains(t4), "t3 and t4 can't be both targets");

        //shooting again, but in barbecue mode

        flamethrower.shoot(new MockInterviewer(1), activePlayer);

        Set<Player> hitByBarbecue = flamethrower.wasHitBy(((WeaponWithAlternative) flamethrower).alternativeAttack);

        assertTrue(hitByBarbecue.containsAll(hitByBasic), "Wrong targets");
        assertEquals(4, hitByBarbecue.size(), "Wrong amount of targets");
        for (Player target : hitByBarbecue) {
            //one of t1 and t2 (the one hit by basic as well) will have 3 damage, the other 2
            //one of t3 and t4 (the one hit by basic as well) will have 2 damage, the other 1
            if (hitByBasic.contains(target) && b12.containsPlayer(target)) {
                assertEquals(3, target.getDamageTokens().size(), "Wrong amount of damage");
            } else if (hitByBasic.contains(target) && b13.containsPlayer(target) || b12.containsPlayer(target)) {
                assertEquals(2, target.getDamageTokens().size(), "Wrong amount of damage");
            } else {
                assertEquals(1, target.getDamageTokens().size(), "Wrong amount of damage");
            }
        }

        int paid = flamethrower.allAttacks.stream()
                .mapToInt(attack -> attack.getCost().size())
                .sum();
        assertEquals(9, activePlayer.getAmmoCubes().size() + paid, "Wrong payment");
    }

    /**
     * Testing the weapon 2x-2:
     * - Basic mode: Deal 1 damgetDamageTokensage and 2 marks to a target you can see
     * - Scanner mode: Choose up to 3 targets you can see and deal 1 mark to each
     */
    @Test
    void twoXTwo() {

        //adding players to the board

        Board board = match.getBoard();
        Player activePlayer = match.getActivePlayer();
        Player t1 = match.getPlayers().get(1);
        Player t2 = match.getPlayers().get(2);
        Player t3 = match.getPlayers().get(3);
        Player t4 = match.getPlayers().get(4);
        Block b11 = board.getBlock(1, 1).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        Block b12 = board.getBlock(1, 2).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        Block b13 = board.getBlock(1, 3).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b12.addPlayer(activePlayer);
        b11.addPlayer(t1);
        b12.addPlayer(t2);
        b13.addPlayer(t3);
        b13.addPlayer(t4);

        //all players are visible by the active player, so they are potential targets for the basic attack
        //only one of them will actually be targeted

        Weapon twoXTwo = WeaponFactory.create("2x-2", board);
        twoXTwo.shoot(new MockInterviewer(0), activePlayer);

        Set<Player> hitByBasic = twoXTwo.wasHitBy(twoXTwo.basicAttack);

        //basic has hit one target and has given him 1 damage and 2 marks

        assertEquals(1, hitByBasic.size(), "Wrong amount of targets");
        for (Player t : hitByBasic) {
            assertEquals(1, t.getDamageTokens().size(), "Wrong damage");
            assertEquals(2, t.getMarks().size(), "Wrong marks");
        }

        //shooting again, but in scanner mode: 3 targets will receive a mark

        twoXTwo.shoot(new MockInterviewer(1), activePlayer);

        Set<Player> hitByScanner = twoXTwo.wasHitBy(((WeaponWithAlternative) twoXTwo).alternativeAttack);

        assertEquals(3, hitByScanner.size(), "Wrong amount of targets");
        for (Player t : hitByScanner) {
            //target will have 3 marks if he was hit before as well, 1 mark if he was hit only by scanner
            if (hitByBasic.contains(t)) {
                assertEquals(3, t.getMarks().size(), "Wrong marks");
            } else {
                assertEquals(1, t.getMarks().size(), "Wrong marks");
            }
        }

        int paid = twoXTwo.allAttacks.stream()
                .mapToInt(attack -> attack.getCost().size())
                .sum();
        assertEquals(9, activePlayer.getAmmoCubes().size() + paid, "Wrong payment");
    }

    /**
     * Testing the weapon Grenade Launcher:
     * - Basic effect: Deal 1 damage to 1 target you can see. Then you may move the target 1 square.
     * - Extra grenade: Deal 1 damage to every player on a square you can see.
     */
    @Test
    void grenadeLauncher() {

        //adding players to the board

        Board board = match.getBoard();
        Player activePlayer = match.getActivePlayer();
        Player t1 = match.getPlayers().get(1);
        Player t2 = match.getPlayers().get(2);
        Player t3 = match.getPlayers().get(3);
        Player t4 = match.getPlayers().get(4);
        Block b11 = board.getBlock(1, 1).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        Block b12 = board.getBlock(1, 2).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        Block b13 = board.getBlock(1, 3).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b12.addPlayer(activePlayer);
        b11.addPlayer(t1);
        b11.addPlayer(t2);
        b13.addPlayer(t3);
        b13.addPlayer(t4);

        //all targets are in visible squares, they are all potential targets for both effects

        Weapon grenadeLauncher = WeaponFactory.create("Grenade Launcher", board);
        grenadeLauncher.shoot(new MockInterviewer(0), activePlayer);

        Set<Player> hitByBasic = grenadeLauncher.wasHitBy(grenadeLauncher.basicAttack);

        //one target was hit by basic, he now has one damage and is not in the same square as before

        assertEquals(1, hitByBasic.size(), "Wrong amount of targets");
        for (Player t : hitByBasic) {
            //damage is greater than 1 because player could have been hit by extra grenade as well
            assertTrue(t.getDamageTokens().size() >= 1, "Wrong damage");
            assertFalse(b11.containsPlayer(t), "Player should not be here, it has either not moved or gone too far");
            assertFalse(b13.containsPlayer(t), "Player should not be here, it has either not moved or gone too far");
            assertTrue(board.getReachableBlocks(t == t1 || t == t2 ? b11 : b13, new Range(1, 1)).contains(t.getBlock()), "Player should not be here");
        }

        Set<Player> hitByExtraGrenade = grenadeLauncher.wasHitBy(((WeaponWithMultipleEffects) grenadeLauncher).getPoweredAttacks().get(0));
        assertFalse(hitByExtraGrenade.isEmpty(), "Attack extra grenade should have hit someone");
        assertTrue(hitByExtraGrenade.iterator().next().getBlock().getPlayers().containsAll(hitByExtraGrenade), "Targets should be all in the same square");
        assertTrue(activePlayer.getDamageTokens().isEmpty(), "Active player should not have been targeted");

        int paid = grenadeLauncher.allAttacks.stream()
                .mapToInt(attack -> attack.getCost().size())
                .sum();
        assertEquals(9, activePlayer.getAmmoCubes().size() + paid, "Wrong payment");
    }

    /**
     * Testing the weapon Shotgun:
     * - Basic mode: deal 3 damage to 1 target on your square. If you want, move it one square
     * - Long barrel mode: deal 2 damage to 1 target exactly one move away
     */
    @Test
    void shotgun() {

        //adding players to the board

        Board board = match.getBoard();
        Player activePlayer = match.getActivePlayer();
        Player t1 = match.getPlayers().get(1);
        Player t2 = match.getPlayers().get(2);
        Player t3 = match.getPlayers().get(3);
        Player t4 = match.getPlayers().get(4);
        Block b11 = board.getBlock(1, 1).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        Block b12 = board.getBlock(1, 2).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        Block b13 = board.getBlock(1, 3).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b12.addPlayer(activePlayer);
        b11.addPlayer(t1);
        b12.addPlayer(t2);
        b13.addPlayer(t3);
        b13.addPlayer(t4);

        //t2 is the only target that is on the same square as the active player and can therefore be targeted

        Weapon shotgun = WeaponFactory.create("Shotgun", board);
        shotgun.shoot(new MockInterviewer(0), activePlayer);

        Set<Player> hitByBasic = shotgun.wasHitBy(shotgun.basicAttack);

        //only 1 player - t2 - was hit by basic mode. t2 now should have 3 damage and be on a different square
        assertEquals(1, hitByBasic.size(), "Wrong amount of targets");
        assertEquals(3, t2.getDamageTokens().size(), "Wrong damage");
        assertTrue(hitByBasic.contains(t2), "t2 should be the target");
        assertTrue(board.getReachableBlocks(b12, new Range(1, 1)).contains(t2.getBlock()), "t2 should be 1 square away");

        //shooting again, but in long barrel mode. This means that the target can be anyone (t1, t2, t3 or t4, because t2 has moved)
        shotgun.shoot(new MockInterviewer(1), activePlayer);

        Set<Player> hitByLongBarrel = shotgun.wasHitBy(((WeaponWithAlternative) shotgun).alternativeAttack);

        //long barrel has hit only one target that is one move away
        assertEquals(1, hitByLongBarrel.size(), "Wrong amount of targets");
        assertTrue(board.getReachableBlocks(b12, new Range(1, 1)).contains(hitByLongBarrel.iterator().next().getBlock()), "target should be 1 square away");
        if (hitByLongBarrel.contains(t2)) {
            //2 damage + 3 damage that t2 received before
            assertEquals(5, t2.getDamageTokens().size(), "Wrong damage");
        } else {
            assertEquals(2, hitByLongBarrel.iterator().next().getDamageTokens().size(), "Wrong damage");
        }

        int paid = shotgun.allAttacks.stream()
                .mapToInt(attack -> attack.getCost().size())
                .sum();
        assertEquals(9, activePlayer.getAmmoCubes().size() + paid, "Wrong payment");
    }

    /**
     * Testing the weapon Rocket Launcher:
     * - Basic effect: Deal 2 damage to 1 target you can see that is not on your square. Then you may move the target 1 square.
     * - Rocket jump: Move 1 or 2 squares.
     * - Fragmenting warhead: deal 1 damage to every player on your target's original square – including the target, even if you move it.
     */
    @Test
    void rocketLauncher() {

        //adding players to the board

        Board board = match.getBoard();
        Player activePlayer = match.getActivePlayer();
        Player t1 = match.getPlayers().get(1);
        Player t2 = match.getPlayers().get(2);
        Player t3 = match.getPlayers().get(3);
        Player t4 = match.getPlayers().get(4);
        Block b12 = board.getBlock(1, 2).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        Block b13 = board.getBlock(1, 3).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b12.addPlayer(activePlayer);
        b13.addPlayer(t1);
        b13.addPlayer(t2);
        b13.addPlayer(t3);
        b13.addPlayer(t4);

        //any of the players will be the target of the basic attack, all of them will be hit by fragmenting warhead

        Weapon rocketLauncher = WeaponFactory.create("Rocket Launcher", board);
        rocketLauncher.shoot(new MockInterviewer(0), activePlayer);

        Set<Player> hitByBasic = rocketLauncher.wasHitBy(rocketLauncher.basicAttack);
        assertEquals(1, hitByBasic.size(), "Wrong amount of targets");
        //2 damage from basic + 1 damage from fragmenting warhead
        assertEquals(3, hitByBasic.iterator().next().getDamageTokens().size(), "Wrong damage");
        assertTrue(board.getReachableBlocks(b13, new Range(1, 1)).contains(hitByBasic.iterator().next().getBlock()), "Wrong target position");

        //the active player is targeted by rocked jump, which allows him to move
        Set<Player> hitByRocketJump = rocketLauncher.wasHitBy(((WeaponWithMultipleEffects) rocketLauncher).getPoweredAttacks().get(0));
        assertEquals(1, hitByRocketJump.size(), "Wrong amount of targets");
        assertTrue(hitByRocketJump.contains(activePlayer), "Target should be the active player");

        Set<Player> hitByFragmentingWarhead = rocketLauncher.wasHitBy(((WeaponWithMultipleEffects) rocketLauncher).getPoweredAttacks().get(1));
        assertEquals(4, hitByFragmentingWarhead.size(), "Wrong amount of targets");
        for (Player target : hitByFragmentingWarhead) {
            if (!hitByBasic.contains(target)) {
                assertEquals(1, target.getDamageTokens().size(), "Wrong damage");
            }
        }

        int paid = rocketLauncher.allAttacks.stream()
                .mapToInt(attack -> attack.getCost().size())
                .sum();
        assertEquals(9, activePlayer.getAmmoCubes().size() + paid, "Wrong payment");
    }

    /**
     * Testing the weapon Power Glove:
     * - Basic mode: Choose 1 target on any square exactly 1 move away. Move onto that square and give the target 1 damage and 2 marks.
     * - Rocket fist mode: Move onto a square 1 move away and deal 2 damage to 1 target there. If you want, you may move 1 more square in that same direction and deal 2 damage to 1 target
     */
    @Test
    void powerGlove() {

        //adding players to the board

        Board board = match.getBoard();
        Player activePlayer = match.getActivePlayer();
        Player t1 = match.getPlayers().get(1);
        Player t2 = match.getPlayers().get(2);
        Block b11 = board.getBlock(1, 1).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        Block b12 = board.getBlock(1, 2).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        Block b13 = board.getBlock(1, 3).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b11.addPlayer(activePlayer);
        b11.addPlayer(t2);
        b12.addPlayer(t1);

        //basic mode can only hit t1

        Weapon powerGlove = WeaponFactory.create("Power Glove", board);
        powerGlove.shoot(new MockInterviewer(0), activePlayer);

        Set<Player> hitByBasic = powerGlove.wasHitBy(powerGlove.basicAttack);

        //2 targets: active player, who has been moved, and t1, who received the damage and the marks

        assertEquals(2, hitByBasic.size(), "Wrong amount of targets");
        assertTrue(hitByBasic.contains(activePlayer), "Active player should be a target of this attack");
        assertTrue(hitByBasic.contains(t1), "t1 should be a target");
        assertEquals(1, t1.getDamageTokens().size(), "Wrong damage");
        assertEquals(2, t1.getMarks().size(), "Wrong marks");
        assertTrue(b12.containsPlayer(activePlayer), "Active player was not moved correctly");

        //Moving active player to execute rocket fist
        board.teleportPlayer(activePlayer, b13);
        //the attack will hit both t1 and t2 and move the active player
        powerGlove.shoot(new MockInterviewer(1), activePlayer);

        Set<Player> hitByRocketFist = powerGlove.wasHitBy(((WeaponWithAlternative) powerGlove).alternativeAttack);

        assertEquals(3, hitByRocketFist.size(), "Wrong amount of targets");
        //5 because he had 1 damage and 2 marks from the previous attack
        assertEquals(5, t1.getDamageTokens().size(), "Wrong damage");
        assertEquals(2, t2.getDamageTokens().size(), "Wrong damage");
        assertTrue(hitByRocketFist.containsAll(b11.getPlayers()), "Wrong targets");
        assertTrue(hitByRocketFist.containsAll(b12.getPlayers()), "Wrong targets");

        int paid = powerGlove.allAttacks.stream()
                .mapToInt(attack -> attack.getCost().size())
                .sum();
        assertEquals(9, activePlayer.getAmmoCubes().size() + paid, "Wrong payment");
    }

    /**
     * Testing the weapon Railgun:
     * - Basic mode: Choose a cardinal direction and 1 target in that direction. Deal 3 damage to it.
     * - Piercing mode: Choose a cardinal direction and 1 or 2 targets in that direction. Deal 2 damage to each.
     */
    @Test
    void railgun() {

        //adding players to the board

        Board board = match.getBoard();
        Player activePlayer = match.getActivePlayer();
        Player t1 = match.getPlayers().get(1);
        Player t2 = match.getPlayers().get(2);
        Block b02 = board.getBlock(0, 2).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        Block b12 = board.getBlock(1, 2).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        Block b22 = board.getBlock(2, 2).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b02.addPlayer(activePlayer);
        b22.addPlayer(t2);
        b12.addPlayer(t1);

        //t1 and t2 are both potential targets, but only one of them will actually be hit

        Weapon railgun = WeaponFactory.create("Railgun", board);
        railgun.shoot(new MockInterviewer(0), activePlayer);

        Set<Player> hitByBasic = railgun.wasHitBy(railgun.basicAttack);

        assertEquals(1, hitByBasic.size(), "Wrong amount of targets");
        assertEquals(3, hitByBasic.iterator().next().getDamageTokens().size(), "Wrong damage tokens");

        railgun.shoot(new MockInterviewer(1), activePlayer);

        Set<Player> hitByPiercing = railgun.wasHitBy(((WeaponWithAlternative) railgun).alternativeAttack);

        //both t1 and t2 were hit by piercing mode

        assertEquals(2, hitByPiercing.size(), "Wrong amount of targets");
        for (Player t : hitByPiercing) {
            if (hitByBasic.contains(t)) {
                //2 + 3 from basic
                assertEquals(5, t.getDamageTokens().size(), "Wrong damage");
            } else {
                assertEquals(2, t.getDamageTokens().size(), "Wrong damage");
            }
        }

        int paid = railgun.allAttacks.stream()
                .mapToInt(attack -> attack.getCost().size())
                .sum();
        assertEquals(9, activePlayer.getAmmoCubes().size() + paid, "Wrong payment");
    }

    /**
     * Testing the weapon Shockwave:
     * - Basic mode: Choose up to 3 targets on different squares, each exactly 1 move away. Deal 1 damage to each target.
     * - Tsunami mode: Deal 1 damage to all targets that are exactly 1 move away.
     */
    @Test
    void shockwave() {
        Board board = match.getBoard();
        Player activePlayer = match.getActivePlayer();
        Player t1 = match.getPlayers().get(1);
        Player t2 = match.getPlayers().get(2);
        Player t3 = match.getPlayers().get(3);
        Player t4 = match.getPlayers().get(4);
        Block b11 = board.getBlock(1, 1).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        Block b12 = board.getBlock(1, 2).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        Block b13 = board.getBlock(1, 3).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b12.addPlayer(activePlayer);
        b11.addPlayer(t2);
        b11.addPlayer(t1);
        b13.addPlayer(t3);
        b13.addPlayer(t4);

        //t1 and t2 (and t3 and t4) are in the same square, so only one of them will be hit

        Weapon shockwave = WeaponFactory.create("Shockwave", board);
        shockwave.shoot(new MockInterviewer(0), activePlayer);

        Set<Player> hitByBasic = shockwave.wasHitBy(shockwave.basicAttack);

        //two targets, one from the block of t1/t2, the other from the block of t3/t4

        assertEquals(2, hitByBasic.size(), "Wrong amount of targets");
        for (Player t : hitByBasic) {
            assertEquals(1, t.getDamageTokens().size(), "Wrong damage");
        }
        assertTrue(hitByBasic.contains(t1) != hitByBasic.contains(t2), "t1 and t2 can't both be hit / non hit");
        assertTrue(hitByBasic.contains(t3) != hitByBasic.contains(t4), "t3 and t4 can't both be hit / non hit");

        //tsunami mode will hit all 4 targets

        shockwave.shoot(new MockInterviewer(1), activePlayer);

        Set<Player> hitByTsunami = shockwave.wasHitBy(((WeaponWithAlternative) shockwave).alternativeAttack);
        assertEquals(4, hitByTsunami.size(), "Wrong amount of targets");
        for (Player t : hitByTsunami) {
            if (hitByBasic.contains(t)) {
                //1 + 1 given by basic
                assertEquals(2, t.getDamageTokens().size(), "Wrong damage");
            } else {
                assertEquals(1, t.getDamageTokens().size(), "Wrong damage");
            }
        }

        int paid = shockwave.allAttacks.stream()
                .mapToInt(attack -> attack.getCost().size())
                .sum();
        assertEquals(9, activePlayer.getAmmoCubes().size() + paid, "Wrong payment");
    }

    /**
     * Testing the weapon Cyberblade:
     * - Basic effect: Deal 2 damage to 1 target on your square.
     * - Shadowstep: Move 1 square before or after the basic effect.
     * - Slice and Dice: Deal 2 damage to a different target on your square.
     */
    @Test
    void cyberblade() {
        Board board = match.getBoard();
        Player activePlayer = match.getActivePlayer();
        Player t1 = match.getPlayers().get(1);
        Player t2 = match.getPlayers().get(2);
        Player t3 = match.getPlayers().get(3);
        Player t4 = match.getPlayers().get(4);
        Block b23 = board.getBlock(2, 3).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        Block b12 = board.getBlock(1, 2).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        Block b13 = board.getBlock(1, 3).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b13.addPlayer(activePlayer);
        b23.addPlayer(t2);
        b12.addPlayer(t1);
        b13.addPlayer(t3);
        b13.addPlayer(t4);

        Weapon cyberblade = WeaponFactory.create("Cyberblade", board);
        cyberblade.shoot(new MockInterviewer(0), activePlayer);

        Set<Player> hitByBasic = cyberblade.wasHitBy(cyberblade.basicAttack);
        Set<Player> hitByShadowStep = cyberblade.wasHitBy(((WeaponWithMultipleEffects) cyberblade).getPoweredAttacks().get(0));
        Set<Player> hitBySliceAndDice = cyberblade.wasHitBy(((WeaponWithMultipleEffects) cyberblade).getPoweredAttacks().get(1));

        //basic attack has hit either t3 or t4, then slice and dice has hit t1 or t2, depending on where the active player moved

        assertEquals(1, hitByBasic.size(), "Wrong amount of targets");
        assertEquals(1, hitByShadowStep.size(), "Wrong amount of targets");
        assertEquals(1, hitBySliceAndDice.size(), "Wrong amount of targets");

        assertTrue(hitByBasic.contains(t3) || hitByBasic.contains(t4), "Wrong targets");
        assertSame(hitBySliceAndDice.iterator().next().getBlock(), activePlayer.getBlock(), "Hit target should be on the same square as active player");
        assertTrue(hitByShadowStep.contains(activePlayer), "Shadowstep should hit active player");
        assertEquals(2, hitByBasic.iterator().next().getDamageTokens().size(), "Wrong damage");
        assertEquals(2, hitBySliceAndDice.iterator().next().getDamageTokens().size(), "Wrong damage");

        int paid = cyberblade.allAttacks.stream()
                .mapToInt(attack -> attack.getCost().size())
                .sum();
        assertEquals(9, activePlayer.getAmmoCubes().size() + paid, "Wrong payment");
    }

    /**
     * Testing the weapon Sledgehammer:
     * - Basic mode: Deal 2 damage to 1 target on your square.
     * - Pulverize mode: Deal 3 damage to 1 target on your square, then move that target 0, 1, or 2 squares in one direction.
     */
    @Test
    void sledgehammer() {
        Board board = match.getBoard();
        Player activePlayer = match.getActivePlayer();
        Player t1 = match.getPlayers().get(1);
        Block b13 = board.getBlock(1, 3).orElseThrow(() -> new IllegalStateException("Block does not exist"));
        b13.addPlayer(activePlayer);
        b13.addPlayer(t1);

        Weapon sledgehammer = WeaponFactory.create("Sledgehammer", board);
        sledgehammer.shoot(new MockInterviewer(0), activePlayer);

        //basic attack hit t1, the only available target
        Set<Player> hitByBasic = sledgehammer.wasHitBy(sledgehammer.basicAttack);
        assertEquals(1, hitByBasic.size(), "Wrong amount of targets");
        assertEquals(2, t1.getDamageTokens().size(), "Wrong damage");

        //shooting again, but in pulverize mode: t1 will be hit again and then moved
        sledgehammer.shoot(new MockInterviewer(1), activePlayer);

        Set<Player> hitByPulverize = sledgehammer.wasHitBy(((WeaponWithAlternative) sledgehammer).alternativeAttack);
        assertEquals(1, hitByPulverize.size(), "Wrong amount of targets");

        //3 damage + 2 from basic
        assertEquals(5, t1.getDamageTokens().size(), "Wrong damage");
        assertFalse(b13.containsPlayer(t1), "Target was not moved at all");
        assertTrue(t1.getBlock().getRow() == 1 || t1.getBlock().getColumn() == 3, "Move was not in the same direction");

        int paid = sledgehammer.allAttacks.stream()
                .mapToInt(attack -> attack.getCost().size())
                .sum();
        assertEquals(9, activePlayer.getAmmoCubes().size() + paid, "Wrong payment");
    }
}