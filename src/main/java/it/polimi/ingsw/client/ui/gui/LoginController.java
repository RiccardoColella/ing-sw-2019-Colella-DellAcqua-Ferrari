package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.client.io.Connector;
import it.polimi.ingsw.client.io.RMIConnector;
import it.polimi.ingsw.client.io.SocketConnector;
import it.polimi.ingsw.client.io.listeners.DuplicatedNicknameListener;
import it.polimi.ingsw.client.io.listeners.MatchListener;
import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.shared.bootstrap.ClientInitializationInfo;
import it.polimi.ingsw.shared.events.networkevents.KillshotTrackChanged;
import it.polimi.ingsw.shared.events.networkevents.MatchEnded;
import it.polimi.ingsw.shared.events.networkevents.MatchModeChanged;
import it.polimi.ingsw.shared.events.networkevents.MatchStarted;
import it.polimi.ingsw.utils.EnumValueByString;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.logging.Logger;

public class LoginController extends WindowController implements MatchListener, DuplicatedNicknameListener {

    /**
     * Logging utility
     */
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @FXML
    private Label title;
    @FXML
    private TextField usernameField;
    @FXML
    private AnchorPane window;
    @FXML
    private TextField serverAddressField;
    @FXML
    private RadioButton socket;
    @FXML
    private RadioButton rmi;
    @FXML
    private RadioButton b1;
    @FXML
    private RadioButton b2;
    @FXML
    private RadioButton b3;
    @FXML
    private RadioButton b4;
    @FXML
    private BoardPane b1Img;
    @FXML
    private BoardPane b2Img;
    @FXML
    private BoardPane b3Img;
    @FXML
    private BoardPane b4Img;
    @FXML
    private ChoiceBox modeChoice;
    @FXML
    private ChoiceBox skullsChoice;

    private ToggleGroup toggleBoard;

    private List<BoardPane> boardPanes;

    private List<RadioButton> boardRadios;

    private GameController gameController;

    private Map<String, Match.Mode> modeChoiceMap = new HashMap<>();
    private Connector connector;

    public LoginController(String title) {
        super(title, "/fxml/login.fxml", "/css/login.css");
        modeChoiceMap.put("STANDARD", Match.Mode.STANDARD);
        modeChoiceMap.put("SUDDEN DEATH", Match.Mode.SUDDEN_DEATH);

        // Debug
        /*new Thread(() -> {
            try {
                Thread.sleep(5000);
                Platform.runLater(() -> {
                    usernameField.setText(UUID.randomUUID().toString());
                    boardRadios.get(0).setSelected(true);
                    socket.setSelected(true);
                    onSend();
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warning(e.toString());
            }
        }).start();*/
    }

    public LoginController() {
        this("Login");
    }

    @FXML
    public void initialize() {
        title.setText("ADRENALINA");
        ToggleGroup toggleGroup = new ToggleGroup();
        rmi.setToggleGroup(toggleGroup);
        socket.setToggleGroup(toggleGroup);
        toggleBoard = new ToggleGroup();
        b1.setToggleGroup(toggleBoard);
        b2.setToggleGroup(toggleBoard);
        b3.setToggleGroup(toggleBoard);
        b4.setToggleGroup(toggleBoard);
        boardPanes = new ArrayList<>();
        boardRadios = new ArrayList<>();
        boardPanes.add(b1Img);
        boardPanes.add(b2Img);
        boardPanes.add(b3Img);
        boardPanes.add(b4Img);
        boardRadios.add(b1);
        boardRadios.add(b2);
        boardRadios.add(b3);
        boardRadios.add(b4);
        setupViewport(window);
    }

    @FXML
    public void onSend() {
        if (usernameField.getText() == null || usernameField.getText().equals("")) {
            sendWarning("Enter a username");
        } else if (!rmi.isSelected() && !socket.isSelected()) {
            sendWarning("Select the connection type");
        } else if (serverAddressField.getText() == null || serverAddressField.getText().equals("")) {
            sendWarning("Enter a server address");
        } else if (toggleBoard.getSelectedToggle() == null) {
            sendWarning("Select a board");
        } else {
            connect(rmi.isSelected() ? "rmi" : "socket");
        }
    }

    private void connect(String connection) {
        ClientInitializationInfo info = new ClientInitializationInfo(
                usernameField.getText(),
                EnumValueByString.findByString(((RadioButton) toggleBoard.getSelectedToggle()).getText(), BoardFactory.Preset.class),
                Integer.parseInt(skullsChoice.getValue().toString()),
                modeChoiceMap.get(modeChoice.getValue().toString())
        );
        try {
            switch (connection) {
                case "rmi":
                    connector = new RMIConnector();
                    connector.addMatchListener(this);
                    connector.addDuplicatedNicknameListener(this);
                    ((RMIConnector) connector).initialize(info, new InetSocketAddress(serverAddressField.getText(), 9090));
                    break;
                case "socket":
                    connector = new SocketConnector();
                    connector.addMatchListener(this);
                    connector.addDuplicatedNicknameListener(this);
                    ((SocketConnector) connector).initialize(info, new InetSocketAddress(serverAddressField.getText(), 9000));
                    break;
                default:
                    throw new IllegalStateException("The user had to choose between Socket or RMI, unrecognized option " + connection);
            }
        } catch (Exception ex) {
            sendError("Server unavailable");
        }

    }

    public Optional<GameController> getGameController() {
        return Optional.ofNullable(gameController);
    }

    private void sendWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message);
        styleAlert(alert);
    }

    private void sendError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        styleAlert(alert);
    }

    private void styleAlert(Alert alert) {
        alert.setHeaderText(alert.getHeaderText().toUpperCase());
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/css/global.css").toExternalForm());
        alert.show();
    }

    @FXML
    private void selectBoard(MouseEvent e) {
        for (int i = 0; i < boardPanes.size(); i++) {
            boardPanes.get(i).getStyleClass().remove("selectedBoard");
            if (e.getSource() == boardPanes.get(i)) {
                boardPanes.get(i).getStyleClass().add("selectedBoard");
                boardRadios.get(i).setSelected(true);
            }
        }
    }

    @Override
    public void onMatchStarted(MatchStarted e) {
        connector.removeMatchListener(this);
        connector.removeDuplicatedNicknameListener(this);
        Platform.runLater(
                () -> {
                    this.gameController = new GameController(connector, e);
                    connector.addQuestionMessageReceivedListener(gameController);
                    connector.addMatchListener(gameController);
                    connector.addBoardListener(gameController);
                    connector.addPlayerListener(gameController);
                    connector.startListeningToQuestions();
                    this.close();
                }
        );

    }

    @Override
    public void onMatchModeChanged(MatchModeChanged e) {
        // Nothing to do here

    }

    @Override
    public void onKillshotTrackChanged(KillshotTrackChanged e) {
        // Nothing to do here

    }

    @Override
    public void onMatchEnded(MatchEnded e) {
        // Nothing to do here

    }

    @Override
    public void onDuplicatedNickname() {
        connector.removeMatchListener(this);
        connector.removeDuplicatedNicknameListener(this);
        Platform.runLater(() -> sendError("Nickname not available, change it and try again"));
        new Thread(() -> {
            try {
                connector.close();
            } catch (Exception ex) {
                logger.warning("Could not close the connector");
            }
        }).start();
    }
}
