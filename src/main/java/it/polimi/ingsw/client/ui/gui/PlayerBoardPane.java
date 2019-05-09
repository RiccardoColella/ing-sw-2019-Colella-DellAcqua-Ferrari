package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.player.PlayerColor;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PlayerBoardPane extends GridPane {
    @FXML
    private ImagePane playerBoardImg;
    @FXML
    private GridPane damageContainerFront;
    @FXML
    private GridPane damageContainerBack;
    @FXML
    private GridPane markContainer;
    @FXML
    private GridPane skullContainerFront;
    @FXML
    private GridPane skullContainerBack;

    private int columnIndexDamage;

    private int columnIndexMark;

    private GridPane activeDamageContainer;
    private GridPane activeSkullContainer;

    private List<PlayerColor> representedDamageTokens;

    private List<PlayerColor> representedMarkTokens;

    private int representedSkulls;

    public PlayerBoardPane(String src, BackgroundPosition backgroundPosition) {
        this();
        playerBoardImg.setImg(src, backgroundPosition);
        findSide(src);
    }

    public PlayerBoardPane(String src) {
        this(src, ImagePane.CENTER);
    }

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

    public void flip(String src) {
        this.flip(src, ImagePane.CENTER);
    }

    public void addToken(PlayerColor color) {
        ImagePane token = new ImagePane(UrlFinder.findToken(color));
        activeDamageContainer.add(token, columnIndexDamage, 1);
        columnIndexDamage += 2;
        representedDamageTokens.add(color);
    }

    public void addMark(PlayerColor color) {
        ImagePane token = new ImagePane(UrlFinder.findToken(color));
        markContainer.add(token, columnIndexMark, 0);
        columnIndexMark += 1;
        representedMarkTokens.add(color);
    }

    public void addSkull() {
        ImagePane skull = new ImagePane(UrlFinder.findSkull());
        representedSkulls++;
        activeSkullContainer.add(skull, representedSkulls , 1);

    }

    public void removeMarks(PlayerColor color) {
        markContainer.getChildren().clear();
        columnIndexMark = 1;
        List<PlayerColor> copy = new LinkedList<>(representedMarkTokens);
        representedMarkTokens.clear();
        for (int i = 0; i < copy.size(); i++) {
            if (!copy.get(i).equals(color)) {
                addMark(copy.get(i));
            }
        }

    }
    public void resetDamage() {
        activeDamageContainer.getChildren().clear();
        columnIndexDamage = 1;
        representedDamageTokens.clear();
    }

    public void setImg(String src, BackgroundPosition position) {
        this.playerBoardImg.setImg(src, position);
        findSide(src);
    }

    public void setImg(String src) {
        this.playerBoardImg.setImg(src);
        findSide(src);
    }

    public String getImgSrc() {
        return this.playerBoardImg.getSrc();
    }

    public List<PlayerColor> getRepresentedDamageTokens() {
        return representedDamageTokens;
    }

    public int getRepresentedSkulls() {
        return representedSkulls;
    }

    public List<PlayerColor> getRepresentedMarkTokens() {
        return representedMarkTokens;
    }
}
