package de.adorsys.datasafe.rest.impl.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.rest.impl.dto.UserDTO;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public abstract class BaseDatasafeEndpointTest extends BaseMockitoTest {

    @Autowired
    protected MockMvc mvc;

    @MockBean
    protected AmazonS3 s3;

    @MockBean
    protected StorageService storageService;

    @MockBean
    protected DFSConfig dfsConfig;

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    @SneakyThrows
    MvcResult sendAuthenticateRequest(UserDTO userDTO) {
        return sendAuthenticateRequestWithStatus(userDTO, status().isOk());
    }

    @SneakyThrows
    MvcResult sendAuthenticateRequestWithStatus(UserDTO userDTO, ResultMatcher statusMatcher) {
        return this.mvc
                .perform(post("/api/authenticate").
                        content(jsonMapper.writeValueAsString(userDTO)).
                        contentType(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(statusMatcher)
                .andReturn();
    }

}
