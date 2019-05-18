package it.polimi.ingsw.client.ui.cli;

import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.player.PlayerColor;
import it.polimi.ingsw.shared.datatransferobjects.PlayerHealth;
import it.polimi.ingsw.shared.events.networkevents.MatchStarted;
import it.polimi.ingsw.shared.datatransferobjects.Player;
import it.polimi.ingsw.shared.datatransferobjects.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.*;
import java.util.List;

class GameRepresentationTest {
    GameRepresentation gameRepresentation;
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
                weaponLeft));
    }

    private Player buildPlayer(int x){
        String name = "Player" + x;
        List<PlayerColor> colors = new LinkedList<>(Arrays.asList(PlayerColor.values()));
        return new Player(
                name,
                colors.get(x),
                new Wallet(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList()),
                new PlayerHealth(0, Collections.emptyList(), Collections.emptyList())
        );
    }

    @Test
    void positPlayersTest() {
        int i = 0;
        for (Player player : gameRepresentation.getPlayers()){
            gameRepresentation.playerLocations.put(player, new Point(i, i));
            i++;
        }
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