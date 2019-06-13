package de.adorsys.datasafe.types.api.context;

import de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Basic implementation for class overriding context - using HashMap.
 */
public class BaseOverridesRegistry implements OverridesRegistry {

    private final Map<Class, Function<Object, ?>> overrides = new HashMap<>();

    @Override
    public <T> void override(Class<T> classToOverride, Function<Object, T> overrideWith) {
        overrides.put(classToOverride, overrideWith);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T findOverride(Class<T> forClass, Object arguments) {
        Function<Object, ?> overrideWith = overrides.get(forClass);
        if (null == overrideWith) {
            return null;
        }

        return (T) overrideWith.apply(arguments);
    }
}
