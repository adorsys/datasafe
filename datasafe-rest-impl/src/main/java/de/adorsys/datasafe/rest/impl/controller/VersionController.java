package de.adorsys.datasafe.rest.impl.controller;

import de.adorsys.datasafe.business.impl.service.VersionedDatasafeServices;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.metainfo.version.impl.version.types.DFSVersion;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.RemoveRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.*;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
public class VersionController {

    private final VersionedDatasafeServices versionedDatasafeServices;

    /**
     * lists latest versions of files in user's private space.
     */
    @GetMapping(value = "/versioned/{path:.*}", produces = APPLICATION_JSON_VALUE)
    public List<String> listVersionedDocuments(@RequestHeader String user,
                                      @RequestHeader String password,
                                      @PathVariable(required = false) String path) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), new ReadKeyPassword(password));
        path = Optional.ofNullable(path).orElse("./");
        List<String> documentList = versionedDatasafeServices.latestPrivate().listWithDetails(ListRequest.forDefaultPrivate(userIDAuth, path))
                .map(e -> e.absolute().getResource().decryptedPath().asString())
                .collect(Collectors.toList());
        log.debug("List for path {} returned {} items", path, documentList.size());
        return documentList;
    }

    /**
     * reads latest version of file from user's private space.
     */
    @SneakyThrows
    @GetMapping(value = "/versioned/{path:.*}", produces = APPLICATION_OCTET_STREAM_VALUE)
    public void readVersionedDocument(@RequestHeader String user,
                             @RequestHeader String password,
                             @PathVariable String path,
                             HttpServletResponse response) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), new ReadKeyPassword(password));
        PrivateResource resource = BasePrivateResource.forPrivate(path);
        ReadRequest<UserIDAuth, PrivateResource> request = ReadRequest.forPrivate(userIDAuth, resource);
        // this is needed for swagger, produces is just a directive:
        response.addHeader(CONTENT_TYPE, APPLICATION_OCTET_STREAM_VALUE);

        try (InputStream is = versionedDatasafeServices.latestPrivate().read(request);
             OutputStream os = response.getOutputStream()
        ) {
            StreamUtils.copy(is, os);
        }
        log.debug("User: {}, read private file from: {}", user, resource);
    }

    /**
     * writes latest version of file to user's private space.
     */
    @SneakyThrows
    @PutMapping(value = "/versioned/{path:.*}", consumes = MULTIPART_FORM_DATA_VALUE)
    public void writeVersionedDocument(@RequestHeader String user,
                              @RequestHeader String password,
                              @PathVariable String path,
                              @RequestParam("file") MultipartFile file) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), new ReadKeyPassword(password));
        WriteRequest<UserIDAuth, PrivateResource> request = WriteRequest.forDefaultPrivate(userIDAuth, path);
        try (OutputStream os = versionedDatasafeServices.latestPrivate().write(request);
             InputStream is = file.getInputStream()) {
            StreamUtils.copy(is, os);
        }
        log.debug("User: {}, write private file to: {}", user, path);
    }

    /**
     * deletes latest version of file from user's private space.
     */
    @DeleteMapping("/versioned/{path:.*}")
    public void deleteVersionedDocument(@RequestHeader String user,
                               @RequestHeader String password,
                               @PathVariable String path) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), new ReadKeyPassword(password));
        PrivateResource resource = BasePrivateResource.forPrivate(path);
        RemoveRequest<UserIDAuth, PrivateResource> request = RemoveRequest.forPrivate(userIDAuth, resource);
        versionedDatasafeServices.latestPrivate().remove(request);
        log.debug("User: {}, delete private file: {}", user, resource);
    }

    /**
     * list of file versions.
     */
    @GetMapping(value = "/versions/list/{path:.*}", produces = APPLICATION_JSON_VALUE)
    public List<String> versionsOf(@RequestHeader String user,
                                   @RequestHeader String password,
                                   @ApiParam(defaultValue = ".")
                                   @PathVariable(required = false) String path) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), new ReadKeyPassword(password));
        path = Optional.ofNullable(path)
                .map(it -> it.replaceAll("^\\.$", ""))
                .orElse("./");
        PrivateResource resource = BasePrivateResource.forPrivate(path);

        ListRequest<UserIDAuth, PrivateResource> request = ListRequest.<UserIDAuth, PrivateResource>builder()
                .location(resource)
                .owner(userIDAuth)
                .build();

        List<Versioned<AbsoluteLocation<ResolvedResource>, PrivateResource, DFSVersion>> versionList =
                versionedDatasafeServices.versionInfo()
                        .versionsOf(request)
                        .collect(Collectors.toList());

        log.debug("Versions for path {} returned {} items", path, versionList.size());
        return versionList.stream().map(a -> a.absolute().getResource().asPrivate().decryptedPath().asString()).collect(Collectors.toList());
    }
}
