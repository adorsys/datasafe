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
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

/**
 * User privatespace REST api.
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
    @GetMapping(value = "/document/{path:.*}", produces = APPLICATION_OCTET_STREAM_VALUE)
    public void readDocument(@RequestHeader String user,
                             @RequestHeader String password,
                             @PathVariable String path,
                             HttpServletResponse response) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), new ReadKeyPassword(password));
        PrivateResource resource = BasePrivateResource.forPrivate(path);
        ReadRequest<UserIDAuth, PrivateResource> request = ReadRequest.forPrivate(userIDAuth, resource);
        // this is needed for swagger, produces is just a directive:
        response.addHeader(CONTENT_TYPE, APPLICATION_OCTET_STREAM_VALUE);

        try (InputStream is = datasafeService.privateService().read(request);
             OutputStream os = response.getOutputStream()
        ) {
            StreamUtils.copy(is, os);
        }
        log.debug("User: {}, read private file from: {}", user, resource);
    }

    /**
     * Writes file to user's private space.
     */
    @SneakyThrows
    @PutMapping(value = "/document/{path:.*}", consumes = MULTIPART_FORM_DATA_VALUE)
    public void writeDocument(@RequestHeader String user,
                              @RequestHeader String password,
                              @PathVariable String path,
                              @RequestParam("file") MultipartFile file) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), new ReadKeyPassword(password));
        WriteRequest<UserIDAuth, PrivateResource> request = WriteRequest.forDefaultPrivate(userIDAuth, path);
        try (OutputStream os = datasafeService.privateService().write(request);
             InputStream is = file.getInputStream()) {
            StreamUtils.copy(is, os);
        }
        log.debug("User: {}, write private file to: {}", user, path);
    }

    /**
     * lists files in user's private space.
     */
    @GetMapping("/documents/{path:.*}")
    public List<String> listDocuments(@RequestHeader String user,
                                      @RequestHeader String password,
                                      @ApiParam(defaultValue = ".")
                                      @PathVariable(required = false) String path) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), new ReadKeyPassword(password));
        path = Optional.ofNullable(path)
                .map(it -> it.replaceAll("^\\.$", ""))
                .orElse("./");
        List<String> documentList = datasafeService.privateService().list(ListRequest.forDefaultPrivate(userIDAuth, path))
                .map(e -> e.getResource().asPrivate().decryptedPath().asString())
                .collect(Collectors.toList());
        log.debug("List for path {} returned {} items", path, documentList.size());
        return documentList;
    }

    /**
     * deletes files from user's private space.
     */
    @DeleteMapping("/document/{path:.*}")
    public void removeDocument(@RequestHeader String user,
                               @RequestHeader String password,
                               @PathVariable String path) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), new ReadKeyPassword(password));
        PrivateResource resource = BasePrivateResource.forPrivate(path);
        RemoveRequest<UserIDAuth, PrivateResource> request = RemoveRequest.forPrivate(userIDAuth, resource);
        datasafeService.privateService().remove(request);
        log.debug("User: {}, delete private file: {}", user, resource);
    }
}
