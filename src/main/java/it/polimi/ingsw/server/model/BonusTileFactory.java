package it.polimi.ingsw.server.model;

public final class BonusTileFactory {

    /**
     * Empty private constructor
     */
    private BonusTileFactory() {

    }

    public static BonusTile create(CoinColor firstColor, CoinColor secondColor) {
        return new BonusTile(AmmoFactory.create(firstColor), AmmoFactory.create(secondColor));
    }

    public static BonusTile create(CoinColor firstColor, CoinColor secondColor, CoinColor thirdColor) {
        return new BonusTile(AmmoFactory.create(firstColor), AmmoFactory.create(secondColor), AmmoFactory.create(thirdColor));
    }
}
