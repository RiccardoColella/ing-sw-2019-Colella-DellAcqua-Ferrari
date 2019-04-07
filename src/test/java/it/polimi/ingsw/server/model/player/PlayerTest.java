package it.polimi.ingsw.server.model.player;

import it.polimi.ingsw.server.model.DamageToken;
import it.polimi.ingsw.server.model.Damageable;
import it.polimi.ingsw.server.model.Match;
import it.polimi.ingsw.server.model.currency.Ammo;
import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.events.PlayerDied;
import it.polimi.ingsw.server.model.exceptions.MissingOwnershipException;
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

    static {
        reloadCost = new LinkedList<>();
        reloadCost.add(AmmoFactory.create(CurrencyColor.BLUE));
        reloadCost.add(AmmoFactory.create(CurrencyColor.RED));
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
            Weapon.Name.CYBERBLADE,
            prototypeAttack,
            new ArrayList<>(Collections.singleton(AmmoFactory.create(CurrencyColor.RED))),
            reloadCost
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
        player.chooseWeapon(prototypeFreeWeapon);
        assertEquals(prototypeFreeWeapon, player.getActiveWeapon().orElse(null));
        player.putAwayActiveWeapon();
        prototypeFreeWeapon.setLoaded(false);
        player.chooseWeapon(prototypeFreeWeapon);
        assertNull(player.getActiveWeapon().orElse(null)); // the weapon can't be active because it is not loaded
        player.chooseWeapon(prototypeCostlyWeapon);
        assertNull(player.getActiveWeapon().orElse(null)); // the weapon can't be active because it is not owned by the player
    }

    @Test
    void grabWeapon() {
        player.grabAmmos(reloadCost.stream().map(coin -> (Ammo) coin).collect(Collectors.toList()));
        assertTrue(player.getAmmos().stream().anyMatch(a -> a.getColor() == CurrencyColor.RED));
        assertTrue(player.getAmmos().stream().anyMatch(a -> a.getColor() == CurrencyColor.BLUE));
        assertEquals(2, player.getAmmos().size());

        player.grabWeapon(prototypeCostlyWeapon, prototypeCostlyWeapon.getAcquisitionCost().stream().map(c -> (Ammo) c).collect(Collectors.toList()), new LinkedList<>());

        assertEquals(1, player.getAmmos().size());
        assertTrue(player.getWeapons().contains(prototypeCostlyWeapon));
        assertTrue(player.getAmmos().stream().anyMatch(a -> a.getColor() == CurrencyColor.BLUE));
    }

    @Test
    void grabWeapon1() {
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