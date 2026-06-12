package org.example.project_webservice_it211.exception;

/**
 * Ném ra khi Cloud Storage (Cloudinary) gặp sự cố.
 * GlobalExceptionHandler sẽ bắt và trả HTTP 503.
 */
public class CloudStorageException extends RuntimeException {

    public CloudStorageException(String message) {
        super(message);
    }

    public CloudStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
