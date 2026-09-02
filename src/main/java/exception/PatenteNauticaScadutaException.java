package exception;

/**
 * Usata quando la patente nautica e' scaduta.
 */
public class PatenteNauticaScadutaException extends NoleggioBarcheException {
    public PatenteNauticaScadutaException(String message) {
        super(message);
    }

    public PatenteNauticaScadutaException(String message, Throwable cause) {
        super(message, cause);
    }
}
