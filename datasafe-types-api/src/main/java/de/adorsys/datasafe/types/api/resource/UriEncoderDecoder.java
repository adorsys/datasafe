package de.adorsys.datasafe.types.api.resource;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Encodes and decodes URI using slash-based segmentation, provides `reasonable` approximate of what user may
 * desire when passing string as argument.
 * Note: it encodes any special URL symbols to be compatible with S3 API.
 */
@UtilityClass
public class UriEncoderDecoder {

    // naive approach, one should really use URI builder
    private static final Pattern SEGMENTS = Pattern.compile("(.+:///*[^/]+/*)*(.*)");

    public URI encode(String uri) {
        return encode(uri, UriEncoderDecoder::encodeSegment);
    }

    public URI encode(String uri, Function<String, String> encode) {
        Matcher matcher = SEGMENTS.matcher(uri);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Unparsable string " + uri);
        }

        if (matcher.groupCount() == 2 && null != matcher.group(1)) {
            return URI.create(
                    matcher.group(1) +
                    Arrays.stream(matcher.group(2).split("/", -1)).map(encode).collect(Collectors.joining("/"))
            );
        }

        return URI.create(
                Arrays.stream(uri.split("/", -1)).map(encode).collect(Collectors.joining("/"))
        );
    }

    public String decodeAndDropAuthority(URI uri) {
        return decodeAndDropAuthority(uri, UriEncoderDecoder::decodeSegment);
    }

    public String decodeAndDropAuthority(URI uri, Function<String, String> decode) {
        String encoded = withoutAuthority(uri);
        Matcher matcher = SEGMENTS.matcher(encoded);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Unparsable string " + uri);
        }

        if (matcher.groupCount() == 2 && null != matcher.group(1)) {
            return matcher.group(1) +
                    Arrays.stream(matcher.group(2).split("/", -1)).map(decode).collect(Collectors.joining("/"));
        }

        return Arrays.stream(encoded.split("/", -1)).map(decode).collect(Collectors.joining("/"));
    }

    public String withoutAuthority(URI uri) {
        return withoutAuthority(uri, UriEncoderDecoder::decodeSegment);
    }

    public String withoutAuthority(URI uri, Function<String, String> decode) {
        if (uri == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        if (null != uri.getScheme()) {
            sb.append(uri.getScheme()).append("://");
        }

        if (null != uri.getHost()) {
            sb.append(decode.apply(uri.getHost()));
        }

        if (-1 != uri.getPort()) {
            sb.append(":");
            sb.append(uri.getPort());
        }

        if (null != uri.getPath()) {
            decodePathIntoStringBuilder(uri.getPath(), sb, decode);
        }

        return sb.toString();
    }

    @SneakyThrows
    private void decodePathIntoStringBuilder(String path, StringBuilder sb, Function<String, String> decode) {
        boolean isStarted = false;
        for (String segment : path.split("/", -1)) {
            if (isStarted) {
                sb.append("/");
            } else {
                isStarted = true;
            }
            sb.append(decode.apply(segment));
        }
    }

    @SneakyThrows
    private String decodeSegment(String segment) {
        return URLDecoder.decode(segment, StandardCharsets.UTF_8.name());
    }

    @SneakyThrows
    private String encodeSegment(String segment) {
        return URLEncoder.encode(segment, StandardCharsets.UTF_8.name());
    }
}
