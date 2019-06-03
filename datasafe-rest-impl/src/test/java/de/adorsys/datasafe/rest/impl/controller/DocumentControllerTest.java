package de.adorsys.datasafe.rest.impl.controller;

import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.privatestore.impl.PrivateSpaceServiceImpl;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class DocumentControllerTest {

    private static final String TEST_USER = "test";
    private static final String TEST_PASS = "test";

    @Autowired
    MockMvc mvc;

    @MockBean
    DefaultDatasafeServices dataSafeService;

    @MockBean
    private PrivateSpaceServiceImpl privateSpaceService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        when(dataSafeService.privateService()).thenReturn(privateSpaceService);
    }


    @SneakyThrows
    @Test
    void readDocumentTest() {
        when(dataSafeService.privateService().read(any())).thenReturn(new ByteArrayInputStream("hello".getBytes()));

        String path = "path/to/file";
        mvc.perform(get("/document/{path}", path)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .accept(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    void writeDocumentTest() {
        when(dataSafeService.privateService().write(any())).thenReturn(new ByteArrayOutputStream());
        String path = "path/to/file";
        mvc.perform(put("/document/{path}", path)
                .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
        )
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    void listDocumentsTest() {
        String path = "";
        mvc.perform(get("/documents/{path}", path)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
        ).andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    void deleteDocumentTest() {
        String path = "path/to/file";
        mvc.perform(delete("/document/{path}", path)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
        ).andExpect(status().isOk());
    }
}
