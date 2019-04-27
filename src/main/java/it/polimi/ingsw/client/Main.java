package it.polimi.ingsw.client;

import it.polimi.ingsw.client.io.Connector;
import it.polimi.ingsw.client.io.RMIConnector;
import it.polimi.ingsw.client.ui.CLI;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.NotBoundException;

/**
 * This is the starting point of the client
 *
 * @author Carlo Dell'Acqua
 */
public class Main {
    public static void main( String[] args ) throws Exception {

        Connector rmiConnector = new RMIConnector(new InetSocketAddress("diemisto", 9090));

        CLI cli = new CLI(
            rmiConnector,
            System.in,
            System.out
        );

        rmiConnector.addQuestionMessageReceivedListener(cli);
    }
}
