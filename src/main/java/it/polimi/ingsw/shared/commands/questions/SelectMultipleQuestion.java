package it.polimi.ingsw.shared.commands.questions;

import it.polimi.ingsw.shared.commands.Command;

import java.util.Collection;

public class SelectMultipleQuestion<TItem> implements Command {
    private Collection<TItem> options;

    public SelectMultipleQuestion(Collection<TItem> options) {
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
