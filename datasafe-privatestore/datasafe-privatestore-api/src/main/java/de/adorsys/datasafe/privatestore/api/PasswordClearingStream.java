package de.adorsys.datasafe.privatestore.api;

import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;

import java.io.Closeable;
import java.util.stream.BaseStream;
import java.util.stream.Stream;

@AllArgsConstructor
public class PasswordClearingStream<T> implements Stream<T> {

    @Delegate(types = LombokGenericStream.class, excludes = Closeable.class)
    private final Stream<T> stream;

    private final ReadKeyPassword readKeyPassword;

    @SneakyThrows
    @Override
    public void close() {
        if (readKeyPassword != null) {
            readKeyPassword.clear();
        }

        stream.close();
    }

    private abstract class LombokGenericStream implements Stream<T>, BaseStream<T, Stream<T>> {
    }

}
