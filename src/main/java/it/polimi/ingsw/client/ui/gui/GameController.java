package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.client.io.Connector;
import it.polimi.ingsw.client.io.listeners.BoardListener;
import it.polimi.ingsw.client.io.listeners.MatchListener;
import it.polimi.ingsw.client.io.listeners.PlayerListener;
import it.polimi.ingsw.client.io.listeners.QuestionMessageReceivedListener;
import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.player.BasicAction;
import it.polimi.ingsw.server.model.player.PlayerColor;
import it.polimi.ingsw.shared.Direction;
import it.polimi.ingsw.shared.events.networkevents.*;
import it.polimi.ingsw.shared.messages.templates.Question;
import it.polimi.ingsw.shared.viewmodels.Player;
import it.polimi.ingsw.shared.viewmodels.Powerup;
import it.polimi.ingsw.utils.Tuple;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class GameController extends WindowController implements AutoCloseable, QuestionMessageReceivedListener, PlayerListener, BoardListener, MatchListener {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    @FXML
    private GridPane window;
    @FXML
    private StackPane boardContainer;
    @FXML
    private GridPane opponentsContainer;
    @FXML
    private PlayerBoardPane playerBoardImg;
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
    @FXML
    private GridPane right;

    private BoardFactory.Preset preset;

    private List<Player> opponents;

    private Player self;

    private Connector connector;

    private BoardContentPane boardContent;

    public GameController(Connector connector, MatchStarted e) {
        super("Adrenalina", "/fxml/game.fxml", "/css/game.css");
        this.connector = connector;
        this.preset = e.getPreset();
        BoardPane board = new BoardPane(preset);
        board.setMinWidth(500);
        board.setMaxWidth(500);
        board.setMinHeight(400);
        board.setMaxHeight(400);
        boardContainer.getChildren().add(board);
        boardContent = new BoardContentPane();
        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(100);
        RowConstraints rc = new RowConstraints();
        rc.setPercentHeight(100);
        boardContent.setMinWidth(500);
        boardContent.setMaxWidth(500);
        boardContent.setMinHeight(400);
        boardContent.setMaxHeight(400);
        boardContainer.getChildren().add(boardContent);
        for (int i = 0; i < e.getWeaponLeft().size(); i++) {
            boardContent.addWeaponTop(e.getWeaponTop().get(i), i);
            boardContent.addWeaponRight(e.getWeaponRight().get(i), i);
            boardContent.addWeaponLeft(e.getWeaponLeft().get(i), i);
        }
        boardContent.setSkulls(e.getSkulls());

        this.opponents = e.getOpponents();
        this.self = e.getSelf();
        initOpponentsBoards();
        initPlayerBoard();
        initAmmo();
        initPowerups();
        initWeapons();
        tileMsg.setText(self.getNickname() + ", " + tileMsg.getText());

    }

    private void initPlayerBoard() {
        playerBoardImg.setImg(UrlFinder.findPlayerBoard(self.getColor(), self.isBoardFlipped()), ImagePane.LEFT);
        updatePlayerBoard(self, playerBoardImg);
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
            PlayerBoardPane opponentPane = new PlayerBoardPane(UrlFinder.findPlayerBoard(opponent.getColor(), opponent.isBoardFlipped()), ImagePane.LEFT);
            opponentPane.setMaxHeight(59);
            opponentPane.setMinHeight(59);
            opponentPane.setMaxWidth(229);
            opponentPane.setMinWidth(229);
            Label opponentName = new Label(opponent.getNickname());
            GridPane opponentContainer = new GridPane();
            ColumnConstraints entireColumn = new ColumnConstraints();
            entireColumn.setPercentWidth(100);
            opponentContainer.getColumnConstraints().add(entireColumn);
            RowConstraints nameRow = new RowConstraints();
            nameRow.setPercentHeight(20);
            RowConstraints imgRow = new RowConstraints();
            imgRow.setPercentHeight(80);
            updatePlayerBoard(opponent, opponentPane);
            opponentContainer.getRowConstraints().addAll(nameRow, imgRow);
            opponentContainer.add(opponentName, 0, 0);
            opponentContainer.add(opponentPane, 0, 1);
            opponentsContainer.add(opponentContainer, i % 2, i / 2);
            opponentPane.setOnMouseClicked(this::showOpponentFullSize);
        }
    }

    private void updatePlayerBoard(Player owner, PlayerBoardPane pane) {
        for (PlayerColor token : owner.getDamage()) {
            pane.addToken(token);
        }
        for (PlayerColor token : owner.getMarks()) {
            pane.addMark(token);
        }
        for (int i = 0; i < owner.getSkulls(); i++) {
            pane.addSkull();
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
            Powerup powerup = self.getWallet().getPowerups().get(i);
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

    private void showOpponentFullSize(MouseEvent e) {
        PlayerBoardPane src = (PlayerBoardPane) e.getSource();
        Stage popup = new Stage();
        popup.initStyle(StageStyle.DECORATED);
        PlayerBoardPane copy = new PlayerBoardPane(src.getImgSrc(), ImagePane.LEFT);
        copy.setMinWidth(467);
        copy.setMinHeight(123);
        copy.setMaxWidth(467);
        copy.setMaxHeight(123);
        List<PlayerColor> damageToRepresent = src.getRepresentedDamageTokens();
        List<PlayerColor> marksToRepresent = src.getRepresentedMarkTokens();
        int skullsToRepresent = src.getRepresentedSkulls();
        for (PlayerColor color : damageToRepresent) {
            copy.addToken(color);
        }
        for (PlayerColor color : marksToRepresent) {
            copy.addToken(color);
        }
        for (int i = 0; i < skullsToRepresent; i++) {
            copy.addSkull();
        }
        Scene scene = new Scene(copy);
        popup.setResizable(false);
        popup.setScene(scene);
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

    @Override
    public void onDirectionQuestion(Question<Direction> question, Consumer<Direction> answerCallback) {

    }

    @Override
    public void onAttackQuestion(Question<String> question, Consumer<String> answerCallback) {

    }

    @Override
    public void onBasicActionQuestion(Question<BasicAction> question, Consumer<BasicAction> answerCallback) {

    }

    @Override
    public void onBlockQuestion(Question<Point> question, Consumer<Point> answerCallback) {

    }

    @Override
    public void onPaymentMethodQuestion(Question<String> question, Consumer<String> answerCallback) {

    }

    @Override
    public void onPowerupQuestion(Question<Powerup> question, Consumer<Powerup> answerCallback) {

    }

    @Override
    public void onWeaponQuestion(Question<String> question, Consumer<String> answerCallback) {

    }

    @Override
    public void onReloadQuestion(Question<String> question, Consumer<String> answerCallback) {

    }

    @Override
    public void onSpawnpointQuestion(Question<Powerup> question, Consumer<Powerup> answerCallback) {
        Platform.runLater( () -> {
                    Dialog<Powerup> dialog = new Dialog<>();
                    dialog.setTitle("Spawnpoint question");
                    dialog.setContentText(question.getText());
                    SelectPane sp = new SelectPane();
                    List<Tuple<ImagePane, String>> options = question.getAvailableOptions()
                            .stream()
                            .map(o -> new Tuple<>(
                                    new ImagePane(UrlFinder.findPowerup(o)),
                                    o.getColor().toString().toLowerCase() + " " + o.getName())
                            )
                            .collect(Collectors.toList());
                    sp.setOptions(options);
                    sp.setSkippable(question.isSkippable());
                    dialog.setDialogPane(sp);
                    dialog.setResultConverter(question.isSkippable() ? CallbackFactory.skippablePowerup() : CallbackFactory.unskippablePowerup());
                    dialog.showAndWait();
                    answerCallback.accept(dialog.getResult());
                }
        );

    }

    @Override
    public void onTargetQuestion(Question<String> question, Consumer<String> answerCallback) {

    }

    @Override
    public void onPlayerDied(PlayerEvent e) {

    }

    @Override
    public void onPlayerReborn(PlayerEvent e) {

    }

    @Override
    public void onPlayerWalletChanged(PlayerWalletChanged e) {
        Platform.runLater(() -> {
            if (e.getPlayer().getNickname().equals(self.getNickname())) {
                self.getWallet().setPowerups(e.getWallet().getPowerups());
                self.getWallet().setAmmoCubes(e.getWallet().getAmmoCubes());
                ammoContainer.getChildren().clear();
                initAmmo();
                powerupContainer.getChildren().clear();
                initPowerups();
            }
            sendNotification(e.getMessage());
        });

    }

    @Override
    public void onPlayerBoardFlipped(PlayerEvent e) {

    }

    @Override
    public void onPlayerHealthChanged(PlayerHealthChanged e) {

    }

    @Override
    public void onWeaponReloaded(WeaponEvent e) {

    }

    @Override
    public void onWeaponUnloaded(WeaponEvent e) {

    }

    @Override
    public void onWeaponPicked(WeaponExchanged e) {

    }

    @Override
    public void onWeaponDropped(WeaponExchanged e) {

    }

    @Override
    public void onPlayerDisconnected(PlayerEvent e) {

    }

    @Override
    public void onPlayerReconnected(PlayerEvent e) {

    }

    @Override
    public void onPlayerMoved(PlayerMoved e) {

    }

    @Override
    public void onPlayerTeleported(PlayerMoved e) {

    }

    @Override
    public void onPlayerSpawned(PlayerSpawned e) {
        System.out.println("EVENTO ARRIVATO");
        Platform.runLater( () -> {
            boardContent.addPlayer(e.getPlayer().getColor(), e.getRow(), e.getColumn());
            String message = " just spawned!";
            sendNotification(e.getPlayer().getNickname().equals(self.getNickname()) ? "You" + message: e.getPlayer().getNickname() + message);
        });
    }

    @Override
    public void onMatchStarted(MatchStarted e) {

    }

    @Override
    public void onMatchModeChanged(MatchModeChanged e) {

    }

    @Override
    public void onKillshotTrackChanged(KillshotTrackChanged e) {

    }

    @Override
    public void onMatchEnded(MatchEnded e) {

    }

    private void sendNotification(String message) {

    }
}
