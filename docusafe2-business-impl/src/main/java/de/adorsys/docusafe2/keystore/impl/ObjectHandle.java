package de.adorsys.docusafe2.keystore.impl;

import de.adorsys.docusafe2.keystore.api.exceptions.BucketRestrictionException;

public class ObjectHandle {

    private String container;

    private String name;

    public ObjectHandle() {
        super();
    }

    public ObjectHandle(String container, String name) {
        checkRestrictions(container);
        this.container = container;
        this.name = name;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        checkRestrictions(container);
        this.container = container;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * https://docs.aws.amazon.com/AmazonS3/latest/dev/BucketRestrictions.html
     * <p>
     * <p>
     * Bucket names must be at least 3 and no more than 63 characters long.
     * <p>
     * Bucket names must be a series of one or more labels. Adjacent labels are separated by a single period (.).
     * Bucket names can contain lowercase letters, numbers, and hyphens.
     * Each label must start and end with a lowercase letter or a number.
     * <p>
     * Bucket names must not be formatted as an IP address (for example, 192.168.5.4).
     * <p>
     * When using virtual hosted–style buckets with SSL, the SSL wildcard certificate only matches buckets that do not contain periods.
     * To work around this, use HTTP or write your own certificate verification logic.
     * We recommend that you do not use periods (".") in bucket names.
     */

    public static void checkRestrictions(String container) {
        if (container == null) {
            throw new BucketRestrictionException("Bucket must not be null");
        }
        if (container.length() < 3) {
            throw new BucketRestrictionException("Bucket length must be at least 3 chars: " + container);
        }
        if (!container.toLowerCase().equals(container)) {
            throw new BucketRestrictionException("Bucket must not contain uppercase letters: " + container);
        }
    }
}
