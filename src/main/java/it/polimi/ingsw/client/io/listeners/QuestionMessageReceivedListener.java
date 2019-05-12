package it.polimi.ingsw.client.io.listeners;

import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.player.BasicAction;
import it.polimi.ingsw.shared.Direction;
import it.polimi.ingsw.shared.events.MessageReceived;
import it.polimi.ingsw.shared.messages.templates.Question;
import it.polimi.ingsw.utils.Tuple;

import java.awt.*;
import java.util.EventListener;
import java.util.function.Consumer;

/**
 * Event listener for question messages
 *
 * @author Carlo Dell'Acqua
 */
public interface QuestionMessageReceivedListener extends EventListener {

    void onDirectionQuestion(Question<Direction> question, Consumer<Direction> answerCallback);

    void onAttackQuestion(Question<String> question, Consumer<String> answerCallback);

    void onBasicActionQuestion(Question<BasicAction> question, Consumer<BasicAction> answerCallback);

    void onBlockQuestion(Question<Point> question, Consumer<Point> answerCallback);

    void onPaymentMethodQuestion(Question<String> question, Consumer<String> answerCallback);

    void onPowerupQuestion(Question<Tuple<String, CurrencyColor>> question, Consumer<Tuple<String, CurrencyColor>> answerCallback);

    void onWeaponQuestion(Question<String> question, Consumer<String> answerCallback);

    void onReloadQuestion(Question<String> question, Consumer<String> answerCallback);

    void onSpawnpointQuestion(Question<Tuple<String, CurrencyColor>> question, Consumer<Tuple<String, CurrencyColor>> answerCallback);

    void onTargetQuestion(Question<String> question, Consumer<String> answerCallback);
}
