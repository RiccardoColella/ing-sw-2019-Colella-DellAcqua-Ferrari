package it.polimi.ingsw.server.model.rewards;

public interface Reward {
    enum Type {
        STANDARD,
        FINAL_FRENZY,
        KILLSHOT,
        DOUBLE_KILL;

        public static Type findByString(String s) {
            for (Type type: Type.values()) {
                if (s.equals(type.toString())) {
                    return type;
                }
            }

            throw new IllegalArgumentException();
        }
    }

    int getRewardFor(int index, boolean ...isFirst);
}
