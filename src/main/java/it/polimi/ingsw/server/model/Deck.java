package it.polimi.ingsw.server.model;

import java.util.List;

/**
 * Class to manages cards of generic types T
 * @param <T> generic type of the deck
 */
public class Deck<T> {
    /**
     * List of all elements still in the deck
     */
    private List<T> cards;

    /**
     * List of the used element's of the deck
     */
    private List<T> discarded;

    /**
     * Set true if the deck should be recreated with discard List when empty
     */
    private boolean autoRecycleDiscarded;

    /**
     * Class constructor
     * @param autoRecycleDiscarded
     */
    public Deck(boolean autoRecycleDiscarded){
        //TODO all function
        this.autoRecycleDiscarded = autoRecycleDiscarded;
    }

    /**
     * Pick a card from the Deck
     * @param <T> generic card type
     * @return the picked card
     */
    public <T> T pick(){
        //TODO all function
        return null;
    }

    /**
     * Shuffle the Deck
     */
    public void shuffle(){
        //TODO all function
    }

    /**
     * Removes an item from the deck and add it to the discard List
     * @param item to be deleted
     */
    public void discard(T item){
        //TODO all function
    }
}
