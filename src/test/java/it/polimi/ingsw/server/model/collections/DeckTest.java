package it.polimi.ingsw.server.model.collections;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {

    @Test
    void pick() {
        for (int i = 0; i < 10; i++) {
            testCollection(IntStream.range(0, i).boxed().collect(Collectors.toList()));
        }
    }

    private void testCollection(List<Integer> cards) {
        Deck<Integer> deck = new Deck<>(cards, false);
        assertTrue(deck.containsAll(cards), "Deck does not contains the original list");
        assertTrue(cards.containsAll(deck), "The original list does not contain the deck");

        for (int i = 0; i < cards.size(); i++) {
            assertTrue(cards.contains(deck.pick().orElse(null)), "Deck returned an extraneous card");
        }
        assertFalse(deck.pick().isPresent(), "Deck contains more cards than expected");

        if (cards.size() > 0) {
            deck = new Deck<>(cards, true);
            Optional<Integer> card = Optional.empty();
            int expectedDeckSize = cards.size();
            for (int i = 0; i < cards.size() + 1 && (card = deck.pick()).isPresent(); i++) {
                expectedDeckSize--;
                if (expectedDeckSize <= 0) {
                    expectedDeckSize = cards.size() - 1;
                }
                assertEquals(expectedDeckSize, deck.size(), "Incoherent deck size");
                deck.discard(card.get());
            }
            if (!card.isPresent()) {
                fail("Cards should always have been re-inserted into the deck from the discard stack");
            }
        }
    }
}