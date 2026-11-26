package dev.smartgiftlist.repository;

/**
 * Unchecked exception raised when a JDBC persistence operation fails unexpectedly.
 */
public class RepositoryException extends RuntimeException {

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
