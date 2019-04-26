package it.polimi.ingsw.server.view.remote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * This class is used to instantiate the IDs of message proxies shared between
 * the client and the server
 *
 * @author Carlo Dell'Acqua
 */
public class RMIStreamProvider extends UnicastRemoteObject implements it.polimi.ingsw.shared.view.remote.RMIStreamProvider {

    private LinkedBlockingQueue<String> messageProxyIds = new LinkedBlockingQueue<>();

    public RMIStreamProvider() throws RemoteException {
        super();
    }

    public Optional<String> getMessageProxyId(int timeout, TimeUnit unit) throws InterruptedException {
        return Optional.ofNullable(messageProxyIds.poll(timeout, unit));
    }

    public synchronized String connect() throws InterruptedException {
        String id = UUID.randomUUID().toString();
        messageProxyIds.add(id);
        wait();
        return id;
    }
}
