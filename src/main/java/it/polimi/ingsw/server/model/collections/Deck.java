package it.polimi.ingsw.server.model.collections;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Class to manages cards of generic types T
 * @param <T> generic type of the deck
 */
public class Deck<T> implements Collection<T> {
    /**
     * List of all elements still in the deck
     */
    private final LinkedList<T> cards;

    /**
     * List of the used element's of the deck
     */
    private final List<T> discarded;

    /**
     * Set true if the deck should be recreated with discard List when empty
     */
    private boolean recycleDiscarded;

    /**
     * This constructor creates a deck from a list of cards that acts like a Queue.
     * If autoRecycle is enabled, when the Queue is empty previously discarded cards
     * are shuffled and re-inserted into the Queue
     *
     * @param cards the initial cards in the deck
     * @param recycleDiscarded specifies whether the deck should be reinitialized automatically
     *                             when it becomes empty, reshuffling the discarded cards
     */
    public Deck(List<T> cards, boolean recycleDiscarded) {
        this.cards = new LinkedList<>(cards);
        this.discarded = new LinkedList<>();
        this.recycleDiscarded = recycleDiscarded;
        shuffle();
    }

    /**
     * This constructor creates a deck from a list of cards that acts like a Queue
     *
     * @param cards the initial cards in the deck
     */
    public Deck(List<T> cards) {
        this(cards, false);
    }

    /**
     * Pick a card from the Deck
     *
     * @return the picked card
     */
    public Optional<T> pick() {
        if (cards.size() > 0) {
            T picked = cards.remove();
            recycleIfNeeded();
            return Optional.of(picked);
        } else {
            return Optional.empty();
        }
    }

    private void recycleIfNeeded() {
        if (cards.isEmpty() && recycleDiscarded) {
            cards.addAll(discarded);
            discarded.clear();
            shuffle();
        }
    }

    /**
     * Shuffle the Deck
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }

    /**
     * Removes an item from the deck and add it to the discard List
     * @param item to be placed in a temporary container for restoring the deck when it becomes empty
     */
    public void discard(T item) {
        discarded.add(item);
        recycleIfNeeded();
    }


    @Override
    public int size() {
        return this.cards.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        return this.cards.contains(o);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return this.cards.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return this.cards.toArray();
    }

    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {
        return this.cards.toArray(a);
    }

    @Override
    public boolean add(T t) {
        return this.cards.add(t);
    }

    @Override
    public boolean remove(Object o) {
        try {
            return this.cards.remove(o);
        } finally {
            recycleIfNeeded();
        }
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return this.cards.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        return this.cards.addAll(c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        try {
            return this.cards.removeAll(c);
        } finally {
            recycleIfNeeded();
        }
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        boolean changed = this.cards.retainAll(c);
        if (changed) {
            recycleIfNeeded();
        }

        return changed;
    }

    @Override
    public void clear() {
        this.cards.clear();
        this.discarded.clear();
    }
}
