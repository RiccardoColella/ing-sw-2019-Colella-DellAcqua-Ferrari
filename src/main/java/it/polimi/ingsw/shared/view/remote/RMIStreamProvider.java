package it.polimi.ingsw.shared.view.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface that represents the end point provided by the server to the clients that want to join the game
 *
 * @author Carlo Dell'Acqua
 */
public interface RMIStreamProvider extends Remote {

    /**
     * Called by the client this method provide establish a virtual connection between the client and the server
     *
     * @return a unique identifier that corresponds to the name of the object in the RMI registry that implements the message proxy for a newly created connection
     * @throws RemoteException if a network error occurs
     * @throws InterruptedException if the thread is forced to stop
     */
    String connect() throws RemoteException, InterruptedException;
}
