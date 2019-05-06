package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.client.io.Connector;
import it.polimi.ingsw.client.io.RMIConnector;
import it.polimi.ingsw.client.io.SocketConnector;
import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.shared.bootstrap.ClientInitializationInfo;
import it.polimi.ingsw.utils.EnumValueByString;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoginController extends WindowController {
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

    private Connector connector;

    public LoginController(String title) {
        super(title, "/fxml/login.fxml", "/css/login.css");
    }

    public LoginController() {
        this("Login");
    }

    public Optional<Connector> getConnector() {
        return Optional.ofNullable(connector);
    }

    @FXML
    public void initialize() {
        title.setText("ADRENALINA");
        window.setMinWidth(600);
        window.setMinHeight(400);
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
                EnumValueByString.findByString(modeChoice.getValue().toString(), Match.Mode.class)
        );
        try {
            if (connection.equals("rmi")) {
                connector = new RMIConnector(new InetSocketAddress(serverAddressField.getText(), 9090));
            } else {
                connector = new SocketConnector(new InetSocketAddress(serverAddressField.getText(), 9000));
            }
        } catch (Exception ex) {
            sendError("Server unavailable");
        }

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
}
