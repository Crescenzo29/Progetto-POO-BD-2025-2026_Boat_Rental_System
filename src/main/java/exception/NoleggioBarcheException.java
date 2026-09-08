package exception;

/**
 * Eccezione base del progetto.
 */
public class NoleggioBarcheException extends RuntimeException {
    public NoleggioBarcheException(String message) {
        super(message);
    }

    public NoleggioBarcheException(String message, Throwable cause) {
        super(message, cause);
    }
}
