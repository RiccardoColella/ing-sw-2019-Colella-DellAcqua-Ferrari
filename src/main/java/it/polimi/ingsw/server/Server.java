package it.polimi.ingsw.server;

import it.polimi.ingsw.server.bootstrap.GameInitializer;
import it.polimi.ingsw.server.bootstrap.WaitingRoom;
import it.polimi.ingsw.server.controller.Controller;
import it.polimi.ingsw.server.controller.events.MatchEnded;
import it.polimi.ingsw.server.controller.events.listeners.ControllerListener;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * This class is the Server, the core of the application
 */
public class Server implements ControllerListener {

    private Logger logger = Logger.getLogger(Server.class.getName());
    private final ServerConfig config;
    private WaitingRoom waitingRoom;
    private Executor threadPool;
    private List<Controller> activeRooms = new LinkedList<>();

    public Server(ServerConfig config) {
        this.config = config;
        waitingRoom = new WaitingRoom(config.getSocketPort(), config.getRMIPort());
        threadPool = Executors.newFixedThreadPool(config.getMaxParallelMatches());
    }

    public void start() throws IOException, InterruptedException {
        logger.info("Server started!");
        logger.info("Opening the waiting room");

        waitingRoom.collectAsync();

        for (int i = 0; i < config.getMaxParallelMatches(); i++) {
            createRoom();
        }

        logger.info("Reached the maximum amount of parallel matches, waiting for a room to free up");
    }

    private void createRoom() throws InterruptedException {
        // This synchronization is needed to prevent multiple threads from polling the waitingRoom concurrently
        synchronized (activeRooms) {
            GameInitializer initializer = new GameInitializer(
                    waitingRoom,
                    config.getMatchStartTimeout(),
                    config.getClientAcceptTimeout(),
                    config.getMinClients(),
                    config.getMaxClients()
            );
            Controller controller = initializer.initialize();
            activeRooms.add(controller);
            threadPool.execute(controller);
        }
    }

    @Override
    public void onMatchEnd(MatchEnded e) {
        logger.info("Room available, waiting for new clients");
        try {
            createRoom();
        } catch (InterruptedException ex) {
            logger.warning("Unable to start a new Room, thread interrupted");
            Thread.currentThread().interrupt();
        }
    }
}
