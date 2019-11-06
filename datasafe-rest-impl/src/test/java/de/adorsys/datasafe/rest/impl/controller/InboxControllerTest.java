package de.adorsys.datasafe.rest.impl.controller;

import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.inbox.impl.InboxServiceImpl;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.BaseResolvedResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.stream.Stream;

import static de.adorsys.datasafe.rest.impl.controller.TestHelper.putFileBuilder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
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

        RestDocumentationResultHandler document = document("inbox-write-success",
                pathParameters(
                        parameterWithName("path").description("path to the file")
                ),
                requestHeaders(
                        headerWithName("token").description(TOKEN_DESCRIPTION),
                        headerWithName("users").description("recipients array")
                )
        );

        mvc.perform(putFileBuilder("/inbox/document/{path}", TEST_PATH)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .header("users", TEST_USER)
                .header("token", token)
        )
                .andExpect(status().isOk())
                .andDo(document);
        verify(inboxService).write(any());
    }

    @SneakyThrows
    @Test
    void readFromInboxTest() {
        when(dataSafeService.inboxService().read(any())).thenReturn(new ByteArrayInputStream("hello".getBytes()));

        RestDocumentationResultHandler document = document("inbox-read-success",
                pathParameters(
                        parameterWithName("path").description("path to the file")
                ),
                requestHeaders(
                        headerWithName("token").description(TOKEN_DESCRIPTION),
                        headerWithName("user").description(USER_DESCRIPTION),
                        headerWithName("password").description(PASSWORD_DESCRIPTION)
                )
        );

        when(dataSafeService.inboxService().read(any())).thenReturn(new ByteArrayInputStream("hello".getBytes()));

        mvc.perform(get("/inbox/document/{path}", TEST_PATH)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
                .accept(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(status().isOk())
                .andExpect(header().string(CONTENT_TYPE, APPLICATION_OCTET_STREAM_VALUE))
                .andDo(document);
        verify(inboxService).read(any());
    }

    @SneakyThrows
    @Test
    void removeFromInboxTest() {
        RestDocumentationResultHandler document = document("inbox-remove-success",
                pathParameters(
                        parameterWithName("path").description("path to the file")
                ),
                requestHeaders(
                        headerWithName("token").description(TOKEN_DESCRIPTION),
                        headerWithName("user").description(USER_DESCRIPTION),
                        headerWithName("password").description(PASSWORD_DESCRIPTION)
                )
        );

        mvc.perform(delete("/inbox/document/{path}", TEST_PATH)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
        ).andExpect(status().isOk())
                .andDo(document);
        verify(inboxService).remove(any());
    }

    @SneakyThrows
    @Test
    void listInboxTest() {
        Uri location = new Uri("s3://bucket/user/inbox/path/to/file.txt");
        PrivateResource privateResource = new BasePrivateResource(location).resolve(new Uri("/path/to/file.txt"), new Uri("/path/to/file.txt"));
        AbsoluteLocation<ResolvedResource> resolvedPrivate = new AbsoluteLocation<>(new BaseResolvedResource(privateResource, Instant.now()));
        when(inboxService.list(any())).thenReturn(Stream.of(resolvedPrivate));

        RestDocumentationResultHandler document = document("inbox-list-success",
                pathParameters(
                        parameterWithName("path").description("path to the file")
                ),
                requestHeaders(
                        headerWithName("token").description(TOKEN_DESCRIPTION),
                        headerWithName("user").description(USER_DESCRIPTION),
                        headerWithName("password").description(PASSWORD_DESCRIPTION)
                )
        );

        mvc.perform(get("/inbox/documents/{path}", TEST_PATH)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(document);
        verify(inboxService).list(any());
    }
}
