package it.polimi.ingsw.client.ui.gui;

import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.image.Image;
import javafx.scene.layout.*;

public class GameController extends WindowController {
    @FXML
    private AnchorPane window;

    @FXML
    private BoardPane board;

    @FXML
    private FlowPane boardLeft;

    @FXML
    private FlowPane boardRight;

    public GameController() {
        super("Adrenalina", "/fxml/game.fxml", "/css/game.css");
    }

    @FXML
    public void initialize() {
        window.setMinWidth(600);
        window.setMinHeight(400);
        setupViewport(window);
        Image left = new Image("/assets/LEFT_1.png", 0, 0, false, true);
        BackgroundPosition right = new BackgroundPosition(Side.RIGHT, 0, true, Side.TOP, 0, true);
        boardLeft.setBackground(
                new Background(
                        new BackgroundImage(
                                left,
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
        boardRight.setBackground(
                new Background(
                        new BackgroundImage(
                                new Image("/assets/RIGHT_1.png"),
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
        /*
        Image boardLeft1 = new Image("/assets/LEFT_1.png");
        boardLeftView.setImage(boardLeft1);
        boardLeftView.setFitWidth(boardLeftView.getParent().prefWidth(0) * 47 / 100 - 1);
        boardLeftView.setFitHeight(boardLeftView.getParent().prefHeight(0));
        boardLeftView.setPreserveRatio(true);
        Image boardRight = new Image("/assets/RIGHT_1.png");
        boardRightView.setImage(boardRight);
        boardRightView.setFitWidth(boardRightView.getParent().prefWidth(0) * 53 / 100 - 1);
        boardRightView.setFitHeight(boardRightView.getParent().prefHeight(0));
        boardRightView.setPreserveRatio(true);

        ((Pane) boardLeftView.getParent()).widthProperty().addListener((a, b, c) -> {
            boardLeftView.setFitWidth(boardLeftView.getParent().prefWidth(0));
        });*/

    }



}
