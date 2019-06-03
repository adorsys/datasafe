package de.adorsys.datasafe.rest.controller;

import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.impl.profile.operations.DFSBasedProfileStorageImpl;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    private static final String TEST_USER = "test";
    private static final String TEST_PASS = "test";

    @Autowired
    MockMvc mvc;

    @MockBean
    DefaultDatasafeServices dataSafeService;

    @MockBean
    DFSBasedProfileStorageImpl userProfile;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(dataSafeService.userProfile()).thenReturn(userProfile);

    }

    @SneakyThrows
    @Test
    public void createUserTest() {

        mvc.perform(put("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content("{ \"userName\" : \"" + TEST_USER + "\" , \"password\" : \"" + TEST_PASS + "\" }")
        )
                .andExpect(status().isOk());
    }


    @SneakyThrows
    @Test
    public void deleteUserTest() {

        mvc.perform(delete("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
        )
                .andExpect(status().isOk());
    }
}
