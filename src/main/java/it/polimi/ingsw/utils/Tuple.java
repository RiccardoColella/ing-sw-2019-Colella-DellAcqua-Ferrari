package it.polimi.ingsw.utils;

/**
 * This class represents a 2-tuple
 *
 * @param <X> the type of the first item
 * @param <Y> the type of the second item
 *
 * @author Adriana Ferrari
 */
public class Tuple<X, Y> {

    /**
     * First item
     */
    private final X item1;
    /**
     * Second item
     */
    private final Y item2;

    /**
     * Constructs a tuple, given two items of any type
     *
     * @param item1 the first item
     * @param item2 the second item
     */
    public Tuple(X item1, Y item2) {
        this.item1 = item1;
        this.item2 = item2;
    }

    /**
     * @return the first item
     */
    public X getItem1() {
        return item1;
    }

    /**
     * @return the second item
     */
    public Y getItem2() {
        return item2;
    }
}