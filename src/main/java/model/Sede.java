package model;

import java.util.Objects;

/**
 * Rappresenta una sede.
 */
public class Sede {
    private int idSede;
    private String nome;
    private String citta;
    private String indirizzo;

    public Sede(int idSede, String nome, String citta, String indirizzo) {
        setIdSede(idSede);
        setNome(nome);
        setCitta(citta);
        setIndirizzo(indirizzo);
    }

    public int getIdSede() {
        return idSede;
    }

    public void setIdSede(int idSede) {
        if (idSede <= 0) {
            throw new IllegalArgumentException("L'id della sede deve essere maggiore di zero.");
        }
        this.idSede = idSede;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = requireNotBlank(nome, "Il nome della sede non puo' essere vuoto.");
    }

    public String getCitta() {
        return citta;
    }

    public void setCitta(String citta) {
        this.citta = requireNotBlank(citta, "La citta' della sede non puo' essere vuota.");
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = requireNotBlank(indirizzo, "L'indirizzo della sede non puo' essere vuoto.");
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
        if (!(obj instanceof Sede sede)) {
            return false;
        }
        return idSede == sede.idSede;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idSede);
    }

    @Override
    public String toString() {
        return "Sede{"
                + "idSede=" + idSede
                + ", nome='" + nome + '\''
                + ", citta='" + citta + '\''
                + ", indirizzo='" + indirizzo + '\''
                + '}';
    }
}
