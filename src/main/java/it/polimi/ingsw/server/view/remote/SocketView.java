package it.polimi.ingsw.server.view.remote;

import it.polimi.ingsw.server.view.View;
import it.polimi.ingsw.shared.InputStreamMessageSupplier;
import it.polimi.ingsw.shared.MessageDispatcher;
import it.polimi.ingsw.shared.OutputStreamMessageConsumer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketOption;
import java.net.SocketOptions;
import java.util.concurrent.TimeUnit;

/**
 * Socket based implementation of the server-side View
 *
 * @author Carlo Dell'Acqua
 */
public class SocketView extends View implements AutoCloseable {


    private static final long LAST_MESSAGE_TIMEOUT = 5000;


    private final InputStreamMessageSupplier inputMessageStreamSupplier;
    private Socket socket;
    private MessageDispatcher messageDispatcher;

    public SocketView(Socket socket, int answerTimeout, TimeUnit answerTimeoutUnit) throws IOException {
        super(answerTimeout, answerTimeoutUnit);

        this.socket = socket;

        inputMessageStreamSupplier = new InputStreamMessageSupplier(new DataInputStream(socket.getInputStream()));

        messageDispatcher = new MessageDispatcher(
                inputMessageQueue,
                outputMessageQueue,
                inputMessageStreamSupplier,
                new OutputStreamMessageConsumer(new DataOutputStream(socket.getOutputStream()))
        );
    }

    // TODO: Verify that the socket is automatically closed
    @Override
    public boolean isConnected() {
        return super.isConnected() && socket.isConnected();
    }

    @Override
    public void close() throws Exception {
        super.close();
        socket.getOutputStream().flush();
        Thread.sleep(LAST_MESSAGE_TIMEOUT);
        socket.close();
        inputMessageStreamSupplier.close();
        messageDispatcher.close();
    }
}
