package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.server.model.player.PlayerColor;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class BoardBlockPane extends GridPane {

    @FXML
    private FlowPane container;

    private Map<PlayerColor, ImagePane> avatars;

    public BoardBlockPane() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/boardBlock.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setControllerFactory(p -> this);
            fxmlLoader.load();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load image " + ex);
        }
        container.getStylesheets().add(getClass().getResource("/css/boardBlock.css").toExternalForm());
        avatars = new EnumMap<>(PlayerColor.class);
        for (PlayerColor color : PlayerColor.values()) {
            ImagePane img = new ImagePane(UrlFinder.findAvatar(color));
            img.setPrefSize(28, 28);
            img.setMinSize(28, 28);
            img.setMaxSize(28, 28);
            avatars.put(color, img);
        }
    }

    public void addPlayer(PlayerColor color) {
        container.getChildren().add(avatars.get(color));
    }

    public void removePlayer(PlayerColor color) {
        container.getChildren().remove(avatars.get(color));
    }
}
