package de.adorsys.datasafe.rest.impl.controller;


import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
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

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

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
     * Sends file to users' INBOX.
     */
    @SneakyThrows
    @PutMapping("/{path:.*}")
    public void sendDocumentToInbox(@RequestHeader String user,
                                    @PathVariable String path,
                                    InputStream is) {
        UserID toUser = new UserID(user);
        try (OutputStream os = dataSafeService.inboxService().write(WriteRequest.forDefaultPublic(toUser, path))) {
            StreamUtils.copy(is, os);
        } finally {
            is.close();
        }
        log.debug("User {}, write to INBOX file: {}", toUser, path);
    }

    /**
     * Reads file from users' INBOX.
     */
    @SneakyThrows
    @GetMapping("/{path:.*}")
    public void readFromInbox(@RequestHeader String user,
                              @RequestHeader String password,
                              @PathVariable String path,
                              HttpServletResponse response) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), new ReadKeyPassword(password));
        PrivateResource resource = BasePrivateResource.forPrivate(URI.create("./" + path));
        try (InputStream is = dataSafeService.inboxService().read(ReadRequest.forPrivate(userIDAuth, resource));
             OutputStream os = response.getOutputStream()
        ) {
            StreamUtils.copy(is, os);
        }
        log.debug("User {}, read from INBOX file {}", user, resource);
    }

    @DeleteMapping("/{path:.*}")
    public void deleteFromInbox(@RequestHeader String user,
                                @RequestHeader String password,
                                @PathVariable String path) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), new ReadKeyPassword(password));
        PrivateResource resource = BasePrivateResource.forPrivate(URI.create("./" + path));
        RemoveRequest<UserIDAuth, PrivateResource> request = RemoveRequest.forPrivate(userIDAuth, resource);
        dataSafeService.inboxService().remove(request);
        log.debug("User {}, delete from INBOX file {}", user, resource);
    }
}
