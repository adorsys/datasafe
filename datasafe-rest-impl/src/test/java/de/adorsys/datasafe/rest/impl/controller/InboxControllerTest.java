package de.adorsys.datasafe.rest.impl.controller;

import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.inbox.impl.InboxServiceImpl;
import lombok.SneakyThrows;
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
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InboxControllerTest extends BaseTokenDatasafeEndpointTest {

    private static final String TEST_PATH = "test.txt";

    @MockBean
    protected DefaultDatasafeServices dataSafeService;

    @MockBean
    private InboxServiceImpl inboxService;

    @BeforeEach
    public void setup() {
        when(dataSafeService.inboxService()).thenReturn(inboxService);
        super.setup();
    }

    @SneakyThrows
    @Test
    void writeToInboxTest() {
        when(dataSafeService.inboxService().write(any())).thenReturn(new ByteArrayOutputStream());

        mvc.perform(putFileBuilder("/inbox/document/{path}", TEST_PATH)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .header("users", TEST_USER)
                .header("token", token)
        )
                .andExpect(status().isOk());
        verify(inboxService).write(any());
    }

    @SneakyThrows
    @Test
    void readFromInboxTest() {
        when(dataSafeService.inboxService().read(any())).thenReturn(new ByteArrayInputStream("hello".getBytes()));

        mvc.perform(get("/inbox/document/{path}", TEST_PATH)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
                .accept(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(status().isOk())
                .andExpect(header().string(CONTENT_TYPE, APPLICATION_OCTET_STREAM_VALUE));
        verify(inboxService).read(any());
    }

    @SneakyThrows
    @Test
    void listInboxTest() {
        mvc.perform(get("/inbox/documents/{path}", TEST_PATH)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
        verify(inboxService).list(any());
    }

    @SneakyThrows
    @Test
    void removeFromInboxTest() {

        mvc.perform(delete("/inbox/document/{path}", TEST_PATH)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
        ).andExpect(status().isOk());
        verify(inboxService).remove(any());
    }
}
