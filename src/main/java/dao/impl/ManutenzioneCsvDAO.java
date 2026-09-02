package dao.impl;

import dao.BarcaDAO;
import dao.ManutenzioneDAO;
import java.nio.file.Path;
import java.util.List;
import model.Barca;
import model.Manutenzione;
import model.StatoManutenzione;

/**
 * DAO CSV per le manutenzioni.
 */
public class ManutenzioneCsvDAO extends AbstractCsvDAO<Manutenzione, Integer> implements ManutenzioneDAO {
    private static final List<String> HEADER = List.of(
            "idManutenzione", "dataInizio", "dataFine", "descrizione", "stato", "matricolaBarca");

    private final BarcaDAO barcaDAO;

    public ManutenzioneCsvDAO(Path file, BarcaDAO barcaDAO) {
        super(file, HEADER);
        if (barcaDAO == null) {
            throw new IllegalArgumentException("BarcaDAO non puo' essere null.");
        }
        this.barcaDAO = barcaDAO;
    }

    @Override
    protected Integer getId(Manutenzione entity) {
        return entity.getIdManutenzione();
    }

    @Override
    protected Manutenzione fromRow(List<String> row) {
        CsvUtils.requireColumnCount(row, 6, getFile());

        String matricola = row.get(5);
        Barca barca = barcaDAO.findById(matricola)
                .orElseThrow(() -> new IllegalStateException(
                        "Barca " + matricola
                                + " non trovata durante il caricamento della manutenzione " + row.get(0)));

        StatoManutenzione stato;
        try {
            stato = StatoManutenzione.valueOf(row.get(4));
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "Stato manutenzione non valido nel file " + getFile() + ": " + row.get(4), ex);
        }

        return new Manutenzione(
                CsvUtils.parseInt(row.get(0), "idManutenzione", getFile()),
                CsvUtils.parseDate(row.get(1), "dataInizio", getFile()),
                CsvUtils.parseDate(row.get(2), "dataFine", getFile()),
                row.get(3),
                stato,
                barca);
    }

    @Override
    protected List<String> toRow(Manutenzione manutenzione) {
        return List.of(
                Integer.toString(manutenzione.getIdManutenzione()),
                CsvUtils.formatDate(manutenzione.getDataInizio()),
                CsvUtils.formatDate(manutenzione.getDataFine()),
                manutenzione.getDescrizione(),
                manutenzione.getStato().name(),
                manutenzione.getBarca().getMatricola());
    }

    @Override
    public List<Manutenzione> findByBarcaMatricola(String matricola) {
        if (matricola == null || matricola.isBlank()) {
            throw new IllegalArgumentException("La matricola da cercare non puo' essere vuota.");
        }
        return findAll().stream()
                .filter(manutenzione -> manutenzione.getBarca().getMatricola().equals(matricola))
                .toList();
    }
}
