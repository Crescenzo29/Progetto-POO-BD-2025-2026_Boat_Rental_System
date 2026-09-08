package dao;

import java.util.List;
import java.util.Optional;

/**
 * Operazioni base dei DAO.
 *
 * @param <T> tipo gestito
 * @param <ID> tipo dell'id
 */
public interface CrudDAO<T, ID> {
    /**
     * Cerca un elemento tramite id.
     *
     * @param id id da cercare
     * @return elemento trovato
     */
    Optional<T> findById(ID id);

    /**
     * Carica tutti gli elementi.
     *
     * @return lista degli elementi
     */
    List<T> findAll();

    /**
     * Salva un nuovo elemento.
     *
     * @param entity elemento da salvare
     */
    void save(T entity);

    /**
     * Aggiorna un elemento.
     *
     * @param entity elemento da aggiornare
     */
    void update(T entity);

    /**
     * Elimina un elemento.
     *
     * @return true se e' stato eliminato
     */
    boolean deleteById(ID id);
}
