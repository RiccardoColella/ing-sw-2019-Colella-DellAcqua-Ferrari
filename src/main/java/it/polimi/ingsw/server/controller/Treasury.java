package it.polimi.ingsw.server.controller;

import it.polimi.ingsw.server.model.currency.AmmoCube;
import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.view.Interviewer;
import it.polimi.ingsw.shared.messages.ClientApi;

import java.util.LinkedList;
import java.util.List;

/**
 * This class handle all payments
 */
public class Treasury {

    /**
     * Debt that needs to be payed.
     */
    private List<Coin> debt;

    /**
     * This variable indicates the player who own the debt.
     */
    private Player owner;

    /**
     * This variable indicates the interface who can handle the payment.
     */
    private Interviewer payer;

    public Treasury(List<AmmoCube> debt, Player owner, Interviewer payer){
        this.debt = new LinkedList<>(debt);
        this.owner = owner;
        this.payer = payer;
    }

    public boolean canAfford(){
        List<AmmoCube> ownerAmmoCubes = new LinkedList<>(owner.getAmmoCubes());
        List<PowerupTile> ownerPowerps = new LinkedList<>(owner.getPowerups());
        boolean canAfford = true;
        for (Coin coinDue : debt){
            int ammo = (int) ownerAmmoCubes.stream().filter(ammoCube -> ammoCube.hasSameValueAs(coinDue)).count();
            int powerups = (int) ownerPowerps.stream().filter(powerup -> powerup.hasSameValueAs(coinDue)).count();
            int totCoinDue = (int) debt.stream().filter(coin -> coin.hasSameValueAs(coinDue)).count();
            if (totCoinDue < ammo + powerups){
                canAfford = false;
            }
        }
        return canAfford;
    }

    public List<Coin> selectPaymentMethod(){
        List<Coin> paymentMeethod = new LinkedList<>();
        if (canAfford()){
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
                paymentMeethod.add(choose);
            }
        } else throw new IllegalStateException("Cannot afford this payment!");
        return paymentMeethod;
    }

    public void pay(){
        if (canAfford()){
            List<Coin> paymentMeethod = new LinkedList<>();
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
                paymentMeethod.add(choose);
            }
            owner.pay(paymentMeethod);
            debt.clear();
        } else throw new IllegalStateException("Cannot afford this payment!");
    }
}
