package pl.ksef.hub.exception;

public class KsefApiException extends RuntimeException {
    
    private final String errorCode;
    
    public KsefApiException(String message) {
        super(message);
        this.errorCode = null;
    }
    
    public KsefApiException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public KsefApiException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
