package it.polimi.ingsw.shared.commands.answers;

import it.polimi.ingsw.shared.commands.Command;

import java.util.Collection;

public class SelectOptionAnswer<T> implements Command {

    private T option;

    public SelectOptionAnswer(T option) {
        this.option = option;
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <TCast> TCast getPayload() {
        return (TCast) option;
    }
}
