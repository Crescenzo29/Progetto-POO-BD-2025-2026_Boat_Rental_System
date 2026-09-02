package dao.impl;

import dao.BarcaDAO;
import dao.SedeDAO;
import java.nio.file.Path;
import java.util.List;
import model.Barca;
import model.BarcaMotore;
import model.BarcaVela;
import model.Sede;
import model.StatoBarca;

/**
 * DAO CSV per le barche.
 */
public class BarcaCsvDAO extends AbstractCsvDAO<Barca, String> implements BarcaDAO {
    private static final List<String> HEADER = List.of(
            "matricola", "tipo", "nome", "capacitaPasseggeri", "tariffaGiornaliera",
            "richiedePatente", "stato", "indisponibileFinoAl", "idSede",
            "potenzaMotoreCV", "capacitaSerbatoio", "superficieVelica", "altezzaAlbero");

    private final SedeDAO sedeDAO;

    public BarcaCsvDAO(Path file, SedeDAO sedeDAO) {
        super(file, HEADER);
        if (sedeDAO == null) {
            throw new IllegalArgumentException("SedeDAO non puo' essere null.");
        }
        this.sedeDAO = sedeDAO;
    }

    @Override
    protected String getId(Barca entity) {
        return entity.getMatricola();
    }

    @Override
    protected Barca fromRow(List<String> row) {
        CsvUtils.requireColumnCount(row, 13, getFile());

        int idSede = CsvUtils.parseInt(row.get(8), "idSede", getFile());
        Sede sede = sedeDAO.findById(idSede)
                .orElseThrow(() -> new IllegalStateException(
                        "Sede " + idSede + " non trovata durante il caricamento della barca " + row.get(0)));

        String tipo = row.get(1);
        String matricola = row.get(0);
        String nome = row.get(2);
        int capacita = CsvUtils.parseInt(row.get(3), "capacitaPasseggeri", getFile());
        double tariffa = CsvUtils.parseDouble(row.get(4), "tariffaGiornaliera", getFile());
        boolean richiedePatente = CsvUtils.parseBoolean(row.get(5), "richiedePatente", getFile());
        StatoBarca stato;
        try {
            stato = StatoBarca.valueOf(row.get(6));
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Stato barca non valido nel file " + getFile() + ": " + row.get(6), ex);
        }

        if ("MOTORE".equals(tipo)) {
            if (row.get(9).isBlank() || row.get(10).isBlank()
                    || !row.get(11).isBlank() || !row.get(12).isBlank()) {
                throw new IllegalStateException("Campi specifici MOTORE incoerenti per la barca " + matricola);
            }
            return new BarcaMotore(
                    matricola, nome, capacita, tariffa, richiedePatente, stato,
                    CsvUtils.parseNullableDate(row.get(7), "indisponibileFinoAl", getFile()),
                    sede,
                    CsvUtils.parseInt(row.get(9), "potenzaMotoreCV", getFile()),
                    CsvUtils.parseDouble(row.get(10), "capacitaSerbatoio", getFile()));
        }

        if ("VELA".equals(tipo)) {
            if (!row.get(9).isBlank() || !row.get(10).isBlank()
                    || row.get(11).isBlank() || row.get(12).isBlank()) {
                throw new IllegalStateException("Campi specifici VELA incoerenti per la barca " + matricola);
            }
            return new BarcaVela(
                    matricola, nome, capacita, tariffa, richiedePatente, stato,
                    CsvUtils.parseNullableDate(row.get(7), "indisponibileFinoAl", getFile()),
                    sede,
                    CsvUtils.parseDouble(row.get(11), "superficieVelica", getFile()),
                    CsvUtils.parseDouble(row.get(12), "altezzaAlbero", getFile()));
        }

        throw new IllegalStateException("Tipo barca non valido nel file " + getFile() + ": " + tipo);
    }

    @Override
    protected List<String> toRow(Barca barca) {
        String tipo;
        String potenzaMotoreCV = "";
        String capacitaSerbatoio = "";
        String superficieVelica = "";
        String altezzaAlbero = "";

        if (barca instanceof BarcaMotore motore) {
            tipo = "MOTORE";
            potenzaMotoreCV = Integer.toString(motore.getPotenzaMotoreCV());
            capacitaSerbatoio = Double.toString(motore.getCapacitaSerbatoio());
        } else if (barca instanceof BarcaVela vela) {
            tipo = "VELA";
            superficieVelica = Double.toString(vela.getSuperficieVelica());
            altezzaAlbero = Double.toString(vela.getAltezzaAlbero());
        } else {
            throw new IllegalArgumentException("Tipo concreto di Barca non supportato: " + barca.getClass().getName());
        }

        return List.of(
                barca.getMatricola(),
                tipo,
                barca.getNome(),
                Integer.toString(barca.getCapacitaPasseggeri()),
                Double.toString(barca.getTariffaGiornaliera()),
                CsvUtils.formatBoolean(barca.isRichiedePatente()),
                barca.getStato().name(),
                CsvUtils.formatNullableDate(barca.getIndisponibileFinoAl()),
                Integer.toString(barca.getSede().getIdSede()),
                potenzaMotoreCV,
                capacitaSerbatoio,
                superficieVelica,
                altezzaAlbero);
    }

    @Override
    public List<Barca> findBySedeId(int idSede) {
        return findAll().stream()
                .filter(barca -> barca.getSede().getIdSede() == idSede)
                .toList();
    }
}
