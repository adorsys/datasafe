package de.adorsys.datasafe.metainfo.version.impl.version.latest;

import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

@Slf4j
public class DefaultVersionEncoderDecoderTest {

    @Test
    @SneakyThrows
    public void testLimits() {
        DefaultVersionEncoderDecoder defaultVersionEncoder = new DefaultVersionEncoderDecoder();

        Uri uri = new Uri("uri");
        Assertions.assertFalse(defaultVersionEncoder.decodeVersion(uri).isPresent());

        uri = new Uri("uri/uri");
        Assertions.assertFalse(defaultVersionEncoder.decodeVersion(uri).isPresent());

        uri = new Uri("uri/uri/BBA");
        Assertions.assertFalse(defaultVersionEncoder.decodeVersion(uri).isPresent());

        String peter = "peter";
        String uuid = UUID.randomUUID().toString();
        uri = new Uri("uri/" +  peter + "/" + uuid);
        Assertions.assertEquals(uuid, defaultVersionEncoder.decodeVersion(uri).get().getVersion());
    }
}
