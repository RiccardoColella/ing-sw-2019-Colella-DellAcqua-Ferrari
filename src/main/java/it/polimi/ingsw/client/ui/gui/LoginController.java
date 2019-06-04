package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.client.io.Connector;
import it.polimi.ingsw.client.io.RMIConnector;
import it.polimi.ingsw.client.io.SocketConnector;
import it.polimi.ingsw.client.io.listeners.ClientListener;
import it.polimi.ingsw.client.io.listeners.DuplicatedNicknameListener;
import it.polimi.ingsw.client.io.listeners.MatchListener;
import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.shared.bootstrap.ClientInitializationInfo;
import it.polimi.ingsw.shared.events.networkevents.*;
import it.polimi.ingsw.utils.EnumValueByString;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.logging.Logger;

/**
 * Class that manages the login screen
 *
 * @author Adriana Ferrari
 */
public class LoginController extends WindowController implements MatchListener, DuplicatedNicknameListener, ClientListener {

    /**
     * Logging utility
     */
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * The title of the login screen
     */
    @FXML
    private Label title;

    /**
     * The field containing the username entered by the user
     */
    @FXML
    private TextField usernameField;

    /**
     * The container window
     */
    @FXML
    private GridPane window;

    /**
     * The field containing the server address entered by the user
     */
    @FXML
    private TextField serverAddressField;

    /**
     * Radio button selecting socket
     */
    @FXML
    private RadioButton socket;

    /**
     * Radio button selecting rmi
     */
    @FXML
    private RadioButton rmi;

    /**
     * Radio button selecting the first board
     */
    @FXML
    private RadioButton b1;

    /**
     * Radio button selecting the second board
     */
    @FXML
    private RadioButton b2;

    /**
     * Radio button selecting the third board
     */
    @FXML
    private RadioButton b3;

    /**
     * Radio button selecting the fourth board
     */
    @FXML
    private RadioButton b4;

    /**
     * Image of the first board
     */
    @FXML
    private BoardPane b1Img;

    /**
     * Image of the second board
     */
    @FXML
    private BoardPane b2Img;

    /**
     * Image of the third board
     */
    @FXML
    private BoardPane b3Img;

    /**
     * Image of the fourth board
     */
    @FXML
    private BoardPane b4Img;

    /**
     * Allows the selection of the match mode
     */
    @FXML
    private ChoiceBox modeChoice;

    /**
     * Allows the selection of the number of skulls for the match
     */
    @FXML
    private ChoiceBox skullsChoice;

    /**
     * Toggle group for the boards
     */
    private ToggleGroup toggleBoard;

    /**
     * List of the possible boards
     */
    private List<BoardPane> boardPanes;

    /**
     * List of the radios related to the boards
     */
    private List<RadioButton> boardRadios;

    /**
     * The game controller which will manage the main game screen
     */
    private GameController gameController;

    /**
     * Maps the string representing the match mode to its corresponding enum
     */
    private Map<String, Match.Mode> modeChoiceMap = new HashMap<>();

    /**
     * The connector used to establish the connection with the server
     */
    private Connector connector;

    /**
     * The button which will send the inserted data to the server
     */
    private Button sendButton;

    /**
     * Whether the connector should be closed if the window is closed by the user
     */
    private boolean closeConnector = true;

    /**
     * Constructor which allows to set a custom title
     *
     * @param title the custom title
     */
    public LoginController(String title) {
        super(title, "/fxml/login.fxml", "/css/login.css");
        modeChoiceMap.put("STANDARD", Match.Mode.STANDARD);
        modeChoiceMap.put("SUDDEN DEATH", Match.Mode.SUDDEN_DEATH);
        stage.setOnCloseRequest(ignored -> this.close());
    }

    /**
     * Default constructor
     */
    public LoginController() {
        this("Login");
    }

    /**
     * Initializes the controller of the FXML
     */
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

    /**
     * Manages the sending of the data entered by the user
     *
     * @param e the mouse event triggered by the user
     */
    @FXML
    public void onSend(MouseEvent e) {
        sendButton = ((Button) e.getSource());
        sendButton.setDisable(true);
        if (usernameField.getText() == null || usernameField.getText().equals("")) {
            sendWarning("Enter a username");
            sendButton.setDisable(false);
        } else if (!rmi.isSelected() && !socket.isSelected()) {
            sendWarning("Select the connection type");
            sendButton.setDisable(false);
        } else if (serverAddressField.getText() == null || serverAddressField.getText().equals("")) {
            sendWarning("Enter a server address");
            sendButton.setDisable(false);
        } else if (toggleBoard.getSelectedToggle() == null) {
            sendWarning("Select a board");
            sendButton.setDisable(false);
        } else {
            connect(rmi.isSelected() ? "rmi" : "socket");
        }
    }

    /**
     * Attempts a connection to the given server address
     *
     * @param connection the type of connection (rmi or socket)
     */
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
                    connector.addClientListener(this);
                    ((RMIConnector) connector).initialize(info, new InetSocketAddress(serverAddressField.getText(), 9090));
                    break;
                case "socket":
                    connector = new SocketConnector();
                    connector.addMatchListener(this);
                    connector.addDuplicatedNicknameListener(this);
                    connector.addClientListener(this);
                    ((SocketConnector) connector).initialize(info, new InetSocketAddress(serverAddressField.getText(), 9001));
                    break;
                default:
                    throw new IllegalStateException("The user had to choose between Socket or RMI, unrecognized option " + connection);
            }
        } catch (Exception ex) {
            sendError("Server unavailable");
            sendButton.setDisable(false);
        }

    }

    /**
     * Gets the GameController created by this login, if any
     * @return an optional with the GameController or an empty optional
     */
    public Optional<GameController> getGameController() {
        return Optional.ofNullable(gameController);
    }

    /**
     * Shows a warning message
     *
     * @param message the message that will be displayed
     */
    private void sendWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message);
        styleAlert(alert);
    }

    /**
     * Shows an error message
     *
     * @param message the message that will be displayed
     */
    private void sendError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        styleAlert(alert);
    }

    /**
     * Styles alert messages
     *
     * @param alert the alert message that shall be styled
     */
    private void styleAlert(Alert alert) {
        alert.setHeaderText(alert.getHeaderText().toUpperCase());
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/css/global.css").toExternalForm());
        alert.show();
    }

    /**
     * Select one of the boards based on user input
     * @param e the mouse click event
     */
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

    /**
     * Handles the start of a new match
     *
     * @param e the MatchStarted event sent by the server
     */
    @Override
    public void onMatchStarted(MatchStarted e) {
        closeConnector = false;
        connector.removeMatchListener(this);
        connector.removeDuplicatedNicknameListener(this);
        connector.removeClientListener(this);
        Platform.runLater(
                () -> {
                    this.gameController = new GameController(connector, e);
                    connector.addClientListener(gameController);
                    connector.addQuestionMessageReceivedListener(gameController);
                    connector.addMatchListener(gameController);
                    connector.addBoardListener(gameController);
                    connector.addPlayerListener(gameController);
                    connector.startListeningToQuestions();
                    this.close();
                }
        );

    }

    /**
     * MatchModeChanged event is ignored by the LoginController
     * @param e the MatchModeChanged event
     */
    @Override
    public void onMatchModeChanged(MatchModeChanged e) {
        // Nothing to do here

    }

    /**
     * KillshotTrackChanged event is ignored by the LoginController
     * @param e the KillshotTrackChanged event
     */
    @Override
    public void onKillshotTrackChanged(KillshotTrackChanged e) {
        // Nothing to do here

    }

    /**
     * MatchEnded event is ignored by the LoginController
     * @param e the MatchEnded event
     */
    @Override
    public void onMatchEnded(MatchEnded e) {
        // Nothing to do here

    }

    /**
     * Handles the creation of a GameController when the match is resumed
     * @param e the MatchResumed event
     */
    @Override
    public void onMatchResumed(MatchResumed e) {
        closeConnector = false;
        connector.removeMatchListener(this);
        connector.removeDuplicatedNicknameListener(this);
        connector.removeClientListener(this);
        Platform.runLater(
                () -> {
                    this.gameController = new GameController(connector, e);
                    connector.addClientListener(gameController);
                    connector.addQuestionMessageReceivedListener(gameController);
                    connector.addMatchListener(gameController);
                    connector.addBoardListener(gameController);
                    connector.addPlayerListener(gameController);
                    connector.startListeningToQuestions();
                    this.close();
                }
        );
    }

    /**
     * Resets the connector if the inserted nickname is not valid
     */
    @Override
    public void onDuplicatedNickname() {
        connector.removeMatchListener(this);
        connector.removeDuplicatedNicknameListener(this);
        connector.removeClientListener(this);
        Platform.runLater(() -> {
            sendError("Nickname not available, change it and try again");
            sendButton.setDisable(false);
        });
        new Thread(() -> {
            try {
                connector.close();
            } catch (Exception ex) {
                logger.warning("Could not close the connector");
            }
        }).start();
    }

    /**
     * Closes itself and the connector
     */
    @Override
    public void close() {
        super.close();
        if (connector != null && closeConnector) {
            new Thread(() -> {
                try {
                    connector.close();
                    System.exit(0);
                } catch (Exception ex) {
                    logger.warning("Could not close the connector");
                }
            }).start();
        }
    }

    /**
     * Shows the waiting screen after a connection has been successfully established and sends a notification for the new connected clients
     *
     *  @param e the ClientEvent signalling the connection
     */
    @Override
    public void onLoginSuccess(ClientEvent e) {
        Platform.runLater(() -> {
            window.getChildren().clear();
            Label text = new Label("Login successful, waiting...");
            text.getStyleClass().add("fieldDescriptor");
            GridPane.setHalignment(text, HPos.CENTER);
            GridPane.setValignment(text, VPos.CENTER);
            window.add(text, 0, 0);
            NotificationController nc = new NotificationController("Connection", e.getNickname() + " connected");
            nc.showWithAutoClose();
        });

    }

    /**
     * Sends a notification if one of the previously connected clients disconnects from the server
     *
     * @param e the ClientEvent signalling the end of the connection
     */
    @Override
    public void onClientDisconnected(ClientEvent e) {
        Platform.runLater(() -> {
            NotificationController nc = new NotificationController("Disconnection", e.getNickname() + " disconnected");
            nc.showWithAutoClose();
        });
    }
}
