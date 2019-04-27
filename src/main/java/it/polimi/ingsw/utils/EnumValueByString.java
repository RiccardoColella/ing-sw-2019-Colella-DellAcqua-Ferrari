package it.polimi.ingsw.utils;

public final class EnumValueByString {

    private EnumValueByString() { }

    public static <T extends Enum<T>> T findByString(String s, Class<T> enumType) {
        for (T enumValue: enumType.getEnumConstants()) {
            if (s.equals(enumValue.toString())) {
                return enumValue;
            }
        }

        throw new IllegalArgumentException(s + " is not a valid element of " + enumType.toString());
    }
}
