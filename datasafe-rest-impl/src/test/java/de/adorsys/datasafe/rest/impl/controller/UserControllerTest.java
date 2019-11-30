package de.adorsys.datasafe.rest.impl.controller;

import com.google.gson.Gson;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.api.types.StorageCredentials;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPublicProfile;
import de.adorsys.datasafe.directory.impl.profile.operations.DFSBasedProfileStorageImpl;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.rest.impl.dto.UserDTO;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends BaseTokenDatasafeEndpointTest {
    private Gson gson;

    @MockBean
    protected DefaultDatasafeServices dataSafeService;

    @MockBean
    private DFSBasedProfileStorageImpl userProfile;

    @Captor
    private ArgumentCaptor<ReadKeyPassword> password;

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
    void changePasswordTest() {
        String newPassword = "NEW!";

        mvc.perform(post("/user/password")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content("{\"newPassword\": \"" + newPassword + "\"}")
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
        )
                .andExpect(status().isOk());

        verify(userProfile).updateReadKeyPassword(
                eq(new UserIDAuth(TEST_USER, TEST_PASS)),
                password.capture()
        );

        assertThat(password.getValue().getValue()).isEqualTo(newPassword.toCharArray());
    }

    @SneakyThrows
    @Test
    void getPrivateProfileTest() {
        when(userProfile.privateProfile(eq(new UserIDAuth(TEST_USER, TEST_PASS))))
            .thenReturn(mock(UserPrivateProfile.class));

        mvc.perform(get("/user/privateProfile")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .header("user", TEST_USER)
            .header("password", TEST_PASS)
            .header("token", token)
        )
            .andExpect(status().isOk());

        verify(userProfile).privateProfile(
            eq(new UserIDAuth(TEST_USER, TEST_PASS))
        );
    }

    @SneakyThrows
    @Test
    void getPublicProfileTest() {
        when(userProfile.publicProfile(eq(new UserID(TEST_USER))))
            .thenReturn(mock(UserPublicProfile.class));

        mvc.perform(get("/user/publicProfile")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .header("user", TEST_USER)
            .header("password", TEST_PASS)
            .header("token", token)
        )
            .andExpect(status().isOk());

        verify(userProfile).publicProfile(
            eq(new UserID(TEST_USER))
        );
    }

    @SneakyThrows
    @Test
    void changePrivateProfileTest() {
        mvc.perform(post("/user/privateProfile")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(Fixture.read("endpoints/private_profile.json"))
            .header("user", TEST_USER)
            .header("password", TEST_PASS)
            .header("token", token)
        )
            .andExpect(status().isOk());

        verify(userProfile).updatePrivateProfile(
            eq(new UserIDAuth(TEST_USER, TEST_PASS)),
            any()
        );
    }

    @SneakyThrows
    @Test
    void changePublicProfileTest() {
        mvc.perform(post("/user/publicProfile")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(Fixture.read("endpoints/public_profile.json"))
            .header("user", TEST_USER)
            .header("password", TEST_PASS)
            .header("token", token)
        )
            .andExpect(status().isOk());

        verify(userProfile).updatePublicProfile(
            eq(new UserIDAuth(TEST_USER, TEST_PASS)),
            any()
        );
    }

    @SneakyThrows
    @Test
    void addStorageCredentialsTest() {
        mvc.perform(post("/user/storages")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(Fixture.read("endpoints/storage_creds.json"))
            .header("user", TEST_USER)
            .header("password", TEST_PASS)
            .header("token", token)
        )
            .andExpect(status().isOk());

        verify(userProfile).registerStorageCredentials(
            eq(new UserIDAuth(TEST_USER, TEST_PASS)),
            eq(new StorageIdentifier("AAA")),
            eq(new StorageCredentials("FOO", "BAR"))
        );
    }

    @SneakyThrows
    @Test
    void removeStorageCredentialsTest() {
        mvc.perform(delete("/user/storages")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content("{}")
            .header("user", TEST_USER)
            .header("password", TEST_PASS)
            .header("token", token)
            .header("storageId", "111")
        )
            .andExpect(status().isOk());

        verify(userProfile).deregisterStorageCredentials(
            eq(new UserIDAuth(TEST_USER, TEST_PASS)),
            eq(new StorageIdentifier("111"))
        );
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
