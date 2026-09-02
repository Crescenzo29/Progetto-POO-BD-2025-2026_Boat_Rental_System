package model;

import exception.CapacitaPasseggeriSuperataException;
import exception.PeriodoPrenotazioneNonValidoException;
import exception.PrenotazioneNonAnnullabileException;
import exception.TransizioneStatoNonValidaException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Rappresenta una prenotazione.
 */
public class Prenotazione {
    private int idPrenotazione;
    private LocalDate dataPrenotazione;
    private LocalDate dataInizio;
    private LocalDate dataFine;
    private int numeroPasseggeri;
    private boolean conPatente;
    private StatoPrenotazione stato;
    private Cliente cliente;
    private Barca barca;

    public Prenotazione(
            int idPrenotazione,
            LocalDate dataPrenotazione,
            LocalDate dataInizio,
            LocalDate dataFine,
            int numeroPasseggeri,
            boolean conPatente,
            StatoPrenotazione stato,
            Cliente cliente,
            Barca barca) {
        setIdPrenotazione(idPrenotazione);
        setDate(dataPrenotazione, dataInizio, dataFine);
        setCliente(cliente);
        setBarca(barca);
        setNumeroPasseggeri(numeroPasseggeri);
        setConPatente(conPatente);
        setStatoIniziale(stato);
    }

    /**
     * Avvia il noleggio della prenotazione.
     */
    public void avviaNoleggio() {
        if (stato != StatoPrenotazione.CONFERMATA) {
            throw new TransizioneStatoNonValidaException(
                    "Solo una prenotazione confermata puo' avviare un noleggio.");
        }
        stato = StatoPrenotazione.NOLEGGIATA;
    }

    /**
     * Annulla la prenotazione.
     */
    public void annulla() {
        if (stato != StatoPrenotazione.CONFERMATA) {
            throw new PrenotazioneNonAnnullabileException(
                    "Solo una prenotazione confermata puo' essere annullata.");
        }
        stato = StatoPrenotazione.ANNULLATA;
    }

    /**
     * Completa la prenotazione.
     */
    public void completa() {
        if (stato != StatoPrenotazione.NOLEGGIATA) {
            throw new TransizioneStatoNonValidaException(
                    "Solo una prenotazione noleggiata puo' essere completata.");
        }
        stato = StatoPrenotazione.COMPLETATA;
    }

    /**
     * Calcola il costo totale.
     *
     * @return costo totale della prenotazione
     */
    public double calcolaCostoTotale() {
        long giorni = ChronoUnit.DAYS.between(dataInizio, dataFine) + 1;
        return barca.calcolaCosto(Math.toIntExact(giorni));
    }

    public int getIdPrenotazione() {
        return idPrenotazione;
    }

    public void setIdPrenotazione(int idPrenotazione) {
        if (idPrenotazione <= 0) {
            throw new IllegalArgumentException("L'id prenotazione deve essere maggiore di zero.");
        }
        this.idPrenotazione = idPrenotazione;
    }

    public LocalDate getDataPrenotazione() {
        return dataPrenotazione;
    }

    public LocalDate getDataInizio() {
        return dataInizio;
    }

    public LocalDate getDataFine() {
        return dataFine;
    }

    public void setDate(LocalDate dataPrenotazione, LocalDate dataInizio, LocalDate dataFine) {
        if (dataPrenotazione == null || dataInizio == null || dataFine == null) {
            throw new IllegalArgumentException("Le date della prenotazione non possono essere null.");
        }
        if (dataPrenotazione.isAfter(dataInizio)) {
            throw new PeriodoPrenotazioneNonValidoException(
                    "La data di prenotazione non puo' essere successiva alla data di inizio.");
        }
        if (dataFine.isBefore(dataInizio)) {
            throw new PeriodoPrenotazioneNonValidoException(
                    "La data di fine non puo' precedere la data di inizio.");
        }
        this.dataPrenotazione = dataPrenotazione;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
    }

    public int getNumeroPasseggeri() {
        return numeroPasseggeri;
    }

    public void setNumeroPasseggeri(int numeroPasseggeri) {
        if (numeroPasseggeri <= 0) {
            throw new IllegalArgumentException("Il numero passeggeri deve essere maggiore di zero.");
        }
        if (barca != null && numeroPasseggeri > barca.getCapacitaPasseggeri()) {
            throw new CapacitaPasseggeriSuperataException(
                    "Il numero di passeggeri supera la capacita' massima della barca.");
        }
        this.numeroPasseggeri = numeroPasseggeri;
    }

    public boolean isConPatente() {
        return conPatente;
    }

    public void setConPatente(boolean conPatente) {
        this.conPatente = conPatente;
    }

    public StatoPrenotazione getStato() {
        return stato;
    }

    private void setStatoIniziale(StatoPrenotazione stato) {
        if (stato == null) {
            throw new IllegalArgumentException("Lo stato della prenotazione non puo' essere null.");
        }
        this.stato = stato;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("Il cliente della prenotazione non puo' essere null.");
        }
        this.cliente = cliente;
    }

    public Barca getBarca() {
        return barca;
    }

    public void setBarca(Barca barca) {
        if (barca == null) {
            throw new IllegalArgumentException("La barca della prenotazione non puo' essere null.");
        }
        if (numeroPasseggeri > 0 && numeroPasseggeri > barca.getCapacitaPasseggeri()) {
            throw new CapacitaPasseggeriSuperataException(
                    "Il numero di passeggeri supera la capacita' massima della barca.");
        }
        this.barca = barca;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Prenotazione prenotazione)) {
            return false;
        }
        return idPrenotazione == prenotazione.idPrenotazione;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPrenotazione);
    }

    @Override
    public String toString() {
        return "Prenotazione{"
                + "idPrenotazione=" + idPrenotazione
                + ", dataPrenotazione=" + dataPrenotazione
                + ", dataInizio=" + dataInizio
                + ", dataFine=" + dataFine
                + ", numeroPasseggeri=" + numeroPasseggeri
                + ", conPatente=" + conPatente
                + ", stato=" + stato
                + ", clienteId=" + cliente.getIdCliente()
                + ", barcaMatricola='" + barca.getMatricola() + '\''
                + '}';
    }
}
