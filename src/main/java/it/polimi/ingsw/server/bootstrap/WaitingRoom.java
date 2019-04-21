package it.polimi.ingsw.server.bootstrap;

import it.polimi.ingsw.server.view.View;
import it.polimi.ingsw.server.view.remote.SocketView;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * This class is used to create a virtual waiting room in which RMIConnector and Socket clients will wait until a virtual
 * game room is available
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
            return new SocketView(socket.accept());
        }
    }

    private Logger logger = Logger.getLogger(WaitingRoom.class.getName());
    private SocketAcceptor socketAcceptor;
    private final Queue<View> connectedViews = new LinkedList<>();
    private ExecutorService threadPool;
    private Future<View> currentSocketTask;
    private int socketPort;
    private int rmiPort;

    public WaitingRoom(int socketPort, int rmiPort) {
        this.socketPort = socketPort;
        this.rmiPort = rmiPort;
    }

    public void collectAsync() throws IOException {
        // TODO: add rmiAcceptor
        socketAcceptor = new SocketAcceptor(socketPort);
        threadPool = Executors.newFixedThreadPool(3);
        // We prepare the task to get our first Future, it will then be overwritten once we get the promised result
        currentSocketTask = threadPool.submit(socketAcceptor);
        threadPool.execute(this::scheduledTask);
    }

    private void scheduledTask() {
        connectToNewViews();
        removeDisconnectedViews();
        try {
            Thread.sleep(SCHEDULED_TASK_PERIOD);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }


        if (!threadPool.isShutdown()) {
            // Scheduling future execution
            threadPool.execute(this::scheduledTask);
        }
    }

    private void connectToNewViews() {
        synchronized (connectedViews) {
            try {
                connectedViews.add(
                    currentSocketTask
                        .get(ACCEPT_TIMEOUT, TimeUnit.MILLISECONDS)
                );

                // If the previous instruction did succeed then we can submit a new task for future calls,
                // otherwise this instruction will not be executed and on the next call currentSocketTask
                // will either hold the promised result or not
                if (!threadPool.isShutdown()) {
                    currentSocketTask = threadPool.submit(socketAcceptor);
                }
            } catch (ExecutionException ex) {
                if (!threadPool.isShutdown()) {
                    currentSocketTask = threadPool.submit(socketAcceptor);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (TimeoutException ex) {
                logger.info("No view tried to connect, retrying...");
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
        threadPool.shutdown();
        while (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
            logger.warning("Thread pool did not shutdown yet, waiting...");
        }
        socketAcceptor.close();
        connectedViews.clear();
    }
}
