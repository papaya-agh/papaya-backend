package pl.edu.agh.papaya.util;

public final class DoubleUtil {

    private DoubleUtil() {
    }

    public static double saturated(double value, double min, double max) {
        if (Double.isNaN(value)) {
            // just anything that doesn't break
            return min;
        } else if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        }

        return value;
    }
}
