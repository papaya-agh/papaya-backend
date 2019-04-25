package pl.edu.agh.papaya.mappers;

import java.util.List;
import java.util.stream.Collectors;

public interface Mapper<M, A> {

    default List<A> mapToApi(List<M> modelItems) {
        return modelItems.stream()
                .map(this::mapToApi)
                .collect(Collectors.toList());
    }

    A mapToApi(M item);
}
