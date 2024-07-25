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

import de.adorsys.datasafe.types.api.callback.PhysicalVersionCallback;
import de.adorsys.datasafe.types.api.callback.ResourceWriteCallback;
import de.adorsys.datasafe.types.api.utils.CustomizableByteArrayOutputStream;
import de.adorsys.datasafe.types.api.utils.Obfuscate;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;

@Slf4j

public class MultipartUploadS3StorageOutputStream extends OutputStream {

    private String bucketName;

    private String objectName;

    private S3Client s3;

    private String multiPartUploadId;

    // The minimum size for a multi part request is 5 MB, hence the buffer size of 5 MB
    static final int BUFFER_SIZE = 1024 * 1024 * 5;

    private final CompletionService<UploadPartResponse> completionService;

    private CustomizableByteArrayOutputStream currentOutputStream = newOutputStream();

    private CreateMultipartUploadResponse multiPartUploadResult;


    private int partCounter = 1;

    private final List<? extends ResourceWriteCallback> callbacks;

    public MultipartUploadS3StorageOutputStream(String bucketName, String objectKey, S3Client s3,
                                         ExecutorService executorService, String multiPartUploadId,
                                         List<? extends ResourceWriteCallback> callbacks) {
        this.bucketName = bucketName;
        this.objectName = objectKey;
        this.s3 = s3;
        this.completionService = new ExecutorCompletionService<>(executorService);
        this.callbacks = callbacks;
        this.multiPartUploadId = multiPartUploadId;

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

        byte[] content = currentOutputStream.getBufferOrCopy();
        int size = currentOutputStream.size();
        // Release the memory
        currentOutputStream = newOutputStream();

        completionService.submit(new UploadChunkResultCallable(
                ChunkUploadRequest
                        .builder()
                        .s3(s3)
                        .content(content)
                        .contentSize(size)
                        .bucketName(bucketName)
                        .objectName(objectName)
                        .uploadId(multiPartUploadId)
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
//        ObjectMetadata objectMetadata = ObjectMetadata.builder()
//                .contentLength(currentOutputStream.size())
//                .build();
        byte[] content = currentOutputStream.getBufferOrCopy();

        // Release the memory
        currentOutputStream = null;

        PutObjectResponse upload = s3.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectName)
                        .contentLength((long) content.length)
                        .build(),
                RequestBody.fromBytes(content));
        notifyCommittedVersionIfPresent(upload.versionId());

        log.debug("Finished simple upload");
    }

    private void finishMultiPartUpload() throws IOException {
        sendLastChunkOfMultipartIfNeeded();

        try {
            List<CompletedPart> partETags = getMultiPartsUploadResults();

            CompleteMultipartUploadResponse upload = s3.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(objectName)
                    .uploadId(multiPartUploadId)
                    .multipartUpload(CompletedMultipartUpload.builder()
                            .parts(partETags)
                            .build())
                    .build());

            notifyCommittedVersionIfPresent(upload.versionId());

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

        byte[] content = currentOutputStream.getBufferOrCopy();
        int size = currentOutputStream.size();
        // Release the memory
        currentOutputStream = null;

        completionService.submit(
                new UploadChunkResultCallable(ChunkUploadRequest.builder()
                        .s3(s3)
                        .content(content)
                        .contentSize(size)
                        .bucketName(bucketName)
                        .objectName(objectName)
                        .uploadId(multiPartUploadId)
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
            CreateMultipartUploadResponse multiPartUploadResult = s3.createMultipartUpload(
                    CreateMultipartUploadRequest.builder()
                            .bucket(bucketName)
                            .key(objectName)
                            .build()
            );
            this.multiPartUploadResult = multiPartUploadResult;
            this.multiPartUploadId = multiPartUploadResult.uploadId();
        }
    }

    private void abortMultiPartUpload() {
        log.debug("Abort multi part");

        if (isMultiPartUpload()) {
            s3.abortMultipartUpload(
                    AbortMultipartUploadRequest.builder()
                            .bucket(bucketName)
                            .key(objectName)
                            .uploadId(multiPartUploadId)
                            .build()
            );
        }
    }

    private List<CompletedPart> getMultiPartsUploadResults() throws ExecutionException, InterruptedException {
        List<CompletedPart> result = new ArrayList<>(partCounter);
        for (int i = 0; i < partCounter; i++) {
            UploadPartResponse partResponse = completionService.take().get();
            int partNumber = i + 1; // Part numbers start from 1
            result.add(CompletedPart.builder()
                    .partNumber(partNumber)
                    .eTag(partResponse.eTag())
                    .build());

            log.debug("Get upload part #{} from {}", i, partCounter);
        }
        return result;
    }
    private CustomizableByteArrayOutputStream newOutputStream() {
        return new CustomizableByteArrayOutputStream(32, BUFFER_SIZE, 0.5);
    }
}

