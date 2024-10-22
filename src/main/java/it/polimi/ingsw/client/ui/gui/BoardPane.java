package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import javafx.beans.NamedArg;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.image.Image;
import javafx.scene.layout.*;

/**
 * This class extends the GridPane element in order to represent the game field
 */
public class BoardPane extends GridPane {

    /**
     * The left side of the board
     */
    @FXML
    private FlowPane boardLeft;

    /**
     * The right side of the board
     */
    @FXML
    private FlowPane boardRight;

    /**
     * Constructor that takes a preset to load the actual board
     *
     * @param preset the preset used for the board
     */
    public BoardPane(@NamedArg("preset") BoardFactory.Preset preset) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/board.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setControllerFactory(p -> this);

            fxmlLoader.load();
            String leftURL;
            String rightURL;

            switch (preset) {
                case BOARD_1:
                case BOARD_2:
                    leftURL = "/assets/battlefield/LEFT_1.png";
                    break;
                case BOARD_3:
                case BOARD_4:
                    leftURL = "/assets/battlefield/LEFT_2.png";
                    break;
                default:
                    throw new EnumConstantNotPresentException(BoardFactory.Preset.class, "Unknown preset value: " + preset);
            }

            switch (preset) {
                case BOARD_1:
                case BOARD_3:
                    rightURL = "/assets/battlefield/RIGHT_2.png";
                    break;
                case BOARD_2:
                case BOARD_4:
                    rightURL = "/assets/battlefield/RIGHT_1.png";
                    break;
                default:
                    throw new EnumConstantNotPresentException(BoardFactory.Preset.class, "Unknown preset value: " + preset);
            }

            Image leftImg = new Image(leftURL, 0, 0, false, true);
            BackgroundPosition right = new BackgroundPosition(Side.RIGHT, 0, true, Side.TOP, 0, true);
            boardLeft.setBackground(
                    new Background(
                            new BackgroundImage(
                                    leftImg,
                                    BackgroundRepeat.NO_REPEAT,
                                    BackgroundRepeat.NO_REPEAT,
                                    right,
                                    new BackgroundSize(
                                            BackgroundSize.AUTO,
                                            BackgroundSize.AUTO,
                                            false,
                                            false,
                                            true,
                                            false
                                    )
                            )
                    )
            );

            Image rightImg = new Image(rightURL, 0, 0, false, true);
            boardRight.setBackground(
                    new Background(
                            new BackgroundImage(
                                    rightImg,
                                    BackgroundRepeat.NO_REPEAT,
                                    BackgroundRepeat.NO_REPEAT,
                                    BackgroundPosition.DEFAULT,
                                    new BackgroundSize(
                                            BackgroundSize.AUTO,
                                            BackgroundSize.AUTO,
                                            false,
                                            false,
                                            true,
                                            false
                                    )
                            )
                    )
            );
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load Board " + ex);
        }
    }
}