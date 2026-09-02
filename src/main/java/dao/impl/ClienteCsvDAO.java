package dao.impl;

import dao.ClienteDAO;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import model.Cliente;

/**
 * DAO CSV per i clienti.
 */
public class ClienteCsvDAO extends AbstractCsvDAO<Cliente, Integer> implements ClienteDAO {
    private static final List<String> HEADER = List.of(
            "idCliente", "nome", "cognome", "email", "passwordHash",
            "dataNascita", "numeroPatenteNautica", "dataScadenzaPatente");

    public ClienteCsvDAO(Path file) {
        super(file, HEADER);
    }

    @Override
    protected Integer getId(Cliente entity) {
        return entity.getIdCliente();
    }

    @Override
    protected Cliente fromRow(List<String> row) {
        CsvUtils.requireColumnCount(row, 8, getFile());
        return new Cliente(
                CsvUtils.parseInt(row.get(0), "idCliente", getFile()),
                row.get(1),
                row.get(2),
                row.get(3),
                row.get(4),
                CsvUtils.parseDate(row.get(5), "dataNascita", getFile()),
                CsvUtils.emptyToNull(row.get(6)),
                CsvUtils.parseNullableDate(row.get(7), "dataScadenzaPatente", getFile()));
    }

    @Override
    protected List<String> toRow(Cliente cliente) {
        return List.of(
                Integer.toString(cliente.getIdCliente()),
                cliente.getNome(),
                cliente.getCognome(),
                cliente.getEmail(),
                cliente.getPasswordHash(),
                CsvUtils.formatDate(cliente.getDataNascita()),
                CsvUtils.nullable(cliente.getNumeroPatenteNautica()),
                CsvUtils.formatNullableDate(cliente.getDataScadenzaPatente()));
    }

    @Override
    public Optional<Cliente> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("L'email da cercare non puo' essere vuota.");
        }
        return findAll().stream()
                .filter(cliente -> cliente.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }
}
