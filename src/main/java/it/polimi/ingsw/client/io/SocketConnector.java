package it.polimi.ingsw.client.io;

import it.polimi.ingsw.shared.view.remote.InputStreamMessageSupplier;
import it.polimi.ingsw.shared.view.remote.MessageDispatcher;
import it.polimi.ingsw.shared.view.remote.OutputStreamMessageConsumer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * This class is the concrete connector implemented with classic socket IO
 *
 * @author Carlo Dell'Acqua
 */
public class SocketConnector extends Connector {
    /**
     * Message supplier object that provides input messages
     */
    private final InputStreamMessageSupplier inputMessageStreamSupplier;

    /**
     * Message dispatching utility for IO
     */
    private MessageDispatcher messageDispatcher;

    /**
     * The client socket used to communicate with the remote end point
     */
    private Socket socket;

    /**
     * Constructs the Socket-based implementation of the Connector
     *
     * @param address the remote address the client needs to connect to
     * @throws IOException if the created socket cannot provide valid input and output streams
     */
    public SocketConnector(InetSocketAddress address) throws IOException {

        socket = new Socket();
        socket.connect(address);

        inputMessageStreamSupplier = new InputStreamMessageSupplier(new DataInputStream(socket.getInputStream()));

        messageDispatcher = new MessageDispatcher(
                inputMessageQueue,
                outputMessageQueue,
                inputMessageStreamSupplier,
                new OutputStreamMessageConsumer(new DataOutputStream(socket.getOutputStream()))
        );
    }

    /**
     * Closes this object and stops the background threads execution
     *
     * @throws Exception if the closing process is forced to stop or the remote resources are unable to correctly close or the socket cannot be closed
     */
    @Override
    public void close() throws Exception {
        super.close();
        inputMessageStreamSupplier.close();
        messageDispatcher.close();
        socket.close();
    }
}

