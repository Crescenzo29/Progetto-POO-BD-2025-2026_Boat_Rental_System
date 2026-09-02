package dao.impl;

import dao.SedeDAO;
import java.nio.file.Path;
import java.util.List;
import model.Sede;

/**
 * DAO CSV per le sedi.
 */
public class SedeCsvDAO extends AbstractCsvDAO<Sede, Integer> implements SedeDAO {
    private static final List<String> HEADER = List.of(
            "idSede", "nome", "citta", "indirizzo");

    public SedeCsvDAO(Path file) {
        super(file, HEADER);
    }

    @Override
    protected Integer getId(Sede entity) {
        return entity.getIdSede();
    }

    @Override
    protected Sede fromRow(List<String> row) {
        CsvUtils.requireColumnCount(row, 4, getFile());
        return new Sede(
                CsvUtils.parseInt(row.get(0), "idSede", getFile()),
                row.get(1),
                row.get(2),
                row.get(3));
    }

    @Override
    protected List<String> toRow(Sede sede) {
        return List.of(
                Integer.toString(sede.getIdSede()),
                sede.getNome(),
                sede.getCitta(),
                sede.getIndirizzo());
    }
}
