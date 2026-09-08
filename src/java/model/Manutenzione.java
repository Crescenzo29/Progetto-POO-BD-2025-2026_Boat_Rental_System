package model;

import exception.ManutenzioneNonAvviabileException;
import exception.TransizioneStatoNonValidaException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Rappresenta una manutenzione di una barca.
 */
public class Manutenzione {
    private int idManutenzione;
    private LocalDate dataInizio;
    private LocalDate dataFine;
    private String descrizione;
    private StatoManutenzione stato;
    private Barca barca;

    public Manutenzione(
            int idManutenzione,
            LocalDate dataInizio,
            LocalDate dataFine,
            String descrizione,
            StatoManutenzione stato,
            Barca barca) {
        setIdManutenzione(idManutenzione);
        setPeriodo(dataInizio, dataFine);
        setDescrizione(descrizione);
        setStatoIniziale(stato);
        setBarca(barca);
    }

    /**
     * Avvia la manutenzione.
     */
    public void avvia() {
        if (stato != StatoManutenzione.PROGRAMMATA) {
            throw new ManutenzioneNonAvviabileException(
                    "Solo una manutenzione programmata puo' essere avviata.");
        }
        stato = StatoManutenzione.IN_CORSO;
    }

    /**
     * Completa la manutenzione.
     */
    public void completa() {
        if (stato != StatoManutenzione.IN_CORSO) {
            throw new TransizioneStatoNonValidaException(
                    "Solo una manutenzione in corso puo' essere completata.");
        }
        stato = StatoManutenzione.COMPLETATA;
    }

    public int getIdManutenzione() {
        return idManutenzione;
    }

    public void setIdManutenzione(int idManutenzione) {
        if (idManutenzione <= 0) {
            throw new IllegalArgumentException("L'id manutenzione deve essere maggiore di zero.");
        }
        this.idManutenzione = idManutenzione;
    }

    public LocalDate getDataInizio() {
        return dataInizio;
    }

    public LocalDate getDataFine() {
        return dataFine;
    }

    public void setPeriodo(LocalDate dataInizio, LocalDate dataFine) {
        if (dataInizio == null || dataFine == null) {
            throw new IllegalArgumentException("Le date della manutenzione non possono essere null.");
        }
        if (dataFine.isBefore(dataInizio)) {
            throw new IllegalArgumentException("La data fine manutenzione non puo' precedere la data inizio.");
        }
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        if (descrizione == null || descrizione.isBlank()) {
            throw new IllegalArgumentException("La descrizione manutenzione non puo' essere vuota.");
        }
        this.descrizione = descrizione;
    }

    public StatoManutenzione getStato() {
        return stato;
    }

    private void setStatoIniziale(StatoManutenzione stato) {
        if (stato == null) {
            throw new IllegalArgumentException("Lo stato manutenzione non puo' essere null.");
        }
        this.stato = stato;
    }

    public Barca getBarca() {
        return barca;
    }

    public void setBarca(Barca barca) {
        if (barca == null) {
            throw new IllegalArgumentException("La barca della manutenzione non puo' essere null.");
        }
        this.barca = barca;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Manutenzione manutenzione)) {
            return false;
        }
        return idManutenzione == manutenzione.idManutenzione;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idManutenzione);
    }

    @Override
    public String toString() {
        return "Manutenzione{"
                + "idManutenzione=" + idManutenzione
                + ", dataInizio=" + dataInizio
                + ", dataFine=" + dataFine
                + ", descrizione='" + descrizione + '\''
                + ", stato=" + stato
                + ", barcaMatricola='" + barca.getMatricola() + '\''
                + '}';
    }
}
