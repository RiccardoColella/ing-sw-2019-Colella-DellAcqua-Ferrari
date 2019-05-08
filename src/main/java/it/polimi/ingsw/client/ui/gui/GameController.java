package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.client.io.Connector;
import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.shared.viewmodels.Player;
import it.polimi.ingsw.utils.Tuple;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;
import java.util.logging.Logger;

public class GameController extends WindowController implements AutoCloseable {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    @FXML
    private AnchorPane window;
    @FXML
    private GridPane boardContainer;
    @FXML
    private GridPane opponentsContainer;
    @FXML
    private ImagePane playerBoardImg;
    @FXML
    private ImagePane playerTileImg;
    @FXML
    private GridPane ammoContainer;
    @FXML
    private GridPane powerupContainer;
    @FXML
    private GridPane weaponContainer;
    @FXML
    private Label tileMsg;

    private BoardFactory.Preset preset;

    private List<Player> opponents;

    private Player self;

    private Connector connector;

    public GameController(Connector connector, BoardFactory.Preset preset, Player self, List<Player> opponents) {
        super("Adrenalina", "/fxml/game.fxml", "/css/game.css");

        this.connector = connector;

        this.preset = preset;
        BoardPane board = new BoardPane(preset);
        boardContainer.getChildren().add(board);
        this.opponents = opponents;
        this.self = self;
        initOpponentsBoards();
        initPlayerBoard();
        initAmmo();
        initPowerups();
        initWeapons();
        tileMsg.setText(self.getNickname() + ", " + tileMsg.getText());
    }

    private void initPlayerBoard() {
        playerBoardImg.setImg(UrlFinder.findPlayerBoard(self.getColor(), self.isBoardFlipped()), ImagePane.LEFT);
        playerTileImg.setImg(UrlFinder.findPlayerTile(self.getColor(), self.isTileFlipped()), ImagePane.RIGHT);
    }

    private void initOpponentsBoards() {
        int rows = opponents.size() / 2 + opponents.size() % 2;
        double rowHeight = 100.0 / rows;
        ColumnConstraints c0 = new ColumnConstraints();
        c0.setPercentWidth(50);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(50);
        opponentsContainer.getColumnConstraints().addAll(c0, c1);
        for (int i = 0; i < rows; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(rowHeight);
            opponentsContainer.getRowConstraints().add(rc);
        }
        for (int i = 0; i < opponents.size(); i++) {
            Player opponent = opponents.get(i);
            ImagePane opponentPane = new ImagePane(UrlFinder.findPlayerBoard(opponent.getColor(), opponent.isBoardFlipped()), ImagePane.LEFT);
            Label opponentName = new Label(opponent.getNickname());
            GridPane opponentContainer = new GridPane();
            ColumnConstraints entireColumn = new ColumnConstraints();
            entireColumn.setPercentWidth(100);
            opponentContainer.getColumnConstraints().add(entireColumn);
            RowConstraints nameRow = new RowConstraints();
            nameRow.setPercentHeight(20);
            RowConstraints imgRow = new RowConstraints();
            imgRow.setPercentHeight(80);
            opponentContainer.getRowConstraints().addAll(nameRow, imgRow);
            opponentContainer.add(opponentName, 0, 0);
            opponentContainer.add(opponentPane, 0, 1);
            opponentsContainer.add(opponentContainer, i % 2, i / 2);
        }
    }

    private void initAmmo() {
        for (int i = 0; i < self.getWallet().getAmmoCubes().size(); i++) {
            CurrencyColor ammoColor = self.getWallet().getAmmoCubes().get(i);
            ImagePane ammoPane = new ImagePane(UrlFinder.findAmmo(ammoColor), ImagePane.CENTER);
            ammoContainer.add(ammoPane, i % 3, 1 + i / 3);
        }
    }

    private void initPowerups() {
        for (int i = 0; i < self.getWallet().getPowerups().size(); i++) {
            Tuple<String, CurrencyColor> powerup = self.getWallet().getPowerups().get(i);
            ImagePane powerupPane = new ImagePane(UrlFinder.findPowerup(powerup), ImagePane.CENTER);
            powerupContainer.add(powerupPane, i, 1);
            powerupPane.setOnMouseClicked(this::showFullSize);
        }
    }

    private void initWeapons() {
        for (int i = 0; i < self.getWallet().getLoadedWeapons().size(); i++) {
            String weaponName = self.getWallet().getLoadedWeapons().get(i);
            ImagePane weaponPane = new ImagePane(UrlFinder.findWeapon(weaponName), ImagePane.CENTER);
            weaponContainer.add(weaponPane, i, 1);
            weaponPane.setOnMouseClicked(this::showFullSize);
        }
        for (int j = 0; j < self.getWallet().getUnloadedWeapons().size(); j++) {
            String weaponName = self.getWallet().getUnloadedWeapons().get(j);
            ImagePane weaponPane = new ImagePane(UrlFinder.findWeapon(weaponName), ImagePane.CENTER);
            weaponContainer.add(weaponPane, 5 - j, 1);
            weaponPane.setOnMouseClicked(this::showFullSize);
        }
    }

    private void showFullSize(MouseEvent e) {
        ImagePane img = (ImagePane) e.getSource();
        Stage popup = new Stage();
        popup.initStyle(StageStyle.DECORATED);
        Scene scene = new Scene(new Pane(new ImageView(new Image(img.getSrc()))));
        popup.setResizable(false);
        popup.setScene(scene);
        popup.sizeToScene();
        popup.show();
    }

    @FXML
    public void initialize() {
        window.setMinWidth(1000);
        window.setMinHeight(700);
        setupViewport(window);
    }


    @Override
    public void close() {
        super.close();
        try {
            connector.close();
        } catch (Exception ex) {
            logger.warning("Could not close the connector " + ex);
        }
    }
}
