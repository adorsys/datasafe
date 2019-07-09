package de.adorsys.datasafe.rest.impl.controller;

import de.adorsys.datasafe.business.impl.service.VersionedDatasafeServices;
import de.adorsys.datasafe.metainfo.version.api.version.VersionedPrivateSpaceService;
import de.adorsys.datasafe.metainfo.version.impl.version.latest.DefaultVersionInfoServiceImpl;
import de.adorsys.datasafe.metainfo.version.impl.version.types.DFSVersion;
import de.adorsys.datasafe.types.api.resource.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
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
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureRestDocs(uriHost = "example.com/datasafe", uriPort = 80)
public class VersionControllerTest extends BaseTokenDatasafeEndpointTest {
    @MockBean
    protected VersionedDatasafeServices versionedDatasafeServices;

    @MockBean
    private VersionedPrivateSpaceService versionedPrivateSpaceService;

    @MockBean
    private DefaultVersionInfoServiceImpl versionInfoService;

    @BeforeEach
    public void setup() {
        when(versionedDatasafeServices.latestPrivate()).thenReturn(versionedPrivateSpaceService);
        super.setup();
    }

    @SneakyThrows
    @Test
    void readVersionedDocumentTest() {
        when(versionedDatasafeServices.latestPrivate().read(any())).thenReturn(new ByteArrayInputStream("hello".getBytes()));

        RestDocumentationResultHandler document = document("versioned-read-success",
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
        mvc.perform(get("/versioned/{path}", path)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
                .accept(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(status().isOk())
                .andExpect(header().string(CONTENT_TYPE, APPLICATION_OCTET_STREAM_VALUE))
                .andDo(document);
        verify(versionedPrivateSpaceService).read(any());
    }

    @SneakyThrows
    @Test
    void writeVersionedDocumentTest() {
        when(versionedDatasafeServices.latestPrivate().write(any())).thenReturn(new ByteArrayOutputStream());

        RestDocumentationResultHandler document = document("versioned-write-success",
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
        mvc.perform(putFileBuilder("/versioned/{path}", path)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .content("file content".getBytes())
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
        )
                .andExpect(status().isOk())
                .andDo(document);
        verify(versionedPrivateSpaceService).write(any());
    }

    @SneakyThrows
    @Test
    void listVersionedDocumentsTest() {
        Uri location = new Uri("s3://bucket/user/path/to/file.txt");
        PrivateResource privateResource = new BasePrivateResource(location).resolve(new Uri("/path/to/file.txt"), new Uri("/path/to/file.txt"));
        BaseResolvedResource resolvedResource = new BaseResolvedResource(privateResource, Instant.now());
        AbsoluteLocation<PrivateResource> absoluteLocation = new AbsoluteLocation<>(privateResource);
        BaseVersionedPath<PrivateResource, ResolvedResource, Version> versionedPath =
                new BaseVersionedPath<>(new DFSVersion("1"), absoluteLocation, resolvedResource);

        when(versionedPrivateSpaceService.listWithDetails(any())).thenReturn(Stream.of(versionedPath));

        RestDocumentationResultHandler document = document("versioned-list-success",
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
        mvc.perform(get("/versioned/{path}", path)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
        ).andExpect(status().isOk())
                .andExpect(content().string("[\"/path/to/file.txt\"]"))
                .andDo(document);
        verify(versionedPrivateSpaceService).listWithDetails(any());
    }

    @SneakyThrows
    @Test
    void deleteDocumentTest() {
        RestDocumentationResultHandler document = document("versioned-remove-success",
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
        mvc.perform(delete("/versioned/{path}", path)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
        ).andExpect(status().isOk())
                .andDo(document);
        verify(versionedPrivateSpaceService).remove(any());
    }

    @SneakyThrows
    @Test
    void getVersionsTest() {
        when(versionedDatasafeServices.versionInfo()).thenReturn(versionInfoService);

        Uri location = new Uri("s3://bucket/user/path/to/file.txt");
        PrivateResource privateResource = new BasePrivateResource(location).resolve(new Uri("/path/to/file.txt"), new Uri("/path/to/file.txt"));
        BaseResolvedResource resolvedResource = new BaseResolvedResource(privateResource, Instant.now());
        AbsoluteLocation<PrivateResource> absoluteLocation = new AbsoluteLocation<>(privateResource);
        BaseVersionedPath<PrivateResource, ResolvedResource, Version> versionedPath =
                new BaseVersionedPath<>(new DFSVersion("1"), absoluteLocation, resolvedResource);


        when(versionedPrivateSpaceService.listWithDetails(any())).thenReturn(Stream.of(versionedPath));

        RestDocumentationResultHandler document = document("versions-list-success",
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
        mvc.perform(get("/versions/list/{path}", path)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("user", TEST_USER)
                .header("password", TEST_PASS)
                .header("token", token)
        ).andExpect(status().isOk())
                .andExpect(content().string("[]"))
                .andDo(document);;
        verify(versionInfoService).versionsOf(any());
    }
}
