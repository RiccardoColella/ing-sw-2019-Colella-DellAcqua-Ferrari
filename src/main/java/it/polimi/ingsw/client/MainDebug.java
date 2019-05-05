package it.polimi.ingsw.client;

import it.polimi.ingsw.client.ui.gui.GUI;
import javafx.fxml.FXMLLoader;

/**
 * Debugging Main
 *
 * @author Carlo Dell'Acqua
 */
public class MainDebug {
    public static void main( String[] args ) {


        FXMLLoader.setDefaultClassLoader(MainDebug.class.getClassLoader());

        /*Connector rmiConnector = new RMIConnector(new InetSocketAddress("diemisto", 9090));

        CLI cli = new CLI(
            rmiConnector,
            System.in,
            System.out
        );

        rmiConnector.addQuestionMessageReceivedListener(cli);*/
        //new LoginController().init();
        new GUI().start();

    }
}
