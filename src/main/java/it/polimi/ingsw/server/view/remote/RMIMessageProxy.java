package it.polimi.ingsw.server.view.remote;

import it.polimi.ingsw.shared.messages.Message;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This class is used as a communication channel between the server and the client.
 * Its methods are invoked by the remote client and take or add messages from and
 * to the output and input message queues
 *
 * @author Carlo Dell'Acqua
 */
public class RMIMessageProxy extends UnicastRemoteObject implements it.polimi.ingsw.shared.view.remote.RMIMessageProxy, AutoCloseable {

    private final Runnable closeCallback;
    private RMIView rmiView;

    public RMIMessageProxy(RMIView view, Runnable closeCallback) throws RemoteException {
        rmiView = view;
        this.closeCallback = closeCallback;
        view.setMessageProxy(this);
    }

    @Override
    public Message receiveMessage(int timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        Message message = rmiView.getOutputMessageQueue().poll(timeout, unit);
        if (message != null) {
            return message;
        } else throw new TimeoutException("Poll timeout");
    }

    @Override
    public void sendMessage(Message message) {
        rmiView.getInputMessageQueue().enqueue(message);
    }

    @Override
    public void close() throws Exception {
        UnicastRemoteObject.unexportObject(this, true);
        closeCallback.run();
    }
}
