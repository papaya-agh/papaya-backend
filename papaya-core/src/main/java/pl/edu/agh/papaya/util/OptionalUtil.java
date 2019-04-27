package pl.edu.agh.papaya.util;

import java.util.Collection;
import java.util.Optional;

public final class OptionalUtil {

    private OptionalUtil() {}

    public static <T> Optional<T> fromCollection(Collection<T> collection) {
        return collection.stream().findFirst();
    }
}
