package de.adorsys.datasafe.rest.impl.controller;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.rest.impl.exceptions.UnauthorizedException;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.RemoveRequest;
import de.adorsys.datasafe.types.api.actions.WriteInboxRequest;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

/**
 * User INBOX REST api.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class InboxController {

    private final DefaultDatasafeServices dataSafeService;

    /**
     * Sends file to multiple users' INBOX.
     */
    @SneakyThrows
    @PutMapping(value = "/inbox/document/{*path}", consumes = MULTIPART_FORM_DATA_VALUE)
    public void writeToInbox(@RequestHeader @NotBlank String user,
                             @RequestHeader @NotBlank  String password,
                             @RequestHeader Set<@NotBlank String> recipients,
                             @PathVariable @NotBlank String path,
                             @RequestParam("file") MultipartFile file) {
        UserIDAuth fromUser = new UserIDAuth(new UserID(user), ReadKeyPasswordHelper.getForString(password));
        Set<UserID> toUsers = recipients.stream().map(UserID::new).collect(Collectors.toSet());
        path = path.replaceAll("^/", "");
        try (OutputStream os = dataSafeService.inboxService().write(WriteInboxRequest.forDefaultPublic(fromUser, toUsers, path));
                InputStream is = file.getInputStream()) {
            StreamUtils.copy(is, os);
        }
        log.debug("Users {}, write to INBOX file: {}", toUsers, path);
    }

    /**
     * Reads file from users' INBOX.
     */
    @SneakyThrows
    @GetMapping(value = "/inbox/document/{*path}", produces = APPLICATION_OCTET_STREAM_VALUE)
    public void readFromInbox(@RequestHeader @NotBlank String user,
                              @RequestHeader @NotBlank  String password,
                              @PathVariable @NotBlank String path,
                              HttpServletResponse response) {
        path = path.replaceAll("^/", "");
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), ReadKeyPasswordHelper.getForString(password));
        PrivateResource resource = BasePrivateResource.forPrivate(path);
        // this is needed for swagger, produces is just a directive:
        response.addHeader(CONTENT_TYPE, APPLICATION_OCTET_STREAM_VALUE);

        try (InputStream is = dataSafeService.inboxService().read(ReadRequest.forPrivate(userIDAuth, resource));
                OutputStream os = response.getOutputStream()) {
            StreamUtils.copy(is, os);
        }
        log.debug("User {}, read from INBOX file {}", user, resource);
    }

    /**
     * Deletes file from users' INBOX.
     */
    @DeleteMapping("/inbox/document/{*path}")
    public void deleteFromInbox(@RequestHeader @NotBlank String user,
                                @RequestHeader @NotBlank String password,
                                @PathVariable @NotBlank String path) {
        path = path.replaceAll("^/", "");
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), ReadKeyPasswordHelper.getForString(password));
        PrivateResource resource = BasePrivateResource.forPrivate(path);
        RemoveRequest<UserIDAuth, PrivateResource> request = RemoveRequest.forPrivate(userIDAuth, resource);
        dataSafeService.inboxService().remove(request);
        log.debug("User {}, delete from INBOX file {}", user, resource);
    }

    /**
     * list files in users' INBOX.
     */
    @GetMapping(value = "/inbox/documents/{*path}", produces = APPLICATION_JSON_VALUE)
    public List<String> listInbox(@RequestHeader @NotBlank String user,
                                  @RequestHeader @NotBlank String password,
                                  @PathVariable(required = false) String path) {
        path = path.replaceAll("^/", "");
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), ReadKeyPasswordHelper.getForString(password));
        try {
            List<String> inboxList = dataSafeService.inboxService().list(ListRequest.forDefaultPrivate(userIDAuth, path))
                    .map(e -> e.getResource().asPrivate().decryptedPath().asString())
                    .toList();
            log.debug("User's {} inbox contains {} items", user, inboxList.size());
            return inboxList;
        } catch (AmazonS3Exception e) { // for list this exception most likely means that user credentials wrong
            throw new UnauthorizedException("Unauthorized", e);
        }
    }
}
