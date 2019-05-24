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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/inbox")
public class InboxController {

    private final DefaultDatasafeServices dataSafeService;

    public InboxController(DefaultDatasafeServices dataSafeService) {
        this.dataSafeService = dataSafeService;
    }

    @SneakyThrows
    @PutMapping("/{path:.*}")
    public void sendDocumentToInbox(@RequestHeader String user,
                                    @PathVariable String path,
                                    InputStream inputStream) {
        UserID toUser = new UserID(user);
        OutputStream stream = dataSafeService.inboxService().write(WriteRequest.forDefaultPublic(toUser, path));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StreamUtils.copy(inputStream, outputStream);
        stream.write(outputStream.toByteArray());
        stream.close();
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
        InputStream inputStream = dataSafeService.inboxService().read(ReadRequest.forPrivate(userIDAuth, resource));
        OutputStream outputStream = response.getOutputStream();
        StreamUtils.copy(inputStream, outputStream);
        inputStream.close();
        outputStream.close();
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
