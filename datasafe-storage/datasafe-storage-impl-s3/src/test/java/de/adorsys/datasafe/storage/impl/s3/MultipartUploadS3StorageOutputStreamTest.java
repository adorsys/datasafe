package de.adorsys.datasafe.storage.impl.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.testcontainers.shaded.com.google.common.io.ByteStreams;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import static de.adorsys.datasafe.storage.impl.s3.MultipartUploadS3StorageOutputStream.BUFFER_SIZE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MultipartUploadS3StorageOutputStreamTest extends BaseMockitoTest {

    private final byte[] shortChunk = randomBytes(100);
    private final byte[] exactOneMultipartChunk = randomBytes(BUFFER_SIZE);
    private final byte[] multipartChunkWithTail = randomBytes(BUFFER_SIZE + 100);

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private ExecutorService executorService;

    @Captor
    private ArgumentCaptor<InputStream> bytesSentDirectly;

    @Captor
    private ArgumentCaptor<UploadPartRequest> uploadChunk;

    private MultipartUploadS3StorageOutputStream tested;

    @BeforeEach
    void init() {
        tested = new MultipartUploadS3StorageOutputStream(
                "bucket",
                "s3://path/to/file.txt",
                amazonS3,
                executorService,
                Collections.emptyList()
        );

        when(amazonS3.putObject(anyString(), anyString(), bytesSentDirectly.capture(), any()))
                .thenReturn(new PutObjectResult());
        when(amazonS3.initiateMultipartUpload(any())).thenReturn(new InitiateMultipartUploadResult());
        doAnswer(inv -> {
            inv.getArgument(0, Runnable.class).run();
            return null;
        }).when(executorService).execute(any());
        when(amazonS3.uploadPart(uploadChunk.capture())).thenReturn(new UploadPartResult());
        when(amazonS3.completeMultipartUpload(any())).thenReturn(new CompleteMultipartUploadResult());
    }

    @Test
    @SneakyThrows
    void writeBulkNonChunked() {
        tested.write(shortChunk, 0 , shortChunk.length);

        tested.close();

        verify(executorService, never()).submit(any(UploadChunkResultCallable.class));
        assertThat(bytesSentDirectly.getValue()).hasContent(new String(shortChunk));
    }

    @Test
    @SneakyThrows
    void writeBulkNonChunkedWithOffset() {
        tested.write(shortChunk, 10 , shortChunk.length - 10);

        tested.close();

        verify(executorService, never()).submit(any(UploadChunkResultCallable.class));
        assertThat(bytesSentDirectly.getValue()).hasContent(
                new String(Arrays.copyOfRange(shortChunk, 10, shortChunk.length))
        );
    }

    @Test
    @SneakyThrows
    void writeBulkChunkedExactlyOne() {
        tested.write(exactOneMultipartChunk, 0 , exactOneMultipartChunk.length);

        tested.close();

        assertThat(bytesSentDirectly.getAllValues()).isEmpty();
        assertThat(uploadChunk.getAllValues()).hasSize(1);
    }

    @Test
    @SneakyThrows
    void writeBulkChunked() {
        tested.write(multipartChunkWithTail, 0 , multipartChunkWithTail.length);

        tested.close();

        assertThat(bytesSentDirectly.getAllValues()).isEmpty();
        assertThat(uploadChunk.getAllValues()).hasSize(2);
        assertThat(uploadChunk.getAllValues().get(0).getInputStream())
                .hasContent(new String(Arrays.copyOfRange(multipartChunkWithTail, 0, BUFFER_SIZE)));
        assertThat(uploadChunk.getAllValues().get(1).getInputStream())
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

        assertThat(bytesSentDirectly.getAllValues()).isEmpty();
        assertThat(uploadChunk.getAllValues()).hasSize(2);
        assertThat(uploadChunk.getAllValues().get(0).getInputStream())
                .hasContent(new String(Arrays.copyOfRange(multipartChunkWithTail, 10, 10 + BUFFER_SIZE)));
        assertThat(uploadChunk.getAllValues().get(1).getInputStream())
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

        verify(executorService, never()).submit(any(UploadChunkResultCallable.class));
        assertThat(bytesSentDirectly.getValue()).hasContent("");
    }

    @Test
    @SneakyThrows
    void writeByteByByteNoChunked() {
        writeByteByByte(shortChunk);

        tested.close();

        verify(executorService, never()).submit(any(UploadChunkResultCallable.class));
        assertThat(bytesSentDirectly.getValue()).hasContent(new String(shortChunk));
    }

    @Test
    @SneakyThrows
    void writeByteByByteChunkedExactChunk() {
        writeByteByByte(exactOneMultipartChunk);

        tested.close();

        assertThat(bytesSentDirectly.getAllValues()).isEmpty();
        assertThat(uploadChunk.getAllValues()).hasSize(1);
        assertThat(uploadChunk.getAllValues().get(0).getInputStream()).hasContent(new String(exactOneMultipartChunk));
    }

    @Test
    @SneakyThrows
    void writeByteByByteChunked() {
        writeByteByByte(multipartChunkWithTail);

        tested.close();

        assertThat(bytesSentDirectly.getAllValues()).isEmpty();
        assertThat(uploadChunk.getAllValues()).hasSize(2);
        assertThat(uploadChunk.getAllValues().get(0).getInputStream())
                .hasContent(new String(Arrays.copyOfRange(multipartChunkWithTail, 0, BUFFER_SIZE)));

        // we are setting size parameter that limits number of bytes read by s3 client:
        int partialPartSize = (int) uploadChunk.getAllValues().get(1).getPartSize();
        byte[] partialChunk = new byte[partialPartSize];
        ByteStreams.readFully(uploadChunk.getAllValues().get(1).getInputStream(), partialChunk, 0, partialPartSize);
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

        verify(executorService, never()).submit(any(UploadChunkResultCallable.class));
        assertThat(bytesSentDirectly.getValue()).hasContent("");
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
