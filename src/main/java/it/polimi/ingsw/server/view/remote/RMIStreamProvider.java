package it.polimi.ingsw.server.view.remote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;

public class RMIStreamProvider extends UnicastRemoteObject implements it.polimi.ingsw.shared.view.remote.RMIStreamProvider {

    private String messageProxyID = "";

    public RMIStreamProvider() throws RemoteException {
        super();
    }

    public String getMessageProxyID() {
        return messageProxyID;
    }

    public synchronized String connect() {
        notifyAll();
        messageProxyID = UUID.randomUUID().toString();
        return messageProxyID;
    }
}
