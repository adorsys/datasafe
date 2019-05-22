package de.adorsys.datasafe.rest.controller;

import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.types.action.RemoveRequest;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.business.api.types.resource.BasePrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import lombok.SneakyThrows;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@RestController
@RequestMapping
public class DocumentController {

    private final DefaultDatasafeServices dataSafeService;

    public DocumentController(DefaultDatasafeServices dataSafeService) {
        this.dataSafeService = dataSafeService;
    }

    @SneakyThrows
    @GetMapping(value = "/document/**", produces = APPLICATION_OCTET_STREAM_VALUE)
    public void readDocument(@RequestHeader String user,
                             @RequestHeader String password,
                             @PathVariable String path,
                             HttpServletResponse response) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), new ReadKeyPassword(password));
        PrivateResource resource = BasePrivateResource.forPrivate(URI.create(path));
        ReadRequest<UserIDAuth, PrivateResource> request = ReadRequest.forPrivate(userIDAuth, resource);
        InputStream inputStream = dataSafeService.privateService().read(request);
        OutputStream outputStream = response.getOutputStream();
        StreamUtils.copy(inputStream, outputStream);
        inputStream.close();
        outputStream.close();
    }

    @SneakyThrows
    @PutMapping(value = "/document/**", consumes = APPLICATION_OCTET_STREAM_VALUE)
    public void writeDocument(@RequestHeader String user,
                              @RequestHeader String password,
                              InputStream inputStream,
                              HttpServletRequest request) {

        final String fullPath =
                request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        final String bestMatchingPattern =
                request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();
        String path = new AntPathMatcher().extractPathWithinPattern(bestMatchingPattern, fullPath);

        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), new ReadKeyPassword(password));
        OutputStream stream = dataSafeService.privateService().write(WriteRequest.forDefaultPrivate(userIDAuth, path));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StreamUtils.copy(inputStream, outputStream);
        stream.write(outputStream.toByteArray());
        stream.close();
    }

    @GetMapping("/documents/**")
    public List<String> listDocuments(@RequestHeader String user,
                                      @RequestHeader String password,
                                      HttpServletRequest request) {
        final String fullPath =
                request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        final String bestMatchingPattern =
                request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();
        String path = new AntPathMatcher().extractPathWithinPattern(bestMatchingPattern, fullPath);

        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), new ReadKeyPassword(password));
        return dataSafeService.privateService().list(ListRequest.forDefaultPrivate(userIDAuth, path))
                        .map(e -> e.getResource().asPrivate().decryptedPath().getPath())
                        .collect(Collectors.toList());
    }

    @DeleteMapping("/document/{path}")
    public void deleteDocument(@RequestHeader String user,
                               @RequestHeader String password,
                               @PathVariable String path) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), new ReadKeyPassword(password));
        PrivateResource resource = BasePrivateResource.forPrivate(URI.create(path));
        RemoveRequest<UserIDAuth, PrivateResource> request = RemoveRequest.forPrivate(userIDAuth, resource);
        dataSafeService.privateService().remove(request);
    }
}
