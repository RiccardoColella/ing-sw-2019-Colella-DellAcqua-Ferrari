package it.polimi.ingsw.client.ui.cli;

import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.player.PlayerColor;
import it.polimi.ingsw.shared.datatransferobjects.PlayerHealth;
import it.polimi.ingsw.shared.datatransferobjects.Powerup;
import it.polimi.ingsw.shared.events.networkevents.MatchStarted;
import it.polimi.ingsw.shared.datatransferobjects.Player;
import it.polimi.ingsw.shared.datatransferobjects.Wallet;
import it.polimi.ingsw.utils.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameRepresentationTest {
    private GameRepresentation gameRepresentation;
    @BeforeEach
    void setUp() {
        List<Player> opponents = new LinkedList<>();
        for (int i = 1; i < 3; i++){
            opponents.add(buildPlayer(i));
        }
        List<String> weaponTop = new LinkedList<>();
        weaponTop.add("weaponTop1");
        List<String> weaponLeft = new LinkedList<>();
        weaponLeft.add("weaponLeft1");
        List<String> weaponRight = new LinkedList<>();
        weaponRight.add("weaponRight1");

        this.gameRepresentation = GameRepresentationFactory.create(new MatchStarted(
                8,
                BoardFactory.Preset.BOARD_1,
                buildPlayer(0),
                opponents,
                weaponTop,
                weaponRight,
                weaponLeft,
                buildPlayer(0),
                new HashSet<>()
                ));
        int i = 0;
        for (Player player : gameRepresentation.getPlayers()){
            gameRepresentation.playerLocations.put(player, new Point(i, i + 1));
            gameRepresentation.setPlayerAlive(player);
            i++;
        }
    }

    private Player buildPlayer(int x){
        String name = "Player" + x;
        List<PlayerColor> colors = new LinkedList<>(Arrays.asList(PlayerColor.values()));
        List<String> loadedWeapons = new LinkedList<>();
        loadedWeapons.add("loadedWeapon1");
        List<String> unloadedWeapons = new LinkedList<>();
        unloadedWeapons.add("unloadedWeapon1");
        unloadedWeapons.add("unloadedWeapon2");
        List<CurrencyColor> ammoCubes = new LinkedList<>(Arrays.asList(CurrencyColor.values()));
        List<Powerup> powerups = new LinkedList<>();
        for (CurrencyColor currencyColor : CurrencyColor.values()){
            String powerupName = "powerup" + currencyColor.toString() + x;
            powerups.add(new Powerup(powerupName, currencyColor));
        }

        List<PlayerColor> damages = new LinkedList<>();
        if (x == 1){
            damages.addAll(Arrays.asList(PlayerColor.values()));
        }
        List<PlayerColor> marks = new LinkedList<>(Arrays.asList(PlayerColor.values()));

        return new Player(
                name,
                colors.get(x),
                new Wallet(loadedWeapons, unloadedWeapons, ammoCubes, powerups),
                new PlayerHealth(8, damages, marks),
                false,
                false
        );
    }

    @Test
    void positKillshots() {
        //Test without kill-shots
        List<Tuple<PlayerColor, Boolean>> killshots = new LinkedList<>();
        List<String> boardWithKillshots = new LinkedList<>();
        boardWithKillshots.add("┌──────────────────┬──────────────────┬──────────────────┐                    \n");
        boardWithKillshots.add("│ββββββββββββββββββββββββββββββββββββββ SPAWN BLUE ββββββ│                    \n");
        boardWithKillshots.add("│β                                                      β│                    \n");
        boardWithKillshots.add("│β                                                      β│                    \n");
        boardWithKillshots.add("│β                                                      β│                    \n");
        boardWithKillshots.add("│β                                                      β│                    \n");
        boardWithKillshots.add("│β                                                      β│                    \n");
        boardWithKillshots.add("│ββββββ      ββββββββββββββββββββββββββββββββ      ββββββ│                    \n");
        boardWithKillshots.add("├──────      ──────┼──────────────────┼──────      ──────┼──────────────────┐ \n");
        boardWithKillshots.add("│ρρρρρρ      ρρρρρρρρρρρρρρρρρρρρρρρρρρρρρρρρ      ρρρρρρ│ψψψψψψψψψψψψψψψψψψ│ \n");
        boardWithKillshots.add("│ρ                                                      ρ│                 ψ│ \n");
        boardWithKillshots.add("│ρ                                                                         ψ│ \n");
        boardWithKillshots.add("│ρ                                                                         ψ│ \n");
        boardWithKillshots.add("│ρ                                                                         ψ│ \n");
        boardWithKillshots.add("│ρ                                                      ρ│ψ                ψ│ \n");
        boardWithKillshots.add("│ SPAWN RED ρρρρρρρρρρρρρρ      ρρρρρρρρρρρρρρρρρρρρρρρρρ│ψ                ψ│ \n");
        boardWithKillshots.add("└──────────────────┼──────      ──────┼──────────────────┼─ψ              ψ─┤ \n");
        boardWithKillshots.add("                   │κκκκκκ      κκκκκκκκκκκκκκκκκκκκκκκκκ│ψ                ψ│ \n");
        boardWithKillshots.add("                   │κ                                   κ│ψ                ψ│ \n");
        boardWithKillshots.add("                   │κ                                                      ψ│ \n");
        boardWithKillshots.add("                   │κ                                                      ψ│ \n");
        boardWithKillshots.add("                   │κ                                                      ψ│ \n");
        boardWithKillshots.add("                   │κ                                   κ│ψ                ψ│ \n");
        boardWithKillshots.add("                   │κκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκ│ψψψψ SPAWN YELLOW │ \n");
        boardWithKillshots.add("                   └──────────────────┴──────────────────┴──────────────────┘ \n");
        boardWithKillshots.add(" - Killshots: [\u001B[1m--------\u001B[0m]");
        //assertEquals(boardWithKillshots, gameRepresentation.positKillshots(gameRepresentation.getBoard()));

        //Test with 2 kill-shots
        Tuple<PlayerColor, Boolean> tupleToAdd = new Tuple<>(PlayerColor.YELLOW, Boolean.TRUE);
        killshots.add(tupleToAdd);
        tupleToAdd = new Tuple<>(PlayerColor.GREEN, Boolean.FALSE);
        killshots.add(tupleToAdd);
        gameRepresentation.setKillshots(killshots);
        boardWithKillshots = new LinkedList<>();
        boardWithKillshots.add("┌──────────────────┬──────────────────┬──────────────────┐                    \n");
        boardWithKillshots.add("│ββββββββββββββββββββββββββββββββββββββ SPAWN BLUE ββββββ│                    \n");
        boardWithKillshots.add("│β                                                      β│                    \n");
        boardWithKillshots.add("│β                                                      β│                    \n");
        boardWithKillshots.add("│β                                                      β│                    \n");
        boardWithKillshots.add("│β                                                      β│                    \n");
        boardWithKillshots.add("│β                                                      β│                    \n");
        boardWithKillshots.add("│ββββββ      ββββββββββββββββββββββββββββββββ      ββββββ│                    \n");
        boardWithKillshots.add("├──────      ──────┼──────────────────┼──────      ──────┼──────────────────┐ \n");
        boardWithKillshots.add("│ρρρρρρ      ρρρρρρρρρρρρρρρρρρρρρρρρρρρρρρρρ      ρρρρρρ│ψψψψψψψψψψψψψψψψψψ│ \n");
        boardWithKillshots.add("│ρ                                                      ρ│                 ψ│ \n");
        boardWithKillshots.add("│ρ                                                                         ψ│ \n");
        boardWithKillshots.add("│ρ                                                                         ψ│ \n");
        boardWithKillshots.add("│ρ                                                                         ψ│ \n");
        boardWithKillshots.add("│ρ                                                      ρ│ψ                ψ│ \n");
        boardWithKillshots.add("│ SPAWN RED ρρρρρρρρρρρρρρ      ρρρρρρρρρρρρρρρρρρρρρρρρρ│ψ                ψ│ \n");
        boardWithKillshots.add("└──────────────────┼──────      ──────┼──────────────────┼─ψ              ψ─┤ \n");
        boardWithKillshots.add("                   │κκκκκκ      κκκκκκκκκκκκκκκκκκκκκκκκκ│ψ                ψ│ \n");
        boardWithKillshots.add("                   │κ                                   κ│ψ                ψ│ \n");
        boardWithKillshots.add("                   │κ                                                      ψ│ \n");
        boardWithKillshots.add("                   │κ                                                      ψ│ \n");
        boardWithKillshots.add("                   │κ                                                      ψ│ \n");
        boardWithKillshots.add("                   │κ                                   κ│ψ                ψ│ \n");
        boardWithKillshots.add("                   │κκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκ│ψψψψ SPAWN YELLOW │ \n");
        boardWithKillshots.add("                   └──────────────────┴──────────────────┴──────────────────┘ \n");
        boardWithKillshots.add(" - Killshots: [\u001B[43m\u001B[1mK\u001B[0m\u001B[42mk\u001B[0m\u001B[1m------\u001B[0m]");
        //assertEquals(boardWithKillshots, gameRepresentation.positKillshots(gameRepresentation.getBoard()));
    }

    @Test
    void positPlayers() {
        List<String> boardWithPlayers = new LinkedList<>();
        boardWithPlayers.add("┌──────────────────┬──────────────────┬──────────────────┐                    \n");
        boardWithPlayers.add("│ββββββββββββββββββββββββββββββββββββββ SPAWN BLUE ββββββ│                    \n");
        boardWithPlayers.add("│β                    \u001B[33m\u001B[1mPlayer0\u001B[0m                           β│                    \n");
        boardWithPlayers.add("│β                                                      β│                    \n");
        boardWithPlayers.add("│β                                                      β│                    \n");
        boardWithPlayers.add("│β                                                      β│                    \n");
        boardWithPlayers.add("│β                                                      β│                    \n");
        boardWithPlayers.add("│ββββββ      ββββββββββββββββββββββββββββββββ      ββββββ│                    \n");
        boardWithPlayers.add("├──────      ──────┼──────────────────┼──────      ──────┼──────────────────┐ \n");
        boardWithPlayers.add("│ρρρρρρ      ρρρρρρρρρρρρρρρρρρρρρρρρρρρρρρρρ      ρρρρρρ│ψψψψψψψψψψψψψψψψψψ│ \n");
        boardWithPlayers.add("│ρ                                                      ρ│                 ψ│ \n");
        boardWithPlayers.add("│ρ                                       \u001B[32m\u001B[1mPlayer1\u001B[0m                           ψ│ \n");
        boardWithPlayers.add("│ρ                                                                         ψ│ \n");
        boardWithPlayers.add("│ρ                                                                         ψ│ \n");
        boardWithPlayers.add("│ρ                                                      ρ│ψ                ψ│ \n");
        boardWithPlayers.add("│ SPAWN RED ρρρρρρρρρρρρρρ      ρρρρρρρρρρρρρρρρρρρρρρρρρ│ψ                ψ│ \n");
        boardWithPlayers.add("└──────────────────┼──────      ──────┼──────────────────┼─ψ              ψ─┤ \n");
        boardWithPlayers.add("                   │κκκκκκ      κκκκκκκκκκκκκκκκκκκκκκκκκ│ψ                ψ│ \n");
        boardWithPlayers.add("                   │κ                                   κ│ψ                ψ│ \n");
        boardWithPlayers.add("                   │κ                                                      ψ│ \n");
        boardWithPlayers.add("                   │κ                                       \u001B[35m\u001B[1mPlayer2\u001B[0m        ψ│ \n");
        boardWithPlayers.add("                   │κ                                                      ψ│ \n");
        boardWithPlayers.add("                   │κ                                   κ│ψ                ψ│ \n");
        boardWithPlayers.add("                   │κκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκ│ψψψψ SPAWN YELLOW │ \n");
        boardWithPlayers.add("                   └──────────────────┴──────────────────┴──────────────────┘ \n");
        //assertEquals(boardWithPlayers, gameRepresentation.positPlayers(gameRepresentation.getBoard()));
    }

    @Test
    void positSpawnpointsWeapons() {
        List<String> boardWithWeapons = new LinkedList<>();
        boardWithWeapons.add("┌──────────────────┬──────────────────┬──────────────────┐                    \n");
        boardWithWeapons.add("│ββββββββββββββββββββββββββββββββββββββ SPAWN BLUE ββββββ│                   \u001B[34m BLUESPAWNPOINT\u001B[0m\n");
        boardWithWeapons.add("│β                                                      β│                    - weaponTop1\n");
        boardWithWeapons.add("│β                                                      β│                    \n");
        boardWithWeapons.add("│β                                                      β│                    \n");
        boardWithWeapons.add("│β                                                      β│                    \n");
        boardWithWeapons.add("│β                                                      β│                    \n");
        boardWithWeapons.add("│ββββββ      ββββββββββββββββββββββββββββββββ      ββββββ│                    \n");
        boardWithWeapons.add("├──────      ──────┼──────────────────┼──────      ──────┼──────────────────┐ \n");
        boardWithWeapons.add("│ρρρρρρ      ρρρρρρρρρρρρρρρρρρρρρρρρρρρρρρρρ      ρρρρρρ│ψψψψψψψψψψψψψψψψψψ│\u001B[31m REDSPAWNPOINT\u001B[0m\n");
        boardWithWeapons.add("│ρ                                                      ρ│                 ψ│ - weaponLeft1\n");
        boardWithWeapons.add("│ρ                                                                         ψ│ \n");
        boardWithWeapons.add("│ρ                                                                         ψ│ \n");
        boardWithWeapons.add("│ρ                                                                         ψ│ \n");
        boardWithWeapons.add("│ρ                                                      ρ│ψ                ψ│ \n");
        boardWithWeapons.add("│ SPAWN RED ρρρρρρρρρρρρρρ      ρρρρρρρρρρρρρρρρρρρρρρρρρ│ψ                ψ│ \n");
        boardWithWeapons.add("└──────────────────┼──────      ──────┼──────────────────┼─ψ              ψ─┤ \n");
        boardWithWeapons.add("                   │κκκκκκ      κκκκκκκκκκκκκκκκκκκκκκκκκ│ψ                ψ│\u001B[33m YELLOWSPAWNPOINT\u001B[0m\n");
        boardWithWeapons.add("                   │κ                                   κ│ψ                ψ│ - weaponRight1\n");
        boardWithWeapons.add("                   │κ                                                      ψ│ \n");
        boardWithWeapons.add("                   │κ                                                      ψ│ \n");
        boardWithWeapons.add("                   │κ                                                      ψ│ \n");
        boardWithWeapons.add("                   │κ                                   κ│ψ                ψ│ \n");
        boardWithWeapons.add("                   │κκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκ│ψψψψ SPAWN YELLOW │ \n");
        boardWithWeapons.add("                   └──────────────────┴──────────────────┴──────────────────┘ \n");
        //assertEquals(boardWithWeapons, gameRepresentation.positSpawnpointsWeapons(gameRepresentation.getBoard()));
    }

    @Test
    void positPlayerInfo() {
        List<String> boardWithPlayerInfo = new LinkedList<>();
        boardWithPlayerInfo.add("┌──────────────────┬──────────────────┬──────────────────┐                    \n");
        boardWithPlayerInfo.add("│ββββββββββββββββββββββββββββββββββββββ SPAWN BLUE ββββββ│                    \n");
        boardWithPlayerInfo.add("│β                                                      β│                    \n");
        boardWithPlayerInfo.add("│β                                                      β│                    \n");
        boardWithPlayerInfo.add("│β                                                      β│                    \n");
        boardWithPlayerInfo.add("│β                                                      β│                    \n");
        boardWithPlayerInfo.add("│β                                                      β│                    \n");
        boardWithPlayerInfo.add("│ββββββ      ββββββββββββββββββββββββββββββββ      ββββββ│                    \n");
        boardWithPlayerInfo.add("├──────      ──────┼──────────────────┼──────      ──────┼──────────────────┐ \n");
        boardWithPlayerInfo.add("│ρρρρρρ      ρρρρρρρρρρρρρρρρρρρρρρρρρρρρρρρρ      ρρρρρρ│ψψψψψψψψψψψψψψψψψψ│ \n");
        boardWithPlayerInfo.add("│ρ                                                      ρ│                 ψ│ \n");
        boardWithPlayerInfo.add("│ρ                                                                         ψ│ \n");
        boardWithPlayerInfo.add("│ρ                                                                         ψ│ \n");
        boardWithPlayerInfo.add("│ρ                                                                         ψ│ \n");
        boardWithPlayerInfo.add("│ρ                                                      ρ│ψ                ψ│ \n");
        boardWithPlayerInfo.add("│ SPAWN RED ρρρρρρρρρρρρρρ      ρρρρρρρρρρρρρρρρρρρρρρρρρ│ψ                ψ│ \n");
        boardWithPlayerInfo.add("└──────────────────┼──────      ──────┼──────────────────┼─ψ              ψ─┤ \n");
        boardWithPlayerInfo.add("                   │κκκκκκ      κκκκκκκκκκκκκκκκκκκκκκκκκ│ψ                ψ│ \n");
        boardWithPlayerInfo.add("                   │κ                                   κ│ψ                ψ│ \n");
        boardWithPlayerInfo.add("                   │κ                                                      ψ│ \n");
        boardWithPlayerInfo.add("                   │κ                                                      ψ│ \n");
        boardWithPlayerInfo.add("                   │κ                                                      ψ│ \n");
        boardWithPlayerInfo.add("                   │κ                                   κ│ψ                ψ│ \n");
        boardWithPlayerInfo.add("                   │κκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκκ│ψψψψ SPAWN YELLOW │ \n");
        boardWithPlayerInfo.add("                   └──────────────────┴──────────────────┴──────────────────┘ \n");
        boardWithPlayerInfo.add(" - Killshots: [\u001B[1m--------\u001B[0m], \n");
        boardWithPlayerInfo.add(" - \u001B[33m\u001B[1mPlayer0\u001B[0m. \u001B[33mYELLOW\u001B[0m. Skulls: 8. Damages: 0. Marks: 5 \u001B[43mY\u001B[0m\u001B[42mG\u001B[0m\u001B[45mP\u001B[0m\u001B[30mB\u001B[0m\u001B[46mT\u001B[0m BoardFlipped: false ActionsTileFlipped: false\n");
        boardWithPlayerInfo.add("\twallet| lw: loadedWeapon1. uw: unloadedWeapon1. uw: unloadedWeapon2. \n");
        boardWithPlayerInfo.add("\t      | Ammocubes: |\u001B[44m B \u001B[0m|\u001B[41m R \u001B[0m|\u001B[43m Y \u001B[0m| p: \u001B[34mpowerupBLUE0\u001B[0m. p: \u001B[31mpowerupRED0\u001B[0m. p: \u001B[33mpowerupYELLOW0\u001B[0m. \n");
        boardWithPlayerInfo.add("\n");
        boardWithPlayerInfo.add(" - \u001B[32m\u001B[1mPlayer1\u001B[0m. \u001B[32mGREEN\u001B[0m. Skulls: 8. Damages: 5. \u001B[43mY\u001B[0m\u001B[42mG\u001B[0m\u001B[45mP\u001B[0m\u001B[30mB\u001B[0m\u001B[46mT\u001B[0m. Marks: 5 \u001B[43mY\u001B[0m\u001B[42mG\u001B[0m\u001B[45mP\u001B[0m\u001B[30mB\u001B[0m\u001B[46mT\u001B[0m BoardFlipped: false ActionsTileFlipped: false\n");
        boardWithPlayerInfo.add("\twallet| lw: loadedWeapon1. uw: unloadedWeapon1. uw: unloadedWeapon2. \n");
        boardWithPlayerInfo.add("\t      | Ammocubes: |\u001B[44m B \u001B[0m|\u001B[41m R \u001B[0m|\u001B[43m Y \u001B[0m| p: \u001B[34mpowerupBLUE1\u001B[0m. p: \u001B[31mpowerupRED1\u001B[0m. p: \u001B[33mpowerupYELLOW1\u001B[0m. \n");
        boardWithPlayerInfo.add("\n");
        boardWithPlayerInfo.add(" - \u001B[35m\u001B[1mPlayer2\u001B[0m. \u001B[35mPURPLE\u001B[0m. Skulls: 8. Damages: 0. Marks: 5 \u001B[43mY\u001B[0m\u001B[42mG\u001B[0m\u001B[45mP\u001B[0m\u001B[30mB\u001B[0m\u001B[46mT\u001B[0m BoardFlipped: false ActionsTileFlipped: false\n");
        boardWithPlayerInfo.add("\twallet| lw: loadedWeapon1. uw: unloadedWeapon1. uw: unloadedWeapon2. \n");
        boardWithPlayerInfo.add("\t      | Ammocubes: |\u001B[44m B \u001B[0m|\u001B[41m R \u001B[0m|\u001B[43m Y \u001B[0m| p: \u001B[34mpowerupBLUE2\u001B[0m. p: \u001B[31mpowerupRED2\u001B[0m. p: \u001B[33mpowerupYELLOW2\u001B[0m. \n");
        //assertEquals(boardWithPlayerInfo, gameRepresentation.positPlayerInfo(gameRepresentation.getBoard()));
    }
}