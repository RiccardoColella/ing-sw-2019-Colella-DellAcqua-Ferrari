package it.polimi.ingsw.server.bootstrap.acceptors;

import it.polimi.ingsw.server.bootstrap.factories.SocketViewFactory;
import it.polimi.ingsw.server.view.View;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Logger;

/**
 * This class is used to listen for socket clients
 */
public class SocketAcceptor implements Acceptor, AutoCloseable {
    /**
     * Timeout needed to prevent deadlocks
     */
    private static final int ACCEPT_TIMEOUT = 1000;

    /**
     * Logging utility
     */
    protected final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Server socket used to listen for clients
     */
    private final ServerSocket socket;


    /**
     * Constructs a SocketAcceptor that will listen on the given port
     *
     * @param port listening port
     * @throws IOException if the socket cannot be set up correctly
     */
    public SocketAcceptor(int port) throws IOException {
        socket = new ServerSocket(port);
        socket.setSoTimeout(ACCEPT_TIMEOUT);
    }

    /**
     * Closes the listening socket
     *
     * @throws Exception if the socket close method fails
     */
    @Override
    public void close() throws Exception {
        socket.close();
    }

    /**
     * Creates a view once a socket-based client connects
     *
     * @return a Socket View bound to the client
     * @throws IOException if any socket IO operation fails
     */
    @Override
    public View call() throws IOException {
        return SocketViewFactory.createSocketView(socket.accept());
    }
}