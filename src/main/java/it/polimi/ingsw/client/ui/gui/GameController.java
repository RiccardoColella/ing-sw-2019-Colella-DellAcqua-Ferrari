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
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.util.Collections;
import java.util.LinkedList;
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
        initOpponentsBoards();boardContent.setMaxHeight(400);
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
        weaponContainer.getChildren().remove(2, weaponContainer.getChildren().size());
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

    private void stringSelectionQuestion(Question<String> question, Consumer<String> answerCallback, String title) {
        Platform.runLater(() -> {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle(title);
            dialog.setHeaderText(question.getText());
            SelectPane sp = new SelectPane();
            sp.setTextOnlyOptions(new LinkedList<>(question.getAvailableOptions()));
            sp.setSkippable(question.isSkippable());
            dialog.setDialogPane(sp);
            dialog.setResultConverter(question.isSkippable() ? CallbackFactory.skippableString() : CallbackFactory.unskippableString());
            dialog.showAndWait();
            answerCallback.accept(dialog.getResult());
        });
    }

    private void weaponSelectionQuestion(Question<String> question, Consumer<String> answerCallback, String title) {
        Platform.runLater( () -> {
                    Dialog<String> dialog = new Dialog<>();
                    dialog.setTitle(title);
                    dialog.setHeaderText(question.getText());
                    SelectPane sp = new SelectPane();
                    List<Tuple<ImagePane, String>> options = question.getAvailableOptions()
                            .stream()
                            .map(o -> new Tuple<>(
                                    new ImagePane(UrlFinder.findWeapon(o)),
                                    o)
                            )
                            .collect(Collectors.toList());
                    sp.setOptions(options);
                    sp.setSkippable(question.isSkippable());
                    dialog.setDialogPane(sp);
                    dialog.setResultConverter(question.isSkippable() ? CallbackFactory.skippableWeapon() : CallbackFactory.unskippableWeapon());
                    dialog.showAndWait();
                    answerCallback.accept(dialog.getResult());
                }
        );
    }

    private void powerupSelectionQuestion(Question<Powerup> question, Consumer<Powerup> answerCallback, String title) {
        Platform.runLater( () -> {
                    Dialog<Powerup> dialog = new Dialog<>();
                    dialog.setTitle(title);
                    dialog.setHeaderText(question.getText());
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
    public void onDirectionQuestion(Question<Direction> question, Consumer<Direction> answerCallback) {
        Platform.runLater(() -> {
            Dialog<Direction> dialog = new Dialog<>();
            dialog.setTitle("Direction question");
            dialog.setHeaderText(question.getText());
            SelectPane sp = new SelectPane();
            sp.setTextOnlyOptions(question.getAvailableOptions().stream().map(e -> e.toString().toLowerCase()).collect(Collectors.toList()));
            sp.setSkippable(question.isSkippable());
            dialog.setDialogPane(sp);
            dialog.setResultConverter(question.isSkippable() ? CallbackFactory.skippableDirection() : CallbackFactory.unskippableDirection());
            dialog.showAndWait();
            answerCallback.accept(dialog.getResult());
        });
    }

    @Override
    public void onAttackQuestion(Question<String> question, Consumer<String> answerCallback) {
        stringSelectionQuestion(question, answerCallback, "Attack question");
    }

    @Override
    public void onBasicActionQuestion(Question<BasicAction> question, Consumer<BasicAction> answerCallback) {
        Platform.runLater(() -> {
            Dialog<BasicAction> dialog = new Dialog<>();
            dialog.setTitle("Basic action question");
            dialog.setHeaderText(question.getText());
            SelectPane sp = new SelectPane();
            sp.setTextOnlyOptions(question.getAvailableOptions().stream().map(e -> e.toString().toLowerCase()).collect(Collectors.toList()));
            sp.setSkippable(question.isSkippable());
            dialog.setDialogPane(sp);
            dialog.setResultConverter(question.isSkippable() ? CallbackFactory.skippableBasicAction() : CallbackFactory.unskippableBasicAction());
            dialog.showAndWait();
            answerCallback.accept(dialog.getResult());
        });
    }

    @Override
    public void onBlockQuestion(Question<Point> question, Consumer<Point> answerCallback) {
        Platform.runLater(() -> {
            Dialog<Point> dialog = new Dialog<>();
            dialog.setTitle("Block question");
            dialog.setHeaderText(question.getText());
            SelectPane sp = new SelectPane();
            sp.setTextOnlyOptions(question.getAvailableOptions().stream().map(point -> point.x + " " + point.y).collect(Collectors.toList()));
            sp.setSkippable(question.isSkippable());
            dialog.setDialogPane(sp);
            dialog.setResultConverter(question.isSkippable() ? CallbackFactory.skippablePoint() : CallbackFactory.unskippablePoint());
            dialog.showAndWait();
            answerCallback.accept(dialog.getResult());
        });
    }

    @Override
    public void onPaymentMethodQuestion(Question<String> question, Consumer<String> answerCallback) {
        stringSelectionQuestion(question, answerCallback, "Payment method question");
    }

    @Override
    public void onPowerupQuestion(Question<Powerup> question, Consumer<Powerup> answerCallback) {
        powerupSelectionQuestion(question, answerCallback, "Powerup usage question");
    }

    @Override
    public void onWeaponQuestion(Question<String> question, Consumer<String> answerCallback) {
        weaponSelectionQuestion(question, answerCallback, "Weapon selection question");
    }

    @Override
    public void onReloadQuestion(Question<String> question, Consumer<String> answerCallback) {
        weaponSelectionQuestion(question, answerCallback, "Weapon reload question");
    }

    @Override
    public void onSpawnpointQuestion(Question<Powerup> question, Consumer<Powerup> answerCallback) {
        powerupSelectionQuestion(question, answerCallback, "Spawnpoint question");
    }

    @Override
    public void onTargetQuestion(Question<String> question, Consumer<String> answerCallback) {
        stringSelectionQuestion(question, answerCallback, "Target question");
    }

    @Override
    public void onPlayerDied(PlayerEvent e) {
        sendNotification(e.getPlayer().getNickname() + " just died");
    }

    @Override
    public void onPlayerReborn(PlayerEvent e) {
        Platform.runLater(() -> {
            PlayerBoardPane paneToUpdate = findPlayerBoard(e.getPlayer());
            paneToUpdate.resetDamage();
            boardContent.movePlayer(e.getPlayer().getColor(), e.getPlayer().getLocation().y, e.getPlayer().getLocation().x);
            sendNotification(e.getPlayer().getNickname() + " was brought back to life");
        });
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
        if (self.getNickname().equals(e.getPlayer().getNickname()) && self.isBoardFlipped() != e.getPlayer().isBoardFlipped()) {
            self = e.getPlayer();
            playerBoardImg.flip(UrlFinder.findPlayerBoard(self.getColor(), self.isBoardFlipped()));
        } else if (self.getNickname().equals(e.getPlayer().getNickname()) && self.isTileFlipped() != e.getPlayer().isTileFlipped()) {
            self = e.getPlayer();
            playerTileImg.setImg(UrlFinder.findPlayerTile(self.getColor(), self.isTileFlipped()));
        } else {
            for (Player opponent : opponents) {
                if (opponent.getNickname().equals(e.getPlayer().getNickname())) {
                    if (self.isBoardFlipped() != e.getPlayer().isBoardFlipped()) {
                        PlayerBoardPane boardToUpdate = findPlayerBoard(e.getPlayer());
                        boardToUpdate.flip(UrlFinder.findPlayerBoard(e.getPlayer().getColor(), e.getPlayer().isBoardFlipped()));
                    } else {
                        findPlayerBoard(e.getPlayer());
                    }
                    break;
                }
            }
        }

    }

    @Override
    public void onPlayerHealthChanged(PlayerHealthChanged e) {
        Platform.runLater(() -> {
            PlayerBoardPane paneToUpdate = findPlayerBoard(e.getPlayer());
            int skullsToAdd = e.getSkulls() - paneToUpdate.getRepresentedSkulls();
            for (int i = 0; i < skullsToAdd; i++) {
                paneToUpdate.addSkull();
            }
            int oldTokens = paneToUpdate.getRepresentedDamageTokens().size();
            int tokensToAdd = e.getDamages().size() - oldTokens;
            for (int i = 0; i < tokensToAdd; i++) {
                paneToUpdate.addToken(e.getDamages().get(oldTokens + i));
            }
            paneToUpdate.clearMarks();
            for (PlayerColor c : e.getMarks()) {
                paneToUpdate.addMark(c);
            }
            sendNotification(e.getPlayer().getNickname() + " now has " + e.getSkulls() + " skulls, " + e.getDamages().size() + " damage tokens and " + e.getMarks().size() + " marks.");

        });
    }

    private PlayerBoardPane findPlayerBoard(Player player) {
        if (player.getNickname().equals(self.getNickname())) {
            self = player;
            return playerBoardImg;
        } else {
            for (int i = 0; i < opponents.size(); i++) {
                if (player.getNickname().equals(opponents.get(i).getNickname())) {
                    opponents.set(i, player);
                    return (PlayerBoardPane) ((GridPane) opponentsContainer.getChildren().get(i)).getChildren().get(1);
                }
            }
        }
        throw new IllegalStateException("Player " + player.getNickname() + " does not exist");
    }

    @Override
    public void onWeaponReloaded(WeaponEvent e) {
        if (e.getPlayer().getNickname().equals(self.getNickname())) {
            self = e.getPlayer();
            initWeapons();
        }
        sendNotification(e.getPlayer().getNickname() + " reloaded their " + e.getWeaponName());

    }

    @Override
    public void onWeaponUnloaded(WeaponEvent e) {
        Platform.runLater(() -> {
            if (e.getPlayer().getNickname().equals(self.getNickname())) {
                self = e.getPlayer();
                initWeapons();
            }
            sendNotification(e.getPlayer().getNickname() + " shot with " + e.getWeaponName() + ", which is now unloaded");
        });
    }

    @Override
    public void onWeaponPicked(WeaponExchanged e) {
        Platform.runLater(() -> {
            if (e.getColumn() == 0) {
                boardContent.removeWeaponLeft(e.getWeaponName());
            } else if (e.getRow() == 0) {
                boardContent.removeWeaponTop(e.getWeaponName());
            } else {
                boardContent.removeWeaponRight(e.getWeaponName());
            }
            if (e.getPlayer().getNickname().equals(self.getNickname())) {
                self = e.getPlayer();
                initWeapons();
            } else {
                for (Player o : opponents) {
                    if (o.getNickname().equals(e.getPlayer().getNickname())) {
                        opponents.set(opponents.indexOf(o), e.getPlayer());
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void onWeaponDropped(WeaponExchanged e) {
        Platform.runLater(() -> {
            if (e.getColumn() == 0) {
                boardContent.addWeaponLeft(e.getWeaponName());
            } else if (e.getRow() == 0) {
                boardContent.addWeaponTop(e.getWeaponName());
            } else {
                boardContent.addWeaponRight(e.getWeaponName());
            }
            if (e.getPlayer().getNickname().equals(self.getNickname())) {
                self = e.getPlayer();
                initWeapons();
            } else {
                for (Player o : opponents) {
                    if (o.getNickname().equals(e.getPlayer().getNickname())) {
                        opponents.set(opponents.indexOf(o), e.getPlayer());
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void onPlayerDisconnected(PlayerEvent e) {
        sendNotification(e.getPlayer().getNickname() + " just disconnected from the match");
    }

    @Override
    public void onPlayerReconnected(PlayerEvent e) {
        sendNotification(e.getPlayer().getNickname() + " is back!");
    }

    @Override
    public void onPlayerMoved(PlayerMoved e) {
        Platform.runLater( () -> {
            boardContent.movePlayer(e.getPlayer().getColor(), e.getRow(), e.getColumn());
            String message = " just moved!";
            sendNotification(e.getPlayer().getNickname().equals(self.getNickname()) ? "You" + message: e.getPlayer().getNickname() + message);
        });
    }

    @Override
    public void onPlayerTeleported(PlayerMoved e) {
        Platform.runLater( () -> {
            boardContent.movePlayer(e.getPlayer().getColor(), e.getRow(), e.getColumn());
            String message = " just teleported!";
            sendNotification(e.getPlayer().getNickname().equals(self.getNickname()) ? "You" + message: e.getPlayer().getNickname() + message);
        });
    }

    @Override
    public void onPlayerSpawned(PlayerSpawned e) {
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
        sendNotification("New match mode was triggered: " + e.getMode().toString().toLowerCase());
    }

    @Override
    public void onKillshotTrackChanged(KillshotTrackChanged e) {
        List<Tuple<PlayerColor, Boolean>> oldTrack = boardContent.getKillshotTrackUnmodifiable();
        int toAdd = e.getKillshots().size() - oldTrack.size();
        if (toAdd > 0) {
            for (int i = 0; i < toAdd; i++) {
                boardContent.addKillshot(e.getKillshots().get(i + toAdd).getItem1());
                if (e.getKillshots().get(i + toAdd).getItem2()) {
                    boardContent.addOverkill();
                }
            }
        }
    }

    @Override
    public void onMatchEnded(MatchEnded e) {

    }

    private void sendNotification(String message) {
        /*Alert info = new Alert(Alert.AlertType.INFORMATION, message);
        info.show();*/
    }
}
