package de.adorsys.datasafe.privatestore.api;

import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;

import java.io.Closeable;
import java.io.InputStream;

@AllArgsConstructor
@RuntimeDelegate
public class PasswordClearingInputStream extends InputStream {

    @Delegate(types = InputStream.class, excludes = Closeable.class)
    private final InputStream inputStream;

    private final ReadKeyPassword readKeyPassword;

    @SneakyThrows
    @Override
    public void close() {
        if (readKeyPassword != null) {
            readKeyPassword.clear();
        }

        inputStream.close();
    }
}
