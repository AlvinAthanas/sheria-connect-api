package co.tz.sheriaconnectapi.exceptions;

public class InvalidLoginCredentialsException extends RuntimeException {

    public InvalidLoginCredentialsException() {
        super(ErrorMessages.INVALID_LOGIN_CREDENTIALS.getMessage());
    }
}