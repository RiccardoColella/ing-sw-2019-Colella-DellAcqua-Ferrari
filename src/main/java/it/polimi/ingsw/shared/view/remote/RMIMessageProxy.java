package it.polimi.ingsw.shared.view.remote;

import it.polimi.ingsw.shared.messages.Message;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIMessageProxy extends Remote {
    Message receiveMessage() throws RemoteException, InterruptedException;

    void sendMessage(Message message) throws RemoteException;
}
