package it.polimi.ingsw.client;

import it.polimi.ingsw.client.ui.cli.CLI;

/**
 * This is the starting point of the client
 *
 * @author Carlo Dell'Acqua
 */
public class Main {
    public static void main( String[] args ) {

        try (CLI cli = new CLI(System.in, System.out)) {
            cli.initialize();
        } catch (Exception e) {
            System.out.println("Could not initialize a valid client, shutting down...");
        }
    }
}
