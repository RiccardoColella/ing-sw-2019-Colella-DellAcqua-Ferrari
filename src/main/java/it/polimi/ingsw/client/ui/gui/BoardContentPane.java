package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.server.model.player.PlayerColor;
import it.polimi.ingsw.utils.Tuple;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class BoardContentPane extends GridPane {

    private int killshotIndex;
    @FXML
    private GridPane weaponTopContainer0;
    @FXML
    private GridPane weaponTopContainer1;
    @FXML
    private GridPane weaponTopContainer2;
    @FXML
    private GridPane weaponLeftContainer0;
    @FXML
    private GridPane weaponLeftContainer1;
    @FXML
    private GridPane weaponLeftContainer2;
    @FXML
    private GridPane weaponRightContainer0;
    @FXML
    private GridPane weaponRightContainer1;
    @FXML
    private GridPane weaponRightContainer2;
    @FXML
    private StackPane container;
    @FXML
    private GridPane skullContainer;
    @FXML
    private GridPane playerContainer;
    @FXML
    private GridPane bonusTilesContainer;

    private GridPane[] rightContainers;
    private GridPane[] leftContainers;
    private GridPane[] topContainers;

    private String[] weaponLeft;
    private String[] weaponTop;
    private String[] weaponRight;

    private Queue<String> rightQueue;
    private Queue<String> leftQueue;
    private Queue<String> topQueue;
    private int skullIndex;

    private List<Tuple<PlayerColor, Boolean>> killshotTrack;

    private static final String HOVERED = "hovered";

    private final Map<Point, Point[]> blocks;

    private EventHandler<MouseEvent> blockSelectionHandler;
    private EventHandler<MouseEvent> blockHoverHandler;
    private boolean isHoveringWeapons = false;
    private boolean isHoveringBlocks = false;

    public BoardContentPane(int skulls) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/boardContent.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setControllerFactory(p -> this);
            fxmlLoader.load();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load Board Content" + ex);
        }
        container.getStylesheets().add(getClass().getResource("/css/boardContent.css").toExternalForm());
        this.weaponLeft = new String[3];
        this.weaponRight = new String[3];
        this.weaponTop = new String[3];
        rightContainers = new GridPane[3];
        leftContainers = new GridPane[3];
        topContainers = new GridPane[3];
        rightContainers[0] = weaponRightContainer0;
        rightContainers[1] = weaponRightContainer1;
        rightContainers[2] = weaponRightContainer2;
        leftContainers[0] = weaponLeftContainer0;
        leftContainers[1] = weaponLeftContainer1;
        leftContainers[2] = weaponLeftContainer2;
        topContainers[0] = weaponTopContainer0;
        topContainers[1] = weaponTopContainer1;
        topContainers[2] = weaponTopContainer2;
        container.setOnMouseMoved(mouseEvent -> mouseEventHandler(mouseEvent, this::mouseHoverHandler, node -> !node.getStyleClass().contains(HOVERED)));
        container.setOnMouseClicked(mouseEvent -> mouseEventHandler(mouseEvent, this::mouseClickHandler, node -> true));
        skullIndex = 8;
        killshotIndex = skulls;
        killshotTrack = new LinkedList<>();
        topQueue = new LinkedList<>();
        rightQueue = new LinkedList<>();
        leftQueue = new LinkedList<>();
        blocks = new HashMap<>();
        for (int r = 0, y = 80; r < 3; r++, y += 88) {
            for (int c = 0, x = 75; c < 4; c++, x += 90) {
                Point[] boundaries = new Point[2];
                boundaries[0] = new Point(x, y);
                boundaries[1] = new Point(x + 90, y + 88);
                blocks.put(new Point(c, r), boundaries);
            }
        }
    }

    private void mouseEventHandler(MouseEvent mouseEvent, Consumer<Parent> consumer, Function<Node, Boolean> additionalCondition) {
        boolean isLeft = mouseEvent.getX() < 80 && mouseEvent.getX() > 0;
        boolean isLeftFirst = mouseEvent.getY() > 130 && mouseEvent.getY() < 185;
        boolean isLeftSecond = mouseEvent.getY() > 185 && mouseEvent.getY() < 240;
        boolean isLeftThird = mouseEvent.getY() > 240 && mouseEvent.getY() < 295;
        boolean isRight = mouseEvent.getX() > 425 && mouseEvent.getX() < 505;
        boolean isRightFirst = mouseEvent.getY() > 205 && mouseEvent.getY() < 260;
        boolean isRightSecond = mouseEvent.getY() > 260 && mouseEvent.getY() < 315;
        boolean isRightThird = mouseEvent.getY() > 315 && mouseEvent.getY() < 370;
        boolean isTop = mouseEvent.getY() < 80 && mouseEvent.getY() > 0;
        boolean isTopFirst = mouseEvent.getX() > 255 && mouseEvent.getX() < 310;
        boolean isTopSecond = mouseEvent.getX() > 310 && mouseEvent.getX() < 365;
        boolean isTopThird = mouseEvent.getX() > 365 && mouseEvent.getX() < 420;

        if (
                isLeft
                && isLeftFirst
                && additionalCondition.apply(weaponLeftContainer0)
        ) {
            consumer.accept(weaponLeftContainer0);
        } else if (
                isLeft
                && isLeftSecond
                && additionalCondition.apply(weaponLeftContainer1)
        ) {
            consumer.accept(weaponLeftContainer1);
        } else if (
                isLeft
                && isLeftThird
                && additionalCondition.apply(weaponLeftContainer2)
        ) {
            consumer.accept(weaponLeftContainer2);
        } else if (
                isRight
                && isRightFirst
                && additionalCondition.apply(weaponRightContainer0)
        ) {
            consumer.accept(weaponRightContainer0);
        } else if (
                isRight
                && isRightSecond
                && additionalCondition.apply(weaponRightContainer1)
        ) {
            consumer.accept(weaponRightContainer1);
        } else if (
                isRight
                && isRightThird
                && additionalCondition.apply(weaponRightContainer2)
        ) {
            consumer.accept(weaponRightContainer2);
        } else if (
                isTop
                && isTopFirst
                && additionalCondition.apply(weaponTopContainer0)
        ) {
            consumer.accept(weaponTopContainer0);
        } else if (
                isTop
                && isTopSecond
                && additionalCondition.apply(weaponTopContainer1)
        ) {
            consumer.accept(weaponTopContainer1);
        } else if (
                isTop
                && isTopThird
                && additionalCondition.apply(weaponTopContainer2)
        ) {
            consumer.accept(weaponTopContainer2);
        } else if (
                ! (isLeft || isRight || isTop)
                || isLeft && !(isLeftFirst || isLeftSecond || isLeftThird)
                || isRight && !(isRightFirst || isRightSecond || isRightThird)
                || isTop && !(isTopFirst || isTopSecond || isTopThird)
        ) {
            clearHovers();
            if (!isHoveringBlocks) {
                container.setCursor(Cursor.DEFAULT);
            }
        }
    }

    private void mouseHoverHandler(Parent node) {
        clearHovers();
        node.getStyleClass().add(HOVERED);
        container.setCursor(Cursor.HAND);
        isHoveringWeapons = true;
    }

    private void mouseClickHandler(Parent node) {
        clearHovers();
        container.setCursor(Cursor.DEFAULT);
        ImagePane img = (ImagePane) node.getChildrenUnmodifiable().get(0);
        mouseClickHandler(img);
    }

    private void mouseClickHandler(ImagePane img) {
        Stage popup = new Stage();
        popup.initStyle(StageStyle.DECORATED);
        Scene scene = new Scene(new Pane(new ImageView(new Image(img.getSrc()))));
        popup.setResizable(false);
        popup.setScene(scene);
        popup.sizeToScene();
        popup.show();
    }

    private void clearHovers() {
        Arrays.stream(leftContainers).forEach(c -> c.getStyleClass().remove(HOVERED));
        Arrays.stream(rightContainers).forEach(c -> c.getStyleClass().remove(HOVERED));
        Arrays.stream(topContainers).forEach(c -> c.getStyleClass().remove(HOVERED));
        isHoveringWeapons = false;
    }

    public void addWeaponTop(String weaponName, int index) {
        if (index < 3) {
            removePreviousElementIfPresent(topContainers, weaponTop, index, weaponName);
            addWeapon(weaponName, 0, 0, 1, topContainers[index]);
        }
    }

    public void addWeaponRight(String weaponName, int index) {
        if (index < 3) {
            removePreviousElementIfPresent(rightContainers, weaponRight, index, weaponName);
            addWeapon(weaponName, 90, 1, 1, rightContainers[index]);
        }
    }

    public void addWeaponLeft(String weaponName, int index) {
        if (index < 3) {
            removePreviousElementIfPresent(leftContainers, weaponLeft, index, weaponName);
            addWeapon(weaponName, 270, 1, 0, leftContainers[index]);
        }
    }

    public void enqueueWeaponLeft(String weaponName) {
        enqueueWeapon(weaponName, weaponLeft, this::addWeaponLeft, leftQueue);
    }

    public void enqueueWeaponRight(String weaponName) {
        enqueueWeapon(weaponName, weaponRight, this::addWeaponRight, rightQueue);
    }

    public void enqueueWeaponTop(String weaponName) {
        enqueueWeapon(weaponName, weaponTop, this::addWeaponTop, topQueue);
    }

    private synchronized void enqueueWeapon(String weaponName, String[] names, BiConsumer<String, Integer> adder, Queue<String> queue) {
        boolean added = false;
        for (int i = 0; i < names.length; i++) {
            if (names[i] == null) {
                adder.accept(weaponName, i);
                added = true;
                break;
            }
        }
        if (!added) {
            queue.add(weaponName);
        }
    }

    private void removePreviousElementIfPresent(GridPane[] containerGroup, String[] previousContentNames, int index, String newName) {
        if (!containerGroup[index].getChildren().isEmpty()) {
            previousContentNames[index] = null;
            containerGroup[index].getChildren().clear();
        }
        previousContentNames[index] = newName;
    }
    private void addWeapon(String weaponName, double rotation, int row, int col, GridPane container) {
        ImagePane weaponImg = new ImagePane(UrlFinder.findWeapon(weaponName));
        weaponImg.setRotate(rotation);
        container.add(weaponImg, col, row);
    }

    private synchronized void removeWeapon(String weaponName, String[] names, GridPane[] containers, Queue<String> queue, BiConsumer<String, Integer> adder) {
        for (int i = 0; i < names.length; i++) {
            if (names[i] != null && names[i].equals(weaponName)) {
                names[i] = null;
                containers[i].getChildren().clear();
                String toAdd = queue.poll();
                if (toAdd != null) {
                    adder.accept(toAdd, i);
                }
                break;
            }
        }
    }
    public void removeWeaponTop(String weaponName) {
        removeWeapon(weaponName, weaponTop, topContainers, topQueue, this::addWeaponTop);
    }

    public void removeWeaponRight(String weaponName) {
        removeWeapon(weaponName, weaponRight, rightContainers, rightQueue, this::addWeaponRight);
    }

    public void removeWeaponLeft(String weaponName) {
        removeWeapon(weaponName, weaponLeft, leftContainers, leftQueue, this::addWeaponLeft);
    }

    public void setSkulls(int skulls) {
        for (int i = 0; i < skulls && skullIndex > 0; i++, skullIndex--) {
            ImagePane skullImg = new ImagePane(UrlFinder.findSkull());
            skullContainer.add(skullImg, skullIndex, 1);
        }
    }

    public void addKillshot(PlayerColor color) {
        if (killshotIndex > 0) {
            ImagePane toChange = (ImagePane) skullContainer.getChildren().get(skullContainer.getChildren().size() - killshotIndex);
            toChange.setImg(UrlFinder.findToken(color));
        } else {
            ImagePane token = new ImagePane(UrlFinder.findToken(color));
            skullContainer.add(token, skullIndex + 1, 1);
        }
        killshotTrack.add(new Tuple<>(color, false));
        skullIndex++;
        killshotIndex--;
    }

    public void addOverkill() {
        ImagePane lastToken = (ImagePane) skullContainer.getChildren().get(skullContainer.getChildren().size() - skullIndex);
        Label overkill = new Label("2");
        GridPane.setHalignment(overkill, HPos.CENTER);
        lastToken.add(overkill, 0, 0);
        killshotTrack.set(killshotTrack.size() - 1, new Tuple<>(killshotTrack.get(killshotTrack.size() - 1).getItem1(), true));
    }

    public List<Tuple<PlayerColor, Boolean>> getKillshotTrackUnmodifiable() {
        return new LinkedList<>(killshotTrack);
    }

    public void addPlayer(PlayerColor color, int row, int col) {
        ((BoardBlockPane) playerContainer.getChildren().get(row * 4 + col)).addPlayer(color);
    }

    public void movePlayer(PlayerColor color, int row, int col) {
        for (Node pane : playerContainer.getChildren()) {
            ((BoardBlockPane) pane).removePlayer(color);
        }
        addPlayer(color, row, col);
    }

    public void waitForBlockSelection(List<Point> availableCoordinates, Consumer<Point> consumer, Text message) {
        blockSelectionHandler = e -> blockSelection(e, availableCoordinates, consumer, message);
        blockHoverHandler = e -> blockHover(e, availableCoordinates);
        container.addEventHandler(MouseEvent.MOUSE_MOVED, blockHoverHandler);
        container.addEventHandler(MouseEvent.MOUSE_CLICKED, blockSelectionHandler);
    }

    private void blockHover(MouseEvent e, List<Point> hoverable) {
        boolean keepHovering = false;
        isHoveringBlocks = false;
        for (Point p : hoverable) {
            Point[] boundaries = blocks.get(p);
            if (e.getX() < boundaries[1].x && e.getX() > boundaries[0].x && e.getY() < boundaries[1].y && e.getY() > boundaries[0].y) {
                keepHovering = true;
                isHoveringBlocks = true;
            }
        }
        if (keepHovering) {
            container.setCursor(Cursor.HAND);
        } else if (!isHoveringWeapons){
            container.setCursor(Cursor.DEFAULT);
        }
    }

    public void removeBlockSelection(Text message) {
        container.removeEventHandler(MouseEvent.MOUSE_CLICKED, blockSelectionHandler);
        container.removeEventHandler(MouseEvent.MOUSE_MOVED, blockHoverHandler);
        isHoveringBlocks = false;
        if (!isHoveringWeapons) {
            container.setCursor(Cursor.DEFAULT);
        }
        message.setText("");
    }

    private void blockSelection(MouseEvent e, List<Point> selectable, Consumer<Point> consumer, Text message) {
        for (Point p : selectable) {
            Point[] boundaries = blocks.get(p);
            if (e.getX() < boundaries[1].x && e.getX() > boundaries[0].x && e.getY() < boundaries[1].y && e.getY() > boundaries[0].y) {
                consumer.accept(p);
                removeBlockSelection(message);
            }
        }
    }
}
