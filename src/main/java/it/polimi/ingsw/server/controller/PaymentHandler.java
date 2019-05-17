package it.polimi.ingsw.server.controller;

import it.polimi.ingsw.server.model.currency.*;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.view.Interviewer;
import it.polimi.ingsw.server.view.exceptions.ViewDisconnectedException;
import it.polimi.ingsw.shared.messages.ClientApi;
import it.polimi.ingsw.shared.viewmodels.Powerup;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * This class handle all payments
 *
 * @author Riccardo Colella, Carlo Dell'Acqua
 */
public class PaymentHandler {

    private static final String PAYMENT_METHOD_POWERUPS = "Powerups";
    private static final String PAYMENT_METHOD_AMMO_CUBES = "AmmoCubes";

    /**
     * Private constructor. This class has no values and doesn't need to be constructed
     */
    private PaymentHandler(){
        throw new IllegalStateException("Class doesn't need to be constructed");
    }

    /**
     * This methods returns true if the player has enough coin to afford the bill.
     * @param debt Is the due coin
     * @param owner is the player who has to pay
     * @return true if the player can afford the debt
     */
    public static boolean canAfford(List<? extends Coin> debt, Player owner){
        List<Coin> activePlayerWallet = new LinkedList<>(owner.getAmmoCubes());
        activePlayerWallet.addAll(owner.getPowerups());
        for (Coin coin : debt) {
            if (activePlayerWallet.stream().anyMatch(coin::hasSameValueAs)) {
                activePlayerWallet.remove(activePlayerWallet
                        .stream()
                        .filter(c -> c.hasSameValueAs(coin))
                        .findAny().orElseThrow(() -> new IllegalStateException("if-Control failed")));
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * This methods returns true if the player has enough coin to afford the bill.
     * @param debt is the amount of due coin of non specified colour
     * @param owner is the player who has to pay
     * @return true if the player can afford the debt
     */
    public static boolean canAfford(int debt, Player owner){
        List<Coin> activePlayerWallet = owner.getAmmoCubes().stream().map(ammoCube -> (Coin) ammoCube).collect(Collectors.toList());
        activePlayerWallet.addAll(owner.getPowerups().stream().map(powerupTile -> (Coin) powerupTile).collect(Collectors.toList()));
        return activePlayerWallet.size() >= debt;
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
        return collectCoins(debt, owner, payer, false);
    }

    /**
     * This methods let the player to choose what payment method to use
     *
     * @param debt is the amount of coin the player needs to pay
     * @param owner is the player who has to pay. He MUST have sufficient coins to cover his debt
     * @param payer is the interface who will be asked to choose how to manage owner's debt
     * @param ignoreColor determines whether or not the only criteria for the currency is the amount and not the color
     * @param restrictedFunds a coin that should not be considered available for payments
     * @return the selected Coin chosen to pay the debt
     */
    public static List<Coin> collectCoins(List<? extends Coin> debt, Player owner, Interviewer payer, boolean ignoreColor, @Nullable PowerupTile restrictedFunds) {

        List<PowerupTile> chosenPowerups = new LinkedList<>();
        List<AmmoCube> chosenAmmoCube = new LinkedList<>();

        for (Coin coinOwed : debt){

            List<PowerupTile> powerupTiles = owner.getPowerups().stream()
                    .filter(p -> (ignoreColor || p.hasSameValueAs(coinOwed)) && !chosenPowerups.contains(p) && p != restrictedFunds)
                    .collect(Collectors.toList());

            for (PowerupTile chosenPowerup : chosenPowerups) {
                powerupTiles.remove(chosenPowerup);
            }

            List<AmmoCube> ammoCubes = owner.getAmmoCubes().stream()
                    .filter(a -> (ignoreColor || a.hasSameValueAs(coinOwed)) && !chosenAmmoCube.contains(a))
                    .collect(Collectors.toList());

            for (AmmoCube chosenAmmo : chosenAmmoCube) {
                ammoCubes.remove(chosenAmmo);
            }

            List<String> paymentMethods = Arrays.asList(PAYMENT_METHOD_AMMO_CUBES, PAYMENT_METHOD_POWERUPS);
            String paymentMethod;
            CurrencyColor actualColor;
            if (ignoreColor) {
                List<CurrencyColor> ownedColors = Stream.concat(powerupTiles.stream().map(PowerupTile::getColor), ammoCubes.stream().map(AmmoCube::getColor)).collect(Collectors.toList());
                actualColor = payer.select("Which color do you want your debt to be?", new HashSet<>(ownedColors), ClientApi.PAYMENT_COLOR_QUESTION);
                if (powerupTiles.stream().noneMatch(p -> AmmoCubeFactory.create(actualColor).hasSameValueAs(p))) {
                    paymentMethod = PAYMENT_METHOD_AMMO_CUBES;
                } else if (ammoCubes.stream().noneMatch(a -> AmmoCubeFactory.create(actualColor).hasSameValueAs(a))) {
                    paymentMethod = PAYMENT_METHOD_POWERUPS;
                } else {
                    paymentMethod = payer.select("Which payment method would you like to use to pay your debt?", paymentMethods, ClientApi.PAYMENT_METHOD_QUESTION);
                }
            } else if (!powerupTiles.isEmpty() && !ammoCubes.isEmpty()) {
                actualColor = coinOwed.getColor();
                paymentMethod = payer.select("Which payment method would you like to use to pay your debt?", paymentMethods, ClientApi.PAYMENT_METHOD_QUESTION);
            } else if (!powerupTiles.isEmpty()) {
                actualColor = coinOwed.getColor();
                paymentMethod = PAYMENT_METHOD_POWERUPS;
            } else if (!ammoCubes.isEmpty()) {
                actualColor = coinOwed.getColor();
                paymentMethod = PAYMENT_METHOD_AMMO_CUBES;
            } else throw new ViewDisconnectedException("Invalid player wallet status, cannot pay the debt");

            switch (paymentMethod)  {
                case PAYMENT_METHOD_AMMO_CUBES:
                    ammoCubes = ammoCubes.stream().filter(a -> a.getColor().equals(actualColor)).collect(Collectors.toList());
                    chosenAmmoCube.add(ammoCubes.get(0));
                    break;
                case PAYMENT_METHOD_POWERUPS:
                    powerupTiles = powerupTiles.stream().filter(p -> p.getColor().equals(actualColor)).collect(Collectors.toList());
                    if (powerupTiles.size() > 1) {
                        List<Powerup> powerups = powerupTiles.stream().map(p -> new Powerup(p.getName(), p.getColor())).collect(Collectors.toList());
                        Powerup powerup = payer.select("Which powerup would you like to discard to pay your debt?", powerups, ClientApi.POWERUP_QUESTION);
                        PowerupTile choice = powerupTiles.stream()
                                .filter(p -> p.getName().equals(powerup.getName()) && p.getColor().equals(powerup.getColor()))
                                .findFirst()
                                .orElseThrow(() -> new ViewDisconnectedException("Invalid response: " + powerup));
                        chosenPowerups.add(choice);
                    } else {
                        chosenPowerups.add(powerupTiles.get(0));
                    }
                    break;
            }
        }

        return Stream.concat(
                chosenAmmoCube.stream(),
                chosenPowerups.stream()
        ).collect(Collectors.toList());
    }

    /**
     * This methods let the player to choose what payment method to use
     *
     * @param debt is the amount of coin the player needs to pay
     * @param owner is the player who has to pay. He MUST have sufficient coins to cover his debt
     * @param payer is the interface who will be asked to choose how to manage owner's debt
     * @param ignoreColor determines whether or not the only criteria for the currency is the amount and not the color
     * @return the selected Coin chosen to pay the debt
     */
    public static List<Coin> collectCoins(List<? extends Coin> debt, Player owner, Interviewer payer, boolean ignoreColor) {

        return collectCoins(debt, owner, payer, ignoreColor, null);
    }

    /**
     * This methods let the player to choose what payment method to use
     *
     * @param debt is the amount of coin of non specified colour the player needs to pay
     * @param owner is the player who has to pay. He MUST have sufficient coins to cover his debt
     * @param payer is the interface who will be asked to choose how to manage owner's debt
     * @param restrictedFunds a coin that should not be considered available for payments
     * @return the selected Coin chosen to pay the debt
     */
    public static List<Coin> collectCoins(int debt, Player owner, Interviewer payer, @Nullable PowerupTile restrictedFunds){
        return collectCoins(
                IntStream.range(0, debt)
                        .boxed()
                        .map(i -> new AmmoCube(CurrencyColor.BLUE))
                        .collect(Collectors.toList()),
                owner,
                payer,
                true,
                restrictedFunds
        );
    }

    /**
     * This methods let the player to choose what payment method to use
     *
     * @param debt is the amount of coin of non specified colour the player needs to pay
     * @param owner is the player who has to pay. He MUST have sufficient coins to cover his debt
     * @param payer is the interface who will be asked to choose how to manage owner's debt
     * @return the selected Coin chosen to pay the debt
     */
    public static List<Coin> collectCoins(int debt, Player owner, Interviewer payer){
        return collectCoins(
                debt,
                owner,
                payer,
                null
        );
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

    /**
     * This methods let the player to choose what payment method to use
     * @param debt is the amount of coin of non specified colour the player needs to pay
     * @param owner is the player who has to pay
     * @param payer is the interface who will be asked to choose how to manage owner's debt
     * @param restrictedFunds a coin that should not be considered available for payments
     */
    public static void pay(int debt, Player owner, Interviewer payer, @Nullable PowerupTile restrictedFunds) {
        List<Coin> coins = collectCoins(debt, owner, payer, restrictedFunds);
        owner.pay(coins);
    }


    /**
     * This methods let the player to choose what payment method to use
     * @param debt is the amount of coin of non specified colour the player needs to pay
     * @param owner is the player who has to pay
     * @param payer is the interface who will be asked to choose how to manage owner's debt
     */
    public static void pay(int debt, Player owner, Interviewer payer) {
        pay(debt, owner, payer, null);
    }


}