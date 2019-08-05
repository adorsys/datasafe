package de.adorsys.datasafe.examples.business.s3;

import lombok.Getter;

@Getter
enum MinioContainerId {
    FILES_BUCKET_ONE,
    FILES_BUCKET_TWO,
    DIRECTORY_BUCKET;

    private final String accessKey;
    private final String secretKey;
    private final String bucketName;

    MinioContainerId() {
        this.accessKey = "access-" + toString();
        this.secretKey = "secret-" + toString();
        this.bucketName = toString();
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase().replaceAll("_", "");
    }
}
