package it.polimi.ingsw.client;

import it.polimi.ingsw.client.io.RMIConnector;
import it.polimi.ingsw.client.io.SocketConnector;
import it.polimi.ingsw.client.ui.CLI;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.NotBoundException;

public class Main {
    public static void main( String[] args ) throws IOException, NotBoundException {

        new CLI(
            new RMIConnector(new InetSocketAddress("localhost", 9090)),
            System.in,
            System.out
        );
    }
}
