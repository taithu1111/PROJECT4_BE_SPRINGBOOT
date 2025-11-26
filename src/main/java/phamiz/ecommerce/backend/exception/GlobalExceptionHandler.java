package phamiz.ecommerce.backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDetails> badCredentialsExceptionHandler(BadCredentialsException ue, WebRequest req) {
        logger.error("BadCredentialsException: ", ue);
        ErrorDetails err = new ErrorDetails(ue.getMessage(), req.getDescription(false), LocalDateTime.now());
        return new ResponseEntity<>(err, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorDetails> usernameNotFoundExceptionHandler(UsernameNotFoundException ue, WebRequest req) {
        logger.error("UsernameNotFoundException: ", ue);
        // Return "Invalid Username" instead of the actual exception message for
        // security
        ErrorDetails err = new ErrorDetails("Invalid Username", req.getDescription(false), LocalDateTime.now());
        return new ResponseEntity<>(err, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorDetails> userExceptionHandler(UserException ue, WebRequest req) {
        logger.error("UserException: ", ue);
        ErrorDetails err = new ErrorDetails(ue.getMessage(), req.getDescription(false), LocalDateTime.now());
        return new ResponseEntity<>(err, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ProductException.class)
    public ResponseEntity<ErrorDetails> productExceptionHandler(ProductException ue, WebRequest req) {
        logger.error("ProductException: ", ue);
        ErrorDetails err = new ErrorDetails(ue.getMessage(), req.getDescription(false), LocalDateTime.now());
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CartItemException.class)
    public ResponseEntity<ErrorDetails> cartItemExceptionHandler(CartItemException ue, WebRequest req) {
        logger.error("CartItemException: ", ue);
        ErrorDetails err = new ErrorDetails(ue.getMessage(), req.getDescription(false), LocalDateTime.now());
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OrderException.class)
    public ResponseEntity<ErrorDetails> orderExceptionHandler(OrderException ue, WebRequest req) {
        logger.error("OrderException: ", ue);
        ErrorDetails err = new ErrorDetails(ue.getMessage(), req.getDescription(false), LocalDateTime.now());
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        logger.error("MethodArgumentNotValidException: ", ex);
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> otherExceptionHandler(Exception ue, WebRequest req) {
        logger.error("Exception: ", ue);
        ErrorDetails err = new ErrorDetails(ue.getMessage(), req.getDescription(false), LocalDateTime.now());
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
