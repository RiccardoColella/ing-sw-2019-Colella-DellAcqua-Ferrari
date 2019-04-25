package it.polimi.ingsw.client;

import it.polimi.ingsw.client.io.Connector;
import it.polimi.ingsw.client.io.RMIConnector;
import it.polimi.ingsw.client.io.SocketConnector;
import it.polimi.ingsw.client.ui.CLI;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.NotBoundException;

public class Main {
    public static void main( String[] args ) throws IOException, NotBoundException, InterruptedException {

        Connector rmiConnector = new RMIConnector(new InetSocketAddress("localhost", 9090));

        CLI cli = new CLI(
            rmiConnector,
            System.in,
            System.out
        );

        rmiConnector.addQuestionMessageReceivedListener(cli);
    }
}
