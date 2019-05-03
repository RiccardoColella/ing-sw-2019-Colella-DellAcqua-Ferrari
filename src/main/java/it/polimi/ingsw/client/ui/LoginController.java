package it.polimi.ingsw.client.ui;

import javafx.fxml.FXML;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;

import java.util.LinkedList;
import java.util.List;

public class LoginController extends BaseController {
    @FXML
    private Label title;
    @FXML
    private TextField usernameField;
    @FXML
    private FlowPane window;
    @FXML
    private Label username;
    @FXML
    private FlowPane connectionPane;
    @FXML
    private FlowPane usernamePane;
    @FXML
    private FlowPane serverAddressPane;
    @FXML
    private FlowPane choices;
    @FXML
    private Label serverAddress;
    @FXML
    private Label connection;
    @FXML
    private TextField serverAddressField;
    @FXML
    private RadioButton socket;
    @FXML
    private RadioButton rmi;

    @FXML
    public void initialize() {
        title.setText("ADRENALINA");
        window.setMinWidth(600);
        window.setMinHeight(400);
        ToggleGroup toggleGroup = new ToggleGroup();
        rmi.setToggleGroup(toggleGroup);
        socket.setToggleGroup(toggleGroup);

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
            //TODO: connect to the server
            System.out.println("Nice");
        }
    }

    private void sendWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/css/global.css").toExternalForm());
        alert.show();
    }
}
