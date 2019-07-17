package de.adorsys.datasafe.rest.impl.controller;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import de.adorsys.datasafe.rest.impl.exceptions.UnauthorizedException;
import de.adorsys.datasafe.rest.impl.exceptions.UserDoesNotExistsException;
import de.adorsys.datasafe.rest.impl.exceptions.UserExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.crypto.BadPaddingException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ControllerAdvice(basePackageClasses = {
        DocumentController.class,
        InboxController.class,
        UserController.class,
        VersionController.class,
        AuthenticateController.class
})
@Slf4j
public class GenericControllerAdvice {

    @ExceptionHandler({UserDoesNotExistsException.class})
    public ResponseEntity<List<String>> handleUserDoesNotExistsException(UserDoesNotExistsException ex) {
        log.debug("User does not exists exception: {}", ex.getMessage(), ex);
        List<String> errors = Collections.singletonList(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ArrayList<>(errors));
    }

    @ExceptionHandler({UserExistsException.class})
    public ResponseEntity<List<String>> handleUserExistsException(UserExistsException ex) {
        log.debug("User already exists exception: {}", ex.getMessage(), ex);
        List<String> errors = Collections.singletonList(ex.getMessage());
        return ResponseEntity.badRequest().body(new ArrayList<>(errors));
    }

    @ExceptionHandler({AmazonS3Exception.class})
    public ResponseEntity<List<String>> handleFileNotFoundException(Exception ex) {
        log.debug("File not found exception: {}", ex.getMessage(), ex);
        List<String> errors = Collections.singletonList("File not found");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ArrayList<>(errors));
    }

    @ExceptionHandler({UnauthorizedException.class, BadCredentialsException.class})
    @ResponseStatus(value=HttpStatus.UNAUTHORIZED, reason="Access Denied")
    public ResponseEntity<List<String>> handleUnauthorizedException(Exception ex) {
        log.debug("Unauthorized exception: {}", ex.getMessage(), ex);
        List<String> errors = Collections.singletonList(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ArrayList<>(errors));
    }

    @ExceptionHandler({UnrecoverableKeyException.class, BadPaddingException.class})
    @ResponseStatus(value=HttpStatus.FORBIDDEN, reason="Access Denied")
    public ResponseEntity<List<String>> handleBadCredentialsException(Exception ex) {
        log.debug("Bad credentials exception: {}", ex.getMessage(), ex);
        List<String> errors = Collections.singletonList(ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ArrayList<>(errors));
    }

    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<List<String>> handleException(Exception ex) {
        log.debug("Unhandled exception: {}", ex.getMessage(), ex);
        List<String> errors = Collections.singletonList(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ArrayList<>(errors));
    }
}
