/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * NOTE:
 * SimpleStorageResource.java from spring-cloud-aws
 * located by https://github.com/spring-cloud/spring-cloud-aws/blob/master/spring-cloud-aws-core/src/main/java/org/springframework/cloud/aws/core/io/s3/SimpleStorageResource.java
 * is used and was modified according Adorsys project needs.
 */

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
        this.partNumber = request.getChunkNumberCounter();
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
