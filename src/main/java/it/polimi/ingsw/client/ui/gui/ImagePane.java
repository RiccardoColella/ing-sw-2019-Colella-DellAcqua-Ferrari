package it.polimi.ingsw.client.ui.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.image.Image;
import javafx.scene.layout.*;

/**
 * This class represents an Image in JavaFX environments, the image is shown as the background of a Pane, so that its
 * dimension can be controlled more easily
 *
 * @author Adriana Ferrari
 */
public class ImagePane extends GridPane {

    /**
     * The main Pane
     */
    @FXML
    private Pane container;

    /**
     * Constant for centered alignment of the image
     */
    public static final BackgroundPosition CENTER = BackgroundPosition.CENTER;

    /**
     * Constant for right alignment of the image
     */
    public static final BackgroundPosition RIGHT = new BackgroundPosition(Side.RIGHT, 0, true, Side.TOP, 0, true);

    /**
     * Constant for left alignment of the image
     */
    public static final BackgroundPosition LEFT = BackgroundPosition.DEFAULT;

    /**
     * The source path of the image
     */
    private String src;

    /**
     * Full constructor
     *
     * @param src The path of the image
     * @param position The desired alignment for the image
     */
    public ImagePane(String src, BackgroundPosition position) {
        this();
        this.src = src;
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

    /**
     * Constructor with default alignment (centered)
     *
     * @param src The path of the image
     */
    public ImagePane(String src) {
        this(src, CENTER);
    }

    /**
     * No parameters constructor that allows for the image to be set later
     */
    public ImagePane() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/image.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setControllerFactory(p -> this);
            fxmlLoader.load();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load image " + ex);
        }
    }

    /**
     * Sets a new image with default alignment (centered)
     *
     * @param src The path of the image
     */
    public void setImg(String src) {
        setImg(src, CENTER);
    }

    /**
     * Sets a new image
     *
     * @param src The path of the image
     * @param position The desired alignment for the image
     */
    public void setImg(String src, BackgroundPosition position) {
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
        this.src = src;
    }

    /**
     * Loads the image from the given path
     *
     * @param src The path of the image
     * @return The Image found at the given path
     */
    private Image loadImg(String src) {
        return new Image(src, 0, 0, false, true);
    }

    /**
     * Gets the path of the current image
     * @return a String representing the path of the current image
     */
    public String getSrc() {
        return this.src;
    }

}
