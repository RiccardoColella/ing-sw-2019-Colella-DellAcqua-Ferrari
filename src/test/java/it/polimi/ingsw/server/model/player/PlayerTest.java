package it.polimi.ingsw.server.model.player;

import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.currency.*;
import it.polimi.ingsw.server.model.events.PlayerDamaged;
import it.polimi.ingsw.server.model.events.PlayerDied;
import it.polimi.ingsw.server.model.events.PlayerOverkilled;
import it.polimi.ingsw.server.model.events.listeners.PlayerListener;
import it.polimi.ingsw.server.model.exceptions.MissingOwnershipException;
import it.polimi.ingsw.server.model.exceptions.UnauthorizedExchangeException;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.match.MatchFactory;
import it.polimi.ingsw.server.model.weapons.WeaponTile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Match match;
    private Player player;

    @BeforeEach
    void setUp() {
        //creating a match with 5 skulls in standard mode with 5 players
        this.match = MatchFactory.create(
                IntStream
                        .range(0, 5)
                        .boxed()
                        .map(i -> "Player" + i)
                        .collect(Collectors.toList()),
                BoardFactory.Preset.BOARD_1,
                5,
                Match.Mode.STANDARD
        );
        this.player = match.getPlayers().get(4); //setting a default player
        this.match.getPlayers().forEach(p -> this.match.getBoard().getSpawnpoint(CurrencyColor.BLUE).addPlayer(p));
    }

    @AfterEach
    void tearDown() {
        //removing the players from the board
        this.match.getPlayers().forEach(p -> this.match.getBoard().getSpawnpoint(CurrencyColor.BLUE).removePlayer(p));
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
        //creating 3 damage tokens for the target
        for (int i = 0; i < 3; i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }

        int expectedTokens = player.getDamageTokens().size();
        player.addDamageTokens(tokens);
        expectedTokens += tokens.size();

        //the target should receive all 3 damage tokens
        assertEquals(expectedTokens, player.getDamageTokens().size(), "Not all damage was added"); //checking all damage has been added

        //giving a mark to the target
        player.addMark(new DamageToken(match.getActivePlayer()));

        //dealing new damage to the target
        player.addDamageToken(new DamageToken(match.getActivePlayer()));

        expectedTokens += 2; // the mark belonging to the attacker will turn into damage

        assertEquals(expectedTokens, player.getDamageTokens().size(), "The mark did not turn into damage");

        match.endTurn();
        match.changeTurn();

        //another player gives a mark to the target
        player.addMark(new DamageToken(match.getActivePlayer()));

        match.endTurn();
        match.changeTurn();

        //the turn has changed again, another player deals damage to the target
        player.addDamageToken(new DamageToken(match.getActivePlayer()));

        expectedTokens += 1; // the mark should not be added because it is not from the same player

        assertEquals(expectedTokens, player.getDamageTokens().size(), "Damage was not added correctly");

        tokens.clear();

        //changing the target will allow this test to be compatible with a much lower mortal damage
        player = match.getPlayers().get(0);
        int remainingShots = player.getConstraints().getMortalDamage() - player.getDamageTokens().size() - 1;
        for (int i = 0; i < remainingShots; i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }

        player.addDamageTokens(tokens);
        expectedTokens = player.getConstraints().getMortalDamage() - 1;
        assertTrue(player.isAlive(), "Player died too soon");
        assertEquals(expectedTokens, player.getDamageTokens().size(), "Damage was not added correctly");

        match.endTurn();
        match.changeTurn();

        player.addDamageToken(new DamageToken(match.getActivePlayer())); //the target receives the mortal shot
        expectedTokens++;

        assertFalse(player.isAlive(), "Player is still alive but should not be");
        assertEquals(expectedTokens, player.getDamageTokens().size(), "Damage was not added correctly");

        tokens.clear();

        for (int i = 0; i < player.getConstraints().getMaxDamage() - player.getConstraints().getMortalDamage() + 1; i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }

        player.addDamageTokens(tokens);
        expectedTokens++; //player cannot receive more than MaxDamage damages

        assertFalse(player.isAlive(), "Player somehow came back to life on his own");
        assertEquals(expectedTokens, player.getDamageTokens().size(), "Too much damage was added, max is " + player.getConstraints().getMaxDamage());
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
        //making sure the player under test actually owns a weapon
        player = match.getActivePlayer();
        while (player.getWeapons().isEmpty()) {
            Optional<WeaponTile> weapon = match.getWeaponDeck().pick();
            if (weapon.isPresent()) {
                player.grabAmmoCubes(weapon.get().getAcquisitionCost());
                player.grabWeapon(weapon.get(), weapon.get().getAcquisitionCost(), new ArrayList<>());
            } else { //if there are no more weapons in the deck, someone must have them right now
                match.endTurn();
                match.changeTurn();
                player = match.getActivePlayer();
            }
        }

        //CHOOSING A WEAPON
        player.chooseWeapon(player.getWeapons().get(0)); //the player has at least 1 weapon
        player.getWeapons().get(0).setLoaded(true); //making sure that weapon is loaded
        assertEquals(player.getWeapons().get(0), player.getActiveWeapon().orElse(null), "Player could not choose his weapon");
        player.putAwayActiveWeapon();

        //CHOOSING AN UNLOADED WEAPON
        player.getWeapons().get(0).setLoaded(false); //making sure that weapon is unloaded
        player.chooseWeapon(player.getWeapons().get(0)); //trying to choose an unloaded weapon
        assertNull(player.getActiveWeapon().orElse(null), "Unloaded weapon cannot be chosen"); // the weapon can't be active because it is not loaded

        //CHOOSING A WEAPON THAT DOES NOT BELONG TO THE PLAYER
        Optional<WeaponTile> notOwnedWeapon = match.getWeaponDeck().pick();
        if (!notOwnedWeapon.isPresent()) { //if the deck was empty, someone else has the weapons
            for (Player p : match.getPlayers()) {
                if (p != player && !p.getWeapons().isEmpty()) {
                    notOwnedWeapon = Optional.of(p.getWeapons().get(0));
                }
            }
        }
        if (notOwnedWeapon.isPresent()) {
            player.chooseWeapon(notOwnedWeapon.get()); //trying to choose a weapon the player does not own as active won't succeed at setting it as active
            assertNull(player.getActiveWeapon().orElse(null), "Player tried to chose a weapon that did not belong to him"); // the weapon can't be active because it is not owned by the player
        }
    }

    /**
     * This test covers the method grabWeapon in the following situations:
     * - grabbing a weapon with enough money
     * - trying to grab a weapon the player can't afford
     * - trying to discard a weapon when the player can still buy more weapons
     * - grabbing a weapon while already owning the max, but selecting a weapon to discard
     * - trying to grab a weapon while already owning the max without discarding a weapon
     * - trying to grab a weapon while already owning the max and discarding a not-owned weapon
     */
    @Test
    void grabWeapon() {
        // FILLING UP THE WALLET SO THAT THE PLAYER CAN BUY A WEAPON
        player.pay(player.getAmmoCubes().stream().map(a -> (Coin) a).collect(Collectors.toList()));
        Optional<WeaponTile> currentWeapon = match.getWeaponDeck().pick();
        int playerAmmoCubes = player.getAmmoCubes().size();

        //if there is a weapon available to buy and the player can buy more, let's buy one
        if (currentWeapon.isPresent() && player.getConstraints().getMaxWeaponsForPlayer() >= player.getWeapons().size() + 1) {
            player.grabAmmoCubes(currentWeapon.get().getAcquisitionCost());
            playerAmmoCubes += currentWeapon.get().getAcquisitionCost().size();
            //BUYING AN AFFORDABLE WEAPON WHILE HAVING LESS THAN MAX WEAPONS
            player.grabWeapon(
                    currentWeapon.get(),
                    currentWeapon.get().getAcquisitionCost(),
                    new LinkedList<>()
            );
            playerAmmoCubes -= currentWeapon.get().getAcquisitionCost().size();
            assertEquals(playerAmmoCubes, player.getAmmoCubes().size(), "Player did not pay correctly");
            assertTrue(player.getWeapons().contains(currentWeapon.get()), "Player did not get the weapon he paid for");
        }

        //TRYING TO BUY A WEAPON THE PLAYER CAN'T AFFORD
        do {
            currentWeapon = match.getWeaponDeck().pick();
        } while (currentWeapon.isPresent() && currentWeapon.get().getAcquisitionCost().isEmpty()); //selecting a weapon that is not free

        //if a non-free weapon was found and the player can buy more, the player will try to buy a new weapon without paying
        if (currentWeapon.isPresent() && player.getConstraints().getMaxWeaponsForPlayer() >= player.getWeapons().size() + 1) {
            WeaponTile costlyWeapon = currentWeapon.get();
            assertThrows(MissingOwnershipException.class,
                    () -> player.grabWeapon(
                            costlyWeapon,
                            costlyWeapon.getAcquisitionCost(),
                            new LinkedList<>()
                    ), "Player bought a weapon without paying");
            assertFalse(player.getWeapons().contains(costlyWeapon), "Player got the weapon although he did not pay for it");
            assertEquals(playerAmmoCubes, player.getAmmoCubes().size(), "Player lost some ammo even if the purchase did not happen");

            //trying to discard a weapon while having less than the maximum allowed
            WeaponTile ownedWeapon = player.getWeapons().get(0);
            assertThrows(IllegalArgumentException.class,
                    () -> player.grabWeapon(
                            costlyWeapon,
                            costlyWeapon.getAcquisitionCost(),
                            new LinkedList<>(),
                            ownedWeapon
                    ), "Player discarded a weapon when he did not have the maximum allowed");
            assertFalse(player.getWeapons().contains(costlyWeapon), "Player got the weapon although he did not pay for it");
            assertEquals(playerAmmoCubes, player.getAmmoCubes().size(), "Player lost some ammo even if the purchase did not happen");
            assertTrue(player.getWeapons().contains(ownedWeapon), "WeaponTile should not have been discarded");

            //FILLING UP THE WALLET TO BUY MORE WEAPONS
            player.grabAmmoCubes(costlyWeapon.getAcquisitionCost());
            playerAmmoCubes += costlyWeapon.getAcquisitionCost().size();

            //actually paying for the weapon
            player.grabWeapon(
                    costlyWeapon,
                    costlyWeapon.getAcquisitionCost(),
                    new LinkedList<>()
            );

            playerAmmoCubes -= costlyWeapon.getAcquisitionCost().size();
            assertTrue(player.getWeapons().contains(costlyWeapon), "Player did not get the weapon he paid for");
            assertEquals(playerAmmoCubes, player.getAmmoCubes().size(), "Player did not pay the right amount of ammo");
        }

        currentWeapon = match.getWeaponDeck().pick();
        while (currentWeapon.isPresent() && player.getConstraints().getMaxWeaponsForPlayer() >= player.getWeapons().size() + 1) {

            //FILLING UP THE WALLET TO BUY AS MANY WEAPONS AS POSSIBLE
            player.grabAmmoCubes(currentWeapon.get().getAcquisitionCost());
            playerAmmoCubes += currentWeapon.get().getAcquisitionCost().size();

            //just a normal purchase to get to the max number allowed
            player.grabWeapon(
                    currentWeapon.get(),
                    currentWeapon.get().getAcquisitionCost(),
                    new LinkedList<>()
            );

            playerAmmoCubes -= currentWeapon.get().getAcquisitionCost().size();
            assertEquals(playerAmmoCubes, player.getAmmoCubes().size(), "Player did not pay the right amount of ammo");
            assertTrue(player.getWeapons().contains(currentWeapon.get()), "Player did not get the weapon he paid for");

            currentWeapon = match.getWeaponDeck().pick();
        }

        //if there still are weapons to be bought, the player now has to discard an owned weapon to buy a new one
        if (currentWeapon.isPresent()) {
            // PLAYER CAN'T BUY ANY MORE WEAPONS WITHOUT DISCARDING AN OLD ONE
            player.grabAmmoCubes(currentWeapon.get().getAcquisitionCost());
            playerAmmoCubes += currentWeapon.get().getAcquisitionCost().size();
            WeaponTile extraWeapon = currentWeapon.get();
            //not discarding a weapon at this point will result in an exception
            assertThrows(UnauthorizedExchangeException.class, () -> player.grabWeapon(extraWeapon, extraWeapon.getAcquisitionCost(), new LinkedList<>()), "Player grabbed a weapon exceeding his limit");
            assertFalse(player.getWeapons().contains(extraWeapon), "The weapon was grabbed even if the purchase did not end correctly");
            assertEquals(playerAmmoCubes, player.getAmmoCubes().size(), "Player could pay for a weapon that he was not allowed to grab");

            //TRYING TO BUY A FOURTH WEAPON DISCARDING A WEAPON THAT DOES NOT BELONG TO THE PLAYER
            //the player can't cheat the system and pretend to discard a weapon that isn't his
            assertThrows(IllegalArgumentException.class, () -> player.grabWeapon(extraWeapon, new LinkedList<>(), new LinkedList<>(), extraWeapon), "Player grabbed a fourth weapon");

            //the player follows the rules and discards a weapon
            WeaponTile toDiscard = player.getWeapons().get(0);
            player.grabWeapon(
                    extraWeapon,
                    extraWeapon.getAcquisitionCost(),
                    new LinkedList<>(),
                    toDiscard
            );
            playerAmmoCubes -= currentWeapon.get().getAcquisitionCost().size();
            assertEquals(playerAmmoCubes, player.getAmmoCubes().size(), "Player did not pay the right amount of ammo");
            assertTrue(player.getWeapons().contains(currentWeapon.get()), "Player did not get the weapon he paid for");
            assertFalse(player.getWeapons().contains(toDiscard), "Player still owns the weapon he discarded"); // the discarded weapon does not belong to the player anymore
        }

    }

    /**
     * This test covers the method grabPowerup in the following situations:
     * - grabbing up to 3 powerups
     * - trying to buy a fourth powerup, which is not allowed
     */
    @Test
    void grabPowerup() {
        this.player = match.getActivePlayer();

        //GRABBING THE MAX ALLOWED POWERUPS
        for (int i = player.getPowerups().size(); i < this.player.getConstraints().getMaxPowerupsForPlayer(); i++) {
            Optional<PowerupTile> powerup = match.getPowerupDeck().pick();
            powerup.ifPresent(powerupTile -> this.player.grabPowerup(powerupTile));
        }
        //NOW PLAYER HAS AS MANY POWERUPS AS HE CAN
        //the deck of powerups cannot run out of tiles, so the player will have definitely grabbed all the powerups he asked for
        //if that is not the case, game constraints have been deeply violated and the test should fail anyway
        assertEquals(this.player.getConstraints().getMaxPowerupsForPlayer(), player.getPowerups().size(), "Player did not grab all powerups");
        //GRABBING ANOTHER POWERUP IS NOT ALLOWED
        Optional<PowerupTile> unallowedPowerup = match.getPowerupDeck().pick();

        if (unallowedPowerup.isPresent()) {
            assertThrows(UnauthorizedExchangeException.class, () -> this.player.grabPowerup(unallowedPowerup.get()), "Player grabbed too many powerups");
            //PLAYER STILL HAS THE MAX ALLOWED NUMBER OF POWERUPS
            assertEquals(this.player.getConstraints().getMaxPowerupsForPlayer(), player.getPowerups().size(), "Player grabbed too many powerups");
        }
    }

    /**
     * This test covers the method grabAmmoCubes in the following situations:
     * - grabbing up to the max number of ammoCubes allowed of each color
     * - trying to grab more ammoCubes when player already has the maximum allowed of that color
     */
    @Test
    void grabAmmoCubes() {
        this.player = match.getActivePlayer();
        this.player.pay(this.player.getAmmoCubes().stream().map(a -> (Coin) a).collect(Collectors.toList()));
        List<AmmoCube> redAmmoCubes = new LinkedList<>();
        List<AmmoCube> blueAmmoCubes = new LinkedList<>();
        List<AmmoCube> yellowAmmoCubes = new LinkedList<>();

        for (int i = 0; i < player.getConstraints().getMaxAmmoCubesOfAColor(); i++) {
            redAmmoCubes.add(AmmoCubeFactory.create(CurrencyColor.RED));
            blueAmmoCubes.add(AmmoCubeFactory.create(CurrencyColor.BLUE));
            yellowAmmoCubes.add(AmmoCubeFactory.create(CurrencyColor.YELLOW));
        }

        int expectedAmmoCubes = player.getAmmoCubes().size();

        //PLAYER GRABS MAX RED AMMO CUBES AND MAX BLUE AMMO CUBES
        this.player.grabAmmoCubes(redAmmoCubes);
        expectedAmmoCubes += redAmmoCubes.size();
        assertEquals(expectedAmmoCubes, player.getAmmoCubes().size());
        this.player.grabAmmoCubes(blueAmmoCubes);
        expectedAmmoCubes += blueAmmoCubes.size();
        assertEquals(expectedAmmoCubes, player.getAmmoCubes().size(), "Player did not grab all ammoCubes");

        //PLAYER ALREADY HAS MAX RED AMMO CUBES
        this.player.grabAmmoCubes(redAmmoCubes);
        assertEquals(expectedAmmoCubes, player.getAmmoCubes().size(), "Player grabbed too many red ammoCubes");

        //PLAYER GRABS MAX YELLOW AMMO CUBES
        this.player.grabAmmoCubes(yellowAmmoCubes);
        expectedAmmoCubes += yellowAmmoCubes.size();
        assertEquals(expectedAmmoCubes, player.getAmmoCubes().size(), "Player did not grab all ammoCubes");

        //PLAYER CAN'T GRAB ANYTHING ANYMORE
        this.player.grabAmmoCubes(yellowAmmoCubes);
        this.player.grabAmmoCubes(blueAmmoCubes);
        assertEquals(expectedAmmoCubes, player.getAmmoCubes().size(), "Player grabbed too many ammoCubes");
    }

    /**
     * This test covers the method reload in the following situations:
     * - reloading a weapon and paying in ammoCubes
     * - trying to reload a weapon with money that is not owned
     */
    @Test
    void reload() {
        //making sure the player has a weapon
        player = match.getActivePlayer();
        while (player.getWeapons().isEmpty()) {
            Optional<WeaponTile> weapon = match.getWeaponDeck().pick();
            if (weapon.isPresent()) {
                player.grabAmmoCubes(weapon.get().getAcquisitionCost());
                player.grabWeapon(weapon.get(), weapon.get().getAcquisitionCost(), new ArrayList<>());
            } else {
                match.endTurn();
                match.changeTurn();
                player = match.getActivePlayer();
            }
        }

        WeaponTile toBeLoaded = player.getWeapons().get(0);
        //making sure the weapon is not loaded
        toBeLoaded.setLoaded(false);

        int oldAmmoCubes = player.getAmmoCubes().size();
        //making sure the player can afford reloading the weapon
        player.grabAmmoCubes(toBeLoaded.getReloadCost());
        //PAYMENT WITH AMMO CUBES
        player.reload(toBeLoaded, toBeLoaded.getReloadCost(), new LinkedList<>());
        assertTrue(toBeLoaded.isLoaded(), "Player could not reload");
        assertEquals(oldAmmoCubes, player.getAmmoCubes().size(), "Player did not pay");

        toBeLoaded.setLoaded(false);
        //making sure the player's wallet is empty
        if (!player.getAmmoCubes().isEmpty()) {
            player.pay(player.getAmmoCubes().stream().map(a -> (Coin) a).collect(Collectors.toList()));
        }
        //now the player does not have any money to reload his weapon, so it will remain unloaded
        assertThrows(
                MissingOwnershipException.class,
                () -> player.reload(toBeLoaded, toBeLoaded.getReloadCost(), new LinkedList<>()),
                "Player did not have enough money to reload this weapon, but exception was not thrown. Player ammos: " + player.getAmmoCubes().size() + ", cost: " + toBeLoaded.getReloadCost().size()
        );
        assertFalse(toBeLoaded.isLoaded());
    }

    /** This test covers the method onMatchModeChanged in the following situations:
     * - standard: final frenzy for player with 0 damage (should flip the board)
     * - standard: final frenzy for player with damage (should not flip the board)
     */
    @Test
    void onMatchModeChanged() {
        List<DamageToken> tokens = new LinkedList<>();
        while (match.getRemainingSkulls() > 0) {
            //filling up tokens with enough damage tokens to kill a player
            for (int i = 0; i < player.getConstraints().getMortalDamage(); i++) {
                tokens.add(new DamageToken(match.getActivePlayer()));
            }
            if (!player.equals(match.getActivePlayer())) {
                player.addDamageTokens(tokens);
                List<Player> dead = match.endTurn();
                assertEquals(player, dead.get(0), "Player should be dead"); //making sure the player died
                for (Player d : dead) {
                    d.bringBackToLife();
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
        //5 players have died, final frenzy is triggered (the match is initialized to have 5 skulls in this test)
        assertEquals(Match.Mode.FINAL_FRENZY, match.getMode());
        for (Player p : match.getPlayers()) {
            if (p.equals(player) || match.getPlayers().indexOf(p) == 0) {
                //these players have no tokens when final frenzy is triggered, so they flip their board
                assertTrue(p.getDamageTokens().isEmpty(), p.getPlayerInfo().getNickname() + " has tokens but should not");
                assertNotEquals(9, p.getCurrentReward().getRewardFor(0, true), "Player does not have final frenzy reward, but should " + p.getPlayerInfo().getNickname());
            } else {
                //these players have some tokens when final frenzy is triggered, so they must not flip their board
                assertFalse(p.getDamageTokens().isEmpty(), p.getPlayerInfo().getNickname() + " does not have tokens but should");
                assertEquals(9, p.getCurrentReward().getRewardFor(0, true), "Player has final frenzy reward, but should not " + p.getPlayerInfo().getNickname());
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
        //enough tokens to kill a player
        for (int i = 0; i < player.getConstraints().getMortalDamage(); i++) {
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
     * - paying with both ammoCubes and powerups
     * - trying to pay with an empty wallet
     */
    @Test
    void pay() {
        //making sure the player spends all his money
        player.pay(player.getAmmoCubes().stream().map(a -> (Coin) a).collect(Collectors.toList()));
        player.pay(player.getPowerups().stream().map(p -> (Coin) p).collect(Collectors.toList()));
        assertTrue(player.getPowerups().isEmpty(), "Powerup wallet should be empty after this payment");
        assertTrue(player.getAmmoCubes().isEmpty(), "Ammo wallet should be empty after this payment");

        //giving the player powerups and ammo cubes
        List<Coin> coins = new LinkedList<>();
        for (int i = 0; i < 2; i++) {
            AmmoCube ammoCube = AmmoCubeFactory.create(CurrencyColor.RED);
            Optional<PowerupTile> powerupTile = match.getPowerupDeck().pick();
            if (powerupTile.isPresent()) {
                coins.add(ammoCube);
                coins.add(powerupTile.get());
                player.grabPowerup(powerupTile.get());
                player.grabAmmoCubes(Collections.singletonList(ammoCube));
            } else {
                fail("Wrong powerupdeck configuration");
            }
        }
        //making the player spend all his money
        player.pay(coins);
        assertTrue(player.getAmmoCubes().isEmpty(), "Player still has the coins he spent");
        assertTrue(player.getPowerups().isEmpty(), "Player still has the powerups he spent");

        //making the player pay money he does not own will result in an exception
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
        int expectedMarks = player.getMarks().size();
        int previousFromThatPlayer = (int) player.getMarks().stream().filter(m -> m.getAttacker() == match.getActivePlayer()).count();
        expectedMarks += previousFromThatPlayer;
        //giving all the possible marks from the same player
        for (int i = previousFromThatPlayer; i < player.getConstraints().getMaxMarksFromPlayer(); i++) {
            player.addMark(new DamageToken(match.getActivePlayer()));
            expectedMarks++;
        }
        //giving a mark that exceeds the max will not actually add it
        player.addMark(new DamageToken(match.getActivePlayer()));
        assertEquals(expectedMarks, player.getMarks().size(), "Too many marks were added");

        //finding a player that can give more marks to the target
        do {
            match.endTurn();
            match.changeTurn();
        } while (player.getMarks().stream().filter(m -> m.getAttacker() == match.getActivePlayer()).count() == player.getConstraints().getMaxMarksFromPlayer());

        player.addMark(new DamageToken(match.getActivePlayer()));
        expectedMarks++;
        //making sure the mark has been added, as it belongs to a different attacker
        assertEquals(expectedMarks, player.getMarks().size(), "Mark should have been added because it's from another player");
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
        for (int i = 0; i < player.getConstraints().getFirstAdrenalineTrigger() - 1; i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }
        player.addDamageTokens(tokens);
        //player has less damage than required to trigger adrenaline for the first time
        assertEquals(ActionTileFactory.create(ActionTile.Type.STANDARD), player.getAvailableMacroActions(), "Wrong action tile, should be STANDARD, but is " + player.getAvailableMacroActions().toString());
        player.addDamageToken(new DamageToken(match.getActivePlayer()));
        //player triggers adrenaline for the first time
        assertEquals(ActionTileFactory.create(ActionTile.Type.ADRENALINE_1), player.getAvailableMacroActions(), "Wrong action tile, should be ADRENALINE_1, but is " + player.getAvailableMacroActions().toString());
        tokens.clear();
        for (int i = 0; i < player.getConstraints().getSecondAdrenalineTrigger() - player.getDamageTokens().size() - 1; i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }
        player.addDamageTokens(tokens);
        //player has less damage than required to trigger adrenaline for the second time
        assertEquals(ActionTileFactory.create(ActionTile.Type.ADRENALINE_1), player.getAvailableMacroActions(), "Wrong action tile, should be ADRENALINE_1, but is " + player.getAvailableMacroActions().toString());
        player.addDamageToken(new DamageToken(match.getActivePlayer()));
        //player triggers adrenaline for the second time
        assertEquals(ActionTileFactory.create(ActionTile.Type.ADRENALINE_2), player.getAvailableMacroActions(), "Wrong action tile, should be ADRENALINE_2, but is " + player.getAvailableMacroActions().toString());

        //killing enough players to trigger final frenzy
        player = match.getPlayers().get(3);
        match.endTurn();
        match.changeTurn(); //Player 1 is active
        tokens.clear();
        for (int i = 0; i < player.getConstraints().getMortalDamage(); i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }
        player.addDamageTokens(tokens);
        match.endTurn();
        match.changeTurn(); //Player 2 is active
        tokens.clear();
        for (int i = 0; i < player.getConstraints().getMortalDamage(); i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }
        match.endTurn();
        match.changeTurn(); // Player 3 is active
        match.endTurn();
        match.changeTurn(); // Player 4 is active
        tokens.clear();
        for (int i = 0; i < player.getConstraints().getMortalDamage(); i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }
        match.endTurn();
        match.changeTurn(); // Player 0 is active
        tokens.clear();
        for (int i = 0; i < player.getConstraints().getMortalDamage(); i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }
        match.endTurn();
        match.changeTurn(); // Player 1 is active
        tokens.clear();
        for (int i = 0; i < player.getConstraints().getMortalDamage(); i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }
        match.endTurn();
        player.bringBackToLife();
        match.changeTurn();
        //Player 3 is dead, final frenzy is triggered. Player 0 and Player 1 should only have 1 action, player 2, 3 and 4 should have 2 because their turn is before that of player 0
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