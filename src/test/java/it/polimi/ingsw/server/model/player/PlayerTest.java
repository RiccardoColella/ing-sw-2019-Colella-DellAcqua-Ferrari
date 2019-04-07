package it.polimi.ingsw.server.model.player;

import it.polimi.ingsw.server.model.DamageToken;
import it.polimi.ingsw.server.model.Damageable;
import it.polimi.ingsw.server.model.Match;
import it.polimi.ingsw.server.model.currency.Ammo;
import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.events.PlayerDied;
import it.polimi.ingsw.server.model.exceptions.MissingOwnershipException;
import it.polimi.ingsw.server.model.exceptions.UnauthorizedGrabException;
import it.polimi.ingsw.server.model.factories.AmmoFactory;
import it.polimi.ingsw.server.model.factories.BoardFactory;
import it.polimi.ingsw.server.model.factories.MatchFactory;
import it.polimi.ingsw.server.model.weapons.Attack;
import it.polimi.ingsw.server.model.weapons.Weapon;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private static List<Coin> reloadCost;
    private static List<Coin> reloadCost1;
    private static List<Coin> reloadCost2;
    private static List<Coin> acquisitionCost;
    private static List<Coin> acquisitionCost1;
    private static List<Coin> acquisitionCost2;

    static {
        reloadCost = new LinkedList<>();
        acquisitionCost = new LinkedList<>();
        acquisitionCost.add(AmmoFactory.create(CurrencyColor.RED));
        reloadCost.add(AmmoFactory.create(CurrencyColor.BLUE));
        reloadCost.add(AmmoFactory.create(CurrencyColor.RED));

        reloadCost1 = new LinkedList<>();
        acquisitionCost1 = new LinkedList<>();
        acquisitionCost1.add(AmmoFactory.create(CurrencyColor.RED));
        acquisitionCost1.add(AmmoFactory.create(CurrencyColor.YELLOW));
        reloadCost1.add(AmmoFactory.create(CurrencyColor.RED));
        reloadCost1.add(AmmoFactory.create(CurrencyColor.YELLOW));
        reloadCost1.add(AmmoFactory.create(CurrencyColor.YELLOW));

        reloadCost2 = new LinkedList<>();
        acquisitionCost2 = new LinkedList<>();
        acquisitionCost2.add(AmmoFactory.create(CurrencyColor.BLUE));
        reloadCost2.add(AmmoFactory.create(CurrencyColor.BLUE));
        reloadCost2.add(AmmoFactory.create(CurrencyColor.YELLOW));
    }

    private Match match;
    private Player player;
    private Attack prototypeAttack = new Attack() {
        @Override
        public List<List<Damageable>> getTargets(TargetType type) {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public List<Coin> getCost() {
            return null;
        }

        @Override
        public List<Damageable> execute(Player attacker, Map<TargetType, List<Damageable>> targets) {
            return null;
        }

        @Override
        public TargetType getSupportedTargetTypes() {
            return null;
        }
    };
    private Weapon prototypeFreeWeapon = new Weapon(Weapon.Name.CYBERBLADE, prototypeAttack, new ArrayList<>(), new ArrayList<>());
    private Weapon prototypeCostlyWeapon = new Weapon(
            Weapon.Name.ELECTROSCYTHE,
            prototypeAttack,
            acquisitionCost,
            reloadCost
    );

    private Weapon prototypeCostlyWeapon1 = new Weapon(
            Weapon.Name.FLAMETHROWER,
            prototypeAttack,
            acquisitionCost1,
            reloadCost1
    );

    private Weapon prototypeCostlyWeapon2 = new Weapon(
            Weapon.Name.FURNACE,
            prototypeAttack,
            acquisitionCost2,
            reloadCost2
    );

    @BeforeEach
    void setUp() {
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

        match.changeTurn();

        player.addMark(new DamageToken(match.getActivePlayer()));

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

    }

    @Test
    void chooseWeapon() {
        player = match.getActivePlayer();
        player.grabWeapon(prototypeFreeWeapon, new ArrayList<>(), new ArrayList<>());

        //CHOOSING A WEAPON
        player.chooseWeapon(prototypeFreeWeapon);
        assertEquals(prototypeFreeWeapon, player.getActiveWeapon().orElse(null));
        player.putAwayActiveWeapon();

        //CHOOSING AN UNLOADED WEAPON
        prototypeFreeWeapon.setLoaded(false);
        player.chooseWeapon(prototypeFreeWeapon);
        assertNull(player.getActiveWeapon().orElse(null)); // the weapon can't be active because it is not loaded

        //CHOOSING A WEAPON THAT DOES NOT BELONG TO THE PLAYER
        player.chooseWeapon(prototypeCostlyWeapon);
        assertNull(player.getActiveWeapon().orElse(null)); // the weapon can't be active because it is not owned by the player
    }

    @Test
    void grabWeapon() {
        // FILLING UP THE WALLET SO THAT THE PLAYER CAN BUY A WEAPON
        player.grabAmmos(reloadCost.stream().map(coin -> (Ammo) coin).collect(Collectors.toList()));
        assertTrue(player.getAmmos().stream().anyMatch(a -> a.getColor() == CurrencyColor.RED));
        assertTrue(player.getAmmos().stream().anyMatch(a -> a.getColor() == CurrencyColor.BLUE));
        assertEquals(2, player.getAmmos().size());
        int playerAmmos = player.getAmmos().size();

        //BUYING AN AFFORDABLE WEAPON WHILE HAVING LESS THAN 3 WEAPONS
        player.grabWeapon(
                prototypeCostlyWeapon,
                prototypeCostlyWeapon.getAcquisitionCost().stream().map(c -> (Ammo) c).collect(Collectors.toList()),
                new LinkedList<>()
        );
        playerAmmos -= prototypeCostlyWeapon.getAcquisitionCost().size();
        assertEquals(playerAmmos, player.getAmmos().size());
        assertTrue(player.getWeapons().contains(prototypeCostlyWeapon));
        assertTrue(player.getAmmos().stream().anyMatch(a -> a.getColor() == CurrencyColor.BLUE));

        //BUYING A FREE WEAPON
        player.grabWeapon(prototypeFreeWeapon, new LinkedList<>(), new LinkedList<>());
        assertTrue(player.getWeapons().contains(prototypeFreeWeapon));
        assertEquals(playerAmmos, player.getAmmos().size());

        //TRYING TO BUY A WEAPON THE PLAYER CAN'T AFFORD
        assertThrows(MissingOwnershipException.class,
                () -> player.grabWeapon(
                prototypeCostlyWeapon1,
                prototypeCostlyWeapon1.getAcquisitionCost().stream().map(coin -> (Ammo) coin).collect(Collectors.toList()),
                new LinkedList<>()
        ));
        assertFalse(player.getWeapons().contains(prototypeCostlyWeapon1));
        assertEquals(playerAmmos, player.getAmmos().size());

        //FILLING UP THE WALLET TO BUY MORE WEAPONS
        player.grabAmmos(acquisitionCost1.stream().map(coin -> (Ammo) coin).collect(Collectors.toList()));
        assertTrue(player.getAmmos().containsAll(acquisitionCost1));
        playerAmmos += acquisitionCost1.size();

        //BUYING THE THIRD WEAPON
        player.grabWeapon(
                prototypeCostlyWeapon1,
                prototypeCostlyWeapon1.getAcquisitionCost().stream().map(coin -> (Ammo) coin).collect(Collectors.toList()),
                new LinkedList<>()
        );

        playerAmmos -= prototypeCostlyWeapon1.getAcquisitionCost().size();
        assertTrue(player.getWeapons().contains(prototypeCostlyWeapon1));
        assertEquals(playerAmmos, player.getAmmos().size());

        //FILLING UP THE WALLET TO BUY THE FOURTH WEAPON
        player.grabAmmos(acquisitionCost2.stream().map(coin -> (Ammo) coin).collect(Collectors.toList()));
        assertTrue(player.getAmmos().containsAll(acquisitionCost2));
        playerAmmos += acquisitionCost2.size();

        //BUYING THE FOURTH WEAPON DISCARDING A WEAPON
        player.grabWeapon(
                prototypeCostlyWeapon2,
                prototypeCostlyWeapon2.getAcquisitionCost().stream().map(coin -> (Ammo) coin).collect(Collectors.toList()),
                new LinkedList<>(),
                prototypeFreeWeapon
        );

        playerAmmos -= prototypeCostlyWeapon2.getAcquisitionCost().size();
        assertEquals(playerAmmos, player.getAmmos().size());
        assertTrue(player.getWeapons().contains(prototypeCostlyWeapon2));
        assertFalse(player.getWeapons().contains(prototypeFreeWeapon)); // the discarded weapon does not belong to the player anymore

        //TRYING TO BUY A FOURTH WEAPON WITHOUT DISCARDING ONE
        assertThrows(UnauthorizedGrabException.class, () -> player.grabWeapon(prototypeFreeWeapon, new LinkedList<>(), new LinkedList<>()));
        assertFalse(player.getWeapons().contains(prototypeFreeWeapon));
        assertEquals(playerAmmos, player.getAmmos().size());
    }

    @Test
    void grabPowerup() {
    }

    @Test
    void grabAmmos() {
    }

    @Test
    void reload() {
    }

    @Test
    void shoot() {
    }

    @Test
    void onMatchModeChanged() {
    }

    @Test
    void bringBackToLife() {
    }
}