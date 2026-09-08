package exception;

/**
 * Usata quando una manutenzione non puo' partire.
 */
public class ManutenzioneNonAvviabileException extends NoleggioBarcheException {
    public ManutenzioneNonAvviabileException(String message) {
        super(message);
    }

    public ManutenzioneNonAvviabileException(String message, Throwable cause) {
        super(message, cause);
    }
}
