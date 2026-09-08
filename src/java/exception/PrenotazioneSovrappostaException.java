package exception;

/**
 * Usata quando due prenotazioni si sovrappongono.
 */
public class PrenotazioneSovrappostaException extends NoleggioBarcheException {
    public PrenotazioneSovrappostaException(String message) {
        super(message);
    }

    public PrenotazioneSovrappostaException(String message, Throwable cause) {
        super(message, cause);
    }
}
