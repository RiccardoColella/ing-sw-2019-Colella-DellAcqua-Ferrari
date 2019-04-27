package it.polimi.ingsw.server.view.remote;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * This class is used to instantiate the IDs of message proxies shared between
 * the client and the server
 *
 * @author Carlo Dell'Acqua
 */
public class RMIStreamProvider extends UnicastRemoteObject implements it.polimi.ingsw.shared.view.remote.RMIStreamProvider, AutoCloseable {
    /**
     * A queue of RMIViews waiting to be taken out
     */
    private LinkedBlockingQueue<RMIView> rmiViews = new LinkedBlockingQueue<>();

    /**
     * A factory function which will generate RMIViews
     */
    private Function<String, RMIView> rmiViewSupplier;

    /**
     * Constructs an RMIStreamProvider which will translate clients' connection requests into the instantiation of
     * RMIViews that will wait into a queue waiting to be taken out
     *
     * @param rmiViewSupplier a factory function needed to generate RMIViews
     * @throws RemoteException if an exception regarding the RMI API occurs
     */
    public RMIStreamProvider(Function<String, RMIView> rmiViewSupplier) throws RemoteException {
        super();
        this.rmiViewSupplier = rmiViewSupplier;
    }

    /**
     * Dequeues an RMIView from the queue
     *
     * @param timeout the maximum timeout
     * @param unit the unit of the timeout
     * @return an RMIView
     * @throws InterruptedException if the thread is forced to stop
     * @throws TimeoutException if the maximum timeout has been reached without obtaining any RMIView
     */
    public RMIView getRMIView(int timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        RMIView view = rmiViews.poll(timeout, unit);
        if (view != null) {
            return view;
        } else throw new TimeoutException("No view available");
    }

    /**
     * Method called by the client who wants to connect to the server
     *
     * @return a unique identifier of the message proxy that will enable the IO stream of messages
     * @throws InterruptedException if the thread is forced to stop
     */
    public synchronized String connect() throws InterruptedException {
        String id = UUID.randomUUID().toString();
        rmiViews.add(rmiViewSupplier.apply(id));
        return id;
    }

    /**
     * Closes this object and unexport it from the RMI server
     *
     * @throws NoSuchObjectException if the object had already been unexported
     */
    @Override
    public void close() throws NoSuchObjectException {
        UnicastRemoteObject.unexportObject(this, true);
    }
}
