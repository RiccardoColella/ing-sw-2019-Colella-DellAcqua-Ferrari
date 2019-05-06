package it.polimi.ingsw.client;

import it.polimi.ingsw.client.io.Connector;
import it.polimi.ingsw.client.io.RMIConnector;
import it.polimi.ingsw.client.ui.CLI;
import it.polimi.ingsw.shared.bootstrap.ClientInitializationInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.NotBoundException;

/**
 * This is the starting point of the client
 *
 * @author Carlo Dell'Acqua
 */
public class Main {
    public static void main( String[] args ) {

        try (CLI cli = new CLI(System.in, System.out)) {
            cli.initialize();
        } catch (Exception e) {
            System.out.println("Could not initialize a valid client, shutting down...");
        }
    }
}
