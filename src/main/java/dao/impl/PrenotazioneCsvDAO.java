package dao.impl;

import dao.BarcaDAO;
import dao.ClienteDAO;
import dao.PrenotazioneDAO;
import java.nio.file.Path;
import java.util.List;
import model.Barca;
import model.Cliente;
import model.Prenotazione;
import model.StatoPrenotazione;

/**
 * DAO CSV per le prenotazioni.
 */
public class PrenotazioneCsvDAO extends AbstractCsvDAO<Prenotazione, Integer> implements PrenotazioneDAO {
    private static final List<String> HEADER = List.of(
            "idPrenotazione", "dataPrenotazione", "dataInizio", "dataFine",
            "numeroPasseggeri", "conPatente", "stato", "idCliente", "matricolaBarca");

    private final ClienteDAO clienteDAO;
    private final BarcaDAO barcaDAO;

    public PrenotazioneCsvDAO(Path file, ClienteDAO clienteDAO, BarcaDAO barcaDAO) {
        super(file, HEADER);
        if (clienteDAO == null || barcaDAO == null) {
            throw new IllegalArgumentException("ClienteDAO e BarcaDAO non possono essere null.");
        }
        this.clienteDAO = clienteDAO;
        this.barcaDAO = barcaDAO;
    }

    @Override
    protected Integer getId(Prenotazione entity) {
        return entity.getIdPrenotazione();
    }

    @Override
    protected Prenotazione fromRow(List<String> row) {
        CsvUtils.requireColumnCount(row, 9, getFile());

        int idCliente = CsvUtils.parseInt(row.get(7), "idCliente", getFile());
        Cliente cliente = clienteDAO.findById(idCliente)
                .orElseThrow(() -> new IllegalStateException(
                        "Cliente " + idCliente + " non trovato durante il caricamento della prenotazione " + row.get(0)));

        String matricola = row.get(8);
        Barca barca = barcaDAO.findById(matricola)
                .orElseThrow(() -> new IllegalStateException(
                        "Barca " + matricola + " non trovata durante il caricamento della prenotazione " + row.get(0)));

        StatoPrenotazione stato;
        try {
            stato = StatoPrenotazione.valueOf(row.get(6));
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "Stato prenotazione non valido nel file " + getFile() + ": " + row.get(6), ex);
        }

        return new Prenotazione(
                CsvUtils.parseInt(row.get(0), "idPrenotazione", getFile()),
                CsvUtils.parseDate(row.get(1), "dataPrenotazione", getFile()),
                CsvUtils.parseDate(row.get(2), "dataInizio", getFile()),
                CsvUtils.parseDate(row.get(3), "dataFine", getFile()),
                CsvUtils.parseInt(row.get(4), "numeroPasseggeri", getFile()),
                CsvUtils.parseBoolean(row.get(5), "conPatente", getFile()),
                stato,
                cliente,
                barca);
    }

    @Override
    protected List<String> toRow(Prenotazione prenotazione) {
        return List.of(
                Integer.toString(prenotazione.getIdPrenotazione()),
                CsvUtils.formatDate(prenotazione.getDataPrenotazione()),
                CsvUtils.formatDate(prenotazione.getDataInizio()),
                CsvUtils.formatDate(prenotazione.getDataFine()),
                Integer.toString(prenotazione.getNumeroPasseggeri()),
                CsvUtils.formatBoolean(prenotazione.isConPatente()),
                prenotazione.getStato().name(),
                Integer.toString(prenotazione.getCliente().getIdCliente()),
                prenotazione.getBarca().getMatricola());
    }

    @Override
    public List<Prenotazione> findByClienteId(int idCliente) {
        return findAll().stream()
                .filter(prenotazione -> prenotazione.getCliente().getIdCliente() == idCliente)
                .toList();
    }

    @Override
    public List<Prenotazione> findByBarcaMatricola(String matricola) {
        if (matricola == null || matricola.isBlank()) {
            throw new IllegalArgumentException("La matricola da cercare non puo' essere vuota.");
        }
        return findAll().stream()
                .filter(prenotazione -> prenotazione.getBarca().getMatricola().equals(matricola))
                .toList();
    }
}
