package it.polimi.ingsw.server.view.remote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * This class is used to instantiate the IDs of message proxies shared between
 * the client and the server
 *
 * @author Carlo Dell'Acqua
 */
public class RMIStreamProvider extends UnicastRemoteObject implements it.polimi.ingsw.shared.view.remote.RMIStreamProvider, AutoCloseable {

    private LinkedBlockingQueue<RMIView> messageProxyIds = new LinkedBlockingQueue<>();
    private Function<String, RMIView> rmiViewProvider;

    public RMIStreamProvider(Function<String, RMIView> rmiViewProvider) throws RemoteException {
        super();
        this.rmiViewProvider = rmiViewProvider;
    }

    public RMIView getMessageProxy(int timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        RMIView view = messageProxyIds.poll(timeout, unit);
        if (view != null) {
            return view;
        } else throw new TimeoutException("No view available");
    }

    public synchronized String connect() throws InterruptedException {
        String id = UUID.randomUUID().toString();
        messageProxyIds.add(rmiViewProvider.apply(id));
        return id;
    }


    @Override
    public void close() throws Exception {
        UnicastRemoteObject.unexportObject(this, true);
    }
}
