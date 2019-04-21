package it.polimi.ingsw.server.view;

import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.player.BasicAction;
import it.polimi.ingsw.shared.Direction;
import it.polimi.ingsw.shared.commands.ClientApi;

import java.util.*;

public interface Interviewer {

    <T> T select(String questionText, Collection<T> options, ClientApi commandName);
    <T> Optional<T> selectOptional(String questionText, Collection<T> options, ClientApi commandName);

    @Deprecated
    <T> T select(Collection<T> options);
    @Deprecated
    <T> Optional<T> selectOptional(Collection<T> options);
}
