package de.adorsys.datasafe.storage.impl.s3;

import com.amazonaws.services.s3.AmazonS3;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class ChunkUploadRequest {

    private AmazonS3 amazonS3;
    @ToString.Exclude
    private byte[] content;
    private int contentSize;
    private String bucketName;
    private String objectName;
    private String uploadId;
    private int chunkNumberCounter;
    private boolean lastChunk;

}
