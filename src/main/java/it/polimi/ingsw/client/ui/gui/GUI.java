package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.client.io.Connector;
import it.polimi.ingsw.client.viewmodels.Player;
import it.polimi.ingsw.client.viewmodels.Wallet;
import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.player.PlayerColor;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * This class represents the graphical interface of the game
 *
 * @author Adriana Ferrari, Carlo Dell'Acqua
 */
public class GUI extends Application {

    public GUI() {
    }

    @Override
    public void start(Stage stage) {
        List<Player> opponents = new LinkedList<>();
        opponents.add(new Player("avv1", PlayerColor.YELLOW, new Wallet()));
        opponents.add(new Player("avv2", PlayerColor.PURPLE, new Wallet()));
        opponents.add(new Player("avv3", PlayerColor.TURQUOISE, new Wallet()));
        opponents.add(new Player("avv4", PlayerColor.GREEN, new Wallet()));
        new GameController(BoardFactory.Preset.BOARD_1, new Player("me", PlayerColor.GRAY, new Wallet()), opponents).showAsModal();

/*
        LoginController controller = new LoginController();
        controller.showAsModal();
        Optional<Connector> connector = controller.getConnector();
        if (connector.isPresent()) {
            // TODO: pass the connector
            // TODO: call the intitialization modal
            new GameController(BoardFactory.Preset.BOARD_1).showAsModal();
        } else {
            Platform.exit();
        }*/
    }

    public void start() {
        launch();
    }
}
