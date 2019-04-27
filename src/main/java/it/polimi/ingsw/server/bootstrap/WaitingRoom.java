package it.polimi.ingsw.server.bootstrap;

import it.polimi.ingsw.server.view.View;
import it.polimi.ingsw.server.view.remote.RMIMessageProxy;
import it.polimi.ingsw.server.view.remote.RMIStreamProvider;
import it.polimi.ingsw.server.view.remote.RMIView;
import it.polimi.ingsw.server.view.remote.SocketView;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * This class is used to create a virtual waiting room in which RMI and Socket clients will wait until a virtual
 * game room is available
 *
 * @author Carlo Dell'Acqua
 */
public class WaitingRoom implements AutoCloseable {

    private static final int ACCEPT_TIMEOUT = 1000;
    private static final int SCHEDULED_TASK_PERIOD = 1000;


    private class SocketAcceptor implements Callable<View>, AutoCloseable {
        private ServerSocket socket;

        public SocketAcceptor(int port) throws IOException {
            socket = new ServerSocket(port);
            socket.setSoTimeout(ACCEPT_TIMEOUT);
        }

        @Override
        public void close() throws Exception {
            socket.close();
        }

        @Override
        public View call() throws Exception {
            return new SocketView(socket.accept(), answerTimeoutMilliseconds, TimeUnit.MILLISECONDS);
        }
    }

    private class RMIAcceptor implements Callable<View>, AutoCloseable {

        private static final String RMI_CONNECTION_END_POINT = "RMIConnectionEndPoint";

        private final RMIStreamProvider provider;
        private Registry registry;
        private boolean closing = false;
        private final ExecutorService threadPool = Executors.newFixedThreadPool(1);

        private class RMIMessageProxyRegisterTask implements Runnable {

            private final String messageProxyId;
            private final RMIView view;

            public RMIMessageProxyRegisterTask(String messageProxyId, RMIView view) {
                this.messageProxyId = messageProxyId;
                this.view = view;
            }

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

        private class RMIMessageProxyUnregisterTask implements Runnable {

            private final String messageProxyId;

            public RMIMessageProxyUnregisterTask(String messageProxyId) {
                this.messageProxyId = messageProxyId;
            }

            @Override
            public void run() {
                try {
                    registry.unbind(messageProxyId);
                } catch (RemoteException | NotBoundException e) {
                    logger.warning("Unable to unbind " + messageProxyId + " " + e);
                }
            }
        }

        public RMIAcceptor(int port) throws IOException {
            System.setProperty("java.rmi.server.hostname", "diemisto");

            registry = java.rmi.registry.LocateRegistry.createRegistry(port);

            provider = new RMIStreamProvider(id -> {
                RMIView view = new RMIView(answerTimeoutMilliseconds, TimeUnit.MILLISECONDS);
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

                return view;
            });
            registry.rebind(RMI_CONNECTION_END_POINT, provider);
        }

        @Override
        public void close() throws Exception {
            closing = true;
            registry.unbind(RMI_CONNECTION_END_POINT);
            provider.close();
            synchronized (threadPool) {
                threadPool.shutdown();
            }
            while (!threadPool.awaitTermination(1, TimeUnit.SECONDS)) {
                logger.warning("Thread pool hasn't shut down yet, waiting...");
            }
        }

        @Override
        public View call() throws Exception {
            do {
                try {
                    return provider.getMessageProxy(ACCEPT_TIMEOUT, TimeUnit.MILLISECONDS);
                } catch (TimeoutException ignored) { }
            } while (!closing);
            throw new InterruptedException("RMIAcceptor stopped");
        }
    }

    private Logger logger = Logger.getLogger(WaitingRoom.class.getName());
    private SocketAcceptor socketAcceptor;
    private RMIAcceptor rmiAcceptor;
    private final Queue<View> connectedViews = new LinkedList<>();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(3);
    private Future<View> currentRMITask;
    private Future<View> currentSocketTask;
    private int socketPort;
    private int rmiPort;
    private int answerTimeoutMilliseconds;

    public WaitingRoom(int socketPort, int rmiPort, int answerTimeoutMilliseconds) {
        this.socketPort = socketPort;
        this.rmiPort = rmiPort;
        this.answerTimeoutMilliseconds = answerTimeoutMilliseconds;
    }

    public void collectAsync() throws IOException {
        rmiAcceptor = new RMIAcceptor(rmiPort);
        socketAcceptor = new SocketAcceptor(socketPort);
        // We prepare the task to get our first Future, it will then be overwritten once we get the promised result
        currentRMITask = threadPool.submit(rmiAcceptor);
        currentSocketTask = threadPool.submit(socketAcceptor);

        threadPool.execute(this::scheduledTask);
    }

    private void scheduledTask() {
        connectToNewViews();
        removeDisconnectedViews();
        try {
            Thread.sleep(SCHEDULED_TASK_PERIOD);
            // Scheduling future execution

            synchronized (threadPool) {
                if (!threadPool.isShutdown()) {
                    threadPool.execute(this::scheduledTask);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void connectToNewViews() {
        synchronized (connectedViews) {
            try {

                if (currentSocketTask.isDone()) {
                    connectedViews.add(
                            currentSocketTask
                                    .get()
                    );

                    // The previous task has been consumed, we can now submit a new task for waiting new views
                    synchronized (threadPool) {
                        if (!threadPool.isShutdown()) {
                            currentSocketTask = threadPool.submit(socketAcceptor);
                        }
                    }
                }
            } catch (ExecutionException ex) {
                synchronized (threadPool) {
                    if (!threadPool.isShutdown()) {
                        currentSocketTask = threadPool.submit(socketAcceptor);
                    }
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            try {
                if (currentRMITask.isDone()) {
                    connectedViews.add(
                            currentRMITask
                                    .get()
                    );

                    // The previous task has been consumed, we can now submit a new task for waiting new views
                    synchronized (threadPool) {
                        if (!threadPool.isShutdown()) {
                            currentRMITask = threadPool.submit(rmiAcceptor);
                        }
                    }
                }
            } catch (ExecutionException ex) {
                synchronized (threadPool) {
                    if (!threadPool.isShutdown()) {
                        currentRMITask = threadPool.submit(rmiAcceptor);
                    }
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void removeDisconnectedViews() {
        synchronized (connectedViews) {
            connectedViews.removeIf(view -> !view.isConnected());
        }
    }

    public Optional<View> pop() {
        synchronized (connectedViews) {
            if (connectedViews.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(connectedViews.remove());
            }
        }
    }


    @Override
    public void close() throws Exception {

        socketAcceptor.close();
        rmiAcceptor.close();
        connectedViews.clear();

        synchronized (threadPool) {
            threadPool.shutdown();
        }
        while (!threadPool.awaitTermination(1, TimeUnit.SECONDS)) {
            logger.warning("Thread pool hasn't shut down yet, waiting...");
        }
    }
}
