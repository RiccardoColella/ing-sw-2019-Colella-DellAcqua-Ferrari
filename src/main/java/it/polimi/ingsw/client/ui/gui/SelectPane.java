package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.utils.Tuple;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SelectPane extends DialogPane {

    @FXML
    private GridPane container;

    public SelectPane() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/select.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setControllerFactory(p -> this);
            fxmlLoader.load();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load Select" + ex);
        }
        this.getStylesheets().add(getClass().getResource("/css/select.css").toExternalForm());
        this.getStylesheets().add(getClass().getResource("/css/global.css").toExternalForm());

    }

    public void setOptions(List<Tuple<ImagePane, String>> options) {
        int optionNumber = options.size();
        List<ColumnConstraints> ccs = new LinkedList<>();
        for (int i = 0; i < optionNumber; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / optionNumber);
            ccs.add(cc);
        }
        container.getColumnConstraints().addAll(ccs);
        RowConstraints rc = new RowConstraints();
        rc.setPercentHeight(100);
        container.getRowConstraints().add(rc);
        for (int i = 0; i < optionNumber; i++) {
            options.get(i).getItem1().setMinSize(200, 250);
            container.add(options.get(i).getItem1(), i, 0);
            getButtonTypes().add(new ButtonType(options.get(i).getItem2(), ButtonBar.ButtonData.LEFT));
        }
    }

    public void setTextOnlyOptions(List<String> textualOptions) {
        for (String textualOption : textualOptions) {
            getButtonTypes().add(new ButtonType(textualOption, ButtonBar.ButtonData.LEFT));
        }
    }

    public void setSkippable(boolean isSkippable) {
        if (isSkippable) {
            getButtonTypes().add(new ButtonType("Skip", ButtonBar.ButtonData.CANCEL_CLOSE));
        }
    }

    @SafeVarargs
    public final void setOptions(Tuple<ImagePane, String>... images) {
        setOptions(Arrays.asList(images));
    }
}
