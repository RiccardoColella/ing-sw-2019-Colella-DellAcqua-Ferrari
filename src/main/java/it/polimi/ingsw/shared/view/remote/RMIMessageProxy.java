package it.polimi.ingsw.shared.view.remote;

import it.polimi.ingsw.shared.messages.Message;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Interface that represents the remote object used as proxy between the client and the server to exchange messages
 *
 * @author Carlo Dell'Acqua
 */
public interface RMIMessageProxy extends Remote, AutoCloseable {

    /**
     * Called by the client this method waits for a message to be available and returns it. A timeout is needed
     * to prevent a deadlock
     *
     * @param timeout the time limit for a message to become available
     * @param unit the measurement unit of the timeout
     * @return the received message
     * @throws RemoteException if a network error occurs
     * @throws InterruptedException if the thread is forced to stop
     * @throws TimeoutException if the specified timeout has been reached without receiving any message
     */
    Message receiveMessage(int timeout, TimeUnit unit) throws RemoteException, InterruptedException, TimeoutException;

    /**
     * Called by the client this method is used to send a message to the server
     *
     * @param message the message to send
     * @throws RemoteException if a network error occurs
     */
    void sendMessage(Message message) throws RemoteException;
}
