package de.adorsys.datasafe.types.api.utils;

import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.BaseResolvedResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;

@Slf4j
class BaseResolvedResourceTest {

    @Test
    @SneakyThrows
    void testResolveMethods() {
        String uriString = "uri";
        URI uri = new URI("uri");
        PrivateResource privateResource = new BasePrivateResource(uri, uri, uri);
        Instant i = null;
        BaseResolvedResource baseResolvedResource = new BaseResolvedResource(privateResource, i);
        Assertions.assertEquals(uriString + "/" + uriString, baseResolvedResource.resolve(privateResource).location().toString());
        Assertions.assertEquals(uriString, baseResolvedResource.asPrivate().location().toString());
        Assertions.assertEquals(uriString, baseResolvedResource.location().toString());
    }
}
