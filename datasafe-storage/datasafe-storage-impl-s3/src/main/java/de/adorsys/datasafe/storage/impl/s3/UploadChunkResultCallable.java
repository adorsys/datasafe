package de.adorsys.datasafe.storage.impl.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.util.concurrent.Callable;

@Slf4j
public class UploadChunkResultCallable implements Callable<UploadPartResult> {

    private final AmazonS3 amazonS3;

    private final int contentLength;

    private final int partNumber;

    private final boolean last;

    private final String bucketName;

    private final String fileName;

    private final String chunkId;

    private byte[] content;

    UploadChunkResultCallable(ChunkUploadRequest request) {
        this.amazonS3 = request.getAmazonS3();
        this.content = request.getContent();
        this.contentLength = request.getContentSize();
        this.partNumber = request.getChuckNumberCounter();
        this.last = request.isLastChunk();
        this.bucketName = request.getBucketName();
        this.fileName = request.getObjectName();
        this.chunkId = request.getUploadId();

        log.debug("Chunk upload request: {}", request.toString());
    }

    @Override
    public UploadPartResult call() {
        log.trace("Upload chunk result call with part: {}", partNumber);
        try {
            return amazonS3.uploadPart(new UploadPartRequest()
                    .withBucketName(bucketName).withKey(fileName)
                    .withUploadId(chunkId)
                    .withInputStream(new ByteArrayInputStream(content))
                    .withPartNumber(partNumber).withLastPart(last)
                    .withPartSize(contentLength)
            );
        } finally {
            // Release the memory, as the callable may still live inside the
            // CompletionService which would cause
            // an exhaustive memory usage
            content = null;
        }
    }

}
