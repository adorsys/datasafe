package de.adorsys.datasafe.rest.impl.controller;

import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.rest.impl.exceptions.UnauthorizedException;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.RemoveRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
import java.util.Optional;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

/**
 * User private space REST api.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class DocumentController {

    private final DefaultDatasafeServices datasafeService;

    /**
     * Reads user's private file.
     */
    @SneakyThrows
    @GetMapping(value = "/document/{*path}", produces = APPLICATION_OCTET_STREAM_VALUE)
    public void readDocument(@RequestHeader @NotBlank String user,
                             @RequestHeader @NotBlank String password,
                             @RequestHeader(defaultValue = StorageIdentifier.DEFAULT_ID) String storageId,
                             @PathVariable @NotBlank String path,
                             HttpServletResponse response) {
        // Validate and sanitize path
        if (path.contains("..")) {
            throw new IllegalArgumentException("Invalid path");
        }

        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), ReadKeyPasswordHelper.getForString(password));
        ReadRequest<UserIDAuth, PrivateResource> request =
            ReadRequest.forPrivate(userIDAuth, new StorageIdentifier(storageId), path);
        // this is needed for swagger, produces is just a directive:
        response.addHeader(CONTENT_TYPE, APPLICATION_OCTET_STREAM_VALUE);

        try (InputStream is = datasafeService.privateService().read(request);
                OutputStream os = response.getOutputStream()) {
            StreamUtils.copy(is, os);
        }
        log.debug("User: {}, read private file from: {}", user, password);
    }

    /**
     * Writes file to user's private space.
     */
    @SneakyThrows
    @PutMapping(value = "/document/{*path}", consumes = MULTIPART_FORM_DATA_VALUE)
    public void writeDocument(@RequestHeader @NotBlank String user,
                              @RequestHeader @NotBlank String password,
                              @RequestHeader(defaultValue = StorageIdentifier.DEFAULT_ID) String storageId,
                              @PathVariable String path,
                              @RequestParam("file") @NotNull MultipartFile file) {
        // Validate and sanitize path
        if (path.contains("..")) {
            throw new IllegalArgumentException("Invalid path");
        }

        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), ReadKeyPasswordHelper.getForString(password));
        WriteRequest<UserIDAuth, PrivateResource> request =
                WriteRequest.forPrivate(userIDAuth, new StorageIdentifier(storageId), path);
        try (OutputStream os = datasafeService.privateService().write(request);
             InputStream is = file.getInputStream()) {
            StreamUtils.copy(is, os);
        }
        log.debug("User: {}, write private file to: {}", user, path);
    }

    /**
     * lists files in user's private space.
     */
    @GetMapping("/documents/{*path}")
    public List<String> listDocuments(@RequestHeader @NotBlank String user,
                                      @RequestHeader @NotBlank String password,
                                      @RequestHeader(defaultValue = StorageIdentifier.DEFAULT_ID) String storageId,
                                      @PathVariable(required = false) String path) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), ReadKeyPasswordHelper.getForString(password));
        path = Optional.ofNullable(path)
                .map(it -> it.replaceAll("^\\.$", ""))
                .orElse("./");

        // Validate and sanitize path
        if (path.contains("..")) {
            throw new IllegalArgumentException("Invalid path");
        }

        try {
            List<String> documentList = datasafeService.privateService().list(
                ListRequest.forPrivate(userIDAuth, new StorageIdentifier(storageId), path))
                    .map(e -> e.getResource().asPrivate().decryptedPath().asString())
                    .toList();
            log.debug("List for path {} returned {} items", path, documentList.size());
            return documentList;
        } catch (S3Exception e) { // for list this exception most likely means that user credentials wrong
            throw new UnauthorizedException("Unauthorized", e);
        }
    }

    /**
     * deletes files from user's private space.
     */
    @DeleteMapping("/document/{*path}")
    public void removeDocument(@RequestHeader @NotBlank String user,
                               @RequestHeader @NotBlank String password,
                               @RequestHeader(defaultValue = StorageIdentifier.DEFAULT_ID) String storageId,
                               @PathVariable @NotBlank String path) {

        // Validate and sanitize path
        if (path.contains("..")) {
            throw new IllegalArgumentException("Invalid path");
        }

        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), ReadKeyPasswordHelper.getForString(password));
        RemoveRequest<UserIDAuth, PrivateResource> request =
            RemoveRequest.forPrivate(userIDAuth, new StorageIdentifier(storageId), path);
        datasafeService.privateService().remove(request);
        log.debug("User: {}, delete private file: {}", user, path);
    }
}
