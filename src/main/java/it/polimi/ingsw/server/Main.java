package it.polimi.ingsw.server;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Main {

    private static final String SERVER_CONFIG_JSON_FILENAME = "./resources/server-config.json";

    /**
     * This method is the entry-point of our application. After reading the basic configuration file, command line
     * parameters are scanned to search for overwritten settings
     *
     * @param args the CLI arguments
     * @throws IOException thrown if the configuration file is not found
     * @throws InterruptedException thrown on forced thread interruption
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        /*
         * "maxParallelMatches"
         * "matchStartTimeout"
         * "clientAnswerTimeout"
         * "minClients"
         * "maxClients"
         * "rmiPort"
         * "socketPort"
         */

        ServerConfig config = new Gson().fromJson(new FileReader(new File(SERVER_CONFIG_JSON_FILENAME)), ServerConfig.class);

        // CLI argument meaning is based on its position
        int settingCount = 0;

        if (args.length > settingCount++)
            config.setMaxParallelMatches(Integer.parseInt(args[settingCount]));
        if (args.length > settingCount++)
            config.setMatchStartTimeout(Integer.parseInt(args[settingCount]));
        if (args.length > settingCount++)
            config.setClientAnswerTimeout(Integer.parseInt(args[settingCount]));
        if (args.length > settingCount++)
            config.setMinClients(Integer.parseInt(args[settingCount]));
        if (args.length > settingCount++)
            config.setMaxClients(Integer.parseInt(args[settingCount]));
        if (args.length > settingCount++)
            config.setRMIPort(Integer.parseInt(args[settingCount]));
        if (args.length > settingCount++)
            config.setSocketPort(Integer.parseInt(args[settingCount]));

        new Server(config).start();
    }
}
