package it.polimi.ingsw.client.io.listeners;

import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.player.BasicAction;
import it.polimi.ingsw.shared.Direction;
import it.polimi.ingsw.shared.datatransferobjects.Powerup;
import it.polimi.ingsw.shared.messages.templates.Question;

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

    /**
     * This function manages the response of the client to a question asking for the selection of a direction
     *
     * @param question the question that is being asked
     * @param answerCallback the consumer that will be called once the answer is found
     */
    void onDirectionQuestion(Question<Direction> question, Consumer<Direction> answerCallback);

    /**
     * This function manages the response of the client to a question asking for the selection of an attack
     *
     * @param question the question that is being asked
     * @param answerCallback the consumer that will be called once the answer is found
     */
    void onAttackQuestion(Question<String> question, Consumer<String> answerCallback);

    /**
     * This function manages the response of the client to a question asking for the selection of a basic action
     *
     * @param question the question that is being asked
     * @param answerCallback the consumer that will be called once the answer is found
     */
    void onBasicActionQuestion(Question<BasicAction> question, Consumer<BasicAction> answerCallback);

    /**
     * This function manages the response of the client to a question asking for the selection of a block
     *
     * @param question the question that is being asked
     * @param answerCallback the consumer that will be called once the answer is found
     */
    void onBlockQuestion(Question<Point> question, Consumer<Point> answerCallback);

    /**
     * This function manages the response of the client to a question asking for the selection of a payment method
     *
     * @param question the question that is being asked
     * @param answerCallback the consumer that will be called once the answer is found
     */
    void onPaymentMethodQuestion(Question<String> question, Consumer<String> answerCallback);

    /**
     * This function manages the response of the client to a question asking for the selection of a powerup
     *
     * @param question the question that is being asked
     * @param answerCallback the consumer that will be called once the answer is found
     */
    void onPowerupQuestion(Question<Powerup> question, Consumer<Powerup> answerCallback);

    /**
     * This function manages the response of the client to a question asking for the selection of a weapon
     *
     * @param question the question that is being asked
     * @param answerCallback the consumer that will be called once the answer is found
     */
    void onWeaponQuestion(Question<String> question, Consumer<String> answerCallback);

    /**
     * This function manages the response of the client to a question asking for the selection of a weapon to reload
     *
     * @param question the question that is being asked
     * @param answerCallback the consumer that will be called once the answer is found
     */
    void onReloadQuestion(Question<String> question, Consumer<String> answerCallback);

    /**
     * This function manages the response of the client to a question asking for the selection of a spawnpoint
     *
     * @param question the question that is being asked
     * @param answerCallback the consumer that will be called once the answer is found
     */
    void onSpawnpointQuestion(Question<Powerup> question, Consumer<Powerup> answerCallback);

    /**
     * This function manages the response of the client to a question asking for the selection of a target
     *
     * @param question the question that is being asked
     * @param answerCallback the consumer that will be called once the answer is found
     */
    void onTargetQuestion(Question<String> question, Consumer<String> answerCallback);

    /**
     * This function manages the response of the client to a question asking for the selection of a group of targets
     *
     * @param question the question that is being asked
     * @param answerCallback the consumer that will be called once the answer is found
     */
    void onTargetSetQuestion(Question<Set<String>> question, Consumer<Set<String>> answerCallback);

    /**
     * This function manages the response of the client to a question asking for the selection of the color of the payment
     *
     * @param question the question that is being asked
     * @param answerCallback the consumer that will be called once the answer is found
     */
    void onPaymentColorQuestion(Question<CurrencyColor> question, Consumer<CurrencyColor> answerCallback);
}
