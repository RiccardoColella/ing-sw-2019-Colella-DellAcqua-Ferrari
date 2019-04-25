package it.polimi.ingsw.server.view.remote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

public class RMIStreamProvider extends UnicastRemoteObject implements it.polimi.ingsw.shared.view.remote.RMIStreamProvider {

    private LinkedBlockingQueue<String> messageProxyIDs = new LinkedBlockingQueue<>();

    public RMIStreamProvider() throws RemoteException {
        super();
    }

    public String getMessageProxyID() throws InterruptedException {
        return messageProxyIDs.take();
    }

    public synchronized String connect() throws InterruptedException {
        String id = UUID.randomUUID().toString();
        messageProxyIDs.add(id);
        wait();
        return id;
    }
}
