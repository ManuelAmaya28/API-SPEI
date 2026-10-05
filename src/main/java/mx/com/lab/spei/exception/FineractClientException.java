package mx.com.lab.spei.exception;

public class FineractClientException extends SpeiBusinessException {

    public FineractClientException(String message) {
        super(message);
    }

    public FineractClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
