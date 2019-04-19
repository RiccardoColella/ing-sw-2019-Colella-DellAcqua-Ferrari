package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.currency.AmmoCube;
import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.weapons.Weapon;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class inherits from the basic weapon and adds an alternative attack that can be used instead of the basic one
 */
public class WeaponWithAlternative extends BasicWeapon {

    /**
     * This property represents the alternative attack of the weapon, which can be used in place of the basic attack
     */
    private final Attack alternativeAttack;

    /**
     * This constructor assignes all the final values to the weapon, making it ready to be bought
     *
     * @param name                  the name of the weapon
     * @param basicAttack           the basic attack of the weapon
     * @param alternativeAttack     the alternative attack of the weapon
     */
    public WeaponWithAlternative(Weapon.Name name, Attack basicAttack, Attack alternativeAttack) {
        super(name, basicAttack);
        this.alternativeAttack = alternativeAttack;
    }

    /**
     * This method gets the alternative attack of the weapon
     * @return the alternative attack of the weapon
     */
    public Attack getAlternativeAttack() {
        return this.alternativeAttack;
    }


    @Override
    protected boolean hasAttack(Attack attack) {
        return super.hasAttack(attack) || this.alternativeAttack.equals(attack);
    }

    @Override
    public void shoot(Communicator communicator, Player activePlayer) {
        List<Coin> activePlayerWallet = activePlayer.getAmmoCubes().stream().map(ammoCube -> (Coin) ammoCube).collect(Collectors.toCollection(LinkedList::new));
        activePlayerWallet.addAll(activePlayer.getPowerups().stream().map(powerupTile -> (Coin) powerupTile).collect(Collectors.toList()));
        Attack selectedAttack;
        if (activePlayerWallet.containsAll(alternativeAttack.getCost())) {
            //player can afford the alternative attack
            Map<String, List<String>> potentialAttacks = new HashMap<>();
            potentialAttacks.put(basicAttack.getName(), basicAttack.getCost().stream().map(Object::toString).collect(Collectors.toList()));
            potentialAttacks.put(alternativeAttack.getName(), alternativeAttack.getCost().stream().map(Object::toString).collect(Collectors.toList()));
            String selectedAttackName = communicator.select(potentialAttacks);
            if (selectedAttackName.equals(basicAttack.getName())) {
                selectedAttack = basicAttack;
            } else {
                selectedAttack = alternativeAttack;
            }
        } else {
            selectedAttack = basicAttack;
        }
        handlePayment(communicator, selectedAttack, activePlayer);
        selectedAttack.execute(communicator, this);
    }

}
