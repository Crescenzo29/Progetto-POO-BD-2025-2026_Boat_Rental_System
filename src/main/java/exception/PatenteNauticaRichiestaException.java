package exception;

/**
 * Usata quando serve la patente nautica.
 */
public class PatenteNauticaRichiestaException extends NoleggioBarcheException {
    public PatenteNauticaRichiestaException(String message) {
        super(message);
    }

    public PatenteNauticaRichiestaException(String message, Throwable cause) {
        super(message, cause);
    }
}
