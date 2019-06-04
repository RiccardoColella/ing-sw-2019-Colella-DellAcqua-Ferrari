package it.polimi.ingsw.client.ui.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ANSIColorTest {

    @Test
    void parseColor() {
        String stringToParse = "Powerup (Color: BLUE)";
        String result = ANSIColor.parseString(stringToParse);
        String expected = "\u001B[34m" + stringToParse + "\u001B[0m";
        assertEquals(expected, result);

        stringToParse = "OtherString is this (Color: RED)";
        result = ANSIColor.parseString(stringToParse);
        expected = "\u001B[31m" + stringToParse + "\u001B[0m";
        assertEquals(expected, result);

        stringToParse = "Newton (Color: YELLOW)";
        result = ANSIColor.parseString(stringToParse);
        expected = "\u001B[33m" + stringToParse + "\u001B[0m";
        assertEquals(expected, result);

        stringToParse = "java.awt.Point[x=2,y=5]";
        result = ANSIColor.parseString(stringToParse);
        expected = "x = 2 | y = 5";
        assertEquals(expected, result);
    }
}