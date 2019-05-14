package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.server.model.player.PlayerColor;
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
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class BoardContentPane extends GridPane {

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

    private GridPane[] rightContainers;
    private GridPane[] leftContainers;
    private GridPane[] topContainers;

    private String[] weaponLeft;
    private String[] weaponTop;
    private String[] weaponRight;

    private int skullIndex;

    private static final String HOVERED = "hovered";

    public BoardContentPane() {
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
            container.setCursor(Cursor.DEFAULT);
        }
    }

    private void mouseHoverHandler(Parent node) {
        clearHovers();
        node.getStyleClass().add(HOVERED);
        container.setCursor(Cursor.HAND);
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

    public void addWeaponLeft(String weaponName) {
        for (int i = 0; i < weaponLeft.length; i++) {
            if (weaponLeft[i] == null) {
                addWeaponLeft(weaponName, i);
                break;
            }
        }
    }

    public void addWeaponRight(String weaponName) {
        for (int i = 0; i < weaponRight.length; i++) {
            if (weaponRight[i] == null) {
                addWeaponRight(weaponName, i);
                break;
            }
        }
    }

    public void addWeaponTop(String weaponName) {
        for (int i = 0; i < weaponTop.length; i++) {
            if (weaponTop[i] == null) {
                addWeaponTop(weaponName, i);
                break;
            }
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

    public void removeWeaponTop(String weaponName) {

    }

    public void removeWeaponRight(String weaponName) {

    }

    public void removeWeaponLeft(String weaponName) {

    }

    public void setSkulls(int skulls) {
        for (int i = 0; i < skulls && skullIndex > 0; i++, skullIndex--) {
            ImagePane skullImg = new ImagePane(UrlFinder.findSkull());
            skullContainer.add(skullImg, skullIndex, 1);
        }
    }

    public void addKillshot(PlayerColor color) {
        if (skullIndex < 8) {
            ImagePane toChange = (ImagePane) skullContainer.getChildren().get(skullContainer.getChildren().size() - skullIndex - 1);
            toChange.setImg(UrlFinder.findToken(color));
        } else {
            ImagePane token = new ImagePane(UrlFinder.findToken(color));
            skullContainer.add(token, skullIndex + 1, 1);
        }
        skullIndex++;
    }

    public void addOverkill() {
        ImagePane lastToken = (ImagePane) skullContainer.getChildren().get(skullContainer.getChildren().size() - skullIndex);
        Label overkill = new Label("2");
        GridPane.setHalignment(overkill, HPos.CENTER);
        lastToken.add(overkill, 0, 0);
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
}
