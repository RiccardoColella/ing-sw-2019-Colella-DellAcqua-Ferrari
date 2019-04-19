package it.polimi.ingsw.shared.commands.answers;

import it.polimi.ingsw.shared.commands.Command;

import java.util.Optional;

public class SelectOptionOrSkipAnswer<T> implements Command {

    private T option;

    public SelectOptionOrSkipAnswer(Optional<T> option) {
        this.option = option.orElse(null);
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <TCast> TCast getPayload() {
        return (TCast) Optional.ofNullable(option);
    }
}
