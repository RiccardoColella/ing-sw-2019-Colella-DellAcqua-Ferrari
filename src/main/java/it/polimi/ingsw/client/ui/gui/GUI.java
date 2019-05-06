package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.client.io.Connector;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

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
//        List<Player> opponents = new LinkedList<>();
//        opponents.add(new Player("avv1", PlayerColor.YELLOW, new Wallet()));
//        opponents.add(new Player("avv2", PlayerColor.PURPLE, new Wallet()));
//        opponents.add(new Player("avv3", PlayerColor.TURQUOISE, new Wallet()));
//        opponents.add(new Player("avv4", PlayerColor.GREEN, new Wallet()));
//        new GameController(BoardFactory.Preset.BOARD_1, new Player("me", PlayerColor.GRAY, new Wallet()), opponents).showAsModal();


        LoginController loginController = new LoginController();
        loginController.showAsModal();
        Optional<GameController> gameController = loginController.getGameController();
        if (gameController.isPresent()) {
            gameController.get().showAsModal();
        } else {
            Platform.exit();
        }
    }

    public void start() {
        launch();
    }
}
