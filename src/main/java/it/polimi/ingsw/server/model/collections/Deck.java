package it.polimi.ingsw.server.model.collections;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This class implements a virtual Deck of generic items of type T
 *
 * @author Carlo Dell'Acqua
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

    /**
     * Shuffles the discarded cards and uses them as the new deck if it is specified so
     */
    private void recycleIfNeeded() {
        if (cards.isEmpty() && recycleDiscarded) {
            cards.addAll(discarded);
            discarded.clear();
            shuffle();
        }
    }

    /**
     * Shuffles the Deck
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


    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return this.cards.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Object o) {
        return this.cards.contains(o);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Iterator<T> iterator() {
        return this.cards.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Object[] toArray() {
        return this.cards.toArray();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {
        return this.cards.toArray(a);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(T t) {
        return this.cards.add(t);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(Object o) {
        try {
            return this.cards.remove(o);
        } finally {
            recycleIfNeeded();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return this.cards.containsAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        return this.cards.addAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        try {
            return this.cards.removeAll(c);
        } finally {
            recycleIfNeeded();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        boolean changed = this.cards.retainAll(c);
        if (changed) {
            recycleIfNeeded();
        }

        return changed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        this.cards.clear();
        this.discarded.clear();
    }
}
