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
public class MultipartS3StorageOutputStream extends OutputStream {

    private String bucketName;

    private String objectName;

    private AmazonS3 amazonS3;

    // The minimum size for a multi part is 5 MB, hence the buffer size of 5 MB
    private static final int BUFFER_SIZE = 1024 * 1024 * 5;

    private final Object monitor = new Object();

    private final CompletionService<UploadPartResult> completionService;

    @SuppressWarnings("FieldMayBeFinal")
    private ByteArrayOutputStream currentOutputStream = new ByteArrayOutputStream(BUFFER_SIZE);

    private int partNumberCounter = 1;

    private InitiateMultipartUploadResult multiPartUploadResult;

    public MultipartS3StorageOutputStream(String bucketName, ResourceLocation resource, AmazonS3 amazonS3) {
        this.bucketName = bucketName;
        this.objectName = resource.location().getPath().replaceFirst("^/", "");;
        this.amazonS3 = amazonS3;

        log.debug("Write to bucket: {} with name: {}", Log.secure(bucketName), Log.secure(objectName));

        completionService  = new ExecutorCompletionService<>(Executors.newFixedThreadPool(5));
    }

    @Override
    public void write(int b) {
        synchronized (monitor) {
            if (currentOutputStream.size() == BUFFER_SIZE) {
                initiateMultiPartIfNeeded();
                completionService.submit(new UploadPartResultCallable(
                        amazonS3,
                        currentOutputStream.toByteArray(),
                        currentOutputStream.size(),
                        bucketName,
                        objectName,
                        multiPartUploadResult.getUploadId(),
                        partNumberCounter++, false));
                currentOutputStream.reset();
            }
            currentOutputStream.write(b);
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
            } else {
                finishSimpleUpload();
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
                new UploadPartResultCallable(amazonS3,
                        currentOutputStream.toByteArray(),
                        currentOutputStream.size(),
                        bucketName,
                        objectName,
                        multiPartUploadResult.getUploadId(),
                        partNumberCounter, true));
        try {
            List<PartETag> partETags = getMultiPartsUploadResults();
            amazonS3
                    .completeMultipartUpload(new CompleteMultipartUploadRequest(
                            multiPartUploadResult.getBucketName(),
                            multiPartUploadResult.getKey(),
                            multiPartUploadResult.getUploadId(), partETags));
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
            multiPartUploadResult = amazonS3
                    .initiateMultipartUpload(new InitiateMultipartUploadRequest(bucketName, objectName));
        }
    }

    private void abortMultiPartUpload() {
        if (isMultiPartUpload()) {
            amazonS3
                    .abortMultipartUpload(new AbortMultipartUploadRequest(
                            multiPartUploadResult.getBucketName(),
                            multiPartUploadResult.getKey(),
                            multiPartUploadResult.getUploadId()));
        }
    }

    private List<PartETag> getMultiPartsUploadResults()
            throws ExecutionException, InterruptedException {
        List<PartETag> result = new ArrayList<>(partNumberCounter);
        for (int i = 0; i < partNumberCounter; i++) {
            Future<UploadPartResult> uploadPartResultFuture = completionService.take();
            result.add(uploadPartResultFuture.get().getPartETag());

            log.info("Upload {} part from {}", i, partNumberCounter);
        }
        return result;
    }

    private final class UploadPartResultCallable
            implements Callable<UploadPartResult> {

        private final AmazonS3 amazonS3;

        private final int contentLength;

        private final int partNumber;

        private final boolean last;

        private final String bucketName;

        private final String key;

        private final String uploadId;

        @SuppressWarnings("FieldMayBeFinal")
        private byte[] content;

        private UploadPartResultCallable(AmazonS3 amazon, byte[] content,
                                         int writtenDataSize, String bucketName, String key, String uploadId,
                                         int partNumber, boolean last) {
            this.amazonS3 = amazon;
            this.content = content;
            this.contentLength = writtenDataSize;
            this.partNumber = partNumber;
            this.last = last;
            this.bucketName = bucketName;
            this.key = key;
            this.uploadId = uploadId;
        }

        @Override
        public UploadPartResult call() throws Exception {
            try {
                return amazonS3.uploadPart(new UploadPartRequest()
                        .withBucketName(bucketName).withKey(key)
                        .withUploadId(uploadId)
                        .withInputStream(new ByteArrayInputStream(content))
                        .withPartNumber(partNumber).withLastPart(last)
                        .withPartSize(contentLength));
            }
            finally {
                // Release the memory, as the callable may still live inside the
                // CompletionService which would cause
                // an exhaustive memory usage
                content = null;
            }
        }

    }

}

