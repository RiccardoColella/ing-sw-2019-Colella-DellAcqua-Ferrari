package it.polimi.ingsw.utils;

/**
 * Helper class that provide a useful method for retrieving enum values by their string representations
 *
 * @author Carlo Dell'Acqua
 */
public final class EnumValueByString {

    private EnumValueByString() { }

    /**
     * Given a string and the class type of an enum, this method returns the corresponding enum value
     *
     * @param s the string representation of the enum value
     * @param enumType the enum class type
     * @param <T> the type of the items inside the enum
     * @return the enum value corresponding to the given string representation
     * @throws IllegalArgumentException if no matching value could be found
     */
    public static <T extends Enum<T>> T findByString(String s, Class<T> enumType) {
        for (T enumValue: enumType.getEnumConstants()) {
            if (s.equals(enumValue.toString())) {
                return enumValue;
            }
        }

        throw new IllegalArgumentException(s + " is not a valid element of " + enumType.toString());
    }
}
