package de.adorsys.datasafe.rest.impl.controller;

import de.adorsys.datasafe.business.impl.service.VersionedDatasafeServices;
import de.adorsys.datasafe.directory.impl.profile.operations.DFSBasedProfileStorageImpl;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.web.util.NestedServletException;

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
                .content(getLoginData())
                .header("token", token)
        )
                .andExpect(status().isOk());
        verify(userProfile).registerUsingDefaults(any());
    }

    @SneakyThrows
    @Test
    void createDuplicateUserTest() {

        when(dataSafeService.userProfile().userExists(new UserID(TEST_USER))).thenReturn(true);

        //Validate the response as de.adorsys.datasafe.rest.impl.exceptions.UserExistsException
        Assertions.assertThrows(NestedServletException.class, () -> {
            mvc.perform(put("/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .characterEncoding("UTF-8")
                    .content(getLoginData())
                    .header("token", token)
            );
        });
    }

    @SneakyThrows
    @Test
    void deleteUserTest() {
        if(userProfile.userExists(new UserID(TEST_USER))) {
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

    @SneakyThrows
    @Test
    void deleteNonExistenceUserTest() {
        when(dataSafeService.userProfile().userExists(new UserID(TEST_USER))).thenReturn(false);

        //Validate the response as de.adorsys.datasafe.rest.impl.exceptions.UserDoesNotExistsException
        Assertions.assertThrows(NestedServletException.class, () -> {
            mvc.perform(delete("/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("user", TEST_USER)
                    .header("password", TEST_PASS)
                    .header("token", token)
            );
        });
    }

    public String getLoginData(){
        return "{ \"userName\" : \"" + TEST_USER + "\" , \"password\" : \"" + TEST_PASS + "\" }";
    }
}
