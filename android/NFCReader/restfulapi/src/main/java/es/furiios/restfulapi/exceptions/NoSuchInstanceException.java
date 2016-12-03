package es.furiios.restfulapi.exceptions;

public class NoSuchInstanceException extends Exception {

    public NoSuchInstanceException() {
        super("Instance not found.");
    }

    public NoSuchInstanceException(String message) {
        super(message);
    }
}
