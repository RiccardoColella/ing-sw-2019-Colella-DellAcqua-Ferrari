package it.polimi.ingsw.server.controller.weapons;

/**
 * This class represents a range of admissible int values
 */
public class Range {
    /**
     * This property represents the minimum
     */
    private int min;

    /**
     * This property represents the maximum
     */
    private int max;

    /**
     * This constructor creates a range knowing the two extremes
     * @param min the minimum admissible value
     * @param max the maximum admissible value
     */
    public Range(int min, int max) {
        this.max = max;
        this.min = min;
    }

    /**
     * This method gets the minimum value of the range
     * @return an int representing the minimum value of the range
     */
    public int getMin() {
        return this.min;
    }

    /**
     * This method gets the maximum value of the range
     * @return an int representing the maximum value of the range
     */
    public int getMax() {
        return this.max;
    }
}
