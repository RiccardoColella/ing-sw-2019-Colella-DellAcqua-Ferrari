package it.polimi.ingsw.server.controller;

import it.polimi.ingsw.server.model.currency.AmmoCube;
import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.view.Interviewer;
import it.polimi.ingsw.shared.messages.ClientApi;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class handle all payments
 *
 * @author Riccardo Colella, Carlo Dell'Acqua
 */
public class PaymentHandler {

    /**
     * Private constructor. This class has no values and doesn't need to be constructed
     */
    private PaymentHandler(){
        throw new IllegalStateException("Class doesn't need to be constructed");
    }

    /**
     * This methods returns true if the player has enough coin to afford the bill.
     * @param debt Is the due bill
     * @param owner is the player who has to pay
     * @return true if the player can afford the debt
     */
    public static boolean canAfford(List<? extends Coin> debt, Player owner){
        List<Coin> activePlayerWallet = owner.getAmmoCubes().stream().map(ammoCube -> (Coin) ammoCube).collect(Collectors.toList());
        activePlayerWallet.addAll(owner.getPowerups().stream().map(powerupTile -> (Coin) powerupTile).collect(Collectors.toList()));
        for (Coin coin : debt) {
            if (activePlayerWallet.stream().anyMatch(playerCoin -> playerCoin.hasSameValueAs(coin))) {
                activePlayerWallet.remove(coin);
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * This methods let the player to choose what payment method to use
     *
     * @param debt is the amount of coin the player needs to pay
     * @param owner is the player who has to pay. He MUST have sufficient coins to cover his debt
     * @param payer is the interface who will be asked to choose how to manage owner's debt
     * @return the selected Coin chosen to pay the debt
     */
    public static List<Coin> collectCoins(List<? extends Coin> debt, Player owner, Interviewer payer){
        List<Coin> coins = new LinkedList<>();
        List<Coin> availableCoins = new LinkedList<>();
        availableCoins.addAll(owner.getAmmoCubes());
        availableCoins.addAll(owner.getPowerups());
        for (Coin coinDue : debt){
            List<Coin> rightColourCoins = new LinkedList<>();
            availableCoins.stream()
                    .filter(ammoCube -> ammoCube.hasSameValueAs(coinDue))
                    .forEach(rightColourCoins::add);

            Coin choose = payer.select("How do you want to pay this " + coinDue.getColor().toString() + " debt?",
                    rightColourCoins, ClientApi.PAYMENT_QUESTION);
            availableCoins.remove(choose);
            coins.add(choose);
        }
        return coins;
    }



    /**
     * This methods let the player to choose what payment method to use
     * @param debt is the amount of coin the player needs to pay
     * @param owner is the player who has to pay
     * @param payer is the interface who will be asked to choose how to manage owner's debt
     */
    public static void pay(List<? extends Coin> debt, Player owner, Interviewer payer) {
        List<Coin> coins = collectCoins(debt, owner, payer);
        owner.pay(coins);
    }


}
