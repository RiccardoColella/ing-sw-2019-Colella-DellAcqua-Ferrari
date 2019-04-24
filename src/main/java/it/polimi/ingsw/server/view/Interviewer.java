package it.polimi.ingsw.server.view;

import it.polimi.ingsw.shared.messages.ClientApi;

import java.util.*;

public interface Interviewer {

    <T> T select(String questionText, Collection<T> options, ClientApi messageName);
    <T> Optional<T> selectOptional(String questionText, Collection<T> options, ClientApi messageName);
}
