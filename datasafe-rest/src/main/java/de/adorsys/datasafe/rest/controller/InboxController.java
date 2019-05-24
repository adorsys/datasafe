package de.adorsys.datasafe.rest.controller;

import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.actions.ReadRequest;
import de.adorsys.datasafe.business.api.types.actions.RemoveRequest;
import de.adorsys.datasafe.business.api.types.actions.WriteRequest;
import de.adorsys.datasafe.business.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.business.api.types.resource.BasePrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/inbox")
@RequiredArgsConstructor
public class InboxController {

    private final DefaultDatasafeServices dataSafeService;

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
