package de.adorsys.datasafe.runtimedelegate;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import javax.annotation.Generated;
import javax.annotation.Nullable;
import javax.annotation.processing.Filer;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.IOError;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

class RuntimeDelegateGenerator {

    private static final String CLASS_PURPOSE_COMMENT = "This class performs functionality delegation based on "
            + "contextClass content. If contextClass contains overriding class - it will be used.";
    private static final String OVERRIDE_WITH_PURPOSE_COMMENT = "This is a typesafe function to register "
            + "overriding class into context.\n";

    private static final String CAPTOR_TYPE_NAME = "ArgumentsCaptor";
    private static final String CAPTOR_NAME = "argumentsCaptor";
    private static final String DELEGATE_NAME = "delegate";

    /**
     * Generate java file that contains delegate
     * @param forClass Create delegate for this class (will extend it)
     * @param contextClass Contains overrides in form of map class-supplier
     * @param usingConstructor Which constructor should be used to create delegating class and {@code forClass}
     * @param addAnnotations Add following annotations on delegating class constructor
     * @param filer Output place for java file
     */
    void generate(TypeElement forClass,
                  Class contextClass,
                  ExecutableElement usingConstructor,
                  Set<Class> addAnnotations,
                  Filer filer) {

        TypeSpec.Builder delegator = buildDelegatingClass(forClass);
        annotateAsGenerated(delegator);
        // extend from delegate-origin class
        delegator.superclass(TypeName.get(forClass.asType()));
        TypeSpec argCaptor = addGenericParametersToClassSignature(forClass, usingConstructor, delegator);
        delegator.addType(argCaptor);
        delegator.addField(addDelegateField(forClass));
        addSuperClassOverrides(delegator, forClass);
        delegator.addMethod(constructor(forClass, contextClass, usingConstructor, addAnnotations));
        delegator.addMethod(overrideWith(forClass, contextClass, argCaptor));

        JavaFile javaFile = JavaFile
            .builder(ClassName.get(forClass).packageName(), delegator.build())
            .indent("    ")
            .build();

        try {
            javaFile.writeTo(filer);
        } catch (IOException ex) {
            throw new IOError(ex);
        }
    }

    private TypeSpec addGenericParametersToClassSignature(TypeElement forClass, ExecutableElement usingConstructor,
                                                          TypeSpec.Builder delegator) {
        List<TypeVariableName> typeVariableNames = forClass.getTypeParameters().stream()
                .map(TypeVariableName::get)
                .collect(Collectors.toList());

        delegator.addTypeVariables(typeVariableNames);
        return addArgsCaptor(usingConstructor, typeVariableNames);
    }

    private FieldSpec addDelegateField(TypeElement forClass) {
        return FieldSpec
                    .builder(TypeName.get(forClass.asType()), DELEGATE_NAME, Modifier.PRIVATE, Modifier.FINAL)
                    .build();
    }

    private void annotateAsGenerated(TypeSpec.Builder delegator) {
        delegator.addAnnotation(AnnotationSpec
                .builder(Generated.class)
                .addMember("value", CodeBlock.of("$S", RuntimeDelegateGenerator.class.getCanonicalName()))
                .addMember("comments", CodeBlock.of("$S", CLASS_PURPOSE_COMMENT))
                .build()
        );
    }

    private TypeSpec.Builder buildDelegatingClass(TypeElement forClass) {
        return TypeSpec
                .classBuilder(forClass.getSimpleName().toString() + "RuntimeDelegatable")
                .addModifiers(Modifier.PUBLIC);
    }

    // perform actual delegation
    private void addSuperClassOverrides(TypeSpec.Builder toClass, TypeElement baseClass) {
        baseClass.getEnclosedElements().stream()
            // limiting to overridable-only methods:
            .filter(it -> it instanceof ExecutableElement)
            .filter(it -> it.getKind() == ElementKind.METHOD)
            .filter(it -> !it.getModifiers().contains(Modifier.PRIVATE))
            .forEach(it -> {
                MethodSpec overriddenBase = MethodSpec.overriding((ExecutableElement) it).build();
                MethodSpec.Builder overridden = MethodSpec.overriding((ExecutableElement) it);
                overridden.addCode(
                    delegateToIfOverrideIsPresent(overriddenBase)
                ).build();

                toClass.addMethod(overridden.build());
            });
    }

    private CodeBlock delegateToIfOverrideIsPresent(MethodSpec target) {
        String params = target.parameters.stream().map(it -> it.name).collect(Collectors.joining(", "));
        String returnStatementIfNeeded = TypeName.VOID.equals(target.returnType) ? "" : "return ";

        return CodeBlock.builder()
                .beginControlFlow("if (null == " + DELEGATE_NAME + ")")
                .addStatement(returnStatementIfNeeded + "super.$N(" + params + ")", target)
                .nextControlFlow("else")
                .addStatement(returnStatementIfNeeded + DELEGATE_NAME + ".$N(" + params + ")", target)
                .endControlFlow()
            .build();
    }

    // Create argument captor that stores original class constructor arguments
    private TypeSpec addArgsCaptor(ExecutableElement usingConstructor, List<TypeVariableName> typeVariables) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(CAPTOR_TYPE_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addTypeVariables(typeVariables);

        MethodSpec.Builder ctor = MethodSpec
            .constructorBuilder()
            .addModifiers(Modifier.PRIVATE);

        usingConstructor.getParameters().forEach(it -> {
            FieldSpec argCaptor = FieldSpec.builder(
                TypeName.get(it.asType()),
                firstCharToLowerCase(it.getSimpleName().toString()),
                Modifier.PRIVATE,
                Modifier.FINAL
            ).build();

            ctor.addParameter(argCaptor.type, argCaptor.name);
            ctor.addStatement("this.$N = $N", argCaptor.name, argCaptor.name);

            builder.addField(argCaptor);
            builder.addMethod(MethodSpec.methodBuilder("get" + firstCharToUpperCase(argCaptor.name))
                .addModifiers(Modifier.PUBLIC)
                .returns(argCaptor.type)
                .addCode(CodeBlock.builder().addStatement("return $N", argCaptor).build())
                .build()
            );
        });

        return builder.addMethod(ctor.build()).build();
    }

    /**
     * Create delegator-constructor that gets delegation context and original class parameters, they should
     * come from DI-injection framework.
     */
    private MethodSpec constructor(TypeElement forClass, Class contextClass,
                                   ExecutableElement usingConstructor, Set<Class> addAnnotations) {
        MethodSpec.Builder method = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
        ParameterSpec contextParam = ParameterSpec.builder(
                ClassName.get(contextClass),
                "context"
        ).addAnnotation(Nullable.class).build();

        method.addParameter(contextParam);

        usingConstructor.getParameters().forEach(it ->
            method.addParameter(
                TypeName.get(it.asType()),
                firstCharToLowerCase(it.getSimpleName().toString())
            )
            .addAnnotations(
                it.getAnnotationMirrors().stream().map(AnnotationSpec::get).collect(Collectors.toList())
            )
        );

        addAnnotations.forEach(method::addAnnotation);

        CodeBlock.Builder block = CodeBlock.builder();

        block.addStatement("super(" + computeOriginalCtorArgs(usingConstructor) + ")");
        createCaptorWithNew(block, usingConstructor);

        block.addStatement(
                DELEGATE_NAME + " = $N != null ? $N.findOverride($T.class, " + CAPTOR_NAME + ") : null",
                contextParam,
                contextParam,
                forClass.getTypeParameters().isEmpty() ? forClass : ClassName.get(forClass) // get rid of generic
        );
        method.addCode(block.build());

        method.addJavadoc("@param context Context class to search for overrides.\n");
        return method.build();
    }

    /**
     * This allows type-safe adding overrides into
     * {@link de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry}
     */
    private MethodSpec overrideWith(TypeElement forClass, Class context, TypeSpec argsCaptor) {
        MethodSpec.Builder methodSpec = MethodSpec.methodBuilder("overrideWith")
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.STATIC)
                .returns(TypeName.VOID);

        ClassName captor = ClassName.bestGuess(argsCaptor.name);
        methodSpec.addParameter(ClassName.get(context), "context");
        methodSpec.addParameter(
                ParameterizedTypeName.get(
                        ClassName.get(Function.class),
                        captor,
                        ClassName.get(forClass)), // get rid of generic
                "ctorCaptor"
        );

        methodSpec.addStatement(
                "context.override($T.class, args -> ctorCaptor.apply(($T) args))",
                ClassName.get(forClass), // get rid of generic
                captor
        );
        methodSpec.addJavadoc(OVERRIDE_WITH_PURPOSE_COMMENT);

        return methodSpec.build();
    }

    private void createCaptorWithNew(CodeBlock.Builder block, ExecutableElement usingConstructor) {
        block.addStatement(String.format(
                "%s %s = new %s(%s)",
                CAPTOR_TYPE_NAME,
                CAPTOR_NAME,
                CAPTOR_TYPE_NAME,
                computeOriginalCtorArgs(usingConstructor))
        );
    }

    private String computeOriginalCtorArgs(ExecutableElement usingConstructor) {
        return usingConstructor.getParameters().stream()
                .map(it -> firstCharToLowerCase(it.getSimpleName().toString()))
                .collect(Collectors.joining(", "));
    }

    private static String firstCharToLowerCase(String str) {
        char[] asChars = str.toCharArray();
        asChars[0] = Character.toLowerCase(asChars[0]);

        return new String(asChars);
    }

    private static String firstCharToUpperCase(String str) {
        char[] asChars = str.toCharArray();
        asChars[0] = Character.toUpperCase(asChars[0]);

        return new String(asChars);
    }
}
