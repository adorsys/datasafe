package de.adorsys.datasafe.business.api.types.action;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import lombok.Data;

/**
 * This is an interface extension to provide for a possibility to add metadata to the DFS object.
 * 
 * Inspired from: com.amazonaws.services.s3.model.ObjectMetadata
 * 
 * Represents the object metadata that is stored with Amazon S3. This includes custom
 * user-supplied metadata, as well as the standard HTTP headers that Amazon S3
 * sends and receives (Content-Length, ETag, Content-MD5, etc.).
 * 
 * @author fpo
 *
 */
@Data
public class ObjectMetadata {

    /**
     * Custom user metadata, represented in responses with the x-amz-meta-
     * header prefix
     */
    private Map<String, String> userMetadata = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

    /**
     * All other (non user custom) headers such as Content-Length, Content-Type,
     * etc.
     */
    private Map<String, Object> metadata = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);

    /**
     * The date when the object is no longer cacheable.
     */
    private Date httpExpiresDate;

    /**
     * The time this object will expire and be completely removed from S3, or
     * null if this object will never expire.
     * <p>
     * This and the expiration time rule aren't stored in the metadata map
     * because the header contains both the time and the rule.
     */
    private Date expirationTime;
}
