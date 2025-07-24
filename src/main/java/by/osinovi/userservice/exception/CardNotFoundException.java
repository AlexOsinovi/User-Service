package by.osinovi.userservice.exception;

public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(String message) {
        super(message);
    }
}