package dao.impl;

import dao.CrudDAO;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Base comune per i DAO CSV.
 *
 * <p>Legge il file e lo riscrive quando i dati cambiano.
 *
 * @param <T> tipo gestito
 * @param <ID> tipo dell'id
 */
abstract class AbstractCsvDAO<T, ID> implements CrudDAO<T, ID> {
    private final Path file;
    private final List<String> header;

    protected AbstractCsvDAO(Path file, List<String> header) {
        this.file = Objects.requireNonNull(file, "Il percorso CSV non puo' essere null.");
        this.header = List.copyOf(Objects.requireNonNull(header, "L'intestazione non puo' essere null."));
        CsvUtils.ensureFile(file, this.header);
    }

    protected final Path getFile() {
        return file;
    }

    protected abstract ID getId(T entity);

    protected abstract T fromRow(List<String> row);

    protected abstract List<String> toRow(T entity);

    @Override
    public synchronized Optional<T> findById(ID id) {
        Objects.requireNonNull(id, "L'identificativo non puo' essere null.");
        return readEntities().stream()
                .filter(entity -> Objects.equals(getId(entity), id))
                .findFirst();
    }

    @Override
    public synchronized List<T> findAll() {
        return new ArrayList<>(readEntities());
    }

    @Override
    public synchronized void save(T entity) {
        Objects.requireNonNull(entity, "L'entita' da salvare non puo' essere null.");
        ID id = Objects.requireNonNull(getId(entity), "L'identificativo dell'entita' non puo' essere null.");
        List<T> entities = readEntities();
        boolean exists = entities.stream().anyMatch(current -> Objects.equals(getId(current), id));
        if (exists) {
            throw new IllegalStateException("Esiste gia' un'entita' con identificativo: " + id);
        }
        entities.add(entity);
        writeEntities(entities);
    }

    @Override
    public synchronized void update(T entity) {
        Objects.requireNonNull(entity, "L'entita' da aggiornare non puo' essere null.");
        ID id = Objects.requireNonNull(getId(entity), "L'identificativo dell'entita' non puo' essere null.");
        List<T> entities = readEntities();
        for (int i = 0; i < entities.size(); i++) {
            if (Objects.equals(getId(entities.get(i)), id)) {
                entities.set(i, entity);
                writeEntities(entities);
                return;
            }
        }
        throw new IllegalStateException("Nessuna entita' esistente con identificativo: " + id);
    }

    @Override
    public synchronized boolean deleteById(ID id) {
        Objects.requireNonNull(id, "L'identificativo non puo' essere null.");
        List<T> entities = readEntities();
        boolean removed = entities.removeIf(entity -> Objects.equals(getId(entity), id));
        if (removed) {
            writeEntities(entities);
        }
        return removed;
    }

    private List<T> readEntities() {
        List<T> entities = new ArrayList<>();
        for (List<String> row : CsvUtils.readRows(file, header)) {
            if (row.stream().allMatch(String::isEmpty)) {
                continue;
            }
            entities.add(fromRow(row));
        }
        return entities;
    }

    private void writeEntities(List<T> entities) {
        List<List<String>> rows = entities.stream()
                .map(this::toRow)
                .toList();
        CsvUtils.writeRows(file, header, rows);
    }
}
