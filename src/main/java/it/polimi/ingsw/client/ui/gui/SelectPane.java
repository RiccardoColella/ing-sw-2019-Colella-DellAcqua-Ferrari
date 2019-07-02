package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.utils.Tuple;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

import java.util.LinkedList;
import java.util.List;

/**
 * This class, which extends DialogPane, allows the selection of one of the given set of options
 *
 * @author Adriana Ferrari
 */
public class SelectPane extends DialogPane {

    /**
     * The main container of the window
     */
    @FXML
    private GridPane container;

    /**
     * Standard constructor that sets up the css styles and fxml layout
     */
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

    /**
     * Adds the given options, which contain images, to the container
     *
     * @param isSkippable whether the selection is skippable
     * @param options a List with all the options, paired in a Tuple containing an ImagePane and its textual description
     */
    public void setOptions(boolean isSkippable, List<Tuple<ImagePane, String>> options) {
        int optionNumber = options.size();
        optionNumber += isSkippable ? 2 : 0;
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
        for (int i = 0; i < options.size(); i++) {
            options.get(i).getItem1().setMinSize(200, 250);
            container.add(options.get(i).getItem1(), i, 0);
            ButtonType bt = new ButtonType(options.get(i).getItem2(), ButtonBar.ButtonData.LEFT);
            getButtonTypes().add(bt);
            options.get(i).getItem1().setOnMouseClicked(e -> ((Button) lookupButton(bt)).fire()
            );
        }
        setSkippable(isSkippable);
    }

    /**
     * Adds text only options to the container
     *
     * @param textualOptions list of text only options
     */
    public void setTextOnlyOptions(List<String> textualOptions) {
        for (String textualOption : textualOptions) {
            getButtonTypes().add(new ButtonType(textualOption, ButtonBar.ButtonData.LEFT));
        }
    }

    /**
     * Adds, if necessary, a skip button to the container
     *
     * @param isSkippable whether the question is skippable
     */
    public void setSkippable(boolean isSkippable) {
        if (isSkippable) {
            getButtonTypes().add(new ButtonType("Skip", ButtonBar.ButtonData.CANCEL_CLOSE));
        }
    }
}
