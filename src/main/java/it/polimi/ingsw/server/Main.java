package it.polimi.ingsw.server;

import com.google.gson.Gson;
import it.polimi.ingsw.server.controller.Controller;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.match.MatchFactory;
import it.polimi.ingsw.server.view.View;
import it.polimi.ingsw.server.view.remote.SocketView;
import it.polimi.ingsw.shared.Direction;
import it.polimi.ingsw.shared.commands.ClientApi;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

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

        View view = new SocketView(new ServerSocket(9090).accept());

        Direction direction = view.select("Scegli", Arrays.asList(Direction.EAST, Direction.NORTH), ClientApi.DIRECTION_QUESTION);




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
