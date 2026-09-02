package exception;

/**
 * Usata quando una manutenzione e' in conflitto.
 */
public class ManutenzioneInConflittoException extends NoleggioBarcheException {
    public ManutenzioneInConflittoException(String message) {
        super(message);
    }

    public ManutenzioneInConflittoException(String message, Throwable cause) {
        super(message, cause);
    }
}
