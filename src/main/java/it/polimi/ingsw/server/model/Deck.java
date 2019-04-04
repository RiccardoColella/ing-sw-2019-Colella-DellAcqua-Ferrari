package it.polimi.ingsw.server.model;

import java.util.Collections;
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
     * @param autoRecycleDiscarded specifies whether the deck should be reinitialized automatically when it becomes empty, reshuffling the discarded cards
     */
    public Deck(boolean autoRecycleDiscarded, List<T> cards) {
        //TODO all function
        this.autoRecycleDiscarded = autoRecycleDiscarded;
        this.cards = cards;
    }

    /**
     * Pick a card from the Deck
     * @param <T> generic card type
     * @return the picked card
     */
    public T pick() {
        //TODO control check
        T picked = null;
        if(cards.isEmpty()){
        }
        else{
            picked = cards.get(0);
            cards.remove(0);
        }
        return picked;
    }

    /**
     * Shuffle the Deck
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }

    /**
     * Removes an item from the deck and add it to the discard List
     * @param item to be deleted
     */
    public void discard(T item) {
        T todiscarde = cards.get(0);
        cards.remove(0);
        discarded.add(todiscarde);
    }
}
