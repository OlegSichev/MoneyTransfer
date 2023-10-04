package oleg.sichev.moneytransfer.exception;

public class ErrorTransferException extends RuntimeException{

    public ErrorTransferException(String message) {
        super(message);
    }
}