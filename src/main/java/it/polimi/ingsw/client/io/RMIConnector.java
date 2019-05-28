package it.polimi.ingsw.client.io;

import it.polimi.ingsw.server.view.exceptions.ViewDisconnectedException;
import it.polimi.ingsw.shared.MessageDispatcher;
import it.polimi.ingsw.shared.bootstrap.ClientInitializationInfo;
import it.polimi.ingsw.shared.rmi.RMIMessageProxy;
import it.polimi.ingsw.shared.rmi.RMIStreamProvider;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * This class is the concrete connector implemented with the RMI technology
 *
 * @author Carlo Dell'Acqua
 */
public class RMIConnector extends Connector {
    /**
     * Message dispatching utility for IO
     */
    private MessageDispatcher messageDispatcher;

    /**
     * The object that represents the connection between the client and the server used to send and receive messages
     */
    private RMIMessageProxy messageProxy;

    /**
     * Initializes the RMI-based implementation of the Connector
     *
     * @param clientInitializationInfo the user preferences for the match
     * @param address the remote address the client needs to connect to
     * @throws RemoteException if the RMI registry cannot be reached
     * @throws NotBoundException if the server couldn't provide a valid RMIMessageProxy
     * @throws InterruptedException if the server couldn't provide a valid RMIMessageProxy
     */
    public void initialize(ClientInitializationInfo clientInitializationInfo, InetSocketAddress address) throws RemoteException, NotBoundException, InterruptedException {
        super.initialize(clientInitializationInfo);
        RMIStreamProvider provider = (RMIStreamProvider) LocateRegistry.getRegistry(address.getHostName(), address.getPort()).lookup("RMIConnectionEndPoint");
        messageProxy = (RMIMessageProxy) LocateRegistry.getRegistry(address.getHostName(), address.getPort()).lookup(provider.connect());

        messageDispatcher = new MessageDispatcher(
                inputMessageQueue,
                outputMessageQueue,
                (timeout, unit) -> {
                    try {
                        return messageProxy.receiveMessage(timeout, unit);
                    } catch (RemoteException e) {
                        throw new IOException(e.toString());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IOException(e.toString());
                    }
                },
                message -> {
                    try {
                        messageProxy.sendMessage(message);
                    } catch (RemoteException e) {
                        throw new IOException(e.toString());
                    }
                }
        );
    }

    /**
     * Closes this object and stops the background threads execution
     *
     * @throws Exception if the closing process is forced to stop or the remote resources are unable to correctly close
     */
    @Override
    public void close() throws Exception {
        try {
            if (messageProxy != null) {
                messageProxy.close();
            }
            if (messageDispatcher != null) {
                messageDispatcher.close();
            }
        } catch (ViewDisconnectedException|NoSuchObjectException ex) {
            logger.info("The server already disconnected this client");
        }
        super.close();
    }
}
