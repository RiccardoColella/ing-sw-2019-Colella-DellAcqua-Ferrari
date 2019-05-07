package it.polimi.ingsw.server.bootstrap.acceptors;

import it.polimi.ingsw.server.bootstrap.factories.RMIViewFactory;
import it.polimi.ingsw.server.view.View;
import it.polimi.ingsw.server.view.remote.RMIMessageProxy;
import it.polimi.ingsw.server.view.remote.RMIStreamProvider;
import it.polimi.ingsw.server.view.remote.RMIView;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * This class is used to listen for RMI clients
 */
public class RMIAcceptor implements Acceptor, AutoCloseable {
    /**
     * Timeout needed to prevent deadlocks
     */
    private static final int ACCEPT_TIMEOUT = 1000;

    /**
     * Logging utility
     */
    protected final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * The name of the remote object used as an end point to connect to
     */
    private static final String RMI_CONNECTION_END_POINT = "RMIConnectionEndPoint";

    /**
     * This object provides a valid stream for the RMI-based communication
     */
    private final RMIStreamProvider provider;

    /**
     * RMI registry
     */
    private Registry registry;

    /**
     * Boolean indicating whether or not the close method has been invoked
     */
    private boolean closing = false;

    /**
     * The thread pool that runs the async tasks
     */
    private final ExecutorService threadPool = Executors.newFixedThreadPool(1);

    /**
     * Task used to register new RMIMessageProxies
     */
    private class RMIMessageProxyRegisterTask implements Runnable {
        /**
         * The message proxy unique identifier
         */
        private final String messageProxyId;

        /**
         * The RMIView to attach the RMIMessageProxy to
         */
        private final RMIView view;

        /**
         * Constructs a register task for the given view
         *
         * @param messageProxyId the ID of the message proxy
         * @param view the view to attach the message proxy to
         */
        public RMIMessageProxyRegisterTask(String messageProxyId, RMIView view) {
            this.messageProxyId = messageProxyId;
            this.view = view;
        }

        /**
         * Runs the task binding the RMIMessageProxy to the RMI registry
         */
        @Override
        public void run() {
            try {
                registry.rebind(messageProxyId, new RMIMessageProxy(view, () -> {
                    synchronized (threadPool) {
                        if (!threadPool.isShutdown()) {
                            threadPool.submit(new RMIMessageProxyUnregisterTask(messageProxyId));
                        }
                    }
                }));
            } catch (RemoteException e) {
                throw new IllegalStateException("Cannot create a message proxy for a disconnected client " + e);
            }
        }
    }

    /**
     * Unregistering task used to dispose the message proxy
     */
    private class RMIMessageProxyUnregisterTask implements Runnable {
        /**
         * The unique identifier of the message proxy to unregister
         */
        private final String messageProxyId;

        /**
         * Constructs a task that will unregister a message proxy
         *
         * @param messageProxyId the id of the message proxy
         */
        public RMIMessageProxyUnregisterTask(String messageProxyId) {
            this.messageProxyId = messageProxyId;
        }

        /**
         * Runs this task, unbinding the message proxy from the RMI registry
         */
        @Override
        public void run() {
            try {
                registry.unbind(messageProxyId);
            } catch (RemoteException | NotBoundException e) {
                logger.warning("Unable to unbind " + messageProxyId + " " + e);
            }
        }
    }

    /**
     * Constructs a RMIAcceptor that will listen on the given port
     *
     * @param port listening port
     * @throws IOException if the registry cannot be created
     */
    public RMIAcceptor(int port) throws IOException {
        registry = java.rmi.registry.LocateRegistry.createRegistry(port);

        provider = new RMIStreamProvider(id -> {
            RMIView view = RMIViewFactory.createRMIView();
            try {
                Future messageProxyTask = null;
                synchronized (threadPool) {
                    if (!threadPool.isShutdown()) {
                        messageProxyTask = threadPool.submit(new RMIMessageProxyRegisterTask(id, view));
                    }
                }
                if (messageProxyTask != null) {
                    messageProxyTask.get();
                } else throw new InterruptedException("Thread pool interrupted");

            } catch (ExecutionException e) {
                logger.warning("Unable to instantiate a valid message proxy " + e);
            } catch (InterruptedException e) {
                logger.warning("Unable to instantiate a valid message proxy " + e);
                Thread.currentThread().interrupt();
            }

            logger.info("New RMI client connected");

            return view;
        });
        registry.rebind(RMI_CONNECTION_END_POINT, provider);
    }

    /**
     * Closes this object and stops the background threads execution
     *
     * @throws Exception if the closing process is forced to stop
     */
    @Override
    public void close() throws Exception {
        closing = true;
        registry.unbind(RMI_CONNECTION_END_POINT);
        provider.close();
        synchronized (threadPool) {
            threadPool.shutdown();
        }
        while (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
            logger.warning("Thread pool hasn't shut down yet, waiting...");
        }
    }

    /**
     * Creates a view once a RMI-based client connects
     *
     * @return an RMIView
     * @throws InterruptedException if the task is forced to stop
     */
    @Override
    public View call() throws InterruptedException {
        do {
            try {
                return provider.getRMIView(ACCEPT_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (TimeoutException ignored) {
                // No client connected within the timeout
            }
        } while (!closing);
        throw new InterruptedException("RMIAcceptor stopped");
    }
}
