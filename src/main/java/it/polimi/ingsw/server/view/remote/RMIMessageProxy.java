package it.polimi.ingsw.server.view.remote;

import it.polimi.ingsw.shared.messages.Message;

import java.rmi.NoSuchObjectException;
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
public class RMIMessageProxy extends UnicastRemoteObject implements it.polimi.ingsw.shared.rmi.RMIMessageProxy, AutoCloseable {

    /**
     * A callback that will be called once this object is closed
     */
    private final Runnable closeCallback;

    /**
     * The associated RMIView needed to interact with its message queues
     */
    private RMIView rmiView;

    /**
     * Constructs an RMI Message Proxy, an object which emulates an IO stream with the client
     *
     * @param view the RMI View associated with this proxy
     * @param closeCallback a Runnable which will be called once this class is closed
     * @throws RemoteException if an error concerning the RMI API occurs
     */
    public RMIMessageProxy(RMIView view, Runnable closeCallback) throws RemoteException {
        rmiView = view;
        this.closeCallback = closeCallback;
        view.setMessageProxy(this);
    }

    /**
     * Method called by the client to receive messages waiting in the output queue
     *
     * @param timeout a maximum timeout
     * @param unit the time unit of the specified timeout
     * @return the received message
     * @throws InterruptedException if the thread is forced to stop
     * @throws TimeoutException if the maximum timeout has been reached without obtaining any message
     */
    @Override
    public Message receiveMessage(int timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        Message message = rmiView.getOutputMessageQueue().poll(timeout, unit);
        if (message != null) {
            return message;
        } else throw new TimeoutException("Poll timeout");
    }

    /**
     * Method called by the cliet to send messages enqueueing them in the RMIView's input message queue
     *
     * @param message the message the client want to send
     */
    @Override
    public void sendMessage(Message message) {
        rmiView.getInputMessageQueue().enqueue(message);
    }

    /**
     * Closes this object and unexport it from the RMI server
     *
     * @throws NoSuchObjectException if the object had already been unexported
     */
    @Override
    public void close() throws NoSuchObjectException {
        UnicastRemoteObject.unexportObject(this, true);
        closeCallback.run();
    }
}
