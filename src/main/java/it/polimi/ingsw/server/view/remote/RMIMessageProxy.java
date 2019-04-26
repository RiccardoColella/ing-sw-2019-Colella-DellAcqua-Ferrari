package it.polimi.ingsw.server.view.remote;

import it.polimi.ingsw.shared.messages.Message;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMIMessageProxy extends UnicastRemoteObject implements it.polimi.ingsw.shared.view.remote.RMIMessageProxy {

    private final Runnable closeCallback;
    private RMIView rmiView;

    public RMIMessageProxy(RMIView view, Runnable closeCallback) throws RemoteException {
        rmiView = view;
        this.closeCallback = closeCallback;
    }

    public Message receiveMessage() throws InterruptedException {

        return rmiView.getOutputMessageQueue().take();
    }

    public void sendMessage(Message message) {
        rmiView.getInputMessageQueue().enqueue(message);
    }

    @Override
    public void close() throws Exception {
        closeCallback.run();
    }
}
