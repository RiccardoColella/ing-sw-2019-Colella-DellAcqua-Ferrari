package it.polimi.ingsw.server;

import com.google.gson.Gson;
import it.polimi.ingsw.utils.ConfigFileMaker;

import java.io.IOException;

/**
 * Entry point of the server
 *
 * @author Carlo Dell'Acqua
 */
public class Main {

    private static final String SERVER_CONFIG_JSON_PATH = "./config/serverConfig.json";
    private static final String SERVER_CONFIG_JSON_PATH_RES = "/config/serverConfig.json";

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
         * "rmiHostname"
         */

        ServerConfig config = new Gson().fromJson(ConfigFileMaker.load(SERVER_CONFIG_JSON_PATH, SERVER_CONFIG_JSON_PATH_RES), ServerConfig.class);

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
        if (args.length > settingCount++)
            config.setRMIHostname(args[settingCount]);

        Server server = new Server(config);
        server.start();


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.close();
            } catch (Exception ex) {
                System.exit(0);
                Runtime.getRuntime().halt(0);
            }
        }));
    }
}
