package it.polimi.ingsw.server.model.collections;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {

    /**
     * This test covers the methods pick, discard and recycleIfNeeded using initial lists of different sizes
     */
    @Test
    void pick() {
        for (int i = 0; i < 10; i++) {
            testCollection(IntStream.range(0, i).boxed().collect(Collectors.toList()));
        }
    }

    /**
     * Test the Deck collection initialized with the given list of cards and creating two decks:
     * - a normal deck that becomes empty after enough calls of the method "pick"
     * - an auto recycling deck, which should preserve all the cards that are picked and then sent back to the discard stack
     *
     * @param cards the initial list of cards
     */
    private void testCollection(List<Integer> cards) {
        // Initializing a normal deck
        Deck<Integer> deck = new Deck<>(cards, false);

        // Checking that cards and deck correspond to the same abstract set of items
        assertTrue(deck.containsAll(cards), "Deck does not contains the original list");
        assertTrue(cards.containsAll(deck), "The original list does not contain the deck");

        // Taking out all the cards
        for (int i = 0; i < cards.size(); i++) {
            assertTrue(cards.contains(deck.pick().orElse(null)), "Deck returned an extraneous card");
        }
        assertFalse(deck.pick().isPresent(), "Deck contains more cards than expected");

        // Initializing an auto-recycling deck
        deck = new Deck<>(cards, true);
        Optional<Integer> card = Optional.empty();
        int expectedDeckSize = cards.size();
        for (int i = 0; i < cards.size() + 1 && (card = deck.pick()).isPresent(); i++) {
            expectedDeckSize--;
            if (expectedDeckSize <= 0) {
                expectedDeckSize = cards.size() - 1;
            }
            assertEquals(expectedDeckSize, deck.size(), "Incoherent deck size");
            // Sending the picked card to the discard stack
            deck.discard(card.get());
        }
        // If the initial list had a size greater than zero this should never be the case,
        // due to the fact that a picked card is always re-inserted into the deck
        if (!card.isPresent() && cards.size() > 0) {
            fail("Cards should always have been re-inserted into the deck from the discard stack");
        }
    }
}