package school.faang.user_service.exceptions;

public class DataValidationException extends RuntimeException {
    public DataValidationException() {
    }

    public DataValidationException(String message) {
        super(message);
    }
}
