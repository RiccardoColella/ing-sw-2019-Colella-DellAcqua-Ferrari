package it.polimi.ingsw.server.model.player;

import it.polimi.ingsw.server.model.DamageToken;
import it.polimi.ingsw.server.model.Match;
import it.polimi.ingsw.server.model.currency.Ammo;
import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.events.PlayerDied;
import it.polimi.ingsw.server.model.exceptions.MissingOwnershipException;
import it.polimi.ingsw.server.model.exceptions.UnauthorizedGrabException;
import it.polimi.ingsw.server.model.factories.*;
import it.polimi.ingsw.server.model.weapons.Weapon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    private static Weapon electroscythe;
    private static Weapon furnace;
    private static Weapon flamethrower;
    private static Weapon heatseeker;
    static {
        electroscythe = WeaponFactory.create(Weapon.Name.ELECTROSCYTHE);
        furnace = WeaponFactory.create(Weapon.Name.FURNACE);
        heatseeker = WeaponFactory.create(Weapon.Name.HEATSEEKER);
        flamethrower = WeaponFactory.create(Weapon.Name.FLAMETHROWER);
    }

    private Match match;
    private Player player;



    @BeforeEach
    void setUp() {
        List<PlayerInfo> playerInfos = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            playerInfos.add(new PlayerInfo("Player" + i, PlayerColor.values()[i]));
        }
        this.match = MatchFactory.create(playerInfos, BoardFactory.Preset.BOARD_1, 5, Match.Mode.STANDARD);
        this.player = match.getPlayers().get(4);
    }

    /**
     * This test covers the methods addDamageTokens and addDamageToken (which calls addDamageTokens) in the following situations:
     * - adding tokens in "standard" configurations
     * - adding tokens when the player has marks given by the attacker
     * - adding tokens when the player has marks, but not belonging to the attacker
     * - adding enough tokens to kill the player
     * - adding an overkill token and some extra tokens that are not counted
     */
    @Test
    void addDamageTokens() {
        List<DamageToken> tokens = new LinkedList<>();

        for (int i = 0; i < 3; i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }

        int expectedTokens = player.getDamageTokens().size();
        player.addDamageTokens(tokens);
        expectedTokens += tokens.size();
        assertEquals(expectedTokens, player.getDamageTokens().size(), "Not all damage was added"); //checking all damage has been added

        player.addMark(new DamageToken(match.getActivePlayer()));

        player.addDamageToken(new DamageToken(match.getActivePlayer()));

        expectedTokens += 2; // the mark belonging to the attacker will turn into damage

        assertEquals(expectedTokens, player.getDamageTokens().size(), "The mark did not turn into damage");

        match.endTurn();
        match.changeTurn();

        player.addMark(new DamageToken(match.getActivePlayer()));

        match.endTurn();
        match.changeTurn();

        player.addDamageToken(new DamageToken(match.getActivePlayer()));

        expectedTokens += 1; // the mark should not be added because it is not from the same player

        assertEquals(expectedTokens, player.getDamageTokens().size(), "Damage was not added correctly");

        tokens.clear();

        for (int i = 0; i < 4; i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }

        player.addDamageTokens(tokens);
        expectedTokens += tokens.size();
        assertTrue(player.isAlive(), "Player died too soon");
        assertEquals(expectedTokens, player.getDamageTokens().size(), "Damage was not added correctly");

        match.endTurn();
        match.changeTurn();

        player.addDamageToken(new DamageToken(match.getActivePlayer()));
        expectedTokens++;

        assertFalse(player.isAlive(), "Player is still alive but should not be"); // player dies with eleven tokens
        assertEquals(expectedTokens, player.getDamageTokens().size(), "Damage was not added correctly");

        tokens.clear();

        for (int i = 0; i < 2; i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }

        player.addDamageTokens(tokens);
        expectedTokens++; //player can only receive overkill at this point

        assertFalse(player.isAlive(), "Player somehow came back to life on his own");
        assertEquals(expectedTokens, player.getDamageTokens().size(), "Too much damage was added, max is 12");
        List<Player> dead = match.endTurn();
        assertEquals(dead.get(0), player, "Player is not counted as dead");

    }

    /**
     * This test covers the method chooseWeapon in the following situations:
     * - choosing a valid weapon
     * - trying to choose an unloaded weapon
     * - trying to choose a weapon that does not belong to the player
     */
    @Test
    void chooseWeapon() {
        player = match.getActivePlayer();
        player.grabAmmos(electroscythe.getAcquisitionCost());
        player.grabWeapon(electroscythe, electroscythe.getAcquisitionCost(), new ArrayList<>());

        //CHOOSING A WEAPON
        player.chooseWeapon(electroscythe);
        assertEquals(electroscythe, player.getActiveWeapon().orElse(null), "Player could not choose his weapon");
        player.putAwayActiveWeapon();

        //CHOOSING AN UNLOADED WEAPON
        electroscythe.setLoaded(false);
        player.chooseWeapon(electroscythe);
        assertNull(player.getActiveWeapon().orElse(null), "Unloaded weapon cannot be chosen"); // the weapon can't be active because it is not loaded

        //CHOOSING A WEAPON THAT DOES NOT BELONG TO THE PLAYER
        player.chooseWeapon(heatseeker);
        assertNull(player.getActiveWeapon().orElse(null), "Player tried to chose a weapon that did not belong to him"); // the weapon can't be active because it is not owned by the player
    }

    /**
     * This test covers the method grabWeapon in the following situations:
     * - grabbing a weapon with enough money
     * - grabbing a weapon that is free (acquisition cost is an empty list)
     * - trying to grab a weapon the player can't afford
     * - grabbing a weapon while already owning 3, but selecting a weapon to discard
     * - trying to grab a weapon while already owning 3 without discarding a weapon
     * - trying to grab a weapon while already owning 3 and discarding a not-owned weapon
     */
    @Test
    void grabWeapon() {
        // FILLING UP THE WALLET SO THAT THE PLAYER CAN BUY A WEAPON
        player.grabAmmos(electroscythe.getAcquisitionCost());
        int playerAmmos = player.getAmmos().size();

        //BUYING AN AFFORDABLE WEAPON WHILE HAVING LESS THAN 3 WEAPONS
        player.grabWeapon(
                electroscythe,
                electroscythe.getAcquisitionCost(),
                new LinkedList<>()
        );
        playerAmmos -= electroscythe.getAcquisitionCost().size();
        assertEquals(playerAmmos, player.getAmmos().size(), "Player did not pay correctly");
        assertTrue(player.getWeapons().contains(electroscythe), "Player did not get the weapon he paid for");

        //BUYING A FREE WEAPON
        player.grabAmmos(flamethrower.getAcquisitionCost());
        playerAmmos += flamethrower.getAcquisitionCost().size();
        player.grabWeapon(flamethrower, flamethrower.getAcquisitionCost(), new LinkedList<>());
        playerAmmos -= flamethrower.getAcquisitionCost().size();
        assertTrue(player.getWeapons().contains(flamethrower), "Player did not get the weapon he paid for");
        assertEquals(playerAmmos, player.getAmmos().size(), "Player did not pay correctly");

        //TRYING TO BUY A WEAPON THE PLAYER CAN'T AFFORD
        assertThrows(MissingOwnershipException.class,
                () -> player.grabWeapon(
                heatseeker,
                heatseeker.getAcquisitionCost(),
                new LinkedList<>()
        ), "Player bought a weapon without paying");
        assertFalse(player.getWeapons().contains(heatseeker), "Player got the weapon although he did not pay for it");
        assertEquals(playerAmmos, player.getAmmos().size(), "Player lost some ammo even if the purchase did not happen");

        //FILLING UP THE WALLET TO BUY MORE WEAPONS
        player.grabAmmos(heatseeker.getAcquisitionCost());
        playerAmmos += heatseeker.getAcquisitionCost().size();

        //BUYING THE THIRD WEAPON
        player.grabWeapon(
                heatseeker,
                heatseeker.getAcquisitionCost(),
                new LinkedList<>()
        );

        playerAmmos -= heatseeker.getAcquisitionCost().size();
        assertTrue(player.getWeapons().contains(heatseeker), "Player did not get the weapon he paid for");
        assertEquals(playerAmmos, player.getAmmos().size(), "Player did not pay the right amount of ammo");

        //FILLING UP THE WALLET TO BUY THE FOURTH WEAPON
        player.grabAmmos(furnace.getAcquisitionCost());
        playerAmmos += furnace.getAcquisitionCost().size();

        //BUYING THE FOURTH WEAPON DISCARDING A WEAPON
        player.grabWeapon(
                furnace,
                furnace.getAcquisitionCost(),
                new LinkedList<>(),
                electroscythe
        );

        playerAmmos -= furnace.getAcquisitionCost().size();
        assertEquals(playerAmmos, player.getAmmos().size(), "Player did not pay the right amount of ammo");
        assertTrue(player.getWeapons().contains(furnace), "Player did not get the weapon he paid for");
        assertFalse(player.getWeapons().contains(electroscythe), "Player still owns the weapon he discarded"); // the discarded weapon does not belong to the player anymore

        //REFILLING THE WALLET
        player.grabAmmos(electroscythe.getAcquisitionCost());
        playerAmmos += electroscythe.getAcquisitionCost().size();

        //TRYING TO BUY A FOURTH WEAPON WITHOUT DISCARDING ONE
        assertThrows(UnauthorizedGrabException.class, () -> player.grabWeapon(electroscythe, new LinkedList<>(), new LinkedList<>()), "Player grabbed a fourth weapon");
        assertFalse(player.getWeapons().contains(electroscythe), "The weapon was grabbed even if the purchase did not end correctly");
        assertEquals(playerAmmos, player.getAmmos().size(), "Player could pay for a weapon that he was not allowed to grab");

        //TRYING TO BUY A FOURTH WEAPON DISCARDING A WEAPON THAT DOES NOT BELONG TO THE PLAYER
        assertThrows(UnauthorizedGrabException.class, () -> player.grabWeapon(electroscythe, new LinkedList<>(), new LinkedList<>(), electroscythe), "Player grabbed a fourth weapon");
    }

    /**
     * This test covers the method grabPowerup in the following situations:
     * - grabbing up to 3 powerups
     * - trying to buy a fourth powerup, which is not allowed
     */
    @Test
    void grabPowerup() {
        this.player = match.getActivePlayer();

        //GRABBING THE THREE ALLOWED POWERUPS
        this.player.grabPowerup(PowerupTileFactory.create(PowerupTile.Type.TELEPORTER, CurrencyColor.RED));
        this.player.grabPowerup(PowerupTileFactory.create(PowerupTile.Type.NEWTON, CurrencyColor.YELLOW));
        this.player.grabPowerup(PowerupTileFactory.create(PowerupTile.Type.TELEPORTER, CurrencyColor.BLUE));

        //NOW PLAYER HAS 3 POWERUPS
        assertEquals(3, player.getPowerups().size(), "Player did not grab all powerups");

        //GRABBING A FOURTH POWERUPS IS NOT ALLOWED
        assertThrows(UnauthorizedGrabException.class, () -> this.player.grabPowerup(PowerupTileFactory.create(PowerupTile.Type.TAGBACK_GRENADE, CurrencyColor.BLUE)), "Player grabbed too many powerups");

        //PLAYER STILL HAS 3 POWERUPS
        assertEquals(3, player.getPowerups().size(), "Player grabbed too many powerups");
    }

    /**
     * This test covers the method grabAmmos in the following situations:
     * - grabbing up to 3 ammos of each color
     * - trying to grab more ammos when player already has 3 of that color
     */
    @Test
    void grabAmmos() {
        this.player = match.getActivePlayer();
        List<Ammo> redAmmos = new LinkedList<>();
        List<Ammo> blueAmmos = new LinkedList<>();
        List<Ammo> yellowAmmos = new LinkedList<>();

        for (int i = 0; i < 3; i++) {
            redAmmos.add(AmmoFactory.create(CurrencyColor.RED));
            blueAmmos.add(AmmoFactory.create(CurrencyColor.BLUE));
            yellowAmmos.add(AmmoFactory.create(CurrencyColor.YELLOW));
        }

        //PLAYER GRABS 3 RED AMMOS AND 3 BLUE AMMOS
        this.player.grabAmmos(redAmmos);
        assertEquals(redAmmos.size(), player.getAmmos().size());
        this.player.grabAmmos(blueAmmos);
        assertEquals(blueAmmos.size() + redAmmos.size(), player.getAmmos().size(), "Player did not grab all ammos");

        //PLAYER ALREADY HAS 3 RED AMMOS
        this.player.grabAmmos(redAmmos);
        assertEquals(blueAmmos.size() + redAmmos.size(), player.getAmmos().size(), "Player grabbed too many red ammos");

        //PLAYER GRABS 3 YELLOW AMMOS
        this.player.grabAmmos(yellowAmmos);
        assertEquals(redAmmos.size() + blueAmmos.size() + yellowAmmos.size(), player.getAmmos().size(), "Player did not grab all ammos");

        //PLAYER CAN'T GRAB ANYTHING ANYMORE
        this.player.grabAmmos(yellowAmmos);
        this.player.grabAmmos(blueAmmos);
        assertEquals(redAmmos.size() + blueAmmos.size() + yellowAmmos.size(), player.getAmmos().size(), "Player grabbed too many ammos");
    }

    /**
     * This test covers the method reload in the following situations:
     * - reloading a weapon and paying in ammos
     * - reloading a weapon and paying in powerups
     * - reloading a weapon and paying with both ammos and powerups
     * - trying to reload a weapon with money that is not owned
     */
    @Test
    void reload() {
        player.grabAmmos(furnace.getAcquisitionCost());
        player.grabWeapon(furnace, furnace.getAcquisitionCost(), new LinkedList<>());
        furnace.setLoaded(false);
        player.grabAmmos(furnace.getReloadCost());
        player.getAmmos().forEach(ammo -> player.grabPowerup(PowerupTileFactory.create(PowerupTile.Type.NEWTON, ammo.getColor())));

        //PAYMENT WITH AMMOS
        player.reload(furnace, furnace.getReloadCost(), new LinkedList<>());

        assertTrue(furnace.isLoaded(), "Player could not reload");
        assertTrue(player.getAmmos().isEmpty(), "Player did not pay");

        furnace.setLoaded(false);

        //TRYING PAYMENT WITH POWERUPS
        player.reload(furnace, new LinkedList<>(), furnace.getReloadCost().stream().map(ammo -> PowerupTileFactory.create(PowerupTile.Type.NEWTON, ammo.getColor())).collect(Collectors.toList()));
        assertTrue(furnace.isLoaded(), "Player could not reload");
        assertTrue(player.getPowerups().isEmpty(), "Player did not pay");


        furnace.setLoaded(false);
        player.grabAmmos(furnace.getReloadCost());
        player.getAmmos().forEach(ammo -> player.grabPowerup(PowerupTileFactory.create(PowerupTile.Type.NEWTON, ammo.getColor())));

        //TRYING PAYMENT WITH POWERUPS - FAILING BECAUSE POWERUPS AREN'T OF THE SAME TYPE
        assertThrows(
                MissingOwnershipException.class,
                () -> player.reload(
                        furnace,
                        new LinkedList<>(),
                        furnace.getReloadCost().stream().map(ammo -> PowerupTileFactory.create(PowerupTile.Type.TELEPORTER, ammo.getColor())).collect(Collectors.toList())
                ),
                "Player paid although powerups were not the same"
        );
        assertFalse(furnace.isLoaded(), "Weapon was loaded without payment");
        assertFalse(player.getPowerups().isEmpty(), "Powerups were used incorrectly");

        //TRYING MIXED PAYMENT
        player.reload(furnace, player.getAmmos().subList(0, 1), player.getPowerups().subList(1, 2));
        assertTrue(furnace.isLoaded(), "Player could not reload");
        assertEquals(1, player.getAmmos().size(), "Wrong amount of ammos spent");
        assertEquals(1, player.getAmmos().size(), "Wrong amount of ammos spent");
    }

    /** This test covers the method onMatchModeChanged in the following situations:
     * - standard -> final frenzy for player with 0 damage (should flip the board)
     * - standard -> final frenzy for player with damage (should not flip the board)
     */
    @Test
    void onMatchModeChanged() {
        List<DamageToken> tokens = new LinkedList<>();
        while (match.getRemainingSkulls() > 0) {
            for (int i = 0; i < 11; i++) {
                tokens.add(new DamageToken(match.getActivePlayer()));
            }
            if (!player.equals(match.getActivePlayer())) {
                player.addDamageTokens(tokens);
                List<Player> dead = match.endTurn();
                assertEquals(player, dead.get(0), "Player should be dead");
                for (Player d : dead) {
                    player.bringBackToLife();
                }
                match.changeTurn();
                if (!match.getActivePlayer().equals(match.getPlayers().get(0))) {
                    match.getActivePlayer().addDamageToken(new DamageToken(player)); // This way all players but player and the first player will have some damage when final frenzy is triggered
                }
            } else {
                match.endTurn();
                match.changeTurn();
            }
        }
        assertEquals(Match.Mode.FINAL_FRENZY, match.getMode());
        for (Player p : match.getPlayers()) {
            if (p.equals(player) || match.getPlayers().indexOf(p) == 0) {
                assertTrue(p.getDamageTokens().isEmpty(), p.getNickname() + " has tokens but should not");
                assertNotEquals(9, p.getCurrentReward().getRewardFor(0, true), "Player does not have final frenzy reward, but should " + p.getNickname());
            } else {
                assertFalse(p.getDamageTokens().isEmpty(), p.getNickname() + " does not have tokens but should");
                assertEquals(9, p.getCurrentReward().getRewardFor(0, true), "Player has final frenzy reward, but should not " + p.getNickname());
            }
        }
    }

    /**
     * This test covers the method bringBackToLife in the following situations:
     * - dead player that shall be brought back to life
     */
    @Test
    void bringBackToLife() {
        List<DamageToken> tokens = new LinkedList<>();
        for (int i = 0; i < 11; i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }
        player.addDamageTokens(tokens);
        assertFalse(player.isAlive(), "Player should have died");
        List<Player> dead = match.endTurn();
        assertEquals(1, dead.size(), "Wrong number of dead players");
        dead.get(0).bringBackToLife();
        assertTrue(player.isAlive());
    }

    /**
     * This test covers the method pay in the following situations:
     * - paying with both ammos and powerups
     * - trying to pay with an empty wallet
     */
    @Test
    void pay() {
        List<Coin> coins = new LinkedList<>();
        for (int i = 0; i < 2; i++) {
            Ammo ammo = AmmoFactory.create(CurrencyColor.RED);
            PowerupTile powerupTile = PowerupTileFactory.create(PowerupTile.Type.NEWTON, CurrencyColor.BLUE);
            coins.add(ammo);
            coins.add(powerupTile);
            player.grabPowerup(powerupTile);
            player.grabAmmos(Collections.singletonList(ammo));
        }
        player.pay(coins);
        assertTrue(player.getAmmos().isEmpty(), "Player still has the coins he spent");
        assertTrue(player.getPowerups().isEmpty(), "Player still has the powerups he spent");
        assertThrows(
                MissingOwnershipException.class,
                () -> player.pay(coins),
                "Player paid with coins he did not have"
        );

    }

    /**
     * This test covers the methods addMarks and addMark (which calls addMarks) in the following situations:
     * - adding up to three marks of the same player
     * - trying to add a fourth mark of the same player, which results in nothing
     * - adding another mark, but belonging to a different player
     */
    @Test
    void addMarks() {
        for (int i = 0; i < 4; i++) {
            player.addMark(new DamageToken(match.getActivePlayer()));
        }
        assertEquals(3, player.getMarks().size(), "Too many marks were added");
        match.endTurn();
        match.changeTurn();
        player.addMark(new DamageToken(match.getActivePlayer()));
        assertEquals(4, player.getMarks().size(), "Mark should have been added because it's from another player");
    }

    /**
     * This test covers the method getAvailableMacroActions in the following situations:
     * - player has less than 3 damage (2) in standard mode
     * - player has more than 3 damage (3, 5) but less than 6 in standard mode
     * - player has more than 6 damage (6) in standard mode
     * - player's turn is before the first player turn in final frenzy
     * - player's turn is after the first player turn in final frenzy
     * - player is the first player in final frenzy
     */
    @Test
    void getAvailableMacroActions() {
        List<DamageToken> tokens = new LinkedList<>();
        for (int i = 0; i < 2; i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }
        player.addDamageTokens(tokens);
        assertEquals(ActionTileFactory.create(ActionTile.Type.STANDARD), player.getAvailableMacroActions(), "Wrong action tile, should be STANDARD, but is " + player.getAvailableMacroActions().toString());
        player.addDamageToken(new DamageToken(match.getActivePlayer()));
        assertEquals(ActionTileFactory.create(ActionTile.Type.ADRENALINE_1), player.getAvailableMacroActions(), "Wrong action tile, should be ADRENALINE_1, but is " + player.getAvailableMacroActions().toString());
        tokens.clear();
        for (int i = 0; i < 2; i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }
        player.addDamageTokens(tokens);
        assertEquals(ActionTileFactory.create(ActionTile.Type.ADRENALINE_1), player.getAvailableMacroActions(), "Wrong action tile, should be ADRENALINE_1, but is " + player.getAvailableMacroActions().toString());
        player.addDamageToken(new DamageToken(match.getActivePlayer()));
        assertEquals(ActionTileFactory.create(ActionTile.Type.ADRENALINE_2), player.getAvailableMacroActions(), "Wrong action tile, should be ADRENALINE_2, but is " + player.getAvailableMacroActions().toString());
        player = match.getPlayers().get(3);
        match.endTurn();
        match.changeTurn(); //Player 1 is active
        tokens.clear();
        for (int i = 0; i < 11; i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }
        player.addDamageTokens(tokens);
        match.endTurn();
        match.changeTurn(); //Player 2 is active
        tokens.clear();
        for (int i = 0; i < 11; i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }
        match.endTurn();
        match.changeTurn(); // Player 3 is active
        match.endTurn();
        match.changeTurn(); // Player 4 is active
        tokens.clear();
        for (int i = 0; i < 11; i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }
        match.endTurn();
        match.changeTurn(); // Player 0 is active
        tokens.clear();
        for (int i = 0; i < 11; i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }
        match.endTurn();
        match.changeTurn(); // Player 1 is active
        tokens.clear();
        for (int i = 0; i < 11; i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }
        match.endTurn();
        player.bringBackToLife();
        match.changeTurn();
        //Player 3 is dead, final frenzy is triggered. Player 0 and Player 1 should only have 1 action, player 2, 3 and 4 should have 2
        for (Player p : match.getPlayers().subList(2, 5)) {
            assertEquals(
                    ActionTileFactory.create(ActionTile.Type.FINAL_FRENZY_DOUBLE_MODE),
                    p.getAvailableMacroActions(), "Wrong action tile, should be FINAL_FRENZY_DOUBLE_MODE, but is " + player.getAvailableMacroActions().toString()
            );
            match.endTurn();
            match.changeTurn();
        }

        for (Player p : match.getPlayers().subList(0, 2)) {
            assertEquals(
                    ActionTileFactory.create(ActionTile.Type.FINAL_FRENZY_SINGLE_MODE),
                    p.getAvailableMacroActions(), "Wrong action tile, should be FINAL_FRENZY_SINGLE_MODE, but is " + player.getAvailableMacroActions().toString()
            );
            match.endTurn();
            match.changeTurn();
        }

    }
}