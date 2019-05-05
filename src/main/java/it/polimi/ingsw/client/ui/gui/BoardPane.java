package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.utils.EnumValueByString;
import javafx.beans.NamedArg;
import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.image.Image;
import javafx.scene.layout.*;

import java.io.IOException;

public class BoardPane extends GridPane {

    @FXML
    private FlowPane boardLeft;

    @FXML
    private FlowPane boardRight;


    public BoardPane(@NamedArg("preset") BoardFactory.Preset preset) {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/board.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setControllerFactory(p -> this);
            try {
                fxmlLoader.load();
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to load resource files " + ex);
            }
            String leftURL;
            String rightURL;
            switch (preset) {
                case BOARD_1:
                case BOARD_3:
                    leftURL = "/assets/LEFT_1.png";
                    break;
                case BOARD_2:
                case BOARD_4:
                    leftURL = "/assets/LEFT_2.png";
                    break;
                default:
                    throw new EnumConstantNotPresentException(BoardFactory.Preset.class, "Unknown preset value: " + preset);
            }

            switch (preset) {
                case BOARD_1:
                case BOARD_2:
                    rightURL = "/assets/RIGHT_1.png";
                    break;
                case BOARD_3:
                case BOARD_4:
                    rightURL = "/assets/RIGHT_2.png";
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
            System.out.println(ex);
        }
    }
}
