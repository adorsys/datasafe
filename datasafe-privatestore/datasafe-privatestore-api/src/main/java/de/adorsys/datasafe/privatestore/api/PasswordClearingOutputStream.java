package de.adorsys.datasafe.privatestore.api;

import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;

import java.io.Closeable;
import java.io.OutputStream;

@AllArgsConstructor
public class PasswordClearingOutputStream extends OutputStream {

    @Delegate(types = OutputStream.class, excludes = Closeable.class)
    private final OutputStream outputStream;

    private final ReadKeyPassword readKeyPassword;

    @SneakyThrows
    @Override
    public void close() {
        if (readKeyPassword != null) {
            readKeyPassword.clear();
        }

        outputStream.close();
    }
}
