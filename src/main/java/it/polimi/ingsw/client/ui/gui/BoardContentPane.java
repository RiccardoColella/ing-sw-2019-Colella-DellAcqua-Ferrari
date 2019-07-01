package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.server.model.player.PlayerColor;
import it.polimi.ingsw.shared.datatransferobjects.BonusTile;
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

/**
 * Represents the content of the board
 *
 * @author Adriana Ferrari
 */
public class BoardContentPane extends GridPane {

    /**
     * Index representing the skulls that are yet to be taken
     */
    private int killshotIndex;

    /**
     * Contains the first weapon to the top of the board
     */
    @FXML
    private GridPane weaponTopContainer0;

    /**
     * Contains the second weapon to the top of the board
     */
    @FXML
    private GridPane weaponTopContainer1;

    /**
     * Contains the third weapon to the top of the board
     */
    @FXML
    private GridPane weaponTopContainer2;

    /**
     * Contains the first weapon to the left of the board
     */
    @FXML
    private GridPane weaponLeftContainer0;

    /**
     * Contains the second weapon to the left of the board
     */
    @FXML
    private GridPane weaponLeftContainer1;

    /**
     * Contains the third weapon to the left of the board
     */
    @FXML
    private GridPane weaponLeftContainer2;

    /**
     * Contains the first weapon to the right of the board
     */
    @FXML
    private GridPane weaponRightContainer0;

    /**
     * Contains the second weapon to the right of the board
     */
    @FXML
    private GridPane weaponRightContainer1;

    /**
     * Contains the third weapon to the right of the board
     */
    @FXML
    private GridPane weaponRightContainer2;

    /**
     * Represents the main container
     */
    @FXML
    private StackPane container;

    /**
     * Stores the skulls and killshot tokens
     */
    @FXML
    private GridPane skullContainer;

    /**
     * Stores the players
     */
    @FXML
    private GridPane playerContainer;

    /**
     * Stores the bonus tiles
     */
    @FXML
    private GridPane bonusTilesContainer;

    /**
     * Ordered array of the three right weapon containers
     */
    private GridPane[] rightContainers;

    /**
     * Ordered array of the three left weapon containers
     */
    private GridPane[] leftContainers;

    /**
     * Ordered array of the three top weapon containers
     */
    private GridPane[] topContainers;

    /**
     * Ordered array of the names of the weapons on the left
     */
    private String[] weaponLeft;

    /**
     * Ordered array of the names of the weapons on the top
     */
    private String[] weaponTop;

    /**
     * Ordered array of the names of the weapons on the right
     */
    private String[] weaponRight;

    /**
     * Queue of the weapons to be added to the right
     */
    private Queue<String> rightQueue;

    /**
     * Queue of the weapons to be added to the left
     */
    private Queue<String> leftQueue;

    /**
     * Queue of the weapons to be added to the top
     */
    private Queue<String> topQueue;

    /**
     * Used to represent the skulls in the correct location
     */
    private int skullIndex;

    /**
     * Represents the status of the killshot track
     */
    private List<Tuple<PlayerColor, Boolean>> killshotTrack;

    /**
     * Const for "hovered"
     */
    private static final String HOVERED = "hovered";

    /**
     * Maps a point in the board to its actual boundaries in the visual representation
     */
    private final Map<Point, Point[]> blocks;

    /**
     * Handles a block being clicked
     */
    private EventHandler<MouseEvent> blockSelectionHandler;

    /**
     * Handles a block being hovered
     */
    private EventHandler<MouseEvent> blockHoverHandler;

    /**
     * Whether the user is currently hovering a weapon
     */
    private boolean isHoveringWeapons = false;

    /**
     * Whether the user is currently hovering a block
     */
    private boolean isHoveringBlocks = false;

    /**
     * Constructor of the board given the skulls
     *
     * @param skulls the starting number of skulls
     */
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

    /**
     * Handles mouse events in standard situations (when no question is being asked)
     *
     * @param mouseEvent the mouse event to analyze
     * @param consumer a consumer that takes the interested node
     * @param additionalCondition an additional condition that needs to be verified for a node to be considered the target
     */
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

    /**
     * Handles the hovering of a weapon
     *
     * @param node the node representing the weapon
     */
    private void mouseHoverHandler(Parent node) {
        clearHovers();
        node.getStyleClass().add(HOVERED);
        container.setCursor(Cursor.HAND);
        isHoveringWeapons = true;
    }

    /**
     * Handles the click of an element
     *
     * @param node the node that was clicked
     */
    private void mouseClickHandler(Parent node) {
        clearHovers();
        container.setCursor(Cursor.DEFAULT);
        ImagePane img = (ImagePane) node.getChildrenUnmodifiable().get(0);
        mouseClickHandler(img);
    }

    /**
     * Handles an image pane being clicked
     * @param img the image pane
     */
    private void mouseClickHandler(ImagePane img) {
        Stage popup = new Stage();
        popup.initStyle(StageStyle.DECORATED);
        Scene scene = new Scene(new Pane(new ImageView(new Image(img.getSrc()))));
        popup.setResizable(false);
        popup.setScene(scene);
        popup.sizeToScene();
        popup.show();
    }

    /**
     * Resets all the elements that are hovered
     */
    private void clearHovers() {
        Arrays.stream(leftContainers).forEach(c -> c.getStyleClass().remove(HOVERED));
        Arrays.stream(rightContainers).forEach(c -> c.getStyleClass().remove(HOVERED));
        Arrays.stream(topContainers).forEach(c -> c.getStyleClass().remove(HOVERED));
        isHoveringWeapons = false;
    }

    /**
     * Adds a weapon to the top container
     *
     * @param weaponName the name of the weapon to add
     * @param index the index at which it should be added
     */
    public void addWeaponTop(String weaponName, int index) {
        if (index < 3) {
            removePreviousElementIfPresent(topContainers, weaponTop, index, weaponName);
            addWeapon(weaponName, 0, 0, 1, topContainers[index]);
        }
    }

    /**
     * Adds a weapon to the right container
     *
     * @param weaponName the name of the weapon to add
     * @param index the index at which it should be added
     */
    public void addWeaponRight(String weaponName, int index) {
        if (index < 3) {
            removePreviousElementIfPresent(rightContainers, weaponRight, index, weaponName);
            addWeapon(weaponName, 90, 1, 1, rightContainers[index]);
        }
    }

    /**
     * Adds a weapon to the left container
     *
     * @param weaponName the name of the weapon to add
     * @param index the index at which it should be added
     */
    public void addWeaponLeft(String weaponName, int index) {
        if (index < 3) {
            removePreviousElementIfPresent(leftContainers, weaponLeft, index, weaponName);
            addWeapon(weaponName, 270, 1, 0, leftContainers[index]);
        }
    }

    /**
     * Enqueues a weapon in the left queue
     * @param weaponName the name of the weapon to enqueue
     */
    public void enqueueWeaponLeft(String weaponName) {
        enqueueWeapon(weaponName, weaponLeft, this::addWeaponLeft, leftQueue);
    }

    /**
     * Enqueues a weapon in the right queue
     * @param weaponName the name of the weapon to enqueue
     */
    public void enqueueWeaponRight(String weaponName) {
        enqueueWeapon(weaponName, weaponRight, this::addWeaponRight, rightQueue);
    }

    /**
     * Enqueues a weapon in the top queue
     * @param weaponName the name of the weapon to enqueue
     */
    public void enqueueWeaponTop(String weaponName) {
        enqueueWeapon(weaponName, weaponTop, this::addWeaponTop, topQueue);
    }

    /**
     * Adds a new weapon if possible, otherwise it enqueues it
     *
     * @param weaponName the name of the weapon to enqueue
     * @param names the array containing the neighboring weapons
     * @param adder the biconsumer that will take care of adding the weapon to its correct place
     * @param queue the queue to which the weapon should be added if there is no place available
     */
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

    /**
     * Removes the old weapon if it is still there and puts the name of a new weapon in its place
     *
     * @param containerGroup the array with the container of the new weapon
     * @param previousContentNames the array with the names of the weapons that are stored in that container
     * @param index the index at which the element will be added
     * @param newName the name of the new weapon
     */
    private void removePreviousElementIfPresent(GridPane[] containerGroup, String[] previousContentNames, int index, String newName) {
        if (!containerGroup[index].getChildren().isEmpty()) {
            previousContentNames[index] = null;
            containerGroup[index].getChildren().clear();
        }
        previousContentNames[index] = newName;
    }

    /**
     * Physically adds a weapon to the screen
     *
     * @param weaponName the name of the weapon to add
     * @param rotation the rotation of the image
     * @param row the row at which it should be added
     * @param col the column at which it should be added
     * @param container the container to which it should be added
     */
    private void addWeapon(String weaponName, double rotation, int row, int col, GridPane container) {
        ImagePane weaponImg = new ImagePane(UrlFinder.findWeapon(weaponName));
        weaponImg.setRotate(rotation);
        container.add(weaponImg, col, row);
    }

    /**
     * Removes a weapon from its container
     *
     * @param weaponName the weapon that shall be removed
     * @param names the names of its neighboring weapons (and its name)
     * @param containers the containers of its neighboring weapons (and itself)
     * @param queue the queue from which to poll a replacement
     * @param adder the biconsumer to call if the replacement is found
     */
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

    /**
     * Removes a weapon from the top container
     *
     * @param weaponName the name of the weapon that shall be removed
     */
    public void removeWeaponTop(String weaponName) {
        removeWeapon(weaponName, weaponTop, topContainers, topQueue, this::addWeaponTop);
    }

    /**
     * Removes a weapon from the right container
     *
     * @param weaponName the name of the weapon that shall be removed
     */
    public void removeWeaponRight(String weaponName) {
        removeWeapon(weaponName, weaponRight, rightContainers, rightQueue, this::addWeaponRight);
    }

    /**
     * Removes a weapon from the left container
     *
     * @param weaponName the name of the weapon that shall be removed
     */
    public void removeWeaponLeft(String weaponName) {
        removeWeapon(weaponName, weaponLeft, leftContainers, leftQueue, this::addWeaponLeft);
    }

    /**
     * Initializes the killshot track with the correct amount of skulls
     *
     * @param skulls the number of skulls that should be added
     */
    public void initSkulls(int skulls) {
        for (int i = 0; i < skulls && skullIndex > 0; i++, skullIndex--) {
            ImagePane skullImg = new ImagePane(UrlFinder.findSkull());
            skullContainer.add(skullImg, skullIndex, 1);
        }
    }

    /**
     * Adds a killshot to the killshot track
     *
     * @param color the color of the killshot
     */
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

    /**
     * Adds an overkill to the last killshot added
     */
    public void addOverkill() {
        ImagePane lastToken = (ImagePane) skullContainer.getChildren().get(skullContainer.getChildren().size() - skullIndex);
        Label overkill = new Label("2");
        GridPane.setHalignment(overkill, HPos.CENTER);
        lastToken.add(overkill, 0, 0);
        killshotTrack.set(killshotTrack.size() - 1, new Tuple<>(killshotTrack.get(killshotTrack.size() - 1).getItem1(), true));
    }

    /**
     * Returns a copy of the killshot track
     *
     * @return a copy of the killshot track
     */
    public List<Tuple<PlayerColor, Boolean>> getKillshotTrackUnmodifiable() {
        return new LinkedList<>(killshotTrack);
    }

    /**
     * Adds a player to the board
     *
     * @param color the color of the player
     * @param row the row at which the player should be added
     * @param col the column at which the player should be added
     */
    public void addPlayer(PlayerColor color, int row, int col) {
        ((BoardBlockPane) playerContainer.getChildren().get(row * 4 + col)).addPlayer(color);
    }

    /**
     * Moves a player already in the board
     *
     * @param color the color of the player
     * @param row the row to which the player should be moved
     * @param col the column to which the player should be moved
     */
    public void movePlayer(PlayerColor color, int row, int col) {
        for (Node pane : playerContainer.getChildren()) {
            ((BoardBlockPane) pane).removePlayer(color);
        }
        addPlayer(color, row, col);
    }

    /**
     * Sets up the board so that it can detect the selection of a block
     *
     * @param availableCoordinates the points that can be selected
     * @param consumer the consumer that will be called after the selection of a block
     * @param message the message that is being showed to the user during the selection
     */
    public void waitForBlockSelection(List<Point> availableCoordinates, Consumer<Point> consumer, Text message) {
        blockSelectionHandler = e -> blockSelection(e, availableCoordinates, consumer, message);
        blockHoverHandler = e -> blockHover(e, availableCoordinates);
        container.addEventHandler(MouseEvent.MOUSE_MOVED, blockHoverHandler);
        container.addEventHandler(MouseEvent.MOUSE_CLICKED, blockSelectionHandler);
    }

    /**
     * Handles the hovering of a block
     *
     * @param e the mouse event
     * @param hoverable the points that can be affected by this event
     */
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

    /**
     * Stops waiting for a block selection
     *
     * @param message the message that was shown to the user during the selection
     */
    public void removeBlockSelection(Text message) {
        container.removeEventHandler(MouseEvent.MOUSE_CLICKED, blockSelectionHandler);
        container.removeEventHandler(MouseEvent.MOUSE_MOVED, blockHoverHandler);
        isHoveringBlocks = false;
        if (!isHoveringWeapons) {
            container.setCursor(Cursor.DEFAULT);
        }
        message.setText("");
    }

    /**
     * Handles the click on a block
     *
     * @param e the mouse click event
     * @param selectable the blocks that can be affected by this event
     * @param consumer the consumer to call if a block is actually selected
     * @param message the message that is being shown during the selection
     */
    private void blockSelection(MouseEvent e, List<Point> selectable, Consumer<Point> consumer, Text message) {
        for (Point p : selectable) {
            Point[] boundaries = blocks.get(p);
            if (e.getX() < boundaries[1].x && e.getX() > boundaries[0].x && e.getY() < boundaries[1].y && e.getY() > boundaries[0].y) {
                consumer.accept(p);
                removeBlockSelection(message);
            }
        }
    }

    /**
     * Adds a set of tiles to the board
     *
     * @param turretBonusTiles the tiles that shall be added
     */
    public void addTiles(Set<BonusTile> turretBonusTiles) {
        for (BonusTile tile : turretBonusTiles) {
           addTile(tile);
        }
    }

    /**
     * Adds a single tile to the board
     *
     * @param tile the tile that shall be added
     */
    public void addTile(BonusTile tile) {
        ImagePane tileImg = new ImagePane(UrlFinder.findBonusTile(tile.getAmmoCubes()));
        ((BoardBlockPane) bonusTilesContainer.getChildren().get(tile.getLocation().y * 4 + tile.getLocation().x)).addTile(tileImg);
    }

    /**
     * Removes the tile in the given location
     *
     * @param tileLocation the location of the tile that should be removed
     */
    public void removeTile(Point tileLocation) {
        ((BoardBlockPane) bonusTilesContainer.getChildren().get(tileLocation.y * 4 + tileLocation.x)).clearCell();
    }
}
