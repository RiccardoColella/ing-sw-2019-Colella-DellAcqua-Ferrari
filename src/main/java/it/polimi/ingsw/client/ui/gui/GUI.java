package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.client.io.Connector;
import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.currency.AmmoCubeFactory;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.player.PlayerColor;
import it.polimi.ingsw.shared.bootstrap.ClientInitializationInfo;
import it.polimi.ingsw.shared.viewmodels.Player;
import it.polimi.ingsw.shared.viewmodels.Wallet;
import it.polimi.ingsw.utils.Tuple;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.net.ConnectException;
import java.util.Arrays;
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


        //debug();
        LoginController loginController = new LoginController();
        loginController.showAsModal();
        Optional<GameController> gameController = loginController.getGameController();
        if (gameController.isPresent()) {
            gameController.get().showAsModal();
        } else {
            Platform.exit();
        }
    }

    public void debug() {
        Connector fakeConnector = new Connector() {
            /**
             * Initializes the connector and its IO queues
             *
             * @param clientInitializationInfo the user preferences for the match
             */
            @Override
            protected void initialize(ClientInitializationInfo clientInitializationInfo) {
                super.initialize(clientInitializationInfo);
            }
        };
        List<Player> opponents = new LinkedList<>();
        opponents.add(new Player("avv1", PlayerColor.YELLOW, new Wallet()));
        opponents.add(new Player("avv2", PlayerColor.PURPLE, new Wallet()));
        opponents.add(new Player("avv3", PlayerColor.TURQUOISE, new Wallet()));
        opponents.add(new Player("avv4", PlayerColor.GREEN, new Wallet()));
        opponents.get(0).getDamage().add(PlayerColor.GRAY);
        opponents.get(0).getDamage().add(PlayerColor.PURPLE);
        opponents.get(0).setSkulls(6);
        Wallet myWallet = new Wallet();
        for (int i = 0; i < 9; i++) {
            myWallet.getAmmoCubes().add(CurrencyColor.YELLOW);
        }
        myWallet.getPowerups().add(new Tuple<>("Tagback Grenade", CurrencyColor.RED));
        myWallet.getPowerups().add(new Tuple<>("Newton", CurrencyColor.YELLOW));
        myWallet.getPowerups().add(new Tuple<>("Teleporter", CurrencyColor.BLUE));
        myWallet.getLoadedWeapons().add("Electroscythe");
        myWallet.getLoadedWeapons().add("Railgun");
        myWallet.getUnloadedWeapons().add("Shockwave");
        Player self = new Player("me", PlayerColor.GRAY, myWallet);
        self.setBoardFlipped(true);
        Arrays.stream(PlayerColor.values()).forEach(c -> {
            self.getDamage().add(c);
            self.getDamage().add(c);
            self.getMarks().add(c);
            self.getMarks().add(c);
        });
        self.getDamage().add(PlayerColor.YELLOW);
        self.getDamage().add(PlayerColor.YELLOW);
        self.getMarks().add(PlayerColor.YELLOW);
        self.getMarks().add(PlayerColor.YELLOW);
        self.setSkulls(4);
        new GameController(fakeConnector, BoardFactory.Preset.BOARD_1, self, opponents).showAsModal();

    }
    public void start() {
        launch();
    }
}