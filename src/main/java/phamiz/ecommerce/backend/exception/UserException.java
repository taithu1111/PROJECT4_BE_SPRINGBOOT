package phamiz.ecommerce.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception class for user-related errors.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class UserException extends Exception {

    /**
     * Constructs a new UserException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the
     *                Throwable.getMessage() method).
     */
    public UserException(String message) {
        super(message);
    }
}
