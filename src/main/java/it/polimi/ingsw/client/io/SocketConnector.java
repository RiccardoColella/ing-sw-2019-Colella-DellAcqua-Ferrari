package it.polimi.ingsw.client.io;

import it.polimi.ingsw.shared.view.remote.SocketMessageManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketConnector extends Connector {

    private SocketMessageManager socketMessageManager;
    private Socket socket;

    public SocketConnector(InetSocketAddress address) throws IOException {

        socket = new Socket();
        socket.connect(address);

        socketMessageManager = new SocketMessageManager(socket, inputMessageQueue, outputMessageQueue);
    }

    @Override
    public void close() throws Exception {
        super.close();
        socketMessageManager.close();
        socket.close();
    }
}

