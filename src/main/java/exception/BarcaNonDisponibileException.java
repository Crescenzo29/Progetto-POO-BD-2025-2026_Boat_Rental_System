package exception;

/**
 * Usata quando una barca non e' disponibile.
 */
public class BarcaNonDisponibileException extends NoleggioBarcheException {
    public BarcaNonDisponibileException(String message) {
        super(message);
    }

    public BarcaNonDisponibileException(String message, Throwable cause) {
        super(message, cause);
    }
}
