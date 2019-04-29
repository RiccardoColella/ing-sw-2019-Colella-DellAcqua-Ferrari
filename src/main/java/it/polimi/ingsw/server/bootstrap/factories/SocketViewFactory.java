package it.polimi.ingsw.server.bootstrap.factories;

import it.polimi.ingsw.server.view.remote.SocketView;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * Creates a SocketView with the initial configuration
 */
public class SocketViewFactory {

    private SocketViewFactory() { }

    /**
     * The answer timeout
     */
    private static int answerTimeoutMilliseconds = 1000;

    /**
     * Initializes this factory
     *
     * @param answerTimeoutMilliseconds the time to wait before considering the view disconnected
     */
    public static void initialize(int answerTimeoutMilliseconds) {
        SocketViewFactory.answerTimeoutMilliseconds = answerTimeoutMilliseconds;
    }

    /**
     * Creates a SocketView
     *
     * @param socket the socket that is associated with the SocketView
     * @return a SocketView
     * @throws IOException if a network error occurs
     */
    public static SocketView createSocketView(Socket socket) throws IOException {
        return new SocketView(socket, answerTimeoutMilliseconds, TimeUnit.MILLISECONDS);
    }
}
