package dao;

import java.util.List;
import model.Prenotazione;

/**
 * DAO per le prenotazioni.
 */
public interface PrenotazioneDAO extends CrudDAO<Prenotazione, Integer> {
    /**
     * Cerca le prenotazioni di un cliente.
     *
     * @param idCliente id del cliente
     * @return prenotazioni del cliente
     */
    List<Prenotazione> findByClienteId(int idCliente);

    /**
     * Cerca le prenotazioni di una barca.
     *
     * @param matricola matricola della barca
     * @return prenotazioni della barca
     */
    List<Prenotazione> findByBarcaMatricola(String matricola);
}
