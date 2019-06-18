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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

        mvc.perform(put("/inbox/{path}", TEST_PATH)
                .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .header("user", TEST_USER)
                .header("token", token)
        )
                .andExpect(status().isOk());
        verify(inboxService).write(any());
    }

    @SneakyThrows
    @Test
    void readFromInboxTest() {
        when(dataSafeService.inboxService().read(any())).thenReturn(new ByteArrayInputStream("hello".getBytes()));

        mvc.perform(get("/inbox/{path}", TEST_PATH)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
                .accept(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(status().isOk());
        verify(inboxService).read(any());
    }

    @SneakyThrows
    @Test
    void removeFromInboxTest() {

        mvc.perform(delete("/inbox/{path}", TEST_PATH)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
        ).andExpect(status().isOk());
        verify(inboxService).remove(any());
    }
}
