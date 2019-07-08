package de.adorsys.datasafe.rest.impl.controller;

import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.privatestore.impl.PrivateSpaceServiceImpl;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static de.adorsys.datasafe.rest.impl.controller.TestHelper.putFileBuilder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
class DocumentControllerTest extends BaseTokenDatasafeEndpointTest {

    @MockBean
    protected DefaultDatasafeServices dataSafeService;

    @MockBean
    private PrivateSpaceServiceImpl privateSpaceService;

    @BeforeEach
    public void setup() {
        when(dataSafeService.privateService()).thenReturn(privateSpaceService);
        super.setup();
    }

    @SneakyThrows
    @Test
    void readDocumentTest() {
        when(dataSafeService.privateService().read(any())).thenReturn(new ByteArrayInputStream("hello".getBytes()));

        String path = "path/to/file";
        mvc.perform(get("/document/{path}", path)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
                .accept(APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(status().isOk())
                .andExpect(header().string(CONTENT_TYPE, APPLICATION_OCTET_STREAM_VALUE));
        verify(privateSpaceService).read(any());
    }

    @SneakyThrows
    @Test
    void writeDocumentTest() {
        when(dataSafeService.privateService().write(any())).thenReturn(new ByteArrayOutputStream());
        String path = "path/to/file";
        mvc.perform(putFileBuilder("/document/{path}", path)
                .content(new MockMultipartFile("file", path.getBytes()).getBytes())
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
        )
                .andExpect(status().isOk());
        verify(privateSpaceService).write(any());
    }

    @SneakyThrows
    @Test
    void listDocumentsTest() {
        String path = "";
        mvc.perform(get("/documents/{path}", path)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
        ).andExpect(status().isOk());
        verify(privateSpaceService).list(any());
    }

    @SneakyThrows
    @Test
    void removeDocumentTest() {
        String path = "path/to/file";
        mvc.perform(delete("/document/{path}", path)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
        ).andExpect(status().isOk());
        verify(privateSpaceService).remove(any());
    }
}
