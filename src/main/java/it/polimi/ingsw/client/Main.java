package it.polimi.ingsw.client;

import com.google.gson.Gson;
import it.polimi.ingsw.client.ui.cli.CLI;
import it.polimi.ingsw.client.ui.gui.GUI;
import it.polimi.ingsw.utils.ConfigFileMaker;
import javafx.fxml.FXMLLoader;

/**
 * This is the starting point of the client
 *
 * @author Carlo Dell'Acqua
 */
public class Main {

    private static final String CONFIG_JSON_PATH = "./config/clientConfig.json";
    private static final String CONFIG_JSON_PATH_RES = "/config/clientConfig.json";

    public static void main( String[] args ) {

        ClientConfig config = new Gson().fromJson(ConfigFileMaker.load(CONFIG_JSON_PATH, CONFIG_JSON_PATH_RES), ClientConfig.class);

        if (args.length > 0 && !args[0].equalsIgnoreCase("gui")) {
            CLI cli;
            try {
                cli = new CLI(config, System.in, System.out);
                cli.initialize();
            } catch (Exception e) {
                System.out.println("Could not initialize a valid client, shutting down...");
            }
        } else {
            FXMLLoader.setDefaultClassLoader(Main.class.getClassLoader());
            GUI.initialize(config);
            new GUI().start();
        }
    }
}
