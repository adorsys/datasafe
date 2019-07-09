package de.adorsys.datasafe.rest.impl.controller;

import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.privatestore.impl.PrivateSpaceServiceImpl;
import de.adorsys.datasafe.types.api.resource.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.stream.Stream;

import static de.adorsys.datasafe.rest.impl.controller.TestHelper.putFileBuilder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureRestDocs(uriHost = "example.com/datasafe", uriPort = 80)
class DocumentControllerTest extends BaseTokenDatasafeEndpointTest {

    @MockBean
    protected DefaultDatasafeServices dataSafeService;

    @MockBean
    private PrivateSpaceServiceImpl privateSpaceService;

    @BeforeEach
    public void setup() {
        when(dataSafeService.privateService()).thenReturn(privateSpaceService);
        super.setup();
    }

    @SneakyThrows
    @Test
    void readDocumentTest() {
        when(dataSafeService.privateService().read(any())).thenReturn(new ByteArrayInputStream("file content".getBytes()));

        RestDocumentationResultHandler document = document("document-read-success",
                pathParameters(
                        parameterWithName("path").description("path to the file")
                ),
                requestHeaders(
                        headerWithName("token").description("Bearer authentication token is required"),
                        headerWithName("user").description("datasafe username"),
                        headerWithName("password").description("datasafe user's password")
                )
        );

        String path = "path/to/file";
        mvc.perform(get("/document/{path}", path)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
                .accept(APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(status().isOk())
                .andExpect(header().string(CONTENT_TYPE, APPLICATION_OCTET_STREAM_VALUE))
                .andDo(document);
        verify(privateSpaceService).read(any());
    }

    @SneakyThrows
    @Test
    void writeDocumentTest() {
        when(dataSafeService.privateService().write(any())).thenReturn(new ByteArrayOutputStream());

        RestDocumentationResultHandler document = document("document-write-success",
                pathParameters(
                        parameterWithName("path").description("path to the file")
                ),
                requestHeaders(
                        headerWithName("token").description("Bearer authentication token is required"),
                        headerWithName("user").description("datasafe username"),
                        headerWithName("password").description("datasafe user's password")
                )
        );
        String path = "path/to/file";
        mvc.perform(putFileBuilder("/document/{path}", path)
                .content(new MockMultipartFile("file", path.getBytes()).getBytes())
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
        )
                .andExpect(status().isOk())
                .andDo(document);
        verify(privateSpaceService).write(any());
    }

    @SneakyThrows
    @Test
    void listDocumentsTest() {
        Uri location = new Uri("s3://bucket/user/path/to/file.txt");
        PrivateResource privateResource = new BasePrivateResource(location).resolve(new Uri("/path/to/file.txt"), new Uri("/path/to/file.txt"));
        AbsoluteLocation<ResolvedResource> resolvedPrivate = new AbsoluteLocation<>(new BaseResolvedResource(privateResource, Instant.now()));
        when(dataSafeService.privateService().list(any())).thenReturn(Stream.of(resolvedPrivate));

        RestDocumentationResultHandler document = document("document-list-success",
                pathParameters(
                        parameterWithName("path").description("path to the file")
                ),
                requestHeaders(
                        headerWithName("token").description("Bearer authentication token is required"),
                        headerWithName("user").description("datasafe username"),
                        headerWithName("password").description("datasafe user's password")
                )
        );

        String path = "";
        mvc.perform(get("/documents/{path}", path)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
        ).andExpect(status().isOk())
                .andDo(document);
        verify(privateSpaceService).list(any());
    }

    @SneakyThrows
    @Test
    void removeDocumentTest() {
        RestDocumentationResultHandler document = document("document-remove-success",
                pathParameters(
                        parameterWithName("path").description("path to the file")
                ),
                requestHeaders(
                        headerWithName("token").description("Bearer authentication token is required"),
                        headerWithName("user").description("datasafe username"),
                        headerWithName("password").description("datasafe user's password")
                )
        );

        String path = "path/to/file";
        mvc.perform(delete("/document/{path}", path)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
        ).andExpect(status().isOk())
                .andDo(document);
        verify(privateSpaceService).remove(any());
    }
}
