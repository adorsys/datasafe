package de.adorsys.datasafe.storage.impl.s3;

import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.testcontainers.shaded.com.google.common.io.ByteStreams;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import static de.adorsys.datasafe.storage.impl.s3.MultipartUploadS3StorageOutputStream.BUFFER_SIZE;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MultipartUploadS3StorageOutputStreamIT extends BaseMockitoTest {

    private final byte[] shortChunk = randomBytes(100);
    private final byte[] exactOneMultipartChunk = randomBytes(BUFFER_SIZE);
    private final byte[] multipartChunkWithTail = randomBytes(BUFFER_SIZE + 100);

    @Mock
    private S3Client s3;

    @Mock
    private ExecutorService executorService;

    @Captor
    private ArgumentCaptor<InputStream> bytesSentDirectly;
    @Captor
    private ArgumentCaptor<RequestBody> requestBodyCaptor;

    @Captor
    private ArgumentCaptor<UploadPartRequest> uploadChunk;

    private MultipartUploadS3StorageOutputStream tested;

    @BeforeEach
    void init() {
        tested = new MultipartUploadS3StorageOutputStream(
                "bucket",
                "s3://path/to/file.txt",
                s3,
                executorService,
                Collections.emptyList()
        );


        when(s3.putObject(any(PutObjectRequest.class), requestBodyCaptor.capture()))
                .thenReturn(PutObjectResponse.builder().build());
        when(s3.createMultipartUpload(any(CreateMultipartUploadRequest.class)))
                .thenReturn(CreateMultipartUploadResponse.builder().uploadId("testUploadId").build());
        doAnswer(inv -> {
            inv.getArgument(0, Runnable.class).run();
            return null;
        }).when(executorService).execute(any());
        when(s3.uploadPart(any(UploadPartRequest.class), requestBodyCaptor.capture()))
                .thenReturn(UploadPartResponse.builder().eTag("testETag").build());
        when(s3.completeMultipartUpload(any(CompleteMultipartUploadRequest.class)))
                .thenReturn(CompleteMultipartUploadResponse.builder().build());
    }


    @Test
    @SneakyThrows
    void writeBulkNonChunked() {
        tested.write(shortChunk, 0 , shortChunk.length);

        tested.close();
        verify(executorService, never()).submit(any(UploadChunkResultCallable.class));
        verify(s3).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @SneakyThrows
    void writeBulkNonChunkedWithOffset() {
        tested.write(shortChunk, 10 , shortChunk.length - 10);

        tested.close();

        verify(s3).putObject(any(PutObjectRequest.class), requestBodyCaptor.capture());
        verify(s3, never()).uploadPart(any(UploadPartRequest.class), any(RequestBody.class));

        RequestBody capturedBody = requestBodyCaptor.getValue();
        byte[] capturedContent = toByteArray(capturedBody.contentStreamProvider().newStream());

        assertThat(new String(capturedContent))
                .isEqualTo(new String(Arrays.copyOfRange(shortChunk, 10, shortChunk.length)));
    }

    @Test
    @SneakyThrows
    void writeBulkChunkedExactlyOne() {
        tested.write(exactOneMultipartChunk, 0 , exactOneMultipartChunk.length);

        tested.close();

        assertThat(requestBodyCaptor.getAllValues()).hasSize(1);
    }

    @Test
    @SneakyThrows
    void writeBulkChunked() {
        tested.write(multipartChunkWithTail, 0 , multipartChunkWithTail.length);

        tested.close();

        verify(s3, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(s3, times(2)).uploadPart(uploadChunk.capture(), requestBodyCaptor.capture());
        assertThat(uploadChunk.getAllValues()).hasSize(2);
        assertThat(requestBodyCaptor.getAllValues().get(0).contentStreamProvider().newStream())
                .hasContent(new String(Arrays.copyOfRange(multipartChunkWithTail, 0, BUFFER_SIZE)));
        assertThat(requestBodyCaptor.getAllValues().get(1).contentStreamProvider().newStream())
                .hasContent(new String(Arrays.copyOfRange(
                                multipartChunkWithTail, BUFFER_SIZE, multipartChunkWithTail.length)
                        )
                );
    }

    @Test
    @SneakyThrows
    void writeBulkChunkedWithOffset() {
        tested.write(multipartChunkWithTail, 10 , multipartChunkWithTail.length - 10);

        tested.close();

        verify(s3, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(s3, times(2)).uploadPart(uploadChunk.capture(), requestBodyCaptor.capture());
        assertThat(uploadChunk.getAllValues()).hasSize(2);
        assertThat(requestBodyCaptor.getAllValues().get(0).contentStreamProvider().newStream())
                .hasContent(new String(Arrays.copyOfRange(multipartChunkWithTail, 10, 10 + BUFFER_SIZE)));
        assertThat(requestBodyCaptor.getAllValues().get(1).contentStreamProvider().newStream())
                .hasContent(new String(Arrays.copyOfRange(
                                multipartChunkWithTail, 10 + BUFFER_SIZE, multipartChunkWithTail.length)
                        )
                );
    }

    @Test
    @SneakyThrows
    void writeBulkZeroSized() {
        tested.write(new byte[0], 0 , 0);

        tested.close();

        verify(executorService, never()).execute(any(Runnable.class));
        verify(s3).putObject(any(PutObjectRequest.class), requestBodyCaptor.capture());

        RequestBody capturedBody = requestBodyCaptor.getValue();
        byte[] capturedContent = toByteArray(capturedBody.contentStreamProvider().newStream());

        assertThat(capturedContent).isEmpty();
    }

    @Test
    @SneakyThrows
    void writeByteByByteNoChunked() {
        writeByteByByte(shortChunk);

        tested.close();

        verify(executorService, never()).execute(any(Runnable.class));
        verify(s3).putObject(any(PutObjectRequest.class), requestBodyCaptor.capture());

        RequestBody capturedBody = requestBodyCaptor.getValue();
        byte[] capturedContent = toByteArray(capturedBody.contentStreamProvider().newStream());

        assertThat(new String(capturedContent)).isEqualTo(new String(shortChunk));
    }
    @Test
    @SneakyThrows
    void writeByteByByteChunkedExactChunk() {
        writeByteByByte(exactOneMultipartChunk);

        tested.close();

        verify(s3, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(s3).uploadPart(uploadChunk.capture(), requestBodyCaptor.capture());
        assertThat(uploadChunk.getValue().partNumber()).isEqualTo(1);
        assertThat(requestBodyCaptor.getValue().contentStreamProvider().newStream()).hasContent(new String(exactOneMultipartChunk));
    }

    @Test
    @SneakyThrows
    void writeByteByByteChunked() {
        writeByteByByte(multipartChunkWithTail);

        tested.close();

        verify(s3, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(s3, times(2)).uploadPart(uploadChunk.capture(), requestBodyCaptor.capture());
        assertThat(uploadChunk.getAllValues()).hasSize(2);
        assertThat(requestBodyCaptor.getAllValues().get(0).contentStreamProvider().newStream())
                .hasContent(new String(Arrays.copyOfRange(multipartChunkWithTail, 0, BUFFER_SIZE)));

        // we are setting size parameter that limits number of bytes read by s3 client:
        long partialPartSizeLong = uploadChunk.getAllValues().get(1).contentLength();
        if (partialPartSizeLong > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Part size too large to fit in an int: " + partialPartSizeLong);
        }
        int partialPartSize = (int) partialPartSizeLong;

        byte[] partialChunk = new byte[partialPartSize];
        ByteStreams.readFully(requestBodyCaptor.getAllValues().get(1).contentStreamProvider().newStream(), partialChunk, 0, partialPartSize);
        assertThat(new String(partialChunk))
                .isEqualTo(new String(Arrays.copyOfRange(
                                multipartChunkWithTail, BUFFER_SIZE, multipartChunkWithTail.length)
                        )
                );
    }

    @Test
    @SneakyThrows
    void writeZeroSized() {
        tested.close();

        verify(executorService, never()).execute(any(Runnable.class));
        verify(s3).putObject(any(PutObjectRequest.class), requestBodyCaptor.capture());

        RequestBody capturedBody = requestBodyCaptor.getValue();
        byte[] capturedContent = toByteArray(capturedBody.contentStreamProvider().newStream());

        assertThat(new String(capturedContent)).isEmpty();
    }


    private void writeByteByByte(byte[] bytes) {
        for (byte b : bytes) {
            tested.write(b);
        }
    }

    private static byte[] randomBytes(int size) {
        byte[] data = new byte[size];
        new Random().nextBytes(data);
        return data;
    }
}
