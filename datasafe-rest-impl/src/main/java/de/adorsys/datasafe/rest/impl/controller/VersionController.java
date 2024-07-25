package de.adorsys.datasafe.rest.impl.controller;

import de.adorsys.datasafe.business.impl.service.VersionedDatasafeServices;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.metainfo.version.impl.version.types.DFSVersion;
import de.adorsys.datasafe.rest.impl.exceptions.UnauthorizedException;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.RemoveRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import de.adorsys.datasafe.types.api.resource.Versioned;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

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
    @GetMapping(value = "/versioned/{*path}", produces = APPLICATION_JSON_VALUE)
    public List<String> listVersionedDocuments(@RequestHeader String user,
                                               @RequestHeader String password,
                                               @RequestHeader(defaultValue = StorageIdentifier.DEFAULT_ID) String storageId,
                                               @PathVariable(required = false) String path) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), ReadKeyPasswordHelper.getForString(password));
        path = path.replaceAll("^/", "");
        try {
            List<String> documentList = versionedDatasafeServices.latestPrivate().listWithDetails(
                ListRequest.forPrivate(userIDAuth, new StorageIdentifier(storageId), path))
                    .map(e -> e.absolute().getResource().decryptedPath().asString())
                    .toList();
            log.debug("List for path {} returned {} items", path, documentList.size());
            return documentList;
        } catch (S3Exception e) { // for list this exception most likely means that user credentials wrong
            throw new UnauthorizedException("Unauthorized", e);
        }

    }

    /**
     * reads latest version of file from user's private space.
     */
    @SneakyThrows
    @GetMapping(value = "/versioned/{*path}", produces = APPLICATION_OCTET_STREAM_VALUE)
    public void readVersionedDocument(@RequestHeader String user,
                                      @RequestHeader String password,
                                      @RequestHeader(defaultValue = StorageIdentifier.DEFAULT_ID) String storageId,
                                      @PathVariable String path,
                                      HttpServletResponse response) {
        path = path.replaceAll("^/", "");
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), ReadKeyPasswordHelper.getForString(password));
        ReadRequest<UserIDAuth, PrivateResource> request =
                ReadRequest.forPrivate(userIDAuth, new StorageIdentifier(storageId), path);
        // this is needed for swagger, produces is just a directive:
        response.addHeader(CONTENT_TYPE, APPLICATION_OCTET_STREAM_VALUE);

        try (InputStream is = versionedDatasafeServices.latestPrivate().read(request);
             OutputStream os = response.getOutputStream()) {
            StreamUtils.copy(is, os);
        }
        log.debug("User: {}, read private file from: {}", user, path);
    }

    /**
     * writes latest version of file to user's private space.
     */
    @SneakyThrows
    @PutMapping(value = "/versioned/{*path}", consumes = MULTIPART_FORM_DATA_VALUE)
    public void writeVersionedDocument(@RequestHeader String user,
                                       @RequestHeader String password,
                                       @RequestHeader(defaultValue = StorageIdentifier.DEFAULT_ID) String storageId,
                                       @PathVariable String path,
                                       @RequestParam("file") MultipartFile file) {
        path = path.replaceAll("^/", "");
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), ReadKeyPasswordHelper.getForString(password));
        WriteRequest<UserIDAuth, PrivateResource> request =
                WriteRequest.forPrivate(userIDAuth, new StorageIdentifier(storageId), path);
        try (OutputStream os = versionedDatasafeServices.latestPrivate().write(request);
             InputStream is = file.getInputStream()) {
            StreamUtils.copy(is, os);
        }
        log.debug("User: {}, write private file to: {}", user, path);
    }

    /**
     * deletes latest version of file from user's private space.
     */
    @DeleteMapping("/versioned/{*path}")
    public void deleteVersionedDocument(@RequestHeader String user,
                                        @RequestHeader String password,
                                        @RequestHeader(defaultValue = StorageIdentifier.DEFAULT_ID) String storageId,
                                        @PathVariable String path) {
        path = path.replaceAll("^/", "");
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), ReadKeyPasswordHelper.getForString(password));
        RemoveRequest<UserIDAuth, PrivateResource> request =
                RemoveRequest.forPrivate(userIDAuth, new StorageIdentifier(storageId), path);
        versionedDatasafeServices.latestPrivate().remove(request);
        log.debug("User: {}, delete private file: {}", user, path);
    }

    /**
     * list of file versions.
     */
    @GetMapping(value = "/versions/list/{*path}", produces = APPLICATION_JSON_VALUE)
    public List<String> versionsOf(@RequestHeader String user,
                                   @RequestHeader String password,
                                   @RequestHeader(defaultValue = StorageIdentifier.DEFAULT_ID) String storageId,
                                   @PathVariable(required = false) String path) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), ReadKeyPasswordHelper.getForString(password));
        path = path.replaceAll("^/", "");

        ListRequest<UserIDAuth, PrivateResource> request =
                ListRequest.forPrivate(userIDAuth, new StorageIdentifier(storageId), path);

        List<Versioned<AbsoluteLocation<ResolvedResource>, PrivateResource, DFSVersion>> versionList =
                versionedDatasafeServices.versionInfo()
                        .versionsOf(request)
                        .toList();

        log.debug("Versions for path {} returned {} items", path, versionList.size());
        return versionList.stream()
                .map(a -> a.absolute().getResource().asPrivate().decryptedPath().asString())
                .toList();
    }
}
