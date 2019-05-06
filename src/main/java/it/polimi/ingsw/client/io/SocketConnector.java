package it.polimi.ingsw.client.io;

import it.polimi.ingsw.shared.InputStreamMessageSupplier;
import it.polimi.ingsw.shared.MessageDispatcher;
import it.polimi.ingsw.shared.OutputStreamMessageConsumer;
import it.polimi.ingsw.shared.bootstrap.ClientInitializationInfo;

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
    private InputStreamMessageSupplier inputMessageStreamSupplier;

    /**
     * Message dispatching utility for IO
     */
    private MessageDispatcher messageDispatcher;

    /**
     * The client socket used to communicate with the remote end point
     */
    private Socket socket;

    /**
     * Initializes the Socket-based implementation of the Connector
     *
     * @param clientInitializationInfo the user preferences for the match
     * @param address the remote address the client needs to connect to
     * @throws IOException if the created socket cannot provide valid input and output streams
     */
    public void initialize(ClientInitializationInfo clientInitializationInfo, InetSocketAddress address) throws IOException {

        super.initialize(clientInitializationInfo);

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

        if (inputMessageStreamSupplier != null) {
            inputMessageStreamSupplier.close();
        }
        if (messageDispatcher != null) {
            messageDispatcher.close();
        }
        if (socket != null) {
            socket.close();
        }
    }
}

