package it.polimi.ingsw.shared.events.listeners;

import it.polimi.ingsw.shared.events.MessageReceived;

import java.util.EventListener;

/**
 * Event listener for question messages
 *
 * @author Carlo Dell'Acqua
 */
public interface QuestionMessageReceivedListener extends EventListener {
    void onQuestionMessageReceived(MessageReceived e);
}
