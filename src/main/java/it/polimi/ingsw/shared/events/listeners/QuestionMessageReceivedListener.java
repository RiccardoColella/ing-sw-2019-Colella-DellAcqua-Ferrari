package it.polimi.ingsw.shared.events.listeners;

import it.polimi.ingsw.shared.events.MessageReceived;

import java.util.EventListener;

public interface QuestionMessageReceivedListener extends EventListener {
    void onQuestionMessageReceived(MessageReceived e);
}
