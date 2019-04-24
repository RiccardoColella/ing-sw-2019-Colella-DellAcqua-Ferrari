package it.polimi.ingsw.server.view.remote;

import it.polimi.ingsw.server.view.remote.RMIView;
import it.polimi.ingsw.shared.messages.Message;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMIMessageProxy extends UnicastRemoteObject implements it.polimi.ingsw.shared.view.remote.RMIMessageProxy {

    private RMIView rmiView;

    public RMIMessageProxy(RMIView view) throws RemoteException {
        rmiView = view;
    }

    public Message receiveMessage() throws InterruptedException {

        return rmiView.getOutputMessageQueue().take();
    }

    public void sendMessage(Message message) {
        rmiView.getInputMessageQueue().enqueue(message);
    }

}
