package it.polimi.ingsw.server;

import it.polimi.ingsw.server.bootstrap.WaitingRoom;
import it.polimi.ingsw.server.view.View;
import it.polimi.ingsw.server.view.remote.SocketView;
import it.polimi.ingsw.shared.Direction;
import it.polimi.ingsw.shared.messages.ClientApi;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;

public class Main {

    private final static String SERVER_CONFIG_JSON_FILENAME = "./resources/server-config.json";

    /**
     * This method is the entry-point of our application. After reading the basic configuration file, command line
     * parameters are scanned to search for overwritten settings
     *
     * @param args the CLI arguments
     * @throws IOException thrown if the configuration file is not found
     * @throws InterruptedException thrown on forced thread interruption
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        // Testing

        WaitingRoom waitingRoom = new WaitingRoom(9000, 9090);
        waitingRoom.collectAsync();


        View view = waitingRoom.pop().orElse(null);
        while (view == null) {
            Thread.sleep(1000);
            view = waitingRoom.pop().orElse(null);
        }


        Direction direction = view.select("Scegli", Arrays.asList(Direction.EAST, Direction.NORTH), ClientApi.DIRECTION_QUESTION);


        System.out.println(direction);

        /*
        ServerConfig config = new Gson().fromJson(new FileReader(new File(SERVER_CONFIG_JSON_FILENAME)), ServerConfig.class);

        /*
         * "maxParallelMatches"
         * "matchStartTimeout"
         * "clientAcceptTimeout"
         * "minClients"
         * "maxClients"
         */

        // CLI argument meaning is based on its position
        /*int settingCount = 0;
        if (args.length > settingCount) {
            config.setMaxParallelMatches(Integer.parseInt(args[settingCount]));
        }
        settingCount++;
        if (args.length > settingCount) {
            config.setMatchStartTimeout(Integer.parseInt(args[settingCount]));
        }
        settingCount++;
        if (args.length > settingCount) {
            config.setClientAcceptTimeout(Integer.parseInt(args[settingCount]));
        }
        settingCount++;
        if (args.length > settingCount) {
            config.setMinClients(Integer.parseInt(args[settingCount]));
        }
        settingCount++;
        if (args.length > settingCount) {
            config.setMinClients(Integer.parseInt(args[settingCount]));
        }

        new Server(config).start();*/
    }
}
