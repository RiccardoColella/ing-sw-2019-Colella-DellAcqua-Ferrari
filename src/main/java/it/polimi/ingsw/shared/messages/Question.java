package it.polimi.ingsw.shared.messages;

import java.util.Collection;

/**
 * This class represents a generic question intended as a question text and a set of options to choose from
 *
 * @author Carlo Dell'Acqua
 * @param <T> the type of the items in the option set
 */
public class Question<T> {

    /**
     * The question text
     */
    private final String text;

    /**
     * The set of possible answers
     */
    private final Collection<T> availableOptions;

    /**
     * Indicates whether or not the answer is mandatory
     */
    private final boolean skippable;

    /**
     * Constructs a question
     *
     * @param text the text of the question
     * @param availableOptions the set of options to choose from
     * @param skippable a flag that indicates whether or not an answer is mandatory
     */
    public Question(String text, Collection<T> availableOptions, boolean skippable) {
        this.text = text;
        this.availableOptions = availableOptions;
        this.skippable = skippable;
    }

    /**
     * Constructs a non-skippable question
     *
     * @param text the text of the question
     * @param availableOptions the set of options to choose from
     */
    public Question(String text, Collection<T> availableOptions) {
        this(text, availableOptions, false);
    }


    public String getText() {
        return text;
    }

    public Collection<T> getAvailableOptions() {
        return availableOptions;
    }

    public boolean isSkippable() {
        return skippable;
    }
}
