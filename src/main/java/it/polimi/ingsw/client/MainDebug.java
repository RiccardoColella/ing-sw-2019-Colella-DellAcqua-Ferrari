package it.polimi.ingsw.client;

import it.polimi.ingsw.client.ui.GUI;

/**
 * Debugging Main
 *
 * @author Carlo Dell'Acqua
 */
public class MainDebug {
    public static void main( String[] args ) throws Exception {

        /*Connector rmiConnector = new RMIConnector(new InetSocketAddress("diemisto", 9090));

        CLI cli = new CLI(
            rmiConnector,
            System.in,
            System.out
        );

        rmiConnector.addQuestionMessageReceivedListener(cli);*/

        new GUI().start();
    }
}
