package it.polimi.ingsw.server.model.player;

import it.polimi.ingsw.server.model.DamageToken;
import it.polimi.ingsw.server.model.Damageable;
import it.polimi.ingsw.server.model.Match;
import it.polimi.ingsw.server.model.currency.Ammo;
import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.exceptions.MissingOwnershipException;
import it.polimi.ingsw.server.model.exceptions.UnauthorizedGrabException;
import it.polimi.ingsw.server.model.factories.*;
import it.polimi.ingsw.server.controller.weapons.Attack;
import it.polimi.ingsw.server.model.weapons.Weapon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    private static Weapon electroscythe;
    private static Weapon furnace;
    private static Weapon flamethrower;
    private static Weapon heatseeker;
    static {
        try {
            electroscythe = WeaponFactory.create(Weapon.Name.ELECTROSCYTHE);
            furnace = WeaponFactory.create(Weapon.Name.FURNACE);
            heatseeker = WeaponFactory.create(Weapon.Name.HEATSEEKER);
            flamethrower = WeaponFactory.create(Weapon.Name.FLAMETHROWER);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Match match;
    private Player player;



    @BeforeEach
    void setUp() throws FileNotFoundException {
        List<PlayerInfo> playerInfos = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            playerInfos.add(new PlayerInfo("Player" + i, PlayerColor.values()[i]));
        }
        this.match = MatchFactory.create(playerInfos, BoardFactory.Preset.BOARD_10, 5, Match.Mode.STANDARD);
        this.player = match.getPlayers().get(4);
    }

    @Test
    void addDamageTokens() {
        List<DamageToken> tokens = new LinkedList<>();

        for (int i = 0; i < 3; i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }

        int expectedTokens = player.getDamageTokens().size();
        player.addDamageTokens(tokens);
        expectedTokens += tokens.size();
        assertEquals(expectedTokens, player.getDamageTokens().size()); //checking all damage has been added

        player.addMark(new DamageToken(match.getActivePlayer()));

        player.addDamageToken(new DamageToken(match.getActivePlayer()));

        expectedTokens += 2; // the mark belonging to the attacker will turn into damage

        assertEquals(expectedTokens, player.getDamageTokens().size());

        match.endTurn();
        match.changeTurn();

        player.addMark(new DamageToken(match.getActivePlayer()));

        match.endTurn();
        match.changeTurn();

        player.addDamageToken(new DamageToken(match.getActivePlayer()));

        expectedTokens += 1; // the mark should not be added because it is not from the same player

        assertEquals(expectedTokens, player.getDamageTokens().size());

        tokens.clear();

        for (int i = 0; i < 4; i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }

        player.addDamageTokens(tokens);
        expectedTokens += tokens.size();
        assertTrue(player.isAlive());
        assertEquals(expectedTokens, player.getDamageTokens().size());

        match.endTurn();
        match.changeTurn();

        player.addDamageToken(new DamageToken(match.getActivePlayer()));
        expectedTokens++;

        assertFalse(player.isAlive()); // player dies with eleven tokens
        assertEquals(expectedTokens, player.getDamageTokens().size());

        tokens.clear();

        for (int i = 0; i < 2; i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }

        player.addDamageTokens(tokens);
        expectedTokens++; //player can only receive overkill at this point

        assertFalse(player.isAlive());
        assertEquals(expectedTokens, player.getDamageTokens().size());
        List<Player> dead = match.endTurn();
        assertEquals(dead.get(0), player);

    }

    @Test
    void chooseWeapon() {
        player = match.getActivePlayer();
        player.grabAmmos(electroscythe.getAcquisitionCost());
        player.grabWeapon(electroscythe, electroscythe.getAcquisitionCost(), new ArrayList<>());

        //CHOOSING A WEAPON
        player.chooseWeapon(electroscythe);
        assertEquals(electroscythe, player.getActiveWeapon().orElse(null));
        player.putAwayActiveWeapon();

        //CHOOSING AN UNLOADED WEAPON
        electroscythe.setLoaded(false);
        player.chooseWeapon(electroscythe);
        assertNull(player.getActiveWeapon().orElse(null)); // the weapon can't be active because it is not loaded

        //CHOOSING A WEAPON THAT DOES NOT BELONG TO THE PLAYER
        player.chooseWeapon(heatseeker);
        assertNull(player.getActiveWeapon().orElse(null)); // the weapon can't be active because it is not owned by the player
    }

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
        assertEquals(playerAmmos, player.getAmmos().size());
        assertTrue(player.getWeapons().contains(electroscythe));

        //BUYING A FREE WEAPON
        player.grabAmmos(flamethrower.getAcquisitionCost());
        playerAmmos += flamethrower.getAcquisitionCost().size();
        player.grabWeapon(flamethrower, flamethrower.getAcquisitionCost(), new LinkedList<>());
        playerAmmos -= flamethrower.getAcquisitionCost().size();
        assertTrue(player.getWeapons().contains(flamethrower));
        assertEquals(playerAmmos, player.getAmmos().size());

        //TRYING TO BUY A WEAPON THE PLAYER CAN'T AFFORD
        assertThrows(MissingOwnershipException.class,
                () -> player.grabWeapon(
                heatseeker,
                heatseeker.getAcquisitionCost(),
                new LinkedList<>()
        ));
        assertFalse(player.getWeapons().contains(heatseeker));
        assertEquals(playerAmmos, player.getAmmos().size());

        //FILLING UP THE WALLET TO BUY MORE WEAPONS
        player.grabAmmos(heatseeker.getAcquisitionCost());
        assertTrue(player.getAmmos().containsAll(heatseeker.getAcquisitionCost()));
        playerAmmos += heatseeker.getAcquisitionCost().size();

        //BUYING THE THIRD WEAPON
        player.grabWeapon(
                heatseeker,
                heatseeker.getAcquisitionCost(),
                new LinkedList<>()
        );

        playerAmmos -= heatseeker.getAcquisitionCost().size();
        assertTrue(player.getWeapons().contains(heatseeker));
        assertEquals(playerAmmos, player.getAmmos().size());

        //FILLING UP THE WALLET TO BUY THE FOURTH WEAPON
        player.grabAmmos(furnace.getAcquisitionCost());
        assertTrue(player.getAmmos().containsAll(furnace.getAcquisitionCost()));
        playerAmmos += furnace.getAcquisitionCost().size();

        //BUYING THE FOURTH WEAPON DISCARDING A WEAPON
        player.grabWeapon(
                furnace,
                furnace.getAcquisitionCost(),
                new LinkedList<>(),
                electroscythe
        );

        playerAmmos -= furnace.getAcquisitionCost().size();
        assertEquals(playerAmmos, player.getAmmos().size());
        assertTrue(player.getWeapons().contains(furnace));
        assertFalse(player.getWeapons().contains(electroscythe)); // the discarded weapon does not belong to the player anymore

        //REFILLING THE WALLET
        player.grabAmmos(electroscythe.getAcquisitionCost());
        playerAmmos += electroscythe.getAcquisitionCost().size();

        //TRYING TO BUY A FOURTH WEAPON WITHOUT DISCARDING ONE
        assertThrows(UnauthorizedGrabException.class, () -> player.grabWeapon(electroscythe, new LinkedList<>(), new LinkedList<>()));
        assertFalse(player.getWeapons().contains(electroscythe));
        assertEquals(playerAmmos, player.getAmmos().size());
    }

    @Test
    void grabPowerup() {
        this.player = match.getActivePlayer();

        //GRABBING THE THREE ALLOWED POWERUPS
        this.player.grabPowerup(PowerupTileFactory.create(PowerupTile.Type.TELEPORTER, CurrencyColor.RED));
        this.player.grabPowerup(PowerupTileFactory.create(PowerupTile.Type.NEWTON, CurrencyColor.YELLOW));
        this.player.grabPowerup(PowerupTileFactory.create(PowerupTile.Type.TELEPORTER, CurrencyColor.BLUE));

        //NOW PLAYER HAS 3 POWERUPS
        assertEquals(3, player.getPowerups().size());

        //GRABBING A FOURTH POWERUPS IS NOT ALLOWED
        assertThrows(UnauthorizedGrabException.class, () -> this.player.grabPowerup(PowerupTileFactory.create(PowerupTile.Type.TAGBACK_GRENADE, CurrencyColor.BLUE)));

        //PLAYER STILL HAS 3 POWERUPS
        assertEquals(3, player.getPowerups().size());
    }

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
        assertEquals(blueAmmos.size() + redAmmos.size(), player.getAmmos().size());

        //PLAYER ALREADY HAS 3 RED AMMOS
        this.player.grabAmmos(redAmmos);
        assertEquals(blueAmmos.size() + redAmmos.size(), player.getAmmos().size());

        //PLAYER GRABS 3 YELLOW AMMOS
        this.player.grabAmmos(yellowAmmos);
        assertEquals(redAmmos.size() + blueAmmos.size() + yellowAmmos.size(), player.getAmmos().size());

        //PLAYER CAN'T GRAB ANYTHING ANYMORE
        this.player.grabAmmos(yellowAmmos);
        this.player.grabAmmos(blueAmmos);
        assertEquals(redAmmos.size() + blueAmmos.size() + yellowAmmos.size(), player.getAmmos().size());
    }

    @Test
    void reload() {
        player.grabAmmos(furnace.getAcquisitionCost());
        player.grabWeapon(furnace, furnace.getAcquisitionCost(), new LinkedList<>());
        furnace.setLoaded(false);
        player.grabAmmos(furnace.getReloadCost());
        player.getAmmos().forEach(ammo -> {
            player.grabPowerup(PowerupTileFactory.create(PowerupTile.Type.NEWTON, ammo.getColor()));
        });

        //PAYMENT WITH AMMOS
        player.reload(furnace, furnace.getReloadCost(), new LinkedList<>());

        assertTrue(furnace.isLoaded());
        assertTrue(player.getAmmos().isEmpty());

        furnace.setLoaded(false);

        //TRYING PAYMENT WITH POWERUPS
        player.reload(furnace, new LinkedList<>(), furnace.getReloadCost().stream().map(ammo -> PowerupTileFactory.create(PowerupTile.Type.NEWTON, ammo.getColor())).collect(Collectors.toList()));
        assertTrue(furnace.isLoaded());
        assertTrue(player.getPowerups().isEmpty());


        furnace.setLoaded(false);
        player.grabAmmos(furnace.getReloadCost());
        player.getAmmos().forEach(ammo -> {
            player.grabPowerup(PowerupTileFactory.create(PowerupTile.Type.NEWTON, ammo.getColor()));
        });

        //TRYING PAYMENT WITH POWERUPS - FAILING BECAUSE POWERUPS AREN'T OF THE SAME TYPE
        assertThrows(
                MissingOwnershipException.class,
                () -> player.reload(
                        furnace,
                        new LinkedList<>(),
                        furnace.getReloadCost().stream().map(ammo -> PowerupTileFactory.create(PowerupTile.Type.TELEPORTER, ammo.getColor())).collect(Collectors.toList())
                )
        );
        assertFalse(furnace.isLoaded());
        assertFalse(player.getPowerups().isEmpty());

        //TRYING MIXED PAYMENT
        player.reload(furnace, player.getAmmos().subList(0, 1), player.getPowerups().subList(1, 2));
        assertTrue(furnace.isLoaded());
        assertEquals(1, player.getAmmos().size());
        assertEquals(1, player.getAmmos().size());
    }

    @Test
    void onMatchModeChanged() {
    }

    @Test
    void bringBackToLife() {
    }
}