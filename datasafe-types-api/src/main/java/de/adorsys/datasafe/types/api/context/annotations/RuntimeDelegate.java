package de.adorsys.datasafe.types.api.context.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation enables the capability of runtime-delegation using
 * {@link de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry} as context.
 * It generates delegating class that wraps original class and provides extension point to change functionality
 * provided by original class functionality.
 * If override is present in {@link de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry} then
 * method from registry will be called, otherwise original method is called.
 * Code generation is done by RuntimeDelegateProcessor.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RuntimeDelegate {
}
