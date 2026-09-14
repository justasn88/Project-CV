package lt.justasn88.jobcheckerapplication.exception;

public class ScraperTimeoutException extends RuntimeException {
    public ScraperTimeoutException(String message) {
        super(message);
    }

    public ScraperTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}