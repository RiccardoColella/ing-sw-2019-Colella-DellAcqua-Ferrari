package it.polimi.ingsw.client.io;

import it.polimi.ingsw.shared.messages.Message;
import it.polimi.ingsw.shared.view.remote.RMIMessageProxy;
import it.polimi.ingsw.shared.view.remote.RMIStreamProvider;
import it.polimi.ingsw.shared.view.remote.SocketMessageManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class RMIConnector extends Connector {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    private RMIMessageProxy messageProxy;
    private ExecutorService threadPool = Executors.newFixedThreadPool(2);

    public RMIConnector(InetSocketAddress address) throws IOException, NotBoundException {

        RMIStreamProvider provider = (RMIStreamProvider) LocateRegistry.getRegistry(address.getHostName(), address.getPort()).lookup("RMIConnectionEndPoint");
        messageProxy = (RMIMessageProxy) LocateRegistry.getRegistry(address.getHostName(), address.getPort()).lookup(provider.connect());

        threadPool.execute(this::receiveMessageAsync);
        threadPool.execute(this::sendMessageAsync);
    }

    private void receiveMessageAsync() {

        try {
            inputMessageQueue.enqueue(
                    messageProxy.receiveMessage()
            );

            if (!threadPool.isShutdown()) {
                threadPool.execute(this::receiveMessageAsync);
            }
        } catch (RemoteException | InterruptedException e) {
            logger.warning("Thread interrupted " + e.toString());
            Thread.currentThread().interrupt();
        }
    }

    private void sendMessageAsync() {
        try {
            messageProxy.sendMessage(outputMessageQueue.take());

            if (!threadPool.isShutdown()) {
                threadPool.execute(this::sendMessageAsync);
            }
        } catch (RemoteException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            logger.warning("Thread interrupted " + ex.toString());
        }
    }

    @Override
    public void close() throws Exception {
        threadPool.shutdown();
        while (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
            logger.warning("Thread pool did not shutdown yet, waiting...");
        }
    }
}
