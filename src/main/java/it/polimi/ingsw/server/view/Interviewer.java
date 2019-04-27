package it.polimi.ingsw.server.view;

import it.polimi.ingsw.shared.messages.ClientApi;

import java.util.Collection;
import java.util.Optional;

/**
 * Core of the IO of the client-server message-based architecture.
 * This interface is an high-level API use by the server to communicate to the client, communicating questions
 * and receiving answers
 */
public interface Interviewer {

    /**
     * Ask the client to select one of the options proposed
     *
     * @param questionText the question to show to the user
     * @param options a collection of options to choose from
     * @param messageName the name which identifies the type of message that is been sent
     * @param <T> the type of items in the collection of options
     * @return the selected option
     */
    <T> T select(String questionText, Collection<T> options, ClientApi messageName);

    /**
     * Ask the client to select one or none of the options proposed
     *
     * @param questionText the question to show to the user
     * @param options a collection of options to choose from
     * @param messageName the name which identifies the type of message that is been sent
     * @param <T> the type of items in the collection of options
     * @return the selected option
     */
    <T> Optional<T> selectOptional(String questionText, Collection<T> options, ClientApi messageName);
}
