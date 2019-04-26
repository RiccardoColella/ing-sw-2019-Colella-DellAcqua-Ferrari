package it.polimi.ingsw.client.io;

import it.polimi.ingsw.shared.view.remote.InputStreamMessageSupplier;
import it.polimi.ingsw.shared.view.remote.MessageDispatcher;
import it.polimi.ingsw.shared.view.remote.OutputStreamMessageConsumer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketConnector extends Connector {

    private MessageDispatcher messageDispatcher;
    private Socket socket;

    public SocketConnector(InetSocketAddress address) throws IOException {

        socket = new Socket();
        socket.connect(address);

        messageDispatcher = new MessageDispatcher(
                inputMessageQueue,
                outputMessageQueue,
                new InputStreamMessageSupplier(new DataInputStream(socket.getInputStream())),
                new OutputStreamMessageConsumer(new DataOutputStream(socket.getOutputStream()))
        );
    }

    @Override
    public void close() throws Exception {
        super.close();
        messageDispatcher.close();
        socket.close();
    }
}

