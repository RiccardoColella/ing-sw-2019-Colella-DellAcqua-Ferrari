package it.polimi.ingsw.client.ui.cli;

import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.player.PlayerColor;
import it.polimi.ingsw.shared.events.networkevents.MatchStarted;
import it.polimi.ingsw.shared.viewmodels.Player;
import it.polimi.ingsw.shared.viewmodels.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        return new Player(name, colors.get(x), new Wallet());
    }

    @Test
    void printEmptyBoard() {
        gameRepresentation.printEmptyBoard(System.out);
    }

    @Test
    void positPlayersTest() {
        int i = 0;
        for (Player player : gameRepresentation.getPlayers()){
            player.setLocation(new Point(i, i));
            i++;
        }
        gameRepresentation.printBoard(
                gameRepresentation.positPlayers(),
                System.out
        );
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
    void printEmptyBoard1() {
    }

    @Test
    void movePlayer1() {
    }
}