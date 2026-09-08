package model;

import java.time.LocalDate;

/**
 * Barca a motore.
 */
public class BarcaMotore extends Barca {
    private int potenzaMotoreCV;
    private double capacitaSerbatoio;

    public BarcaMotore(
            String matricola,
            String nome,
            int capacitaPasseggeri,
            double tariffaGiornaliera,
            boolean richiedePatente,
            StatoBarca stato,
            LocalDate indisponibileFinoAl,
            Sede sede,
            int potenzaMotoreCV,
            double capacitaSerbatoio) {
        super(matricola, nome, capacitaPasseggeri, tariffaGiornaliera, richiedePatente, stato, indisponibileFinoAl, sede);
        setPotenzaMotoreCV(potenzaMotoreCV);
        setCapacitaSerbatoio(capacitaSerbatoio);
    }

    public int getPotenzaMotoreCV() {
        return potenzaMotoreCV;
    }

    public void setPotenzaMotoreCV(int potenzaMotoreCV) {
        if (potenzaMotoreCV <= 0) {
            throw new IllegalArgumentException("La potenza del motore deve essere maggiore di zero.");
        }
        this.potenzaMotoreCV = potenzaMotoreCV;
    }

    public double getCapacitaSerbatoio() {
        return capacitaSerbatoio;
    }

    public void setCapacitaSerbatoio(double capacitaSerbatoio) {
        if (capacitaSerbatoio <= 0) {
            throw new IllegalArgumentException("La capacita' del serbatoio deve essere maggiore di zero.");
        }
        this.capacitaSerbatoio = capacitaSerbatoio;
    }

    @Override
    public String toString() {
        return super.toString().replace("}", "")
                + ", potenzaMotoreCV=" + potenzaMotoreCV
                + ", capacitaSerbatoio=" + capacitaSerbatoio
                + '}';
    }
}
