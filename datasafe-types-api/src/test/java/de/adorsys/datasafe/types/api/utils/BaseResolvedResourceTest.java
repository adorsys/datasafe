package de.adorsys.datasafe.types.api.utils;

import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.BaseResolvedResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

@Slf4j
class BaseResolvedResourceTest {

    @Test
    @SneakyThrows
    void testResolveMethods() {
        String uriString = "uri";
        Uri uri = new Uri("uri");
        PrivateResource privateResource = new BasePrivateResource(uri, uri, uri);
        Instant i = null;
        BaseResolvedResource baseResolvedResource = new BaseResolvedResource(privateResource, i);
        Assertions.assertEquals(
                uriString + "/" + uriString,
                baseResolvedResource.resolveFrom(privateResource).location().toASCIIString()
        );
        Assertions.assertEquals(uriString, baseResolvedResource.asPrivate().location().toASCIIString());
        Assertions.assertEquals(uriString, baseResolvedResource.location().toASCIIString());
    }
}
