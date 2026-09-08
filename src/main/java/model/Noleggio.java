package model;

import exception.NoleggioNonTerminabileException;
import exception.TransizioneStatoNonValidaException;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Rappresenta un noleggio.
 */
public class Noleggio {
    private int idNoleggio;
    private LocalDateTime dataOraRitiro;
    private LocalDateTime dataOraRestituzione;
    private String noteRitiro;
    private String noteRestituzione;
    private boolean completatoCorrettamente;
    private StatoNoleggio stato;
    private Prenotazione prenotazione;

    public Noleggio(
            int idNoleggio,
            LocalDateTime dataOraRitiro,
            LocalDateTime dataOraRestituzione,
            String noteRitiro,
            String noteRestituzione,
            boolean completatoCorrettamente,
            StatoNoleggio stato,
            Prenotazione prenotazione) {
        setIdNoleggio(idNoleggio);
        setPrenotazione(prenotazione);
        setDataOraRitiro(dataOraRitiro);
        setDataOraRestituzione(dataOraRestituzione);
        setNoteRitiro(noteRitiro);
        setNoteRestituzione(noteRestituzione);
        setCompletatoCorrettamente(completatoCorrettamente);
        setStatoIniziale(stato);
    }

    /**
     * Termina il noleggio.
     */
    public void terminaNoleggio() {
        if (stato == StatoNoleggio.TERMINATO) {
            throw new NoleggioNonTerminabileException(
                    "Un noleggio terminato non puo' essere terminato di nuovo.");
        }
        if (dataOraRestituzione == null) {
            throw new NoleggioNonTerminabileException(
                    "La data di restituzione deve essere valorizzata per terminare il noleggio.");
        }
        setStato(StatoNoleggio.TERMINATO);
    }

    public int getIdNoleggio() {
        return idNoleggio;
    }

    public void setIdNoleggio(int idNoleggio) {
        if (idNoleggio <= 0) {
            throw new IllegalArgumentException("L'id noleggio deve essere maggiore di zero.");
        }
        this.idNoleggio = idNoleggio;
    }

    public LocalDateTime getDataOraRitiro() {
        return dataOraRitiro;
    }

    public void setDataOraRitiro(LocalDateTime dataOraRitiro) {
        if (dataOraRitiro == null) {
            throw new IllegalArgumentException("La data e ora di ritiro non puo' essere null.");
        }
        if (dataOraRestituzione != null && dataOraRestituzione.isBefore(dataOraRitiro)) {
            throw new IllegalArgumentException("La restituzione non puo' precedere il ritiro.");
        }
        this.dataOraRitiro = dataOraRitiro;
    }

    public LocalDateTime getDataOraRestituzione() {
        return dataOraRestituzione;
    }

    public void setDataOraRestituzione(LocalDateTime dataOraRestituzione) {
        if (dataOraRestituzione != null && dataOraRitiro != null && dataOraRestituzione.isBefore(dataOraRitiro)) {
            throw new IllegalArgumentException("La restituzione non puo' precedere il ritiro.");
        }
        this.dataOraRestituzione = dataOraRestituzione;
    }

    public String getNoteRitiro() {
        return noteRitiro;
    }

    public void setNoteRitiro(String noteRitiro) {
        this.noteRitiro = noteRitiro;
    }

    public String getNoteRestituzione() {
        return noteRestituzione;
    }

    public void setNoteRestituzione(String noteRestituzione) {
        this.noteRestituzione = noteRestituzione;
    }

    public boolean isCompletatoCorrettamente() {
        return completatoCorrettamente;
    }

    public void setCompletatoCorrettamente(boolean completatoCorrettamente) {
        this.completatoCorrettamente = completatoCorrettamente;
    }

    public StatoNoleggio getStato() {
        return stato;
    }

    private void setStatoIniziale(StatoNoleggio stato) {
        if (stato == null) {
            throw new IllegalArgumentException("Lo stato del noleggio non puo' essere null.");
        }
        if (stato == StatoNoleggio.TERMINATO && dataOraRestituzione == null) {
            throw new IllegalArgumentException(
                    "Un noleggio terminato deve avere una data di restituzione.");
        }
        this.stato = stato;
    }

    /**
     * Cambia lo stato del noleggio.
     *
     * <p>Gestisce anche sospensione e ripresa.
     *
     * @param nuovoStato nuovo stato
     */
    public void setStato(StatoNoleggio nuovoStato) {
        if (nuovoStato == null) {
            throw new IllegalArgumentException("Lo stato del noleggio non puo' essere null.");
        }
        if (stato == null) {
            this.stato = nuovoStato;
            return;
        }
        if (stato == StatoNoleggio.TERMINATO) {
            throw new TransizioneStatoNonValidaException("Lo stato TERMINATO e' terminale.");
        }
        if (stato == nuovoStato) {
            return;
        }
        boolean consentita = (stato == StatoNoleggio.IN_CORSO && nuovoStato == StatoNoleggio.SOSPESO)
                || (stato == StatoNoleggio.IN_CORSO && nuovoStato == StatoNoleggio.TERMINATO)
                || (stato == StatoNoleggio.SOSPESO && nuovoStato == StatoNoleggio.IN_CORSO)
                || (stato == StatoNoleggio.SOSPESO && nuovoStato == StatoNoleggio.TERMINATO);
        if (!consentita) {
            throw new TransizioneStatoNonValidaException("Transizione di stato del noleggio non consentita.");
        }
        if (nuovoStato == StatoNoleggio.TERMINATO && dataOraRestituzione == null) {
            throw new NoleggioNonTerminabileException(
                    "La data di restituzione deve essere valorizzata per terminare il noleggio.");
        }
        this.stato = nuovoStato;
    }

    public Prenotazione getPrenotazione() {
        return prenotazione;
    }

    public void setPrenotazione(Prenotazione prenotazione) {
        if (prenotazione == null) {
            throw new IllegalArgumentException("La prenotazione del noleggio non puo' essere null.");
        }
        this.prenotazione = prenotazione;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Noleggio noleggio)) {
            return false;
        }
        return idNoleggio == noleggio.idNoleggio;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idNoleggio);
    }

    @Override
    public String toString() {
        return "Noleggio{"
                + "idNoleggio=" + idNoleggio
                + ", dataOraRitiro=" + dataOraRitiro
                + ", dataOraRestituzione=" + dataOraRestituzione
                + ", noteRitiro='" + noteRitiro + '\''
                + ", noteRestituzione='" + noteRestituzione + '\''
                + ", completatoCorrettamente=" + completatoCorrettamente
                + ", stato=" + stato
                + ", prenotazioneId=" + prenotazione.getIdPrenotazione()
                + '}';
    }
}
