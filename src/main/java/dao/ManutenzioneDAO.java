package dao;

import java.util.List;
import model.Manutenzione;

/**
 * DAO per le manutenzioni.
 */
public interface ManutenzioneDAO extends CrudDAO<Manutenzione, Integer> {
    /**
     * Cerca le manutenzioni di una barca.
     *
     * @param matricola matricola della barca
     * @return manutenzioni della barca
     */
    List<Manutenzione> findByBarcaMatricola(String matricola);
}
