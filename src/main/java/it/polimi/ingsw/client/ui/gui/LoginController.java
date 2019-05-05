package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.client.io.Connector;
import it.polimi.ingsw.client.io.RMIConnector;
import it.polimi.ingsw.client.io.SocketConnector;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;

import java.net.InetSocketAddress;
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
        ToggleGroup toggleBoard = new ToggleGroup();
        b1.setToggleGroup(toggleBoard);
        b2.setToggleGroup(toggleBoard);
        b3.setToggleGroup(toggleBoard);
        b4.setToggleGroup(toggleBoard);
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
        } else {
            connect(usernameField.getText(), rmi.isSelected() ? "rmi" : "socket", serverAddressField.getText());
        }
    }

    private void connect(String nickname, String connection, String serverAddress) {
        try {
            if (connection.equals("rmi")) {
                connector = new RMIConnector(new InetSocketAddress(serverAddress, 9090));
            } else {
                connector = new SocketConnector(new InetSocketAddress(serverAddress, 9000));
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
}
