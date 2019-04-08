package it.polimi.ingsw.server.model.currency;

import it.polimi.ingsw.server.model.collections.Deck;

import java.util.LinkedList;
import java.util.List;

public final class BonusTileFactory {

    /**
     * Empty private constructor
     */
    private BonusTileFactory() {

    }

    public static BonusTile create(CurrencyColor firstColor, CurrencyColor secondColor) {
        return new BonusTile(AmmoCubeFactory.create(firstColor), AmmoCubeFactory.create(secondColor));
    }

    public static BonusTile create(CurrencyColor firstColor, CurrencyColor secondColor, CurrencyColor thirdColor) {
        return new BonusTile(AmmoCubeFactory.create(firstColor), AmmoCubeFactory.create(secondColor), AmmoCubeFactory.create(thirdColor));
    }

    public static Deck<BonusTile> createDeck() {
        List<BonusTile> bonusTiles = new LinkedList<>();
        // 36 bonus card, 18 with 3 ammoCubes, 18 2 ammoCubes + powerup
        // 2 ammoCubes + powerup: 2 with 2 ammoCubes of the same color for each color (= 6 cards), 4 for every combination (= 12 cards) RY RB BY
        // 3 ammoCubes: 3 for each combo of 2 ammoCubes of the same color + 1 different color (YBB, YRR, BYY, BRR, RYY, RBB) (= 18 cards)
        for (CurrencyColor mainColor : CurrencyColor.values()) {
            for (CurrencyColor secondColor : CurrencyColor.values()) {
                if (mainColor != secondColor) {
                    for (int i = 0; i < 2; i++) {
                        bonusTiles.add(BonusTileFactory.create(mainColor, mainColor, secondColor));
                        bonusTiles.add(BonusTileFactory.create(mainColor, secondColor));
                    }
                    bonusTiles.add(BonusTileFactory.create(mainColor, mainColor, secondColor));
                }
            }
            for (int i = 0; i < 2; i++) {
                bonusTiles.add(BonusTileFactory.create(mainColor, mainColor));
            }
        }

        return new Deck<>(bonusTiles, true);
    }
}
