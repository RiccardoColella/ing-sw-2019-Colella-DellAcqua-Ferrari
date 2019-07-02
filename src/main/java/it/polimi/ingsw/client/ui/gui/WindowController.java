package it.polimi.ingsw.client.ui.gui;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Representation of the main window of the GUI, which should be extended by the specific implementations, such as the
 * actual game window or the login screen
 *
 * @author Carlo Dell'Acqua, Adriana Ferrari
 */
public abstract class WindowController {

    /**
     * Nodes that are adaptive when the screen is resized
     */
    private final List<Region> autoResizableNodes = new LinkedList<>();

    /**
     * The main Pane
     */
    private Pane window;

    /**
     * The Stage containing the scene
     */
    protected Stage stage = new Stage();

    /**
     * Constructor which allows to set a title, a fxml source and multiple CSS stylesheets
     * @param title the title of the window
     * @param fxml the path to the fxml file
     * @param customStylesheets a list of the CSS stylesheets to apply
     */
    protected WindowController(String title, String fxml, List<String> customStylesheets) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            loader.setControllerFactory(p -> this);
            Pane root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/global.css").toExternalForm());
            for (String customStylesheet : customStylesheets) {
                scene.getStylesheets().add(getClass().getResource(customStylesheet).toExternalForm());
            }
            stage.setScene(scene);
            stage.setMinWidth(root.getMinWidth());
            stage.setMinHeight(root.getMinHeight());
            stage.widthProperty().addListener((obs, obj, newVal) -> this.onResize());
            stage.heightProperty().addListener((obs, obj, newVal) -> this.onResize());
            stage.maximizedProperty().addListener((obs, obj, newVal) -> this.onResize());
            stage.setTitle(title);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load resource files " + ex);
        }
    }

    /**
     * Constructor which allows to set a title, a fxml source and a single CSS stylesheet
     * @param title the title of the window
     * @param fxml the path to the fxml file
     * @param customStylesheet the path to the CSS file
     */
    protected WindowController(String title, String fxml, String customStylesheet) {
        this(title, fxml, Collections.singletonList(customStylesheet));
    }

    /**
     * Constructor which allows to set a title and a fxml source
     * @param title the title of the window
     * @param fxml the path to the fxml file
     */
    protected WindowController(String title, String fxml) {
        this(title, fxml, Collections.emptyList());
    }

    /**
     * Constructor which allows to a fxml source
     * @param fxml the path to the fxml file
     */
    protected WindowController(String fxml) {
        this("", fxml);
    }

    /**
     * Shows the window in a non-blocking way
     */
    public void show() {
        stage.show();
    }

    /**
     * Shows the window in a blocking way
     */
    public void showAsModal() {
        stage.showAndWait();
    }

    /**
     * Closes the stage associated to this window
     */
    public void close() {
        this.stage.close();
    }

    /**
     * Sets the viewport used for properties represented in vw and vh
     * @param window the viewport
     */
    protected void setupViewport(Pane window) {
        this.window = window;
        initializeViewport(window);
    }

    /**
     * Enum of possible dimensions
     */
    private enum Dimension {
        WIDTH,
        HEIGHT
    }

    /**
     * Gives the consumer an updated value based on the viewport dimension
     *
     * @param vw the width of the viewport
     * @param vh the height of the viewport
     * @param parentWidth the width of the parent element
     * @param parentHeight the height of the parent element
     * @param dimension the dimension of the parent element to which the measurement is relative
     * @param setter the consumer of the updated double value
     * @param rawValue the string representing the actual measurement unit in vw, vh or %
     */
    private void setViewportSize(double vw, double vh, double parentWidth, double parentHeight, Dimension dimension, Consumer<Double> setter, String rawValue) {

        Matcher m = Pattern.compile("^(\\d+(\\.\\d+)?)(.*)$").matcher(rawValue);
        if (!m.find()) {
            throw new IllegalArgumentException("Unsupported format " + rawValue);
        }
        double height = Double.parseDouble(m.group(1));
        String measurementUnit = m.group(3);
        switch (measurementUnit) {
            case "vw":
                setter.accept(Math.floor(vw * height / 100));
                break;
            case "vh":
                setter.accept(Math.floor(vh * height / 100));
                break;
            case "%":
                setter.accept(Math.floor((dimension == Dimension.WIDTH ? parentWidth : parentHeight) * height / 100));
                break;
            default:
                throw new IllegalArgumentException("Unsupported measurement unit");
        }
    }

    /**
     * Resizes the autoresizable nodes according to the new size of the main window
     *
     * @param vw viewport width
     * @param vh viewporth height
     */
    protected void autoResizeNodes(double vw, double vh) {
        for (Region node : autoResizableNodes) {
            if (node.getProperties().containsKey("height")) {
                setViewportSize(
                        vw,
                        vh,
                        node.getParent() != null ? node.getParent().prefWidth(0) : node.getPrefWidth(),
                        node.getParent() != null ? node.getParent().prefHeight(0) : node.getPrefHeight(),
                        Dimension.HEIGHT,
                        node::setPrefHeight,
                        (String)node.getProperties().get("height")
                );
            }
            if (node.getProperties().containsKey("width")) {
                setViewportSize(
                        vw,
                        vh,
                        node.getParent() != null ? node.getParent().prefWidth(0) : node.getPrefWidth(),
                        node.getParent() != null ? node.getParent().prefHeight(0) : node.getPrefHeight(),
                        Dimension.WIDTH,
                        node::setPrefWidth,
                        (String)node.getProperties().get("width")
                );
            }

            readPadding(node, vw, vh);

            if (node instanceof FlowPane) {
                readFlowPane((FlowPane) node, vw, vh);
            }

            if (node instanceof GridPane) {
                readGridPane((GridPane) node, vw, vh);
            }
        }
    }

    /**
     * If a node is a FlowPane, it has more properties that can be set: vgap, hgap and wrap
     *
     * @param node the FlowPane that shall be resized
     * @param vw viewport width
     * @param vh viewport height
     */
    private void readFlowPane(FlowPane node, double vw, double vh) {
        if (node.getProperties().containsKey("vgap")) {
            setViewportSize(
                    vw,
                    vh,
                    node.getParent() != null ? node.getParent().prefWidth(0) : node.getPrefWidth(),
                    node.getParent() != null ? node.getParent().prefHeight(0) : node.getPrefHeight(),
                    Dimension.HEIGHT,
                    node::setVgap,
                    (String)node.getProperties().get("vgap")
            );
        }
        if (node.getProperties().containsKey("hgap")) {
            setViewportSize(
                    vw,
                    vh,
                    node.getParent() != null ? node.getParent().prefWidth(0) : node.getPrefWidth(),
                    node.getParent() != null ? node.getParent().prefHeight(0) : node.getPrefHeight(),
                    Dimension.WIDTH,
                    node::setHgap,
                    (String)node.getProperties().get("hgap")
            );
        }
        if (node.getProperties().containsKey("wrap")) {
            setViewportSize(
                    vw,
                    vh,
                    node.getParent() != null ? node.getParent().prefWidth(0) : node.getPrefWidth(),
                    node.getParent() != null ? node.getParent().prefHeight(0) : node.getPrefHeight(),
                    Dimension.WIDTH,
                    node::setPrefWrapLength,
                    (String)node.getProperties().get("wrap")
            );
        }
    }

    /**
     * If a node is a GridPane, it has more properties that can be set: vgap and hgap
     *
     * @param node the GridPane that shall be resized
     * @param vw viewport width
     * @param vh viewport height
     */
    private void readGridPane(GridPane node, double vw, double vh) {
        if (node.getProperties().containsKey("vgap")) {
            setViewportSize(
                    vw,
                    vh,
                    node.getParent() != null ? node.getParent().prefWidth(0) : node.getPrefWidth(),
                    node.getParent() != null ? node.getParent().prefHeight(0) : node.getPrefHeight(),
                    Dimension.HEIGHT,
                    node::setVgap,
                    (String)node.getProperties().get("vgap")
            );
        }
        if (node.getProperties().containsKey("hgap")) {
            setViewportSize(
                    vw,
                    vh,
                    node.getParent() != null ? node.getParent().prefWidth(0) : node.getPrefWidth(),
                    node.getParent() != null ? node.getParent().prefHeight(0) : node.getPrefHeight(),
                    Dimension.WIDTH,
                    node::setHgap,
                    (String)node.getProperties().get("hgap")
            );
        }
    }

    /**
     * Updates the padding of a node according to the dimension of the whole window
     *
     * @param node the node that shall be updated
     * @param vw viewport width
     * @param vh viewport height
     */
    private void readPadding(Region node, double vw, double vh) {
        if (node.getProperties().containsKey("padding")) {
            setViewportSize(
                    vw,
                    vh,
                    node.getParent() != null ? node.getParent().prefWidth(0) : node.getPrefWidth(),
                    node.getParent() != null ? node.getParent().prefHeight(0) : node.getPrefHeight(),
                    Dimension.WIDTH,
                    padding -> node.setPadding(new Insets(padding)),
                    (String)node.getProperties().get("padding")
            );
        }

        if (node.getProperties().containsKey("padding-h")) {
            setViewportSize(
                    vw,
                    vh,
                    node.getParent() != null ? node.getParent().prefWidth(0) : node.getPrefWidth(),
                    node.getParent() != null ? node.getParent().prefHeight(0) : node.getPrefHeight(),
                    Dimension.WIDTH,
                    padding -> node.setPadding(
                            new Insets(
                                    node.getPadding().getTop(),
                                    padding,
                                    node.getPadding().getBottom(),
                                    padding
                            )
                    ),
                    (String)node.getProperties().get("padding-h")
            );
        }
        if (node.getProperties().containsKey("padding-v")) {
            setViewportSize(
                    vw,
                    vh,
                    node.getParent() != null ? node.getParent().prefWidth(0) : node.getPrefWidth(),
                    node.getParent() != null ? node.getParent().prefHeight(0) : node.getPrefHeight(),
                    Dimension.HEIGHT,
                    padding -> node.setPadding(
                            new Insets(
                                    padding,
                                    node.getPadding().getRight(),
                                    padding,
                                    node.getPadding().getLeft()
                            )
                    ),
                    (String)node.getProperties().get("padding-v")
            );
        }

        if (node.getProperties().containsKey("padding-left")) {
            setViewportSize(
                    vw,
                    vh,
                    node.getParent() != null ? node.getParent().prefWidth(0) : node.getPrefWidth(),
                    node.getParent() != null ? node.getParent().prefHeight(0) : node.getPrefHeight(),
                    Dimension.WIDTH,
                    padding -> node.setPadding(
                            new Insets(
                                    node.getPadding().getTop(),
                                    node.getPadding().getRight(),
                                    node.getPadding().getBottom(),
                                    padding
                            )
                    ),
                    (String)node.getProperties().get("padding-left")
            );
        }
        if (node.getProperties().containsKey("padding-right")) {
            setViewportSize(
                    vw,
                    vh,
                    node.getParent() != null ? node.getParent().prefWidth(0) : node.getPrefWidth(),
                    node.getParent() != null ? node.getParent().prefHeight(0) : node.getPrefHeight(),
                    Dimension.WIDTH,
                    padding -> node.setPadding(
                            new Insets(
                                    node.getPadding().getTop(),
                                    padding,
                                    node.getPadding().getBottom(),
                                    node.getPadding().getLeft()
                            )
                    ),
                    (String)node.getProperties().get("padding-right")
            );
        }
        if (node.getProperties().containsKey("padding-top")) {
            setViewportSize(
                    vw,
                    vh,
                    node.getParent() != null ? node.getParent().prefWidth(0) : node.getPrefWidth(),
                    node.getParent() != null ? node.getParent().prefHeight(0) : node.getPrefHeight(),
                    Dimension.HEIGHT,
                    padding -> node.setPadding(
                            new Insets(
                                    padding,
                                    node.getPadding().getRight(),
                                    node.getPadding().getBottom(),
                                    node.getPadding().getLeft()
                            )
                    ),
                    (String)node.getProperties().get("padding-top")
            );
        }
        if (node.getProperties().containsKey("padding-bottom")) {
            setViewportSize(
                    vw,
                    vh,
                    node.getParent() != null ? node.getParent().prefWidth(0) : node.getPrefWidth(),
                    node.getParent() != null ? node.getParent().prefHeight(0) : node.getPrefHeight(),
                    Dimension.HEIGHT,
                    padding -> node.setPadding(
                            new Insets(
                                    node.getPadding().getTop(),
                                    node.getPadding().getRight(),
                                    padding,
                                    node.getPadding().getLeft()
                            )
                    ),
                    (String)node.getProperties().get("padding-bottom")
            );
        }
    }

    /**
     * Initializes the viewport and its children, which are added to the autoResizableNodes if they have adaptive properties
     *
     * @param parent the viewport
     */
    protected void initializeViewport(Region parent) {
        for (Node child : parent.getChildrenUnmodifiable()) {

            if (child instanceof Region) {
                if (child.hasProperties()) {
                    autoResizableNodes.add((Region)child);
                }
                initializeViewport((Region)child);
            }
        }
    }

    /**
     * Updates the elements of the window when the viewport is resized
     */
    public void onResize() {
        autoResizeNodes(window.getWidth(), window.getHeight());
    }

}
