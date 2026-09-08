package exception;

/**
 * Usata quando ci sono troppi passeggeri.
 */
public class CapacitaPasseggeriSuperataException extends NoleggioBarcheException {
    public CapacitaPasseggeriSuperataException(String message) {
        super(message);
    }

    public CapacitaPasseggeriSuperataException(String message, Throwable cause) {
        super(message, cause);
    }
}
