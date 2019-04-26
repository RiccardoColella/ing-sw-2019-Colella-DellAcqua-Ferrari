package it.polimi.ingsw.client.io;

import it.polimi.ingsw.shared.view.remote.MessageDispatcher;
import it.polimi.ingsw.shared.view.remote.RMIMessageProxy;
import it.polimi.ingsw.shared.view.remote.RMIStreamProvider;
import it.polimi.ingsw.utils.function.exceptions.UnsafeConsumerException;
import it.polimi.ingsw.utils.function.exceptions.UnsafeSupplierException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class RMIConnector extends Connector {

    private final MessageDispatcher messageDispatcher;
    private final RMIMessageProxy messageProxy;

    public RMIConnector(InetSocketAddress address) throws IOException, NotBoundException, InterruptedException {

        RMIStreamProvider provider = (RMIStreamProvider) LocateRegistry.getRegistry(address.getHostName(), address.getPort()).lookup("RMIConnectionEndPoint");
        messageProxy = (RMIMessageProxy) LocateRegistry.getRegistry(address.getHostName(), address.getPort()).lookup(provider.connect());

        messageDispatcher = new MessageDispatcher(
                inputMessageQueue,
                outputMessageQueue,
                () -> {
                    try {
                        return messageProxy.receiveMessage();
                    } catch (RemoteException e) {
                        throw new UnsafeSupplierException("Remote exception occurred " + e);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new UnsafeSupplierException("Thread interrupted " + e);
                    }
                },
                (message) -> {
                    try {
                        messageProxy.sendMessage(message);
                    } catch (RemoteException e) {
                        throw new UnsafeConsumerException("Unable to read data " + e);
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
