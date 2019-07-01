package it.polimi.ingsw.client;

import it.polimi.ingsw.client.ui.cli.CLI;
import it.polimi.ingsw.client.ui.gui.GUI;
import javafx.fxml.FXMLLoader;

/**
 * This is the starting point of the client
 *
 * @author Carlo Dell'Acqua
 */
public class Main {
    public static void main( String[] args ) {

        if (args.length > 0 && !args[0].equalsIgnoreCase("gui")) {
            CLI cli;
            try {
                cli = new CLI(System.in, System.out);
                cli.initialize();
            } catch (Exception e) {
                System.out.println("Could not initialize a valid client, shutting down...");
            }
        } else {
            FXMLLoader.setDefaultClassLoader(Main.class.getClassLoader());
            new GUI().start();
        }
    }
}
