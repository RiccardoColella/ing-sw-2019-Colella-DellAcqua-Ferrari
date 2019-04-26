package it.polimi.ingsw.client.io;

import it.polimi.ingsw.server.view.exceptions.ViewDisconnectedException;
import it.polimi.ingsw.shared.view.remote.MessageDispatcher;
import it.polimi.ingsw.shared.view.remote.RMIMessageProxy;
import it.polimi.ingsw.shared.view.remote.RMIStreamProvider;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * This class is the concrete connector implemented with the RMI technology
 *
 * @author Carlo Dell'Acqua
 */
public class RMIConnector extends Connector {

    private final MessageDispatcher messageDispatcher;
    private final RMIMessageProxy messageProxy;

    public RMIConnector(InetSocketAddress address) throws IOException, NotBoundException, InterruptedException {

        RMIStreamProvider provider = (RMIStreamProvider) LocateRegistry.getRegistry(address.getHostName(), address.getPort()).lookup("RMIConnectionEndPoint");
        messageProxy = (RMIMessageProxy) LocateRegistry.getRegistry(address.getHostName(), address.getPort()).lookup(provider.connect());

        messageDispatcher = new MessageDispatcher(
                inputMessageQueue,
                outputMessageQueue,
                (timeout, unit) -> {
                    try {
                        return messageProxy.receiveMessage(timeout, unit);
                    } catch (RemoteException e) {
                        throw new ViewDisconnectedException("Remote exception occurred " + e);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new ViewDisconnectedException("Remote exception occurred " + e);
                    }
                },
                (message) -> {
                    try {
                        messageProxy.sendMessage(message);
                    } catch (RemoteException e) {
                        throw new ViewDisconnectedException("Remote exception occurred " + e);
                    }
                }
        );
    }

    @Override
    public void close() throws Exception {
        messageDispatcher.close();
        messageProxy.close();
    }
}
