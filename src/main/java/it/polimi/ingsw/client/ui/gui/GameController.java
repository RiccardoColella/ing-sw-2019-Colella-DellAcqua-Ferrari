package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.client.viewmodels.Player;
import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

import java.util.List;

public class GameController extends WindowController {
    @FXML
    private AnchorPane window;
    @FXML
    private GridPane boardContainer;
    @FXML
    private GridPane opponentsContainer;
    @FXML
    private ImagePane playerBoardImg;
    @FXML
    private ImagePane playerTileImg;

    private BoardFactory.Preset preset;

    private List<Player> opponents;

    private Player self;

    public GameController(BoardFactory.Preset preset, Player self, List<Player> opponents) {
        super("Adrenalina", "/fxml/game.fxml", "/css/game.css");
        this.preset = preset;
        BoardPane board = new BoardPane(preset);
        boardContainer.getChildren().add(board);
        this.opponents = opponents;
        this.self = self;
        initOpponentsBoards();
        initPlayerBoard();
    }

    private void initPlayerBoard() {
        playerBoardImg.setImg(UrlFinder.findPlayerBoard(self.getColor(), self.isBoardFlipped()), ImagePane.LEFT);
        playerTileImg.setImg(UrlFinder.findPlayerTile(self.getColor(), self.isTileFlipped()), ImagePane.RIGHT);
    }

    private void initOpponentsBoards() {
        int rows = opponents.size() / 2 + 1;
        double rowHeight = 100.0 / rows;
        ColumnConstraints c0 = new ColumnConstraints();
        c0.setPercentWidth(50);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(50);
        opponentsContainer.getColumnConstraints().addAll(c0, c1);
        for (int i = 0; i < rows; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(rowHeight);
            opponentsContainer.getRowConstraints().add(rc);
        }
        for (int i = 0; i < opponents.size(); i++) {
            Player opponent = opponents.get(i);
            ImagePane opponentPane = new ImagePane(UrlFinder.findPlayerBoard(opponent.getColor(), opponent.isBoardFlipped()), ImagePane.LEFT);
            Label opponentName = new Label(opponent.getNickname());
            GridPane opponentContainer = new GridPane();
            ColumnConstraints entireColumn = new ColumnConstraints();
            entireColumn.setPercentWidth(100);
            opponentContainer.getColumnConstraints().add(entireColumn);
            RowConstraints nameRow = new RowConstraints();
            nameRow.setPercentHeight(15);
            RowConstraints imgRow = new RowConstraints();
            imgRow.setPercentHeight(85);
            opponentContainer.getRowConstraints().addAll(nameRow, imgRow);
            opponentContainer.add(opponentName, 0, 0);
            opponentContainer.add(opponentPane, 0, 1);
            opponentsContainer.add(opponentContainer, i % 2, i / 2);
        }
    }

    @FXML
    public void initialize() {
        window.setMinWidth(600);
        window.setMinHeight(400);
        setupViewport(window);
    }



}
