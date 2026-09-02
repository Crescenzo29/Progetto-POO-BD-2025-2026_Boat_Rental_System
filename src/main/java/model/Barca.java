package model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Classe base per le barche.
 */
public abstract class Barca {
    private String matricola;
    private String nome;
    private int capacitaPasseggeri;
    private double tariffaGiornaliera;
    private boolean richiedePatente;
    private StatoBarca stato;
    private LocalDate indisponibileFinoAl;
    private Sede sede;

    protected Barca(
            String matricola,
            String nome,
            int capacitaPasseggeri,
            double tariffaGiornaliera,
            boolean richiedePatente,
            StatoBarca stato,
            LocalDate indisponibileFinoAl,
            Sede sede) {
        setMatricola(matricola);
        setNome(nome);
        setCapacitaPasseggeri(capacitaPasseggeri);
        setTariffaGiornaliera(tariffaGiornaliera);
        setRichiedePatente(richiedePatente);
        setStato(stato);
        setIndisponibileFinoAl(indisponibileFinoAl);
        setSede(sede);
    }

    /**
     * Controlla se la barca e' disponibile oggi.
     *
     * @return true se la barca e' disponibile
     */
    public boolean isDisponibile() {
        return stato == StatoBarca.DISPONIBILE
                && (indisponibileFinoAl == null || indisponibileFinoAl.isBefore(LocalDate.now()));
    }

    /**
     * Calcola il costo del noleggio.
     *
     * @param giorni giorni richiesti
     * @return costo totale
     */
    public double calcolaCosto(int giorni) {
        if (giorni <= 0) {
            throw new IllegalArgumentException("I giorni di noleggio devono essere maggiori di zero.");
        }
        return tariffaGiornaliera * giorni;
    }

    public String getMatricola() {
        return matricola;
    }

    public void setMatricola(String matricola) {
        this.matricola = requireNotBlank(matricola, "La matricola della barca non puo' essere vuota.");
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = requireNotBlank(nome, "Il nome della barca non puo' essere vuoto.");
    }

    public int getCapacitaPasseggeri() {
        return capacitaPasseggeri;
    }

    public void setCapacitaPasseggeri(int capacitaPasseggeri) {
        if (capacitaPasseggeri <= 0) {
            throw new IllegalArgumentException("La capacita' passeggeri deve essere maggiore di zero.");
        }
        this.capacitaPasseggeri = capacitaPasseggeri;
    }

    public double getTariffaGiornaliera() {
        return tariffaGiornaliera;
    }

    public void setTariffaGiornaliera(double tariffaGiornaliera) {
        if (tariffaGiornaliera <= 0) {
            throw new IllegalArgumentException("La tariffa giornaliera deve essere maggiore di zero.");
        }
        this.tariffaGiornaliera = tariffaGiornaliera;
    }

    public boolean isRichiedePatente() {
        return richiedePatente;
    }

    public void setRichiedePatente(boolean richiedePatente) {
        this.richiedePatente = richiedePatente;
    }

    public StatoBarca getStato() {
        return stato;
    }

    public void setStato(StatoBarca stato) {
        if (stato == null) {
            throw new IllegalArgumentException("Lo stato della barca non puo' essere null.");
        }
        this.stato = stato;
    }

    public LocalDate getIndisponibileFinoAl() {
        return indisponibileFinoAl;
    }

    public void setIndisponibileFinoAl(LocalDate indisponibileFinoAl) {
        this.indisponibileFinoAl = indisponibileFinoAl;
    }

    public Sede getSede() {
        return sede;
    }

    public void setSede(Sede sede) {
        if (sede == null) {
            throw new IllegalArgumentException("La sede della barca non puo' essere null.");
        }
        this.sede = sede;
    }

    private static String requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Barca barca)) {
            return false;
        }
        return Objects.equals(matricola, barca.matricola);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matricola);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{"
                + "matricola='" + matricola + '\''
                + ", nome='" + nome + '\''
                + ", capacitaPasseggeri=" + capacitaPasseggeri
                + ", tariffaGiornaliera=" + tariffaGiornaliera
                + ", richiedePatente=" + richiedePatente
                + ", stato=" + stato
                + ", indisponibileFinoAl=" + indisponibileFinoAl
                + ", sedeId=" + sede.getIdSede()
                + '}';
    }
}
