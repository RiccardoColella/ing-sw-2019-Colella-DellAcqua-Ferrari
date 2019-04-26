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
    private static final int SCHEDULED_TASK_PERIOD = 10000;


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
        private boolean closed = false;

        public RMIAcceptor(int port) throws IOException {
            System.setProperty("java.rmi.server.hostname", "192.168.1.251");
            registry = java.rmi.registry.LocateRegistry.createRegistry(port);
            provider = new RMIStreamProvider();
            registry.rebind(RMI_CONNECTION_END_POINT, provider);
        }

        @Override
        public void close() throws Exception {
            registry.unbind(RMI_CONNECTION_END_POINT);
            closed = true;
        }

        @Override
        public View call() throws Exception {
            Optional<String> idOptional;
            do {
                idOptional = provider.getMessageProxyId(ACCEPT_TIMEOUT, TimeUnit.MILLISECONDS);
            } while (!idOptional.isPresent() && !closed);
            if (idOptional.isPresent()) {
                String id = idOptional.get();
                RMIView view = new RMIView(answerTimeoutMilliseconds, TimeUnit.MILLISECONDS);
                registry.rebind(id, new RMIMessageProxy(view, () -> {
                    try {
                        // TODO: verify that the default RMI policy does not prevent this registry call from a remote object callback
                        registry.unbind(id);
                    } catch (RemoteException | NotBoundException e) {
                        logger.warning("Unable to unbind " + id + " " + e);
                    }
                }));
                synchronized (provider) {
                    provider.notifyAll();
                }
                return view;
            } else throw new InterruptedException("RMIAcceptor stopped");
        }
    }

    private Logger logger = Logger.getLogger(WaitingRoom.class.getName());
    private SocketAcceptor socketAcceptor;
    private RMIAcceptor rmiAcceptor;
    private final Queue<View> connectedViews = new LinkedList<>();
    private ExecutorService threadPool;
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
        threadPool = Executors.newFixedThreadPool(3);
        // We prepare the task to get our first Future, it will then be overwritten once we get the promised result
        currentRMITask = threadPool.submit(rmiAcceptor);
        currentSocketTask = threadPool.submit(socketAcceptor);

        synchronized (threadPool) {
            if (!threadPool.isShutdown()) {
                threadPool.execute(this::scheduledTask);
            }
        }
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
                    currentSocketTask = threadPool.submit(socketAcceptor);
                }
            } catch (ExecutionException ex) {
                currentSocketTask = threadPool.submit(socketAcceptor);
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
                    currentRMITask = threadPool.submit(rmiAcceptor);
                }
            } catch (ExecutionException ex) {
                currentRMITask = threadPool.submit(rmiAcceptor);
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
        synchronized (threadPool) {
            threadPool.shutdown();
        }
        while (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
            logger.warning("Thread pool hasn't shut down yet, waiting...");
        }
        socketAcceptor.close();
        connectedViews.clear();
    }
}
