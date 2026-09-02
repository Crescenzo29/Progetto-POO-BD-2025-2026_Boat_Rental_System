package exception;

/**
 * Usata quando un noleggio non puo' terminare.
 */
public class NoleggioNonTerminabileException extends NoleggioBarcheException {
    public NoleggioNonTerminabileException(String message) {
        super(message);
    }

    public NoleggioNonTerminabileException(String message, Throwable cause) {
        super(message, cause);
    }
}
