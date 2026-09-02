package exception;

/**
 * Usata quando le date della prenotazione non vanno bene.
 */
public class PeriodoPrenotazioneNonValidoException extends NoleggioBarcheException {
    public PeriodoPrenotazioneNonValidoException(String message) {
        super(message);
    }

    public PeriodoPrenotazioneNonValidoException(String message, Throwable cause) {
        super(message, cause);
    }
}
