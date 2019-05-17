package it.polimi.ingsw.client.ui.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.rmi.NotBoundException;

import static org.junit.jupiter.api.Assertions.*;

class CLITest {

    CLI cli;

    @BeforeEach
    void setUp() {
        cli = new CLI(System.in, System.out);
    }

    @Test
    void initialize() {
        /*try {
            cli.initialize();
        } catch (InterruptedException | IOException | NotBoundException e){
            e.printStackTrace();
        }*/
    }
}