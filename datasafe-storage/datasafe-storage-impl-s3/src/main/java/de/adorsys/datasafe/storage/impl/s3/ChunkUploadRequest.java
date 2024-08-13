package de.adorsys.datasafe.storage.impl.s3;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import software.amazon.awssdk.services.s3.S3Client;

@Getter
@Builder
@ToString
public class ChunkUploadRequest {

    private S3Client s3;
    @ToString.Exclude
    private byte[] content;
    private int contentSize;
    private String bucketName;
    private String objectName;
    private String uploadId;
    private int chunkNumberCounter;
    private boolean lastChunk;



}
