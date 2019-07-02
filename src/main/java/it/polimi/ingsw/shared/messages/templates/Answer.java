package it.polimi.ingsw.shared.messages.templates;

import com.google.gson.Gson;

import javax.annotation.Nullable;

/**
 * This class represents a generic answer intended as a result of the previously asked question
 *
 * @author Carlo Dell'Acqua
 * @param <T> the type of the items in the option set
 */
public class Answer<T> {

    private static final Gson gson = new Gson();

    private final T choice;

    /**
     * Constructs an answer
     *
     * @param choice the answer data
     */
    public Answer(@Nullable  T choice) {
        this.choice = choice;
    }

    /**
     * Constructs an empty answer
     */
    public Answer() {
        this(null);
    }

    /**
     * @return true if an answer has been given
     */
    public boolean isPresent() {
        return choice != null;
    }

    /**
     * @return the answer made by the user
     */
    @Nullable
    public T getChoice() {
        return choice;
    }
}
