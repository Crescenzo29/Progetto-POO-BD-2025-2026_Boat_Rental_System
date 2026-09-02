package dao;

import java.util.List;
import model.Barca;

/**
 * DAO per le barche.
 */
public interface BarcaDAO extends CrudDAO<Barca, String> {
    /**
     * Cerca le barche di una sede.
     *
     * @param idSede id della sede
     * @return barche della sede
     */
    List<Barca> findBySedeId(int idSede);
}
