package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.server.model.player.PlayerColor;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.GridPane;

import java.util.LinkedList;
import java.util.List;

/**
 * This class represents the Board of a single Player
 *
 * @author Adriana Ferrari
 */
public class PlayerBoardPane extends GridPane {

    /**
     * The image of the board
     */
    @FXML
    private ImagePane playerBoardImg;

    /**
     * The container of the damage tokens when the board is flipped to the front
     */
    @FXML
    private GridPane damageContainerFront;

    /**
     * The container of the damage tokens when the board is flipped to the back
     */
    @FXML
    private GridPane damageContainerBack;

    /**
     * The container of the marks
     */
    @FXML
    private GridPane markContainer;

    /**
     * The container of the skulls when the board is flipped to the front
     */
    @FXML
    private GridPane skullContainerFront;

    /**
     * The container of the skulls when the board is flipped to the back
     */
    @FXML
    private GridPane skullContainerBack;

    /**
     * The index of the damages stored in the board
     */
    private int columnIndexDamage;

    /**
     * The index of the marks stored in the board
     */
    private int columnIndexMark;

    /**
     * The damage container that is currently used (front or back)
     */
    private GridPane activeDamageContainer;

    /**
     * The skull container that is currently used (front or back)
     */
    private GridPane activeSkullContainer;

    /**
     * The list of currently represented damage tokens
     */
    private List<PlayerColor> representedDamageTokens;

    /**
     * The list of currently represented mark tokens
     */
    private List<PlayerColor> representedMarkTokens;

    /**
     * The number of skulls currently displayed on the board
     */
    private int representedSkulls;

    /**
     * Constructor given the source of the board image and its alignment
     *
     * @param src The source path of the board image
     * @param backgroundPosition The desired alignment for the board image
     */
    public PlayerBoardPane(String src, BackgroundPosition backgroundPosition) {
        this();
        playerBoardImg.setImg(src, backgroundPosition);
        findSide(src);
    }

    /**
     * Constructor given the source of the board image and with default alignment (centered)
     *
     * @param src The source path of the board image
     */
    public PlayerBoardPane(String src) {
        this(src, ImagePane.CENTER);
    }

    /**
     * Finds the side of the board given the image source path and sets up the environment accordingly
     * @param src The source path of the board image
     */
    private void findSide(String src) {
        if (src.contains("BACK")) {
            damageContainerFront.setVisible(false);
            damageContainerBack.setVisible(true);
            activeDamageContainer = damageContainerBack;
            activeSkullContainer = skullContainerBack;
        } else {
            damageContainerBack.setVisible(false);
            damageContainerFront.setVisible(true);
            activeDamageContainer = damageContainerFront;
            activeSkullContainer = skullContainerFront;
        }
        columnIndexDamage = 1;
        columnIndexMark = 1;
        representedSkulls = 0;
    }

    /**
     * Constructor with no parameters that allows to set the image later
     */
    public PlayerBoardPane() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/playerBoard.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setControllerFactory(p -> this);
            fxmlLoader.load();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load Player Board " + ex);
        }
        this.representedDamageTokens = new LinkedList<>();
        this.representedMarkTokens = new LinkedList<>();
    }

    /**
     * Flips the board from front to back or vice-versa according to the new source path
     *
     * @param src The source path of the board image
     * @param backgroundPosition The desired alignment for the board image
     */
    public void flip(String src, BackgroundPosition backgroundPosition) {
        damageContainerFront.setVisible(!damageContainerFront.isVisible());
        damageContainerBack.setVisible(!damageContainerBack.isVisible());
        skullContainerBack.setVisible(!skullContainerBack.isVisible());
        skullContainerFront.setVisible(!skullContainerFront.isVisible());
        activeDamageContainer = activeDamageContainer == damageContainerFront ? damageContainerBack : damageContainerFront;
        activeSkullContainer = activeSkullContainer == skullContainerFront ? skullContainerBack : skullContainerFront;
        playerBoardImg.setImg(src, backgroundPosition);
        columnIndexDamage = 1;
        representedSkulls = 0;
        representedDamageTokens.clear();
    }

    /**
     * Flips the board from front to back or vice-versa according to the new source path, using a default alignment
     * for the image (centered)
     *
     * @param src The source path of the board image
     */
    public void flip(String src) {
        this.flip(src, ImagePane.CENTER);
    }

    /**
     * Adds a new damage token of the given color to the board
     *
     * @param color the color of the damage token
     */
    public void addToken(PlayerColor color) {
        ImagePane token = new ImagePane(UrlFinder.findToken(color));
        activeDamageContainer.add(token, columnIndexDamage, 1);
        columnIndexDamage += 2;
        representedDamageTokens.add(color);
    }

    /**
     * Adds a new mark token of the given color to the board
     *
     * @param color the color of the mark token
     */
    public void addMark(PlayerColor color) {
        ImagePane token = new ImagePane(UrlFinder.findToken(color));
        markContainer.add(token, columnIndexMark, 0);
        columnIndexMark += 1;
        representedMarkTokens.add(color);
    }

    /**
     * Adds a new skull to the board
     */
    public void addSkull() {
        ImagePane skull = new ImagePane(UrlFinder.findSkull());
        representedSkulls++;
        activeSkullContainer.add(skull, representedSkulls , 1);

    }

    /**
     * Removes all the marks from the board
     */
    public void clearMarks() {
        columnIndexMark = 1;
        representedMarkTokens.clear();
        markContainer.getChildren().clear();
    }

    /**
     * Removes all the damage tokens from the board
     */
    public void resetDamage() {
        activeDamageContainer.getChildren().clear();
        columnIndexDamage = 1;
        representedDamageTokens.clear();
    }

    /**
     * Sets the given image as the board
     *
     * @param src The source path of the image
     * @param position The desired alignment for the image
     */
    public void setImg(String src, BackgroundPosition position) {
        this.playerBoardImg.setImg(src, position);
        findSide(src);
    }

    /**
     * Sets the given image as the board with a default alignment (centered)
     *
     * @param src The source path of the image
     */
    public void setImg(String src) {
        this.playerBoardImg.setImg(src);
        findSide(src);
    }

    /**
     * Gets the source of the current board image
     *
     * @return the path of the image
     */
    public String getImgSrc() {
        return this.playerBoardImg.getSrc();
    }

    /**
     * Gets the colors of the damage tokens
     *
     * @return a List with the colors of the damage tokens currently on the board
     */
    public List<PlayerColor> getRepresentedDamageTokens() {
        return representedDamageTokens;
    }

    /**
     * Gets how many skulls are on the board
     *
     * @return the number of skulls on the board
     */
    public int getRepresentedSkulls() {
        return representedSkulls;
    }

    /**
     * Gets the colors of the marks
     *
     * @return a List with the colors of the mark tokens currently on the board
     */
    public List<PlayerColor> getRepresentedMarkTokens() {
        return representedMarkTokens;
    }
}
