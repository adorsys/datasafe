package de.adorsys.datasafe.runtimedelegate;

import com.google.auto.service.AutoService;
import de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static de.adorsys.datasafe.runtimedelegate.RuntimeDelegateProcessor.ANNOTATION_CLASS;

/**
 * This is an annotation processor that scans classes annotated with
 * {@link de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate} and creates
 * delegation class that uses {@link OverridesRegistry} for runtime overriding capability. Delegation class
 * checks if there exist an override in {@link OverridesRegistry} and if it does - calls that override, if
 * there is nothing associated with annotated class in {@link OverridesRegistry} - it calls default implementation.
 */
@SupportedAnnotationTypes(ANNOTATION_CLASS)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class RuntimeDelegateProcessor extends AbstractProcessor {

    static final String ANNOTATION_CLASS = "de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate";

    /**
     * Reads annotated elements from compile-time classpath
     * @param annotations Annotation to scan for
     * @param roundEnv Processing environment
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        RuntimeDelegateGenerator generator = new RuntimeDelegateGenerator();

        for (TypeElement annotation : annotations) {
            // limit to elements annotated with {@link RuntimeDelegate}
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            for (Element annotated : annotatedElements) {
                if (annotated.getKind() != ElementKind.CLASS) {
                    processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "Only classes should be annotated with @" + ANNOTATION_CLASS,
                            annotated
                    );
                    return false;
                }

                TypeElement clazz = (TypeElement) annotated;

                if (clazz.getModifiers().contains(Modifier.FINAL)) {
                    processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "Class should not be final",
                            clazz
                    );
                    return false;
                }

                // {@link RuntimeDelegate} must contain Inject-annotated constructor - we will delegate to it
                ExecutableElement ctor = findAnnotatedConstructor(clazz, Inject.class);
                if (null == ctor) {
                    continue;
                }

                // generating RuntimeDelegatable class for annotated one
                generator.generate(
                        clazz,
                        OverridesRegistry.class,
                        ctor,
                        Collections.singleton(Inject.class),
                        super.processingEnv.getFiler()
                );
            }
        }

        return false;
    }

    /**
     * Gets single Inject annotated constructor, fails if there is no such constructor or more than one.
     */
    private <A extends Annotation> ExecutableElement findAnnotatedConstructor(TypeElement element,
                                                                              Class<A> annotation) {
        Set<ExecutableElement> annotated = new HashSet<>();

        for (Element inner : element.getEnclosedElements()) {
            if (inner.getKind() != ElementKind.CONSTRUCTOR) {
                continue;
            }

            A onCtor = inner.getAnnotation(annotation);

            if (null != onCtor) {
                annotated.add((ExecutableElement) inner);
            }
        }

        if (annotated.size() != 1) {
            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                "Class should have exactly one @Inject annotation",
                    element
            );

            return null;
        }

        return annotated.iterator().next();
    }
}
