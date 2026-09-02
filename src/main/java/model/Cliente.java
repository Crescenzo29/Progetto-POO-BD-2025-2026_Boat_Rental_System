package model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Rappresenta un cliente del sistema.
 */
public class Cliente {
    private int idCliente;
    private String nome;
    private String cognome;
    private String email;
    private String passwordHash;
    private LocalDate dataNascita;
    private String numeroPatenteNautica;
    private LocalDate dataScadenzaPatente;

    public Cliente(
            int idCliente,
            String nome,
            String cognome,
            String email,
            String passwordHash,
            LocalDate dataNascita,
            String numeroPatenteNautica,
            LocalDate dataScadenzaPatente) {
        setIdCliente(idCliente);
        setNome(nome);
        setCognome(cognome);
        setEmail(email);
        setPasswordHash(passwordHash);
        setDataNascita(dataNascita);
        setPatenteNautica(numeroPatenteNautica, dataScadenzaPatente);
    }

    /**
     * Controlla se il cliente ha la patente nautica.
     *
     * @return true se la patente e' presente
     */
    public boolean haPatenteNautica() {
        return numeroPatenteNautica != null
                && !numeroPatenteNautica.isBlank()
                && dataScadenzaPatente != null;
    }

    /**
     * Controlla se la patente e' valida.
     *
     * @param data data da controllare
     * @return true se la patente e' valida
     */
    public boolean patenteValida(LocalDate data) {
        if (data == null) {
            throw new IllegalArgumentException("La data di verifica della patente non puo' essere null.");
        }
        return haPatenteNautica() && !dataScadenzaPatente.isBefore(data);
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        if (idCliente <= 0) {
            throw new IllegalArgumentException("L'id cliente deve essere maggiore di zero.");
        }
        this.idCliente = idCliente;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = requireNotBlank(nome, "Il nome del cliente non puo' essere vuoto.");
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = requireNotBlank(cognome, "Il cognome del cliente non puo' essere vuoto.");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = requireNotBlank(email, "L'email del cliente non puo' essere vuota.");
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = requireNotBlank(passwordHash, "La password hash non puo' essere vuota.");
    }

    public LocalDate getDataNascita() {
        return dataNascita;
    }

    public void setDataNascita(LocalDate dataNascita) {
        if (dataNascita == null) {
            throw new IllegalArgumentException("La data di nascita non puo' essere null.");
        }
        if (dataNascita.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La data di nascita non puo' essere futura.");
        }
        this.dataNascita = dataNascita;
    }

    public String getNumeroPatenteNautica() {
        return numeroPatenteNautica;
    }

    public LocalDate getDataScadenzaPatente() {
        return dataScadenzaPatente;
    }

    public void setPatenteNautica(String numeroPatenteNautica, LocalDate dataScadenzaPatente) {
        boolean numeroPresente = numeroPatenteNautica != null && !numeroPatenteNautica.isBlank();
        boolean scadenzaPresente = dataScadenzaPatente != null;
        if (numeroPresente != scadenzaPresente) {
            throw new IllegalArgumentException("Numero e data scadenza patente devono essere entrambi presenti o assenti.");
        }
        this.numeroPatenteNautica = numeroPresente ? numeroPatenteNautica : null;
        this.dataScadenzaPatente = dataScadenzaPatente;
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
        if (!(obj instanceof Cliente cliente)) {
            return false;
        }
        return idCliente == cliente.idCliente;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCliente);
    }

    @Override
    public String toString() {
        return "Cliente{"
                + "idCliente=" + idCliente
                + ", nome='" + nome + '\''
                + ", cognome='" + cognome + '\''
                + ", email='" + email + '\''
                + ", dataNascita=" + dataNascita
                + ", numeroPatenteNautica='" + numeroPatenteNautica + '\''
                + ", dataScadenzaPatente=" + dataScadenzaPatente
                + '}';
    }
}
