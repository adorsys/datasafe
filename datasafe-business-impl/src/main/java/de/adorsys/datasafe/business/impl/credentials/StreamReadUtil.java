package de.adorsys.datasafe.business.impl.credentials;

import com.google.common.io.ByteStreams;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

class StreamReadUtil {

    @Inject
    public StreamReadUtil() {
    }

    @SneakyThrows
    byte[] readStream(InputStream stream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteStreams.copy(stream, outputStream);
        return outputStream.toByteArray();
    }
}
