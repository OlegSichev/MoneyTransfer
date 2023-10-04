package oleg.sichev.moneytransfer.exception;

public class ErrorInputDataException extends RuntimeException{

    public ErrorInputDataException (String message) {
        super(message);
    }
}