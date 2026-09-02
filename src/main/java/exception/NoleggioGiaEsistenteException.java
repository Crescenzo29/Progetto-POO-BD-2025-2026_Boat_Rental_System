package exception;

/**
 * Usata quando una prenotazione ha gia' un noleggio.
 */
public class NoleggioGiaEsistenteException extends NoleggioBarcheException {
    public NoleggioGiaEsistenteException(String message) {
        super(message);
    }

    public NoleggioGiaEsistenteException(String message, Throwable cause) {
        super(message, cause);
    }
}
