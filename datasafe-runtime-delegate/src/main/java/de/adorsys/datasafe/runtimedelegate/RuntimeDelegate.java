package de.adorsys.datasafe.runtimedelegate;

import com.google.auto.service.AutoService;
import de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry;

import javax.annotation.processing.*;
import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static de.adorsys.datasafe.runtimedelegate.RuntimeDelegate.ANNOTATION_CLASS;

@SupportedAnnotationTypes(ANNOTATION_CLASS)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class RuntimeDelegate extends AbstractProcessor {

    static final String ANNOTATION_CLASS = "de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        RuntimeDelegateGenerator generator = new RuntimeDelegateGenerator();

        for (TypeElement annotation : annotations) {
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

                ExecutableElement ctor = findAnnotatedConstructor(clazz, Inject.class);
                if (null == ctor) {
                    continue;
                }

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
