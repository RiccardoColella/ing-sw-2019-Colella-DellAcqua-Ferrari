package it.polimi.ingsw.client.ui;

import it.polimi.ingsw.server.model.match.Match;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseController {


    private final List<Region> autoResizableNodes = new LinkedList<>();
    private Pane window;

    protected void setupViewport(Pane window) {
        this.window = window;
        initializeViewport(window);
        autoResizeNodes(window.getPrefWidth(), window.getPrefHeight());
    }

    private enum Dimension {
        WIDTH,
        HEIGHT
    }

    private void setViewportSize(double vw, double vh, double parentWidth, double parentHeight, Dimension dimension, Consumer<Double> setter, String rawValue) {

        Matcher m = Pattern.compile("^(\\d+)(.*)$").matcher(rawValue);
        if (!m.find()) {
            throw new IllegalArgumentException("Unsupported format " + rawValue);
        }
        int height = Integer.parseInt(m.group(1));
        String measurementUnit = m.group(2);
        switch (measurementUnit) {
            case "vw":
                setter.accept(vw * height / 100);
                break;
            case "vh":
                setter.accept(vh * height / 100);
                break;
            case "%":
                setter.accept((dimension == Dimension.WIDTH ? parentWidth : parentHeight) * height / 100);
                break;
            default:
                throw new IllegalArgumentException("Unsupported measurement unit");
        }
    }

    protected void autoResizeNodes(double vw, double vh) {
        for (Region node : autoResizableNodes) {
            if (node.getProperties().containsKey("height")) {
                setViewportSize(
                        vw,
                        vh,
                        node.getParent() != null ? node.getParent().prefWidth(0) : node.prefWidth(0),
                        node.getParent() != null ? node.getParent().prefHeight(0) : node.prefHeight(0),
                        Dimension.HEIGHT,
                        node::setPrefHeight,
                        (String)node.getProperties().get("height")
                );
            }
            if (node.getProperties().containsKey("width")) {
                setViewportSize(
                        vw,
                        vh,
                        node.getParent() != null ? node.getParent().prefWidth(0) : node.prefWidth(0),
                        node.getParent() != null ? node.getParent().prefHeight(0) : node.prefHeight(0),
                        Dimension.WIDTH,
                        node::setPrefWidth,
                        (String)node.getProperties().get("width")
                );
            }

            readPadding(node, vw, vh);

            if (node instanceof FlowPane) {
                readFlowPane((FlowPane) node, vw, vh);
            }

        }
    }

    private void readFlowPane(FlowPane node, double vw, double vh) {
        if (node.getProperties().containsKey("vgap")) {
            setViewportSize(
                    vw,
                    vh,
                    node.getParent() != null ? node.getParent().prefWidth(0) : node.prefWidth(0),
                    node.getParent() != null ? node.getParent().prefHeight(0) : node.prefHeight(0),
                    Dimension.HEIGHT,
                    node::setVgap,
                    (String)node.getProperties().get("vgap")
            );
        }
        if (node.getProperties().containsKey("hgap")) {
            setViewportSize(
                    vw,
                    vh,
                    node.getParent() != null ? node.getParent().prefWidth(0) : node.prefWidth(0),
                    node.getParent() != null ? node.getParent().prefHeight(0) : node.prefHeight(0),
                    Dimension.WIDTH,
                    node::setHgap,
                    (String)node.getProperties().get("hgap")
            );
        }
        if (node.getProperties().containsKey("wrap")) {
            setViewportSize(
                    vw,
                    vh,
                    node.getParent() != null ? node.getParent().prefWidth(0) : node.prefWidth(0),
                    node.getParent() != null ? node.getParent().prefHeight(0) : node.prefHeight(0),
                    Dimension.WIDTH,
                    node::setPrefWrapLength,
                    (String)node.getProperties().get("wrap")
            );
        }
    }

    private void readPadding(Region node, double vw, double vh) {
        if (node.getProperties().containsKey("padding")) {
            setViewportSize(
                    vw,
                    vh,
                    node.getParent() != null ? node.getParent().prefWidth(0) : node.prefWidth(0),
                    node.getParent() != null ? node.getParent().prefHeight(0) : node.prefHeight(0),
                    Dimension.WIDTH,
                    padding -> node.setPadding(new Insets(padding)),
                    (String)node.getProperties().get("padding")
            );
        }

        if (node.getProperties().containsKey("padding-h")) {
            setViewportSize(
                    vw,
                    vh,
                    node.getParent() != null ? node.getParent().prefWidth(0) : node.prefWidth(0),
                    node.getParent() != null ? node.getParent().prefHeight(0) : node.prefHeight(0),
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
                    node.getParent() != null ? node.getParent().prefWidth(0) : node.prefWidth(0),
                    node.getParent() != null ? node.getParent().prefHeight(0) : node.prefHeight(0),
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
                    node.getParent() != null ? node.getParent().prefWidth(0) : node.prefWidth(0),
                    node.getParent() != null ? node.getParent().prefHeight(0) : node.prefHeight(0),
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
                    node.getParent() != null ? node.getParent().prefWidth(0) : node.prefWidth(0),
                    node.getParent() != null ? node.getParent().prefHeight(0) : node.prefHeight(0),
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
                    node.getParent() != null ? node.getParent().prefWidth(0) : node.prefWidth(0),
                    node.getParent() != null ? node.getParent().prefHeight(0) : node.prefHeight(0),
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
                    node.getParent() != null ? node.getParent().prefWidth(0) : node.prefWidth(0),
                    node.getParent() != null ? node.getParent().prefHeight(0) : node.prefHeight(0),
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

    protected void initializeViewport(Region parent) {

        if (parent.hasProperties()) {
            autoResizableNodes.add(parent);
        }

        for (Node child : parent.getChildrenUnmodifiable()) {

            if (child instanceof Region) {
                if (child.hasProperties()) {
                    autoResizableNodes.add((Region)child);
                }
                initializeViewport((Region)child);
            }
        }
    }

    public void onResize() {
        autoResizeNodes(window.getWidth(), window.getHeight());
    }

}
