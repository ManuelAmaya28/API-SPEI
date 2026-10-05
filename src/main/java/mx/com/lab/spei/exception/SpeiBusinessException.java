package mx.com.lab.spei.exception;

public class SpeiBusinessException extends RuntimeException {

    public SpeiBusinessException(String message) {
        super(message);
    }

    public SpeiBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
