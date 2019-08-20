/*
 * Copyright 2013-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * NOTE:
 * SimpleStorageResource.java from spring-cloud-aws
 * located by https://github.com/spring-cloud/spring-cloud-aws/blob/master/spring-cloud-aws-core/src/main/java/org/springframework/cloud/aws/core/io/s3/SimpleStorageResource.java
 * is used and was modified according Adorsys project needs.
 */

package de.adorsys.datasafe.storage.impl.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import de.adorsys.datasafe.types.api.callback.PhysicalVersionCallback;
import de.adorsys.datasafe.types.api.callback.ResourceWriteCallback;
import de.adorsys.datasafe.types.api.utils.Obfuscate;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;

@Slf4j
public class MultipartUploadS3StorageOutputStream extends OutputStream {

    private String bucketName;

    private String objectName;

    private AmazonS3 amazonS3;

    // The minimum size for a multi part request is 5 MB, hence the buffer size of 5 MB
    static final int BUFFER_SIZE = 1024 * 1024 * 5;

    private final CompletionService<UploadPartResult> completionService;

    private CustomizableByteArrayOutputStream currentOutputStream = newOutputStream();

    private InitiateMultipartUploadResult multiPartUploadResult;

    private int partCounter = 1;

    private final List<? extends ResourceWriteCallback> callbacks;

    MultipartUploadS3StorageOutputStream(String bucketName, String objectKey, AmazonS3 amazonS3,
                                         ExecutorService executorService,
                                         List<? extends ResourceWriteCallback> callbacks) {
        this.bucketName = bucketName;
        this.objectName = objectKey;
        this.amazonS3 = amazonS3;
        this.completionService = new ExecutorCompletionService<>(executorService);
        this.callbacks = callbacks;

        log.debug("Write to bucket: {} with name: {}", Obfuscate.secure(bucketName), Obfuscate.secure(objectName));
    }

    @Override
    @Synchronized
    public void write(byte[] bytes, int off, int len) {
        int remainingSizeToWrite = len;
        int inputPosition = off;

        do {
            int availableCapacity = BUFFER_SIZE - currentOutputStream.size();
            int bytesToWrite = Math.min(availableCapacity, remainingSizeToWrite);
            currentOutputStream.write(bytes, inputPosition, bytesToWrite);
            inputPosition += bytesToWrite;
            remainingSizeToWrite -= bytesToWrite;

            initiateMultipartRequestAndCommitPartIfNeeded();
        } while (remainingSizeToWrite > 0);
    }

    @Override
    @Synchronized
    public void write(int b) {
        currentOutputStream.write(b);
        initiateMultipartRequestAndCommitPartIfNeeded();
    }

    @Override
    @Synchronized
    public void close() throws IOException {
        if (currentOutputStream == null) {
            return;
        }

        if (isMultiPartUpload()) {
            finishMultiPartUpload();
        } else {
            finishSimpleUpload();
        }
    }

    private void initiateMultipartRequestAndCommitPartIfNeeded() {
        if (currentOutputStream.size() != BUFFER_SIZE) {
            return;
        }

        initiateMultiPartIfNeeded();

        byte[] content = currentOutputStream.getBuffer();
        int size = currentOutputStream.size();
        // Release the memory
        currentOutputStream = newOutputStream();

        completionService.submit(new UploadChunkResultCallable(
                ChunkUploadRequest
                        .builder()
                        .amazonS3(amazonS3)
                        .content(content)
                        .contentSize(size)
                        .bucketName(bucketName)
                        .objectName(objectName)
                        .uploadId(multiPartUploadResult.getUploadId())
                        .chunkNumberCounter(partCounter)
                        .lastChunk(false)
                        .build()
        ));

        ++partCounter;
    }

    private boolean isMultiPartUpload() {
        return multiPartUploadResult != null;
    }

    @SneakyThrows
    private void finishSimpleUpload() {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        int size = currentOutputStream.size();
        objectMetadata.setContentLength(size);
        byte[] content = currentOutputStream.getBuffer();

        // Release the memory
        currentOutputStream = null;

        PutObjectResult upload = amazonS3.putObject(
                bucketName,
                objectName,
                new ByteArrayInputStream(content, 0, size),
                objectMetadata);

        notifyCommittedVersionIfPresent(upload.getVersionId());

        log.debug("Finished simple upload");
    }

    private void finishMultiPartUpload() throws IOException {
        sendLastChunkOfMultipartIfNeeded();

        try {
            List<PartETag> partETags = getMultiPartsUploadResults();

            log.debug("Send multipart request to S3");
            CompleteMultipartUploadResult upload = amazonS3.completeMultipartUpload(
                    new CompleteMultipartUploadRequest(
                            multiPartUploadResult.getBucketName(),
                            multiPartUploadResult.getKey(),
                            multiPartUploadResult.getUploadId(),
                            partETags
                    )
            );

            notifyCommittedVersionIfPresent(upload.getVersionId());

            log.debug("Finished multi part upload");
        } catch (ExecutionException e) {
            abortMultiPartUpload();

            log.error(e.getMessage(), e);
            throw new IOException("Multi part upload failed ", e.getCause());
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            abortMultiPartUpload();
            Thread.currentThread().interrupt();
        } finally {
            currentOutputStream = null;
        }
    }

    private void sendLastChunkOfMultipartIfNeeded() {
        // empty file can be created only using simple upload:
        if (currentOutputStream.size() == 0) {
            partCounter--;
            return;
        }

        byte[] content = currentOutputStream.getBuffer();
        int size = currentOutputStream.size();
        // Release the memory
        currentOutputStream = null;

        completionService.submit(
                new UploadChunkResultCallable(ChunkUploadRequest.builder()
                        .amazonS3(amazonS3)
                        .content(content)
                        .contentSize(size)
                        .bucketName(bucketName)
                        .objectName(objectName)
                        .uploadId(multiPartUploadResult.getUploadId())
                        .chunkNumberCounter(partCounter)
                        .lastChunk(true)
                        .build()
                )
        );
    }

    private void notifyCommittedVersionIfPresent(String version) {
        if (null == version) {
            return;
        }

        callbacks.stream()
                .filter(it -> it instanceof PhysicalVersionCallback)
                .forEach(it -> ((PhysicalVersionCallback) it).handleVersionAssigned(version));
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

    private List<PartETag> getMultiPartsUploadResults() throws ExecutionException, InterruptedException {
        List<PartETag> result = new ArrayList<>(partCounter);
        for (int i = 0; i < partCounter; i++) {
            UploadPartResult partResult = completionService.take().get();
            result.add(partResult.getPartETag());

            log.debug("Get upload part #{} from {}", i, partCounter);
        }
        return result;
    }

    private CustomizableByteArrayOutputStream newOutputStream() {
        return new CustomizableByteArrayOutputStream(32, BUFFER_SIZE);
    }

    /**
     * {@link ByteArrayOutputStream}-alike stream that has customized maximum capacity and growing strategy in
     * order to minimize memory usage.
     */
    private class CustomizableByteArrayOutputStream extends OutputStream {

        private final int maxArraySize;
        private byte[] buffer;
        private int count;

        CustomizableByteArrayOutputStream(int initialCapacity, int maxArraySize) {
            this.buffer = new byte[initialCapacity];
            this.maxArraySize = maxArraySize;
        }

        @Override
        @Synchronized
        public void write(int b) {
            ensureCapacity(count + 1);
            buffer[count] = (byte) b;
            count += 1;
        }

        @Override
        @Synchronized
        public void write(byte[] buffer, int off, int len) {
            if ((off < 0) || (off > buffer.length) || (len < 0) ||
                    ((off + len) - buffer.length > 0)) {
                throw new IndexOutOfBoundsException();
            }

            ensureCapacity(count + len);
            System.arraycopy(buffer, off, this.buffer, count, len);
            count += len;
        }

        @Synchronized
        public byte[] getBuffer() {
            return buffer;
        }

        @Synchronized
        public int size() {
            return count;
        }

        @Override
        public void close() {
            // NOP
        }

        private void ensureCapacity(int minCapacity) {
            if (minCapacity <= buffer.length) {
                return;
            }

            grow(minCapacity);
        }

        private void grow(int minCapacity) {
            int oldCapacity = buffer.length;

            int newCapacity = Math.min(oldCapacity << 1, maxArraySize);

            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }

            if (newCapacity > maxArraySize) {
                throw new OutOfMemoryError();
            }

            buffer = Arrays.copyOf(buffer, newCapacity);
        }
    }
}

