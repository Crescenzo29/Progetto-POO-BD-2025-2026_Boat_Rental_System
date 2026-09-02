package dao;

import java.util.Optional;
import model.Noleggio;

/**
 * DAO per i noleggi.
 */
public interface NoleggioDAO extends CrudDAO<Noleggio, Integer> {
    /**
     * Cerca il noleggio di una prenotazione.
     *
     * @param idPrenotazione id della prenotazione
     * @return noleggio trovato
     */
    Optional<Noleggio> findByPrenotazioneId(int idPrenotazione);
}
