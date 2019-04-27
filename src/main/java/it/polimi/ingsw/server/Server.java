package it.polimi.ingsw.server;

import it.polimi.ingsw.server.bootstrap.GameInitializer;
import it.polimi.ingsw.server.bootstrap.WaitingRoom;
import it.polimi.ingsw.server.bootstrap.factories.RMIViewFactory;
import it.polimi.ingsw.server.bootstrap.factories.SocketViewFactory;
import it.polimi.ingsw.server.controller.Controller;
import it.polimi.ingsw.server.controller.events.MatchEnded;
import it.polimi.ingsw.server.controller.events.listeners.ControllerListener;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;


/**
 * This class is the Server, the core of the application
 *
 * @author Carlo Dell'Acqua
 */
public class Server implements ControllerListener {
    /**
     * Logging utility
     */
    protected final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * A set of configurations for the server
     */
    private final ServerConfig config;

    /**
     * The waiting room which will take care of the clients that connect asynchronously
     */
    private WaitingRoom waitingRoom;

    /**
     * A thread pool used for background task execution
     */
    private ExecutorService threadPool;

    /**
     * A list of currently active virtual rooms representing the matches that are being played
     */
    private List<Controller> activeRooms = new LinkedList<>();

    /**
     * Constructs the server of the game
     *
     * @param config a collection of options that configures the behavior of the server
     */
    public Server(ServerConfig config) {

        RMIViewFactory.initialize(config.getClientAnswerTimeout());
        SocketViewFactory.initialize(config.getClientAnswerTimeout());

        this.config = config;
        waitingRoom = new WaitingRoom(config.getSocketPort(), config.getRMIPort());
        threadPool = Executors.newFixedThreadPool(config.getMaxParallelMatches());
    }

    /**
     * Starts collecting players from the waiting room and fills up the available rooms
     *
     * @throws IOException if errors regarding the socket infrastructure occur
     * @throws InterruptedException if the thread is forced to stop
     */
    public void start() throws IOException, InterruptedException {
        logger.info("Server started!");
        logger.info("Opening the waiting room...");

        waitingRoom.collectAsync();

        for (int i = 0; i < config.getMaxParallelMatches(); i++) {
            createRoom();
        }

        logger.info("Reached the maximum amount of parallel matches, waiting for a room to free up...");
    }

    /**
     * Creates a virtual room using the GameInitializer class and start the execution of the controller
     *
     * @throws InterruptedException if the thread is forced to stop
     */
    private void createRoom() throws InterruptedException {
        // This synchronization is needed to prevent multiple threads from polling the waitingRoom concurrently
        synchronized (activeRooms) {
            GameInitializer initializer = new GameInitializer(
                    waitingRoom,
                    config.getMatchStartTimeout(),
                    config.getMinClients(),
                    config.getMaxClients()
            );
            Controller controller = initializer.initialize();
            activeRooms.add(controller);
            logger.info("Room setup completed, starting the match controller...");

            synchronized (threadPool) {
                if (!threadPool.isShutdown()) {
                    threadPool.execute(controller);
                }
            }
        }
    }

    /**
     * Once a match end a new match is created with the createRoom method
     *
     * @param e the MatchEnded event object
     */
    @Override
    public void onMatchEnd(MatchEnded e) {
        logger.info("Room available, waiting for new clients...");
        try {
            createRoom();
        } catch (InterruptedException ex) {
            logger.warning("Unable to start a new Room, thread interrupted");
            Thread.currentThread().interrupt();
        }
    }
}
