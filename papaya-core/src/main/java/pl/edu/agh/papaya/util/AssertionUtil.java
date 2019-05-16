package pl.edu.agh.papaya.util;

public final class AssertionUtil {

    private AssertionUtil() {
    }

    public static <T> T require(String fieldName, T value) {
        if (value == null) {
            throw new IllegalStateException(fieldName + " is not initialized");
        }
        return value;
    }

    public static Long requirePositive(String name, Long value) {
        return requireGreaterOrEqual(name, value, 1);
    }

    public static Long requireGreaterOrEqual(String name, Long value, long minimum) {
        if (value == null) {
            throw new IllegalStateException(name + " must not be null");
        } else if (value < minimum) {
            throw new IllegalStateException(name + " must be positive");
        }

        return value;
    }

    public static Long requireNonNegative(String name, Long value) {
        return requireGreaterOrEqual(name, value, 0);
    }
}
