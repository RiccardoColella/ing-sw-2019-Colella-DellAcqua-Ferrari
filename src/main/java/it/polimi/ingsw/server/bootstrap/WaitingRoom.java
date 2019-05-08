package it.polimi.ingsw.server.bootstrap;

import it.polimi.ingsw.server.bootstrap.acceptors.RMIAcceptor;
import it.polimi.ingsw.server.bootstrap.acceptors.SocketAcceptor;
import it.polimi.ingsw.server.view.View;
import it.polimi.ingsw.server.view.exceptions.ViewDisconnectedException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This class is used to create a virtual waiting room in which RMI and Socket clients will wait until a virtual
 * game room is available
 *
 * @author Carlo Dell'Acqua
 */
public class WaitingRoom implements AutoCloseable {
    /**
     * Interval between the execution of the scheduleTask method
     */
    private static final int SCHEDULED_TASK_PERIOD = 1000;



    /**
     * Logging utility
     */
    protected final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * The Socket-based acceptor
     */
    private SocketAcceptor socketAcceptor;

    /**
     * The RMI-based acceptor
     */
    private RMIAcceptor rmiAcceptor;

    /**
     * A queue containing an updated collection of connected views
     */
    private final Queue<View> connectedViews = new LinkedList<>();

    /**
     * A list containing the collection of the nicknames of the players that are connected
     */
    private final List<String> connectedNicknames = new LinkedList<>();

    /**
     * The thread pool that runs the background tasks
     */
    private final ExecutorService threadPool = Executors.newFixedThreadPool(3);

    /**
     * The current RMI task that will hold the result as a future "promise"
     */
    private Future<View> currentRMITask;

    /**
     * The current Socket task that will hold the result as a future "promise"
     */
    private Future<View> currentSocketTask;

    /**
     * The socket listening port
     */
    private int socketPort;

    /**
     * The RMI listening port
     */
    private int rmiPort;

    /**
     * Constructs a waiting room to let clients connect to the server and wait till there is a room available for a Match
     *
     * @param socketPort the port to listen for the socket clients
     * @param rmiPort the port to listen for the RMI clients
     */
    public WaitingRoom(int socketPort, int rmiPort) {
        this.socketPort = socketPort;
        this.rmiPort = rmiPort;
    }

    /**
     * Starts the background task that will collect clients
     *
     * @throws IOException if the server socket creation fails
     */
    public void collectAsync() throws IOException {
        rmiAcceptor = new RMIAcceptor(rmiPort);
        socketAcceptor = new SocketAcceptor(socketPort);
        // We prepare the task to get our first Future, it will then be overwritten once we get the promised result
        currentRMITask = threadPool.submit(rmiAcceptor);
        currentSocketTask = threadPool.submit(socketAcceptor);

        threadPool.execute(this::scheduledTask);
    }

    /**
     * Periodically retrieves clients from the acceptors and removes the ones that disconnected
     */
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

    /**
     * Using the acceptors verifies that one client per technology (socket or RMI) connected and adds
     * it to the queue
     */
    private void connectToNewViews() {
        try {

            if (currentSocketTask.isDone()) {
                addNewView(currentSocketTask.get());

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
                addNewView(currentRMITask.get());

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

    /**
     * Removes views that disconnected while waiting in the queue
     */
    private void removeDisconnectedViews() {
        List<View> disconnectedViews;
        synchronized (connectedViews) {
            disconnectedViews = connectedViews
                    .stream()
                    .filter(view -> !view.isConnected())
                    .collect(Collectors.toList());
            connectedViews.removeAll(disconnectedViews);
        }
        synchronized (connectedNicknames) {
            disconnectedViews.forEach(v -> connectedNicknames.remove(v.getNickname()));
        }
        disconnectedViews.forEach(this::closeView);
    }

    private void addNewView(View view) {

        try {
            view.initialize();
            synchronized (connectedViews) {
                if (connectedNicknames.stream().anyMatch(n -> n.equals(view.getNickname()))) {
                    logger.info("Cannot add player. Duplicated nickname " + view.getNickname());
                    closeView(view);
                } else {
                    connectedViews.add(view);
                    connectedNicknames.add(view.getNickname());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warning("Thread stopped " + e);
        } catch (ViewDisconnectedException e) {
            logger.warning("View disconnected during initialization");
        }
    }

    private void closeView(View view) {
        try {
            view.close();
        } catch (Exception e) {
            logger.warning("Unable to close view " + e);
        }
    }

    /**
     * Return a connected with if there is any, otherwise an empty optional
     * @return an optional of a view
     */
    public Optional<View> pop() {
        synchronized (connectedViews) {
            if (connectedViews.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(connectedViews.remove());
            }
        }
    }

    /**
     * Closes this object and stops the background threads execution
     *
     * @throws Exception if the closing process is forced to stop or the remote resources are unable to correctly close or the socket cannot be closed
     */
    @Override
    public void close() throws Exception {

        socketAcceptor.close();
        rmiAcceptor.close();
        connectedViews.clear();

        synchronized (threadPool) {
            threadPool.shutdown();
        }
        while (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
            logger.warning("Thread pool hasn't shut down yet, waiting...");
        }
    }
}
