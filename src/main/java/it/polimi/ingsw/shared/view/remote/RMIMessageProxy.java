package it.polimi.ingsw.shared.view.remote;

import it.polimi.ingsw.shared.messages.Message;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface RMIMessageProxy extends Remote, AutoCloseable {
    Message receiveMessage(int timeout, TimeUnit unit) throws RemoteException, InterruptedException, TimeoutException;

    void sendMessage(Message message) throws RemoteException;
}
