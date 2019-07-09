package de.adorsys.datasafe.rest.impl.controller;

import de.adorsys.datasafe.rest.impl.exceptions.EmptyInputStreamException;
import de.adorsys.datasafe.rest.impl.exceptions.FileNotFoundException;
import de.adorsys.datasafe.rest.impl.exceptions.UserDoesNotExistsException;
import de.adorsys.datasafe.rest.impl.exceptions.UserExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ControllerAdvice(basePackages = {"de.adorsys.datasafe.rest.impl"})
@RestController
@Slf4j
public class ExceptionHandlerApiController {

    @ExceptionHandler({UserDoesNotExistsException.class})
    public ResponseEntity<String> handleUserDoesNotExistsException(UserDoesNotExistsException ex) {
        log.debug("User does not exists exception: " + ex.getMessage());
        return ResponseEntity.notFound().header("Error", ex.getMessage()).build();
    }

    @ExceptionHandler({UserExistsException.class})
    public ResponseEntity<List<String>> handleUserExistsException(UserExistsException ex) {
        log.debug("User already exists exception: " + ex.getMessage());
        List<String> errors = Collections.singletonList(ex.getMessage());
        return ResponseEntity.badRequest().body(new ArrayList<>(errors));
    }

    @ExceptionHandler({FileNotFoundException.class})
    public ResponseEntity<List<String>> handleFileNotFoundException(FileNotFoundException ex) {
        log.debug("File not found exception: " + ex.getMessage());
        return ResponseEntity.notFound().header("Error", ex.getMessage()).build();
    }

    @ExceptionHandler({EmptyInputStreamException.class})
    public ResponseEntity<List<String>> handleEmptyInputStreamException(EmptyInputStreamException ex) {
        log.debug("Empty input stream exception: " + ex.getMessage());
        return ResponseEntity.badRequest().header("Error", ex.getMessage()).build();
    }

//    @ExceptionHandler({BadCredentialsException.class})
//    public ResponseEntity<List<String>> handleBadCredentialsException(BadCredentialsException ex) {
//        log.debug("Bad credentials exception: " + ex.getMessage());
//        List<String> errors = Collections.singletonList(ex.getMessage());
//
//        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ArrayList<>(errors));
//    }

//    @ExceptionHandler({RuntimeException.class})
//    public ResponseEntity<List<String>> handleException(Exception ex) {
//        log.debug("Unhandled exception: " + ex.getMessage());
//        List<String> errors = Collections.singletonList(ex.getMessage());
//        return ResponseEntity.badRequest().body(new ArrayList<>(errors));
//    }
}
