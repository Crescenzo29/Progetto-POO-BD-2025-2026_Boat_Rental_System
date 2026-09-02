package dao.impl;

import dao.NoleggioDAO;
import dao.PrenotazioneDAO;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import model.Noleggio;
import model.Prenotazione;
import model.StatoNoleggio;

/**
 * DAO CSV per i noleggi.
 */
public class NoleggioCsvDAO extends AbstractCsvDAO<Noleggio, Integer> implements NoleggioDAO {
    private static final List<String> HEADER = List.of(
            "idNoleggio", "dataOraRitiro", "dataOraRestituzione", "noteRitiro",
            "noteRestituzione", "completatoCorrettamente", "stato", "idPrenotazione");

    private final PrenotazioneDAO prenotazioneDAO;

    public NoleggioCsvDAO(Path file, PrenotazioneDAO prenotazioneDAO) {
        super(file, HEADER);
        if (prenotazioneDAO == null) {
            throw new IllegalArgumentException("PrenotazioneDAO non puo' essere null.");
        }
        this.prenotazioneDAO = prenotazioneDAO;
    }

    @Override
    protected Integer getId(Noleggio entity) {
        return entity.getIdNoleggio();
    }

    @Override
    protected Noleggio fromRow(List<String> row) {
        CsvUtils.requireColumnCount(row, 8, getFile());

        int idPrenotazione = CsvUtils.parseInt(row.get(7), "idPrenotazione", getFile());
        Prenotazione prenotazione = prenotazioneDAO.findById(idPrenotazione)
                .orElseThrow(() -> new IllegalStateException(
                        "Prenotazione " + idPrenotazione
                                + " non trovata durante il caricamento del noleggio " + row.get(0)));

        StatoNoleggio stato;
        try {
            stato = StatoNoleggio.valueOf(row.get(6));
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "Stato noleggio non valido nel file " + getFile() + ": " + row.get(6), ex);
        }

        return new Noleggio(
                CsvUtils.parseInt(row.get(0), "idNoleggio", getFile()),
                CsvUtils.parseDateTime(row.get(1), "dataOraRitiro", getFile()),
                CsvUtils.parseNullableDateTime(row.get(2), "dataOraRestituzione", getFile()),
                CsvUtils.emptyToNull(row.get(3)),
                CsvUtils.emptyToNull(row.get(4)),
                CsvUtils.parseBoolean(row.get(5), "completatoCorrettamente", getFile()),
                stato,
                prenotazione);
    }

    @Override
    protected List<String> toRow(Noleggio noleggio) {
        return List.of(
                Integer.toString(noleggio.getIdNoleggio()),
                CsvUtils.formatDateTime(noleggio.getDataOraRitiro()),
                CsvUtils.formatNullableDateTime(noleggio.getDataOraRestituzione()),
                CsvUtils.nullable(noleggio.getNoteRitiro()),
                CsvUtils.nullable(noleggio.getNoteRestituzione()),
                CsvUtils.formatBoolean(noleggio.isCompletatoCorrettamente()),
                noleggio.getStato().name(),
                Integer.toString(noleggio.getPrenotazione().getIdPrenotazione()));
    }

    @Override
    public Optional<Noleggio> findByPrenotazioneId(int idPrenotazione) {
        return findAll().stream()
                .filter(noleggio -> noleggio.getPrenotazione().getIdPrenotazione() == idPrenotazione)
                .findFirst();
    }
}
