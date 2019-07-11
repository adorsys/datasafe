package de.adorsys.datasafe.rest.impl.controller;

import com.google.gson.Gson;
import de.adorsys.datasafe.business.impl.service.VersionedDatasafeServices;
import de.adorsys.datasafe.directory.impl.profile.operations.DFSBasedProfileStorageImpl;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.rest.impl.dto.UserDTO;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends BaseTokenDatasafeEndpointTest {
    private Gson gson;

    @MockBean
    protected VersionedDatasafeServices dataSafeService;

    @MockBean
    private DFSBasedProfileStorageImpl userProfile;


    @BeforeEach
    public void setup() {
        when(dataSafeService.userProfile()).thenReturn(userProfile);
        gson = new Gson();
    }

    @SneakyThrows
    @Test
    void createUserTest() {
        UserDTO request = new UserDTO("testUser", "testPassword");

        RestDocumentationResultHandler document = document("user-create-success",
                requestHeaders(headerWithName("token").description(TOKEN_DESCRIPTION)),
                requestFields(
                        fieldWithPath("userName").description("Name of user to create "),
                        fieldWithPath("password").description("Password of user")
                ));

        mvc.perform(put("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(getLoginData())
                .header("token", token)
                .content(gson.toJson(request))
        )
                .andExpect(status().isOk())
                .andDo(document);
        verify(userProfile).registerUsingDefaults(any());
    }

    @SneakyThrows
    @Test
    void createDuplicateUserTest() {

        when(dataSafeService.userProfile().userExists(new UserID(TEST_USER))).thenReturn(true);

        //Validate the response as de.adorsys.datasafe.rest.impl.exceptions.UserExistsException
        mvc.perform(put("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(getLoginData())
                .header("token", token)
        ).andExpect(status().isBadRequest());

    }

    @SneakyThrows
    @Test
    void deleteUserTest() {
        RestDocumentationResultHandler document = document("user-delete-success",
                requestHeaders(
                        headerWithName("token").description(TOKEN_DESCRIPTION),
                        headerWithName("user").description(USER_DESCRIPTION),
                        headerWithName("password").description(PASSWORD_DESCRIPTION)
                ));

        when(dataSafeService.userProfile().userExists(any())).thenReturn(true);

        mvc.perform(delete("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
        )
                .andExpect(status().isOk())
                .andDo(document);
        verify(userProfile).deregister(any());
    }

    @SneakyThrows
    @Test
    void deleteNonExistenceUserTest() {
        when(dataSafeService.userProfile().userExists(new UserID(TEST_USER))).thenReturn(false);

        //Validate the response as de.adorsys.datasafe.rest.impl.exceptions.UserDoesNotExistsException
        mvc.perform(delete("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
        ).andExpect(status().isNotFound());
    }

    public String getLoginData() {
        return "{ \"userName\" : \"" + TEST_USER + "\" , \"password\" : \"" + TEST_PASS + "\" }";
    }
}
