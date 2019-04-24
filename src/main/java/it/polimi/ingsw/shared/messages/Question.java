package it.polimi.ingsw.shared.messages;

import java.util.Collection;

public class Question<T> {

    private final String text;
    private final Collection<T> availableOptions;
    private final boolean skippable;

    public Question(String text, Collection<T> availableOptions, boolean skippable) {
        this.text = text;
        this.availableOptions = availableOptions;
        this.skippable = skippable;
    }

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
