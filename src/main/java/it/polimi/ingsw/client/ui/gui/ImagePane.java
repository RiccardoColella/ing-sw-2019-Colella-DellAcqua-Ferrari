package it.polimi.ingsw.client.ui.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.image.Image;
import javafx.scene.layout.*;

public class ImagePane extends GridPane {

    @FXML
    private Pane container;

    public static final BackgroundPosition CENTER = BackgroundPosition.CENTER;
    public static final BackgroundPosition RIGHT = new BackgroundPosition(Side.RIGHT, 0, true, Side.TOP, 0, true);
    public static final BackgroundPosition LEFT = BackgroundPosition.DEFAULT;

    public ImagePane(String src, BackgroundPosition position) {
        this();
        container.setBackground(
                new Background(
                        new BackgroundImage(
                                loadImg(src),
                                BackgroundRepeat.NO_REPEAT,
                                BackgroundRepeat.NO_REPEAT,
                                position,
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
    }

    public ImagePane(String src) {
        this(src, CENTER);
    }

    public ImagePane() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/image.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setControllerFactory(p -> this);
            fxmlLoader.load();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load Board " + ex);
        }
    }

    public void setImg(String src) {
        setImg(src, CENTER);
    }

    public void setImg(String src, BackgroundPosition position) {
        if (container.getBackground() != null) {
            container.getBackground().getImages().clear();
        }
        container.setBackground(
                new Background(
                    new BackgroundImage(
                        loadImg(src),
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        position,
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
    }

    private Image loadImg(String src) {
        return new Image(src, 0, 0, false, true);
    }
}
