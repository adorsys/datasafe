package de.adorsys.datasafe.rest.impl.controller;

import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.RemoveRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
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
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

/**
 * User INBOX REST api.
 */
@Slf4j
@RestController
@RequestMapping("/inbox")
@RequiredArgsConstructor
public class InboxController {

    private final DefaultDatasafeServices dataSafeService;

    /**
     * Sends file to multiple users' INBOX.
     */
    @SneakyThrows
    @PutMapping(value = "/{path:.*}", consumes = MULTIPART_FORM_DATA_VALUE)
    public void writeToInbox(@RequestHeader Set<String> users,
                             @PathVariable String path,
                             @RequestParam("file") MultipartFile file) {
        Set<UserID> toUsers = users.stream().map(UserID::new).collect(Collectors.toSet());
        try (OutputStream os = dataSafeService.inboxService().write(WriteRequest.forDefaultPublic(toUsers, path));
             InputStream is = file.getInputStream()) {
            StreamUtils.copy(is, os);
        }
        log.debug("Users {}, write to INBOX file: {}", toUsers, path);
    }

    /**
     * Reads file from users' INBOX.
     */
    @SneakyThrows
    @GetMapping(value = "/{path:.*}", produces = APPLICATION_OCTET_STREAM_VALUE)
    public void readFromInbox(@RequestHeader String user,
                              @RequestHeader String password,
                              @PathVariable String path,
                              HttpServletResponse response) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), new ReadKeyPassword(password));
        PrivateResource resource = BasePrivateResource.forPrivate(path);
        try (InputStream is = dataSafeService.inboxService().read(ReadRequest.forPrivate(userIDAuth, resource));
             OutputStream os = response.getOutputStream()
        ) {
            StreamUtils.copy(is, os);
        }
        log.debug("User {}, read from INBOX file {}", user, resource);
    }

    /**
     * Deletes file from users' INBOX.
     */
    @DeleteMapping("/{path:.*}")
    public void deleteFromInbox(@RequestHeader String user,
                                @RequestHeader String password,
                                @PathVariable String path) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), new ReadKeyPassword(password));
        PrivateResource resource = BasePrivateResource.forPrivate(path);
        RemoveRequest<UserIDAuth, PrivateResource> request = RemoveRequest.forPrivate(userIDAuth, resource);
        dataSafeService.inboxService().remove(request);
        log.debug("User {}, delete from INBOX file {}", user, resource);
    }

    /**
     * list files in users' INBOX.
     */
    @GetMapping(value = "/{path:.*}", produces = APPLICATION_JSON_VALUE)
    public List<String> listInbox(@RequestHeader String user,
                                  @RequestHeader String password,
                                  @PathVariable(required = false) String path) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), new ReadKeyPassword(password));
        path = Optional.ofNullable(path).orElse("./");
        List<String> inboxList = dataSafeService.inboxService().list(ListRequest.forDefaultPrivate(userIDAuth, path))
                .map(e -> e.getResource().asPrivate().decryptedPath().asString())
                .collect(Collectors.toList());
        log.debug("User's {} inbox contains {} items", user, inboxList.size());
        return inboxList;
    }
}
