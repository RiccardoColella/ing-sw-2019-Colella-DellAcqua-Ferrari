package it.polimi.ingsw.server.model.factories;

import it.polimi.ingsw.server.model.currency.BonusTile;
import it.polimi.ingsw.server.model.currency.CurrencyColor;

public final class BonusTileFactory {

    /**
     * Empty private constructor
     */
    private BonusTileFactory() {

    }

    public static BonusTile create(CurrencyColor firstColor, CurrencyColor secondColor) {
        return new BonusTile(AmmoFactory.create(firstColor), AmmoFactory.create(secondColor));
    }

    public static BonusTile create(CurrencyColor firstColor, CurrencyColor secondColor, CurrencyColor thirdColor) {
        return new BonusTile(AmmoFactory.create(firstColor), AmmoFactory.create(secondColor), AmmoFactory.create(thirdColor));
    }
}
