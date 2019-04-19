package it.polimi.ingsw.shared.commands.answers;

import it.polimi.ingsw.shared.commands.Command;

import java.util.Collection;

public class SelectMultipleAnswer<TItem> implements Command {
    private Collection<TItem> options;

    public SelectMultipleAnswer(Collection<TItem> options) {
        this.options = options;
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <TCast> TCast getPayload() {
        return (TCast) options;
    }
}
