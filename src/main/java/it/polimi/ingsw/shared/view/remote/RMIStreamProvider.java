package it.polimi.ingsw.shared.view.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIStreamProvider extends Remote {
    String connect() throws RemoteException;
}
