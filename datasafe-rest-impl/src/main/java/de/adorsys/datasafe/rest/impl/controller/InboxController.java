package de.adorsys.datasafe.rest.impl.controller;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.rest.impl.exceptions.UnauthorizedException;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.RemoveRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
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
@Api(description = "Operations with inbox")
public class InboxController {

    private final DefaultDatasafeServices dataSafeService;

    /**
     * Sends file to multiple users' INBOX.
     */
    @SneakyThrows
    @PutMapping(value = "/inbox/document/{path:.*}", consumes = MULTIPART_FORM_DATA_VALUE)
    @ApiOperation("Send document to inbox")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Document was successfully sent"),
            @ApiResponse(code = 403, message = "Access denied")
    })
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
    @GetMapping(value = "/inbox/document/{path:.*}", produces = APPLICATION_OCTET_STREAM_VALUE)
    @ApiOperation("Read document from inbox")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Document was successfully read"),
            @ApiResponse(code = 404, message = "Document not found")
    })
    public void readFromInbox(@RequestHeader String user,
                              @RequestHeader String password,
                              @PathVariable String path,
                              HttpServletResponse response) {
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
    @DeleteMapping("/inbox/document/{path:.*}")
    @ApiOperation("Delete document from inbox")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Document successfully deleted")
    })
    public void deleteFromInbox(@RequestHeader String user,
                                @RequestHeader String password,
                                @PathVariable String path) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), ReadKeyPasswordHelper.getForString(password));
        PrivateResource resource = BasePrivateResource.forPrivate(path);
        RemoveRequest<UserIDAuth, PrivateResource> request = RemoveRequest.forPrivate(userIDAuth, resource);
        dataSafeService.inboxService().remove(request);
        log.debug("User {}, delete from INBOX file {}", user, resource);
    }

    /**
     * list files in users' INBOX.
     */
    @GetMapping(value = "/inbox/documents/{path:.*}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation("List files in inbox")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List command successfully completed"),
            @ApiResponse(code = 401, message = "Unauthorised")
    })
    public List<String> listInbox(@RequestHeader String user,
                                  @RequestHeader String password,
                                  @ApiParam(defaultValue = ".")
                                  @PathVariable(required = false) String path) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), ReadKeyPasswordHelper.getForString(password));
        path = Optional.ofNullable(path)
                .map(it -> it.replaceAll("^\\.$", ""))
                .orElse("./");
        try {
            List<String> inboxList = dataSafeService.inboxService().list(ListRequest.forDefaultPrivate(userIDAuth, path))
                    .map(e -> e.getResource().asPrivate().decryptedPath().asString())
                    .collect(Collectors.toList());
            log.debug("User's {} inbox contains {} items", user, inboxList.size());
            return inboxList;
        } catch (AmazonS3Exception e) { // for list this exception most likely means that user credentials wrong
            throw new UnauthorizedException("Unauthorized", e);
        }
    }
}
