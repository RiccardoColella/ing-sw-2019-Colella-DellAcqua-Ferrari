package it.polimi.ingsw.utils;

public class Tuple<X, Y> {

    private final X item1;
    private final Y item2;

    public Tuple(X item1, Y item2) {
        this.item1 = item1;
        this.item2 = item2;
    }

    public X getItem1() {
        return item1;
    }

    public Y getItem2() {
        return item2;
    }
}