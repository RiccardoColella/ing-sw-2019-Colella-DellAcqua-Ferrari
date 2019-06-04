package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.server.model.player.PlayerColor;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;

import java.util.EnumMap;
import java.util.Map;

/**
 * Represents a single block (cell) of the board game
 *
 * @author Adriana Ferrari
 */
public class BoardBlockPane extends GridPane {

    /**
     * The main container
     */
    @FXML
    private FlowPane container;

    /**
     * The possible avatars used by the players
     */
    private Map<PlayerColor, ImagePane> avatars;

    /**
     * The width of the player avatars
     */
    private static final int AVATAR_WIDTH = 28;

    /**
     * The height of the player avatars
     */
    private static final int AVATAR_HEIGHT = 28;

    /**
     * Default constructor
     */
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
            img.setPrefSize(AVATAR_WIDTH, AVATAR_HEIGHT);
            img.setMinSize(AVATAR_WIDTH, AVATAR_HEIGHT);
            img.setMaxSize(AVATAR_WIDTH, AVATAR_HEIGHT);
            avatars.put(color, img);
        }
    }

    /**
     * Adds a player to this block
     *
     * @param color the color of the player that should be added
     */
    public void addPlayer(PlayerColor color) {
        container.getChildren().add(avatars.get(color));
    }

    /**
     * Removes a player from this block
     *
     * @param color the color of the player that should be removed
     */
    public void removePlayer(PlayerColor color) {
        container.getChildren().remove(avatars.get(color));
    }

    /**
     * Adds a tile to this block
     *
     * @param tile the image representing the tile that should be added
     */
    public void addTile(ImagePane tile) {
        tile.setPrefSize(40, 40);
        tile.setMinSize(40, 40);
        tile.setMaxSize(40, 40);
        container.getChildren().add(tile);
    }

    /**
     * Clears the content of this block
     */
    public void clearCell() {
        container.getChildren().clear();
    }
}
