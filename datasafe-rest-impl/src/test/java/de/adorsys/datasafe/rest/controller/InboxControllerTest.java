package de.adorsys.datasafe.rest.controller;

import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.inbox.impl.InboxServiceImpl;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class InboxControllerTest {

    private static final String TEST_USER = "test";
    private static final String TEST_PASS = "test";
    private static final String TEST_PATH = "test.txt";

    @Autowired
    MockMvc mvc;

    @MockBean
    DefaultDatasafeServices dataSafeService;

    @MockBean
    private InboxServiceImpl inboxService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(dataSafeService.inboxService()).thenReturn(inboxService);
    }

    @SneakyThrows
    @Test
    public void sendDocumentToInboxTest() {
        when(dataSafeService.inboxService().write(any())).thenReturn(new ByteArrayOutputStream());

        mvc.perform(put("/inbox/{path}", TEST_PATH)
                .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .header("user", TEST_USER)
        )
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    public void readFromInboxTest() {
        when(dataSafeService.inboxService().read(any())).thenReturn(new ByteArrayInputStream("hello".getBytes()));

        mvc.perform(get("/inbox/{path}", TEST_PATH)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .accept(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    public void deleteFromInboxTest() {

        mvc.perform(delete("/inbox/{path}", TEST_PATH)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
        ).andExpect(status().isOk());
    }
}
