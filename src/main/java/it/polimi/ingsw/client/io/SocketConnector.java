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

    private final InputStreamMessageSupplier inputMessageStreamSupplier;
    private MessageDispatcher messageDispatcher;
    private Socket socket;

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

    @Override
    public void close() throws Exception {
        super.close();
        inputMessageStreamSupplier.close();
        messageDispatcher.close();
        socket.close();
    }
}

