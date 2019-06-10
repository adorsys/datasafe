package de.adorsys.datasafe.types.api.context;

import de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Basic implementation for class overriding - using HashMap.
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
        return (T) overrides.get(forClass);
    }
}
