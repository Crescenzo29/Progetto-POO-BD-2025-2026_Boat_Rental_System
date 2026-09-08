package exception;

/**
 * Usata quando uno stato non puo' cambiare cosi'.
 */
public class TransizioneStatoNonValidaException extends NoleggioBarcheException {
    public TransizioneStatoNonValidaException(String message) {
        super(message);
    }

    public TransizioneStatoNonValidaException(String message, Throwable cause) {
        super(message, cause);
    }
}
