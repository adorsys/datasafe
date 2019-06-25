package de.adorsys.datasafe.types.api.resource;

import de.adorsys.datasafe.types.api.callback.Callback;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps some class with its callbacks.
 * @param <T> Class to wrap, it will contain it.
 * @param <C> Callbacks associate with wrapped class.
 */
@Getter
@Builder(toBuilder = true)
public class WithCallback<T, C extends Callback> {

    private final T wrapped;

    @Singular
    private final List<C> callbacks;

    public static <T, C extends Callback> WithCallback<T, C> noCallback(T wrapped) {
        return new WithCallback<>(wrapped, new ArrayList<>());
    }
}
