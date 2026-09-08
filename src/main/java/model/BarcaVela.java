package model;

import java.time.LocalDate;

/**
 * Barca a vela.
 */
public class BarcaVela extends Barca {
    private double superficieVelica;
    private double altezzaAlbero;

    public BarcaVela(
            String matricola,
            String nome,
            int capacitaPasseggeri,
            double tariffaGiornaliera,
            boolean richiedePatente,
            StatoBarca stato,
            LocalDate indisponibileFinoAl,
            Sede sede,
            double superficieVelica,
            double altezzaAlbero) {
        super(matricola, nome, capacitaPasseggeri, tariffaGiornaliera, richiedePatente, stato, indisponibileFinoAl, sede);
        setSuperficieVelica(superficieVelica);
        setAltezzaAlbero(altezzaAlbero);
    }

    public double getSuperficieVelica() {
        return superficieVelica;
    }

    public void setSuperficieVelica(double superficieVelica) {
        if (superficieVelica <= 0) {
            throw new IllegalArgumentException("La superficie velica deve essere maggiore di zero.");
        }
        this.superficieVelica = superficieVelica;
    }

    public double getAltezzaAlbero() {
        return altezzaAlbero;
    }

    public void setAltezzaAlbero(double altezzaAlbero) {
        if (altezzaAlbero <= 0) {
            throw new IllegalArgumentException("L'altezza dell'albero deve essere maggiore di zero.");
        }
        this.altezzaAlbero = altezzaAlbero;
    }

    @Override
    public String toString() {
        return super.toString().replace("}", "")
                + ", superficieVelica=" + superficieVelica
                + ", altezzaAlbero=" + altezzaAlbero
                + '}';
    }
}
