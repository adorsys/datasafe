package de.adorsys.datasafe.rest.impl.controller;

import de.adorsys.datasafe.business.impl.service.VersionedDatasafeServices;
import de.adorsys.datasafe.directory.impl.profile.operations.DFSBasedProfileStorageImpl;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends BaseTokenDatasafeEndpointTest {

    @MockBean
    protected VersionedDatasafeServices dataSafeService;

    @MockBean
    private DFSBasedProfileStorageImpl userProfile;

    @BeforeEach
    public void setup() {
        when(dataSafeService.userProfile()).thenReturn(userProfile);
    }

    @SneakyThrows
    @Test
    void createUserTest() {

        mvc.perform(put("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content("{ \"userName\" : \"" + TEST_USER + "\" , \"password\" : \"" + TEST_PASS + "\" }")
                .header("token", token)
        )
                .andExpect(status().isOk());
        verify(userProfile).registerUsingDefaults(any());
    }


    @SneakyThrows
    @Test
    void deleteUserTest() {

        mvc.perform(delete("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
        )
                .andExpect(status().isOk());
        verify(userProfile).deregister(any());
    }
}
