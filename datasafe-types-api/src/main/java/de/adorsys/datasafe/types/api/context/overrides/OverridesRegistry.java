package de.adorsys.datasafe.types.api.context.overrides;

import java.util.function.Function;

/**
 * This is runtime functionality override registry (similar to Spring context). Its purpose is
 * to provide capability to override Datasafe-classes without recompilation of Datasafe module,
 * so purely importing Datasafe as a jar. However, one should understand
 * that dependency graph is static, managed by Dagger library, so while it is possible to override class functionality,
 * it is not possible to add extra dependencies.
 */
public interface OverridesRegistry {

    /**
     * Overrides some class (implementation, concrete class) with other one that extends it.
     * @param classToOverride Target class that will be overridden (so it won't be called)
     * @param overrideWith Overriding class supplier - supplies implementation based on ArgumentCaptor in Delegate.
     * @param <T> Generic parameter
     */
    <T> void override(Class<T> classToOverride, Function<Object, T> overrideWith);

    /**
     * Gets overriding class based on arguments passed to original class constructor.
     * @param forClass Original class
     * @param arguments Arguments used to build original class
     * @param <T> Generic parameter
     * @return Overriding class, or null if no override is present
     */
    <T> T findOverride(Class<T> forClass, Object arguments);
}
