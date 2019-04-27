package pl.edu.agh.papaya.util;

public final class AssertionUtil {

    private AssertionUtil() {}

    public static <T> T require(String fieldName, T value) {
        if (value == null) {
            throw new IllegalStateException(fieldName + " is not initialized");
        }
        return value;
    }
}
