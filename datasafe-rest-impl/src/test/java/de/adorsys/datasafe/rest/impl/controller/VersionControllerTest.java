package de.adorsys.datasafe.rest.impl.controller;

import de.adorsys.datasafe.business.impl.service.VersionedDatasafeServices;
import de.adorsys.datasafe.metainfo.version.api.version.VersionedPrivateSpaceService;
import de.adorsys.datasafe.metainfo.version.impl.version.latest.DefaultVersionInfoServiceImpl;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static de.adorsys.datasafe.rest.impl.controller.TestHelper.putFileBuilder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
public class VersionControllerTest extends BaseTokenDatasafeEndpointTest {
    @MockBean
    protected VersionedDatasafeServices versionedDatasafeServices;

    @MockBean
    private VersionedPrivateSpaceService versionedPrivateSpaceService;

    @MockBean
    private DefaultVersionInfoServiceImpl versionInfoService;

    @BeforeEach
    public void setup() {
        when(versionedDatasafeServices.latestPrivate()).thenReturn(versionedPrivateSpaceService);
        super.setup();
    }

    @SneakyThrows
    @Test
    void readVersionedDocumentTest() {
        when(versionedDatasafeServices.latestPrivate().read(any())).thenReturn(new ByteArrayInputStream("hello".getBytes()));

        String path = "path/to/file";
        mvc.perform(get("/versioned/{path}", path)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
                .accept(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(status().isOk())
                .andExpect(header().string(CONTENT_TYPE, APPLICATION_OCTET_STREAM_VALUE));
        verify(versionedPrivateSpaceService).read(any());
    }

    @SneakyThrows
    @Test
    void writeVersionedDocumentTest() {
        when(versionedDatasafeServices.latestPrivate().write(any())).thenReturn(new ByteArrayOutputStream());
        String path = "path/to/file";
        mvc.perform(putFileBuilder("/versioned/{path}", path)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
        )
                .andExpect(status().isOk());
        verify(versionedPrivateSpaceService).write(any());
    }

    @SneakyThrows
    @Test
    void listVersionedDocumentsTest() {
        String path = "";
        mvc.perform(get("/versioned/{path}", path)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
        ).andExpect(status().isOk());
        verify(versionedPrivateSpaceService).listWithDetails(any());
    }

    @SneakyThrows
    @Test
    void deleteDocumentTest() {
        String path = "path/to/file";
        mvc.perform(delete("/versioned/{path}", path)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
        ).andExpect(status().isOk());
        verify(versionedPrivateSpaceService).remove(any());
    }

    @SneakyThrows
    @Test
    void getVersionsTest() {
        when(versionedDatasafeServices.versionInfo()).thenReturn(versionInfoService);
        String path = "path/to/file";
        mvc.perform(get("/versions/list/{path}", path)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
        ).andExpect(status().isOk());
        verify(versionInfoService).versionsOf(any());
    }
}
