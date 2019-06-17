package de.adorsys.datasafe.rest.impl.controller;

import de.adorsys.datasafe.business.impl.service.VersionedDatasafeServices;
import de.adorsys.datasafe.metainfo.version.api.version.VersionedPrivateSpaceService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
public class VersionControllerTest extends BaseTokenDatasafeEndpointTest {
    @MockBean
    protected VersionedDatasafeServices versionedDatasafeServices;

    @MockBean
    private VersionedPrivateSpaceService versionedPrivateSpaceService;

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
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    void writeVersionedDocumentTest() {
        when(versionedDatasafeServices.latestPrivate().write(any())).thenReturn(new ByteArrayOutputStream());
        String path = "path/to/file";
        mvc.perform(put("/versioned/{path}", path)
                .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
        )
                .andExpect(status().isOk());
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
    }
}
