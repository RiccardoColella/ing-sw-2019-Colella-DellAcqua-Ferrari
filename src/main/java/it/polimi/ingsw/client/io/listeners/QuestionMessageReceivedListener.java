package it.polimi.ingsw.client.io.listeners;

import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.player.BasicAction;
import it.polimi.ingsw.shared.Direction;
import it.polimi.ingsw.shared.messages.templates.Question;
import it.polimi.ingsw.shared.datatransferobjects.Powerup;

import java.awt.*;
import java.util.EventListener;
import java.util.Set;
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

    void onPowerupQuestion(Question<Powerup> question, Consumer<Powerup> answerCallback);

    void onWeaponQuestion(Question<String> question, Consumer<String> answerCallback);

    void onReloadQuestion(Question<String> question, Consumer<String> answerCallback);

    void onSpawnpointQuestion(Question<Powerup> question, Consumer<Powerup> answerCallback);

    void onTargetQuestion(Question<String> question, Consumer<String> answerCallback);

    void onTargetSetQuestion(Question<Set<String>> question, Consumer<Set<String>> answerCallback);

    void onPaymentColorQuestion(Question<CurrencyColor> question, Consumer<CurrencyColor> answerCallback);
}
