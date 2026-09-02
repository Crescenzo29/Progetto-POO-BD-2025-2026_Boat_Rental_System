package exception;

/**
 * Usata quando un noleggio non puo' partire.
 */
public class NoleggioNonAvviabileException extends NoleggioBarcheException {
    public NoleggioNonAvviabileException(String message) {
        super(message);
    }

    public NoleggioNonAvviabileException(String message, Throwable cause) {
        super(message, cause);
    }
}
