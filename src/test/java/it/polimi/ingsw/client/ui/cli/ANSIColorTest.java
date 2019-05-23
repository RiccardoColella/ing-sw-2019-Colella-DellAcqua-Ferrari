package it.polimi.ingsw.client.ui.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ANSIColorTest {

    @Test
    void parseColor() {
        String stringToParse = "Powerup (Color: BLUE)";
        String result = ANSIColor.parseColor(stringToParse);
        String expected = "\u001B[34m" + stringToParse + "\u001B[0m";
        assertEquals(expected, result);

        stringToParse = "OtherString is this (Color: RED)";
        result = ANSIColor.parseColor(stringToParse);
        expected = "\u001B[31m" + stringToParse + "\u001B[0m";
        assertEquals(expected, result);

        stringToParse = "Newton (Color: YELLOW)";
        result = ANSIColor.parseColor(stringToParse);
        expected = "\u001B[33m" + stringToParse + "\u001B[0m";
        assertEquals(expected, result);
    }
}