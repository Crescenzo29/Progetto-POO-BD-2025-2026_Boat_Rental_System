package exception;

/**
 * Usata quando una prenotazione non puo' essere annullata.
 */
public class PrenotazioneNonAnnullabileException extends NoleggioBarcheException {
    public PrenotazioneNonAnnullabileException(String message) {
        super(message);
    }

    public PrenotazioneNonAnnullabileException(String message, Throwable cause) {
        super(message, cause);
    }
}
