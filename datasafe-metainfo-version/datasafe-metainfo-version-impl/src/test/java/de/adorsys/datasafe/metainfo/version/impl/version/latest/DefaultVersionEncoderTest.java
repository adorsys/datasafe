package de.adorsys.datasafe.metainfo.version.impl.version.latest;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.UUID;

@Slf4j
public class DefaultVersionEncoderTest {

    @Test
    @SneakyThrows
    public void testLimits() {
        DefaultVersionEncoder defaultVersionEncoder = new DefaultVersionEncoder();

        URI uri = new URI("uri");
        Assertions.assertFalse(defaultVersionEncoder.decodeVersion(uri).isPresent());

        uri = new URI("uri/uri");
        Assertions.assertFalse(defaultVersionEncoder.decodeVersion(uri).isPresent());

        String peter = "peter";
        String uuid = UUID.randomUUID().toString();
        uri = new URI("uri/" +  peter + "--" + uuid);
        Assertions.assertEquals(uuid, defaultVersionEncoder.decodeVersion(uri).get().getVersion());
    }
}
