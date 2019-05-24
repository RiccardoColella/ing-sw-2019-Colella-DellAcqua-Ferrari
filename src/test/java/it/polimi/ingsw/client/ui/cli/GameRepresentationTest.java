package it.polimi.ingsw.client.ui.cli;

import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.player.PlayerColor;
import it.polimi.ingsw.shared.datatransferobjects.PlayerHealth;
import it.polimi.ingsw.shared.datatransferobjects.Powerup;
import it.polimi.ingsw.shared.events.networkevents.MatchStarted;
import it.polimi.ingsw.shared.datatransferobjects.Player;
import it.polimi.ingsw.shared.datatransferobjects.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

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

        this.gameRepresentation = new GameRepresentation(new MatchStarted(
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
            gameRepresentation.playerLocations.put(player, new Point(i + 1, i));
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
    void positPlayers() {
        List<String> boardWithPlayers = new LinkedList<>();
        boardWithPlayers.add("┌──────────────────┬──────────────────┬──────────────────┐                    \n");
        boardWithPlayers.add("│BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB│                    \n");
        boardWithPlayers.add("│B                    \u001B[33mPlayer0\u001B[0m                           B│                    \n");
        boardWithPlayers.add("│B                                                      B│                    \n");
        boardWithPlayers.add("│B                                                      B│                    \n");
        boardWithPlayers.add("│B                                                      B│                    \n");
        boardWithPlayers.add("│BBBBBB      BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB      BBBBBB│                    \n");
        boardWithPlayers.add("├──────      ──────┼──────────────────┼──────      ──────┼──────────────────┐ \n");
        boardWithPlayers.add("│RRRRRR      RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR      RRRRRR│YYYYYYYYYYYYYYYYYY│ \n");
        boardWithPlayers.add("│R                                                      R│                 Y│ \n");
        boardWithPlayers.add("│R                                       \u001B[32mPlayer1\u001B[0m                           Y│ \n");
        boardWithPlayers.add("│R                                                                         Y│ \n");
        boardWithPlayers.add("│R                                                      R│Y                Y│ \n");
        boardWithPlayers.add("│RRRRRRRRRRRRRRRRRRRRRRRRR      RRRRRRRRRRRRRRRRRRRRRRRRR│Y                Y│ \n");
        boardWithPlayers.add("└──────────────────┼──────      ──────┼──────────────────┼─Y              Y─┤ \n");
        boardWithPlayers.add("                   │KKKKKK      KKKKKKKKKKKKKKKKKKKKKKKKK│Y                Y│ \n");
        boardWithPlayers.add("                   │K                                   K│Y                Y│ \n");
        boardWithPlayers.add("                   │K                                                      Y│ \n");
        boardWithPlayers.add("                   │K                                       \u001B[35mPlayer2\u001B[0m        Y│ \n");
        boardWithPlayers.add("                   │K                                   K│Y                Y│ \n");
        boardWithPlayers.add("                   │KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK│YYYYYYYYYYYYYYYYYY│ \n");
        boardWithPlayers.add("                   └──────────────────┴──────────────────┴──────────────────┘ \n");
        //assertEquals(boardWithPlayers, gameRepresentation.positPlayers(gameRepresentation.getBoard()));
    }

    @Test
    void positSpawnpointsWeapons() {
        List<String> boardWithWeapons = new LinkedList<>();
        boardWithWeapons.add("┌──────────────────┬──────────────────┬──────────────────┐                    \n");
        boardWithWeapons.add("│BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB SPAWN BLUE BBBBBB│                   \u001B[34m BLUESPAWNPOINT\u001B[0m\n");
        boardWithWeapons.add("│B                                                      B│                    - weaponTop1\n");
        boardWithWeapons.add("│B                                                      B│                    \n");
        boardWithWeapons.add("│B                                                      B│                    \n");
        boardWithWeapons.add("│B                                                      B│                    \n");
        boardWithWeapons.add("│BBBBBB      BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB      BBBBBB│                    \n");
        boardWithWeapons.add("├──────      ──────┼──────────────────┼──────      ──────┼──────────────────┐ \n");
        boardWithWeapons.add("│RRRRRR      RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR      RRRRRR│YYYYYYYYYYYYYYYYYY│\u001B[31m REDSPAWNPOINT\u001B[0m\n");
        boardWithWeapons.add("│R                                                      R│                 Y│ - weaponLeft1\n");
        boardWithWeapons.add("│R                                                                         Y│ \n");
        boardWithWeapons.add("│R                                                                         Y│ \n");
        boardWithWeapons.add("│R                                                      R│Y                Y│ \n");
        boardWithWeapons.add("│ SPAWN RED RRRRRRRRRRRRRR      RRRRRRRRRRRRRRRRRRRRRRRRR│Y                Y│ \n");
        boardWithWeapons.add("└──────────────────┼──────      ──────┼──────────────────┼─Y              Y─┤ \n");
        boardWithWeapons.add("                   │KKKKKK      KKKKKKKKKKKKKKKKKKKKKKKKK│Y                Y│\u001B[33m YELLOWSPAWNPOINT\u001B[0m\n");
        boardWithWeapons.add("                   │K                                   K│Y                Y│ - weaponRight1\n");
        boardWithWeapons.add("                   │K                                                      Y│ \n");
        boardWithWeapons.add("                   │K                                                      Y│ \n");
        boardWithWeapons.add("                   │K                                   K│Y                Y│ \n");
        boardWithWeapons.add("                   │KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK│YYYYY SPAWN YELLOW│ \n");
        boardWithWeapons.add("                   └──────────────────┴──────────────────┴──────────────────┘ \n");
        //assertEquals(boardWithWeapons, gameRepresentation.positSpawnpointsWeapons(gameRepresentation.getBoard()));
    }

    @Test
    void positPlayerInfo() {
        List<String> boardWithPlayerInfo = new LinkedList<>();
        boardWithPlayerInfo.add("┌──────────────────┬──────────────────┬──────────────────┐                    \n");
        boardWithPlayerInfo.add("│BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB SPAWN BLUE BBBBBB│                    \n");
        boardWithPlayerInfo.add("│B                                                      B│                    \n");
        boardWithPlayerInfo.add("│B                                                      B│                    \n");
        boardWithPlayerInfo.add("│B                                                      B│                    \n");
        boardWithPlayerInfo.add("│B                                                      B│                    \n");
        boardWithPlayerInfo.add("│BBBBBB      BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB      BBBBBB│                    \n");
        boardWithPlayerInfo.add("├──────      ──────┼──────────────────┼──────      ──────┼──────────────────┐ \n");
        boardWithPlayerInfo.add("│RRRRRR      RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR      RRRRRR│YYYYYYYYYYYYYYYYYY│ \n");
        boardWithPlayerInfo.add("│R                                                      R│                 Y│ \n");
        boardWithPlayerInfo.add("│R                                                                         Y│ \n");
        boardWithPlayerInfo.add("│R                                                                         Y│ \n");
        boardWithPlayerInfo.add("│R                                                      R│Y                Y│ \n");
        boardWithPlayerInfo.add("│ SPAWN RED RRRRRRRRRRRRRR      RRRRRRRRRRRRRRRRRRRRRRRRR│Y                Y│ \n");
        boardWithPlayerInfo.add("└──────────────────┼──────      ──────┼──────────────────┼─Y              Y─┤ \n");
        boardWithPlayerInfo.add("                   │KKKKKK      KKKKKKKKKKKKKKKKKKKKKKKKK│Y                Y│ \n");
        boardWithPlayerInfo.add("                   │K                                   K│Y                Y│ \n");
        boardWithPlayerInfo.add("                   │K                                                      Y│ \n");
        boardWithPlayerInfo.add("                   │K                                                      Y│ \n");
        boardWithPlayerInfo.add("                   │K                                   K│Y                Y│ \n");
        boardWithPlayerInfo.add("                   │KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK│YYYYY SPAWN YELLOW│ \n");
        boardWithPlayerInfo.add("                   └──────────────────┴──────────────────┴──────────────────┘ \n");
        boardWithPlayerInfo.add("\n");
        boardWithPlayerInfo.add(" - \u001B[33mPlayer0\u001B[0m. \u001B[33mYELLOW\u001B[0m. Skulls: 8. Damages: 0. Marks: 5 \u001B[43mY\u001B[0m\u001B[42mG\u001B[0m\u001B[45mP\u001B[0m\u001B[30mB\u001B[0m\u001B[46mT\u001B[0m\n");
        boardWithPlayerInfo.add("\twallet| lw: loadedWeapon1. uw: unloadedWeapon1. uw: unloadedWeapon2. \n");
        boardWithPlayerInfo.add("\t      | Ammocubes: |\u001B[44m B \u001B[0m|\u001B[41m R \u001B[0m|\u001B[43m Y \u001B[0m| p: \u001B[34mpowerupBLUE0\u001B[0m. p: \u001B[31mpowerupRED0\u001B[0m. p: \u001B[33mpowerupYELLOW0\u001B[0m. \n");
        boardWithPlayerInfo.add("\n");
        boardWithPlayerInfo.add(" - \u001B[32mPlayer1\u001B[0m. \u001B[32mGREEN\u001B[0m. Skulls: 8. Damages: 5. \u001B[43mY\u001B[0m\u001B[42mG\u001B[0m\u001B[45mP\u001B[0m\u001B[30mB\u001B[0m\u001B[46mT\u001B[0m. Marks: 5 \u001B[43mY\u001B[0m\u001B[42mG\u001B[0m\u001B[45mP\u001B[0m\u001B[30mB\u001B[0m\u001B[46mT\u001B[0m\n");
        boardWithPlayerInfo.add("\twallet| lw: loadedWeapon1. uw: unloadedWeapon1. uw: unloadedWeapon2. \n");
        boardWithPlayerInfo.add("\t      | Ammocubes: |\u001B[44m B \u001B[0m|\u001B[41m R \u001B[0m|\u001B[43m Y \u001B[0m| p: \u001B[34mpowerupBLUE1\u001B[0m. p: \u001B[31mpowerupRED1\u001B[0m. p: \u001B[33mpowerupYELLOW1\u001B[0m. \n");
        boardWithPlayerInfo.add("\n");
        boardWithPlayerInfo.add(" - \u001B[35mPlayer2\u001B[0m. \u001B[35mPURPLE\u001B[0m. Skulls: 8. Damages: 0. Marks: 5 \u001B[43mY\u001B[0m\u001B[42mG\u001B[0m\u001B[45mP\u001B[0m\u001B[30mB\u001B[0m\u001B[46mT\u001B[0m\n");
        boardWithPlayerInfo.add("\twallet| lw: loadedWeapon1. uw: unloadedWeapon1. uw: unloadedWeapon2. \n");
        boardWithPlayerInfo.add("\t      | Ammocubes: |\u001B[44m B \u001B[0m|\u001B[41m R \u001B[0m|\u001B[43m Y \u001B[0m| p: \u001B[34mpowerupBLUE2\u001B[0m. p: \u001B[31mpowerupRED2\u001B[0m. p: \u001B[33mpowerupYELLOW2\u001B[0m. \n");
        //assertEquals(boardWithPlayerInfo, gameRepresentation.positPlayerInfo(gameRepresentation.getBoard()));
    }

    @Test
    void movePlayer() {
    }

    @Test
    void setPlayerWallet() {
    }

    @Test
    void updatePlayerHealth() {
    }

    @Test
    void setPlayerWeaponLoaded() {
    }

    @Test
    void setPlayerWeaponUnloaded() {
    }

    @Test
    void grabPlayerWeapon() {
    }

    @Test
    void dropPlayerWeapon() {
    }

    @Test
    void movePlayer1() {
    }

}