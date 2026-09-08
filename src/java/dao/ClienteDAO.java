package dao;

import java.util.Optional;
import model.Cliente;

/**
 * DAO per i clienti.
 */
public interface ClienteDAO extends CrudDAO<Cliente, Integer> {
    /**
     * Cerca un cliente tramite email.
     *
     * @param email email del cliente
     * @return cliente trovato
     */
    Optional<Cliente> findByEmail(String email);
}
