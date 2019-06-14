package de.adorsys.datasafe.runtimedelegate;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

class RuntimeDelegateTest {

    @ParameterizedTest
    @MethodSource("inputAndOutputPaths")
    void testBasicCases(String input, String output) {
        Compilation compilation = javac()
                .withProcessors(new RuntimeDelegateProcessor())
                .compile(JavaFileObjects.forResource(input));

        assertThat(compilation).succeededWithoutWarnings();

        assertThat(compilation)
                .generatedSourceFile(output.split("/")[1].split("\\.")[0])
                .hasSourceEquivalentTo(JavaFileObjects.forResource(output));
    }

    @Test
    void testRequiresInject() {
        Compilation compilation = javac()
                .withProcessors(new RuntimeDelegateProcessor())
                .compile(JavaFileObjects.forResource("errors/SimpleClassNoInject.java"));

        assertThat(compilation).hadErrorContaining("Class should have exactly one @Inject annotation");
    }

    @Test
    void testRequiresExactlyOneInject() {
        Compilation compilation = javac()
                .withProcessors(new RuntimeDelegateProcessor())
                .compile(JavaFileObjects.forResource("errors/SimpleClassMultiInject.java"));

        assertThat(compilation).hadErrorContaining("Class should have exactly one @Inject annotation");
    }

    @Test
    void testRequiresNonFinal() {
        Compilation compilation = javac()
                .withProcessors(new RuntimeDelegateProcessor())
                .compile(JavaFileObjects.forResource("errors/SimpleClassFinal.java"));

        assertThat(compilation).hadErrorContaining("Class should not be final");
    }

    static Stream<String[]> inputAndOutputPaths() {
        return Stream.of("SimpleClass", "SimpleClassWithGeneric", "SimpleClassWithVoid")
                .map(it -> new String[] {
                        "input/" + it + ".java",
                        "output/" + it + "RuntimeDelegatable.java"
                });
    }
}