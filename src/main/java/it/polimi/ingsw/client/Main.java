package it.polimi.ingsw.client;

import it.polimi.ingsw.client.io.SocketConnector;
import it.polimi.ingsw.client.ui.CLI;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main( String[] args ) throws IOException {

        new CLI(
            new SocketConnector(new InetSocketAddress("localhost", 9090)),
            System.in,
            System.out
        );
    }
}
