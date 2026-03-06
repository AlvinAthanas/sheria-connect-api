package co.tz.sheriaconnectapi.exceptions;

public class EmailNotVerifiedException
        extends RuntimeException{

    public EmailNotVerifiedException() {
        super(ErrorMessages.EMAIL_NOT_VERIFIED.getMessage());
    }
}
