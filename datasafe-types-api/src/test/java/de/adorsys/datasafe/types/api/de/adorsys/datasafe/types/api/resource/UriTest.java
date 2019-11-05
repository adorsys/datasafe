package de.adorsys.datasafe.types.api.de.adorsys.datasafe.types.api.resource;

import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UriTest {
    @Test
    public void anUriResolveTest() {
        String uriString = "http://Uri.peter.at.home/";
        Uri uri = new Uri(uriString);
        uri = uri.resolve("a/").resolve("b/");
        assertEquals(uriString + "a/b/", uri.toASCIIString());
    }

    @SneakyThrows
    @Test
    public void anURIResolveTest() {
        String uriString = "http://URI.peter.at.home/";
        URI uri = new URI(uriString);
        uri = uri.resolve("a/").resolve("b/");
        assertEquals(uriString + "a/b/", uri.toASCIIString());
    }
}
