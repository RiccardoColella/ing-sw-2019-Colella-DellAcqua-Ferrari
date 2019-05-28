package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.client.io.Connector;
import it.polimi.ingsw.client.io.listeners.BoardListener;
import it.polimi.ingsw.client.io.listeners.MatchListener;
import it.polimi.ingsw.client.io.listeners.PlayerListener;
import it.polimi.ingsw.client.io.listeners.QuestionMessageReceivedListener;
import it.polimi.ingsw.client.ui.gui.events.NotificationClosed;
import it.polimi.ingsw.client.ui.gui.events.listeners.NotificationListener;
import it.polimi.ingsw.client.io.listeners.*;
import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.player.BasicAction;
import it.polimi.ingsw.server.model.player.PlayerColor;
import it.polimi.ingsw.shared.Direction;
import it.polimi.ingsw.shared.events.networkevents.*;
import it.polimi.ingsw.shared.messages.templates.Question;
import it.polimi.ingsw.shared.datatransferobjects.Player;
import it.polimi.ingsw.shared.datatransferobjects.Powerup;
import it.polimi.ingsw.utils.Tuple;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class GameController extends WindowController implements AutoCloseable, QuestionMessageReceivedListener, PlayerListener, BoardListener, MatchListener, NotificationListener, ClientListener {

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
    @FXML
    private Text message;

    private BoardFactory.Preset preset;

    private List<Player> opponents;

    private Player self;

    private Connector connector;

    private final BoardContentPane boardContent;

    private Queue<NotificationController> notifications;

    private NotificationController currentNotification = null;

    private EventHandler<KeyEvent> directionHandler;

    private EventHandler<KeyEvent> basicActionHandler;

    private EventHandler<KeyEvent> skipHandler;

    public GameController(Connector connector, MatchStarted e) {
        super("Adrenalina", "/fxml/game.fxml", "/css/game.css");
        this.notifications = new LinkedList<>();
        this.connector = connector;
        this.preset = e.getPreset();
        BoardPane board = new BoardPane(preset);
        board.setMinWidth(500);
        board.setMaxWidth(500);
        board.setMinHeight(400);
        board.setMaxHeight(400);
        boardContainer.getChildren().add(board);
        boardContent = new BoardContentPane(e.getSkulls());
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
        initAmmo(e.getSelf().getWallet().getAmmoCubes());
        initPowerups(e.getSelf().getWallet().getPowerups());
        initWeapons();
        boardContent.addTiles(e.getTurretBonusTiles());
        tileMsg.setText(self.getNickname() + ", " + tileMsg.getText());
        message.setFill(Color.rgb(194, 194, 214));
        stage.setOnCloseRequest(ignored -> this.close());
    }

    public GameController(Connector connector, MatchResumed e) {
        this(connector, (MatchStarted) e);
        for (Player opponent : e.getOpponents()) {
            PlayerBoardPane paneToUpdate = findPlayerBoard(opponent);
            for (PlayerColor damage : opponent.getDamage()) {
                paneToUpdate.addToken(damage);
            }
            for (PlayerColor mark : opponent.getMarks()) {
                paneToUpdate.addMark(mark);
            }
            for (int i = 0; i < opponent.getSkulls(); i++) {
                paneToUpdate.addSkull();
            }
        }
        PlayerBoardPane paneToUpdate = findPlayerBoard(e.getSelf());
        for (PlayerColor damage : e.getSelf().getDamage()) {
            paneToUpdate.addToken(damage);
        }
        for (PlayerColor mark : e.getSelf().getMarks()) {
            paneToUpdate.addMark(mark);
        }
        for (int i = 0; i < e.getSelf().getSkulls(); i++) {
            paneToUpdate.addSkull();
        }
        for (Tuple<PlayerColor, Boolean> killshot : e.getKillshots()) {
            boardContent.addKillshot(killshot.getItem1());
            if (killshot.getItem2()) {
                boardContent.addOverkill();
            }
        }
        e.getPlayerLocations().forEach((p, l) -> boardContent.addPlayer(p, l.y, l.x));
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

    private void initAmmo(List<CurrencyColor> ammoCubes) {
        for (int i = 0; i < ammoCubes.size(); i++) {
            CurrencyColor ammoColor = ammoCubes.get(i);
            ImagePane ammoPane = new ImagePane(UrlFinder.findAmmo(ammoColor), ImagePane.CENTER);
            ammoContainer.add(ammoPane, i % 3, 1 + i / 3);
        }
    }

    private void initPowerups(List<Powerup> powerups) {
        for (int i = 0; i < powerups.size(); i++) {
            Powerup powerup = powerups.get(i);
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
        new Thread(() -> {
            try {
                connector.close();
                System.exit(0);
            } catch (Exception ex) {
                logger.warning("Could not close the connector");
                System.exit(0);
            }
        }).start();
    }

    private void stringSelectionQuestion(Question<String> question, Consumer<String> answerCallback, String title) {
        Platform.runLater(() -> {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle(title);
            SelectPane sp = new SelectPane();
            sp.setHeaderText(question.getText());
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
                    SelectPane sp = new SelectPane();
                    sp.setHeaderText(question.getText());
                    List<Tuple<ImagePane, String>> options = question.getAvailableOptions()
                            .stream()
                            .map(o -> new Tuple<>(
                                    new ImagePane(UrlFinder.findWeapon(o)),
                                    o)
                            )
                            .collect(Collectors.toList());
                    sp.setOptions(question.isSkippable(), options);
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
                    SelectPane sp = new SelectPane();
                    sp.setHeaderText(question.getText());
                    List<Tuple<ImagePane, String>> options = question.getAvailableOptions()
                            .stream()
                            .map(o -> new Tuple<>(
                                    new ImagePane(UrlFinder.findPowerup(o)),
                                    o.getColor().toString().toLowerCase() + " " + o.getName())
                            )
                            .collect(Collectors.toList());
                    sp.setOptions(question.isSkippable(), options);
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
            String skip = question.isSkippable() ? ", press SPACE to skip" : "";
            message.setText(question.getText() + " Use the arrow keys" + skip);
            directionHandler = e -> arrowKeyListener(e, question.getAvailableOptions(), answerCallback, question.isSkippable());
            stage.addEventHandler(KeyEvent.KEY_PRESSED, directionHandler);
        });
    }

    private void arrowKeyListener(KeyEvent e, Collection<Direction> directions, Consumer<Direction> answerCallback, boolean isSkippable) {
        switch (e.getCode()) {
            case UP:
                checkDirection(directions, Direction.NORTH, answerCallback);
                break;
            case DOWN:
                checkDirection(directions, Direction.SOUTH, answerCallback);
                break;
            case LEFT:
                checkDirection(directions, Direction.WEST, answerCallback);
                break;
            case RIGHT:
                checkDirection(directions, Direction.EAST, answerCallback);
                break;
            case SPACE:
                if (isSkippable) {
                    stage.removeEventHandler(KeyEvent.KEY_PRESSED, directionHandler);
                    message.setText("");
                    answerCallback.accept(null);
                }
                break;
            default:
                break;
        }
    }

    private void actionTypeKeyListener(KeyEvent e, Collection<BasicAction> actions, Consumer<BasicAction> answerCallback, boolean isSkippable) {
        switch (e.getCode()) {
            case S:
                checkBasicAction(actions, BasicAction.SHOOT, answerCallback);
                break;
            case R:
                checkBasicAction(actions, BasicAction.RELOAD, answerCallback);
                break;
            case G:
                checkBasicAction(actions, BasicAction.GRAB, answerCallback);
                break;
            case M:
                checkBasicAction(actions, BasicAction.MOVE, answerCallback);
                break;
            case SPACE:
                if (isSkippable) {
                    stage.removeEventHandler(KeyEvent.KEY_PRESSED, basicActionHandler);
                    message.setText("");
                    answerCallback.accept(null);
                }
                break;
            default:
                break;
        }
    }

    private void skipListener(KeyEvent e, Consumer<Text> consumer) {
        if (e.getCode().equals(KeyCode.SPACE)) {
            stage.removeEventHandler(KeyEvent.KEY_PRESSED, skipHandler);
            consumer.accept(message);
        }
    }

    private void checkDirection(Collection<Direction> directions, Direction selected, Consumer<Direction> answerCallback) {
        if (directions.contains(selected)) {
            stage.removeEventHandler(KeyEvent.KEY_PRESSED, directionHandler);
            message.setText("");
            answerCallback.accept(selected);
        }
    }


    private void checkBasicAction(Collection<BasicAction> basicActions, BasicAction selected, Consumer<BasicAction> answerCallback) {
        if (basicActions.contains(selected)) {
            stage.removeEventHandler(KeyEvent.KEY_PRESSED, basicActionHandler);
            message.setText("");
            answerCallback.accept(selected);
        }
    }

    @Override
    public void onAttackQuestion(Question<String> question, Consumer<String> answerCallback) {
        stringSelectionQuestion(question, answerCallback, "Attack question");
    }

    @Override
    public void onBasicActionQuestion(Question<BasicAction> question, Consumer<BasicAction> answerCallback) {
        Platform.runLater(() -> {
            String skip = question.isSkippable() ? "- SPACE to skip" : "";
            String press = "Press: ";
            String move = question.getAvailableOptions().contains(BasicAction.MOVE) ? "- M to move " : "";
            String grab = question.getAvailableOptions().contains(BasicAction.GRAB) ? "- G to grab " : "";
            String shoot = question.getAvailableOptions().contains(BasicAction.SHOOT) ? "- S to shoot"  : "";
            String reload = question.getAvailableOptions().contains(BasicAction.RELOAD) ? "- R to reload " : "";
            message.setText(press + move + grab + shoot + reload + skip);
            basicActionHandler = e -> actionTypeKeyListener(e, question.getAvailableOptions(), answerCallback, question.isSkippable());
            stage.addEventHandler(KeyEvent.KEY_PRESSED, basicActionHandler);
        });
    }

    @Override
    public void onBlockQuestion(Question<Point> question, Consumer<Point> answerCallback) {
        Platform.runLater(() -> {
            if (question.isSkippable()) {
                message.setText(question.getText() + " Click on the block or press SPACE to skip");
                skipHandler = e -> {
                    skipListener(e, boardContent::removeBlockSelection);
                    answerCallback.accept(null);
                };
            } else {
                message.setText(question.getText() + " Click on the block!");
            }
            boardContent.waitForBlockSelection(new LinkedList<>(question.getAvailableOptions()), answerCallback, message);
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
    public void onTargetSetQuestion(Question<Set<String>> question, Consumer<Set<String>> answerCallback) {
        Platform.runLater(() -> {
            Dialog<Set<String>> dialog = new Dialog<>();
            dialog.setTitle("Multiple target question");
            SelectPane sp = new SelectPane();
            sp.setHeaderText(question.getText());
            sp.setTextOnlyOptions(
                    question
                            .getAvailableOptions()
                            .stream()
                            .map(
                                    set -> set.stream()
                                    .collect(Collectors.joining("\n", " - ", ""))
                            )
                    .collect(Collectors.toList())
            );
            sp.setSkippable(question.isSkippable());
            dialog.setDialogPane(sp);
            dialog.setResultConverter(question.isSkippable() ? CallbackFactory.skippableStringSet() : CallbackFactory.unskippableStringSet());
            dialog.showAndWait();
            answerCallback.accept(dialog.getResult());
        });
    }

    @Override
    public void onPaymentColorQuestion(Question<CurrencyColor> question, Consumer<CurrencyColor> answerCallback) {
        Platform.runLater(() -> {
            Platform.runLater(() -> {
                Dialog<CurrencyColor> dialog = new Dialog<>();
                dialog.setTitle("Payment color question");
                SelectPane sp = new SelectPane();
                sp.setHeaderText(question.getText());
                sp.setTextOnlyOptions(question.getAvailableOptions().stream().map(e -> e.toString().toLowerCase()).collect(Collectors.toList()));
                sp.setSkippable(question.isSkippable());
                dialog.setDialogPane(sp);
                dialog.setResultConverter(question.isSkippable() ? CallbackFactory.skippableCurrencyColor() : CallbackFactory.unskippableCurrencyColor());
                dialog.showAndWait();
                answerCallback.accept(dialog.getResult());
            });
        });
    }

    @Override
    public void onPlayerDied(PlayerEvent e) {
        sendNotification("Death",e.getPlayer().getNickname() + " just died");
    }

    @Override
    public void onPlayerReborn(PlayerEvent e) {
        Platform.runLater(() -> {
            PlayerBoardPane paneToUpdate = findPlayerBoard(e.getPlayer());
            paneToUpdate.resetDamage();
            sendNotification("Rebirth", e.getPlayer().getNickname() + " was brought back to life");
        });
    }

    @Override
    public void onPlayerWalletChanged(PlayerWalletChanged e) {
        Platform.runLater(() -> {
            if (e.getPlayer().getNickname().equals(self.getNickname())) {
                ammoContainer.getChildren().clear();
                initAmmo(e.getPlayer().getWallet().getAmmoCubes());
                powerupContainer.getChildren().clear();
                initPowerups(e.getPlayer().getWallet().getPowerups());
            }
            sendNotification("Wallet", e.getMessage());
        });

    }

    @Override
    public void onPlayerBoardFlipped(PlayerEvent e) {
        Platform.runLater(() -> {
            if (self.getNickname().equals(e.getPlayer().getNickname())) {
                self = e.getPlayer();
                playerBoardImg.flip(UrlFinder.findPlayerBoard(self.getColor(), true));
            } else {
                for (Player opponent : opponents) {
                    if (opponent.getNickname().equals(e.getPlayer().getNickname())) {
                        PlayerBoardPane boardToUpdate = findPlayerBoard(e.getPlayer());
                        boardToUpdate.flip(UrlFinder.findPlayerBoard(e.getPlayer().getColor(), true));
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void onPlayerTileFlipped(PlayerEvent e) {
        Platform.runLater(() -> {
            if (self.getNickname().equals(e.getPlayer().getNickname())) {
                self = e.getPlayer();
                playerTileImg.setImg(UrlFinder.findPlayerTile(self.getColor(), true));
            }
        });
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
            sendNotification("Health",e.getPlayer().getNickname() + " now has " + e.getSkulls() + " skulls, " + e.getDamages().size() + " damage tokens and " + e.getMarks().size() + " marks.");

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
    public void onWeaponReloaded(PlayerWeaponEvent e) {
        Platform.runLater(() -> {
            if (e.getPlayer().getNickname().equals(self.getNickname())) {
                self = e.getPlayer();
                initWeapons();
            }
            sendNotification("Weapon", e.getPlayer().getNickname() + " reloaded their " + e.getWeaponName());
        });
    }

    @Override
    public void onWeaponUnloaded(PlayerWeaponEvent e) {
        Platform.runLater(() -> {
            if (e.getPlayer().getNickname().equals(self.getNickname())) {
                self = e.getPlayer();
                initWeapons();
            }
            sendNotification("Weapon", e.getPlayer().getNickname() + " shot with " + e.getWeaponName() + ", which is now unloaded");
        });
    }

    @Override
    public void onWeaponPicked(PlayerWeaponExchanged e) {
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
    public void onWeaponDropped(PlayerWeaponExchanged e) {
        Platform.runLater(() -> {
            if (e.getColumn() == 0) {
                boardContent.enqueueWeaponLeft(e.getWeaponName());
            } else if (e.getRow() == 0) {
                boardContent.enqueueWeaponTop(e.getWeaponName());
            } else {
                boardContent.enqueueWeaponRight(e.getWeaponName());
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
    public void onLoginSuccess(ClientEvent e) {
        // TODO: implement

    }

    @Override
    public void onClientDisconnected(ClientEvent e) {
        sendNotification("Disconnection", e.getNickname() + " just disconnected from the match");
    }

    @Override
    public void onPlayerReconnected(PlayerEvent e) {
        sendNotification("Reconnection", e.getPlayer().getNickname() + " is back!");
    }

    @Override
    public void onPlayerMoved(PlayerMoved e) {
        Platform.runLater( () -> {
            boardContent.movePlayer(e.getPlayer().getColor(), e.getRow(), e.getColumn());
            sendNotification("Movement", (e.getPlayer().getNickname().equals(self.getNickname()) ? "You " : e.getPlayer().getNickname()) + " just moved!");
        });
    }

    @Override
    public void onPlayerTeleported(PlayerMoved e) {
        Platform.runLater( () -> {
            boardContent.movePlayer(e.getPlayer().getColor(), e.getRow(), e.getColumn());
            sendNotification("Movement", (e.getPlayer().getNickname().equals(self.getNickname()) ? "You " : e.getPlayer().getNickname()) + " just teleported!");
        });
    }

    @Override
    public void onNewWeaponAvailable(WeaponEvent e) {
        Platform.runLater(() -> {
            if (e.getColumn() == 0) {
                boardContent.enqueueWeaponLeft(e.getWeaponName());
            } else if (e.getRow() == 0) {
                boardContent.enqueueWeaponTop(e.getWeaponName());
            } else {
                boardContent.enqueueWeaponRight(e.getWeaponName());
            }
            sendNotification("Weapon", e.getWeaponName() + " can now be bought");
        });
    }

    @Override
    public void onBonusTileGrabbed(BonusTileEvent e) {
        Platform.runLater(() -> {
            boardContent.removeTile(e.getBonusTile().getLocation());
        });
    }

    @Override
    public void onBonusTileDropped(BonusTileEvent e) {
        Platform.runLater(() -> {
            boardContent.addTile(e.getBonusTile());
        });
    }

    @Override
    public void onPlayerSpawned(PlayerSpawned e) {
        Platform.runLater( () -> {
            boardContent.movePlayer(e.getPlayer().getColor(), e.getRow(), e.getColumn());
            sendNotification("Movement", (e.getPlayer().getNickname().equals(self.getNickname()) ? "You " : e.getPlayer().getNickname()) + " just spawned!");
        });
    }

    @Override
    public void onPlayerOverkilled(PlayerEvent e) {
        sendNotification("Health", e.getPlayer().getNickname() + " was overkilled!");
    }

    @Override
    public void onActivePlayerChanged(PlayerEvent e) {
        sendNotification("Logistics", "It's " + e.getPlayer().getNickname() + "'s turn");
    }

    @Override
    public void onMatchStarted(MatchStarted e) {

    }

    @Override
    public void onMatchModeChanged(MatchModeChanged e) {
        sendNotification("Logistics", "New match mode was triggered: " + e.getMode().toString().toLowerCase());
    }

    @Override
    public void onKillshotTrackChanged(KillshotTrackChanged e) {
        Platform.runLater(() -> {
            List<Tuple<PlayerColor, Boolean>> oldTrack = boardContent.getKillshotTrackUnmodifiable();
            int toAdd = e.getKillshots().size() - oldTrack.size();
            if (toAdd > 0) {
                for (int i = 0; i < toAdd; i++) {
                    boardContent.addKillshot(e.getKillshots().get(i + toAdd - 1).getItem1());
                    if (e.getKillshots().get(i + toAdd - 1).getItem2()) {
                        boardContent.addOverkill();
                    }
                }
            }
        });
    }

    @Override
    public void onMatchEnded(MatchEnded e) {
        String youLost = "The match is over, YOU LOSE\n";
        String youWin = "The match is over, and CONGRATULATIONS: YOU WIN\n";
        String rankings = "\n-----RANKINGS------\n";
        String result = e.getRankings().entrySet()
                .stream()
                .map(ranking -> ranking.getKey() + ") " + ranking.getValue()
                        .stream()
                        .map(player -> player.getNickname() + " " + "pt. " + e.getScore(player.getNickname()))
                        .collect(Collectors.joining(",", " ", "")))
                .collect(Collectors.joining("\n"));
        Platform.runLater(() -> {
            NotificationController nc = new NotificationController("Rankings", (e.isTheWinner(self) ? youWin : youLost) + rankings + result);
            nc.show();
        });
    }

    @Override
    public void onMatchResumed(MatchResumed e) {
        // TODO: implement
    }

    private void sendNotification(String title, String message) {
        Platform.runLater(() -> {
            NotificationController nc = new NotificationController(title, message);
            nc.addNotificationListener(this);
            addNotification(nc);
        });
    }

    private void addNotification(NotificationController nc) {
        notifications.add(nc);
        showNewNotification();
    }

    private void showNewNotification() {
        if (currentNotification == null) {
            currentNotification = notifications.poll();
            if (currentNotification != null) {
                currentNotification.showWithAutoClose();
                stage.requestFocus();
            }
        }
    }

    @Override
    public void onNotificationClosed(NotificationClosed e) {
        currentNotification = null;
        showNewNotification();
    }
}
