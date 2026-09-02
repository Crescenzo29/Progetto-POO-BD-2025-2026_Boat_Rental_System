package exception;

/**
 * Usata quando il cliente e' minorenne.
 */
public class ClienteMinorenneException extends NoleggioBarcheException {
    public ClienteMinorenneException(String message) {
        super(message);
    }

    public ClienteMinorenneException(String message, Throwable cause) {
        super(message, cause);
    }
}
