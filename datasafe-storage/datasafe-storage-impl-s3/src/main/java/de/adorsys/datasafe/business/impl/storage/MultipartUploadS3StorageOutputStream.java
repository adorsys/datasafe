package de.adorsys.datasafe.business.impl.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.BinaryUtils;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;
import de.adorsys.datasafe.business.api.types.utils.Log;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class MultipartUploadS3StorageOutputStream extends OutputStream {

    private String bucketName;

    private String objectName;

    private AmazonS3 amazonS3;

    // The minimum contentSize for a multi part request is 5 MB, hence the buffer contentSize of 5 MB
    private static final int BUFFER_SIZE = 1024 * 1024 * 5;

    private final Object monitor = new Object();

    private final CompletionService<UploadPartResult> completionService;

    private ByteArrayOutputStream currentOutputStream = new ByteArrayOutputStream(BUFFER_SIZE);

    private InitiateMultipartUploadResult multiPartUploadResult;

    private int partCounter = 1;

    public MultipartUploadS3StorageOutputStream(String bucketName, ResourceLocation resource, AmazonS3 amazonS3) {
        this.bucketName = bucketName;
        this.objectName = resource.location().getPath().replaceFirst("^/", "");;
        this.amazonS3 = amazonS3;

        log.debug("Write to bucket: {} with name: {}", Log.secure(bucketName), Log.secure(objectName));

        completionService  = new ExecutorCompletionService<>(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
    }

    @Override
    public void write(int b) {
        synchronized (monitor) {
            currentOutputStream.write(b);

            if (currentOutputStream.size() == BUFFER_SIZE) {
                initiateMultiPartIfNeeded();
                completionService.submit(new UploadChunkResultCallable(
                        ChunkUploadRequest
                                .builder()
                                .amazonS3(amazonS3)
                                .content(currentOutputStream.toByteArray())
                                .contentSize(currentOutputStream.size())
                                .bucketName(bucketName)
                                .objectName(objectName)
                                .uploadId(multiPartUploadResult.getUploadId())
                                .chuckNumberCounter(partCounter++)
                                .lastChunk(false)
                                .build()
                ));
                currentOutputStream.reset();
            }
        }
    }

    @Override
    public void close() throws IOException {
        synchronized (monitor) {
            if (currentOutputStream == null) {
                return;
            }

            if (isMultiPartUpload()) {
                finishMultiPartUpload();

                log.debug("Finished multi part upload");
            } else {
                finishSimpleUpload();

                log.debug("Finished simple upload");
            }
        }
    }

    private boolean isMultiPartUpload() {
        return multiPartUploadResult != null;
    }

    private void finishSimpleUpload() {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(currentOutputStream.size());

        byte[] content = currentOutputStream.toByteArray();

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            String md5Digest = BinaryUtils.toBase64(messageDigest.digest(content));
            objectMetadata.setContentMD5(md5Digest);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "MessageDigest could not be initialized because it uses an unknown algorithm", e);
        }

        amazonS3.putObject(
                bucketName,
                objectName,
                new ByteArrayInputStream(content), objectMetadata);

        // Release the memory early
        currentOutputStream = null;
    }

    private void finishMultiPartUpload() throws IOException {
        completionService.submit(
                new UploadChunkResultCallable(ChunkUploadRequest.builder()
                        .amazonS3(amazonS3)
                        .content(currentOutputStream.toByteArray())
                        .contentSize(currentOutputStream.size())
                        .bucketName(bucketName)
                        .objectName(objectName)
                        .uploadId(multiPartUploadResult.getUploadId())
                        .chuckNumberCounter(partCounter)
                        .lastChunk(true)
                        .build()
                )
        );

        try {
            List<PartETag> partETags = getMultiPartsUploadResults();

            log.debug("Send multipart request to S3");
            amazonS3.completeMultipartUpload(
                    new CompleteMultipartUploadRequest(
                            multiPartUploadResult.getBucketName(),
                            multiPartUploadResult.getKey(),
                            multiPartUploadResult.getUploadId(),
                            partETags
                    )
            );
        }
        catch (ExecutionException e) {
            abortMultiPartUpload();

            log.error(e.getMessage(), e);
            throw new IOException("Multi part upload failed ", e.getCause());
        }
        catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            abortMultiPartUpload();
            Thread.currentThread().interrupt();
        }
        finally {
            currentOutputStream = null;
        }
    }

    private void initiateMultiPartIfNeeded() {
        if (multiPartUploadResult == null) {

            log.debug("Initiate multi part");
            multiPartUploadResult = amazonS3
                    .initiateMultipartUpload(new InitiateMultipartUploadRequest(bucketName, objectName));
        }
    }

    private void abortMultiPartUpload() {
        log.debug("Abort multi part");

        if (isMultiPartUpload()) {
            amazonS3.abortMultipartUpload(new AbortMultipartUploadRequest(
                            multiPartUploadResult.getBucketName(),
                            multiPartUploadResult.getKey(),
                            multiPartUploadResult.getUploadId()));
        }
    }

    private List<PartETag> getMultiPartsUploadResults()
            throws ExecutionException, InterruptedException {
        List<PartETag> result = new ArrayList<>(partCounter);
        for (int i = 0; i < partCounter; i++) {
            result.add(completionService.take().get().getPartETag());

            log.info("Get upload part #{} from {}", i, partCounter);
        }
        return result;
    }

}

