package dao.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

/**
 * Metodi utili per leggere e scrivere CSV.
 */
final class CsvUtils {
    private static final char SEPARATOR = ';';
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/uuuu HH:mm:ss")
            .withResolverStyle(ResolverStyle.STRICT);

    private CsvUtils() {
    }

    static void ensureFile(Path file, List<String> header) {
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (Files.notExists(file) || Files.size(file) == 0) {
                writeRows(file, header, List.of());
            } else {
                readRows(file, header);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException("Impossibile inizializzare il file CSV: " + file, ex);
        }
    }

    static List<List<String>> readRows(Path file, List<String> expectedHeader) {
        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            List<List<String>> rows = parse(content);
            if (rows.isEmpty()) {
                throw new IllegalStateException("Il file CSV non contiene l'intestazione: " + file);
            }
            if (!rows.get(0).equals(expectedHeader)) {
                throw new IllegalStateException(
                        "Intestazione CSV non valida in " + file + ". Attesa: "
                                + expectedHeader + ", trovata: " + rows.get(0));
            }
            return new ArrayList<>(rows.subList(1, rows.size()));
        } catch (IOException ex) {
            throw new UncheckedIOException("Errore durante la lettura del file CSV: " + file, ex);
        }
    }

    static void writeRows(Path file, List<String> header, List<List<String>> rows) {
        StringBuilder output = new StringBuilder();
        appendRow(output, header);
        for (List<String> row : rows) {
            appendRow(output, row);
        }

        Path absolute = file.toAbsolutePath();
        Path parent = absolute.getParent();
        try {
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path temp = Files.createTempFile(parent, absolute.getFileName().toString(), ".tmp");
            Files.writeString(temp, output.toString(), StandardCharsets.UTF_8);
            try {
                Files.move(temp, absolute, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException("Errore durante la scrittura del file CSV: " + file, ex);
        }
    }

    static void requireColumnCount(List<String> row, int expected, Path file) {
        if (row.size() != expected) {
            throw new IllegalStateException(
                    "Numero di colonne non valido in " + file + ": attese "
                            + expected + ", trovate " + row.size() + ". Riga: " + row);
        }
    }

    static int parseInt(String value, String field, Path file) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw invalidValue(field, value, file, ex);
        }
    }

    static double parseDouble(String value, String field, Path file) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            throw invalidValue(field, value, file, ex);
        }
    }

    static boolean parseBoolean(String value, String field, Path file) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if ("sì".equals(normalized) || "si".equals(normalized) || "true".equals(normalized)) {
            return true;
        }
        if ("no".equals(normalized) || "false".equals(normalized)) {
            return false;
        }
        throw invalidValue(field, value, file, null);
    }

    static String formatBoolean(boolean value) {
        return value ? "sì" : "no";
    }

    static LocalDate parseDate(String value, String field, Path file) {
        try {
            return LocalDate.parse(value, DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            try {
                // Accetta anche le vecchie date ISO
                return LocalDate.parse(value);
            } catch (DateTimeParseException legacyEx) {
                throw invalidValue(field, value, file, ex);
            }
        }
    }

    static String formatDate(LocalDate value) {
        return value.format(DATE_FORMATTER);
    }

    static String formatNullableDate(LocalDate value) {
        return value == null ? "" : formatDate(value);
    }

    static LocalDate parseNullableDate(String value, String field, Path file) {
        return value == null || value.isBlank() ? null : parseDate(value, field, file);
    }

    static LocalDateTime parseDateTime(String value, String field, Path file) {
        try {
            return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException ex) {
            try {
                // Accetta anche le vecchie date e ore ISO
                return LocalDateTime.parse(value);
            } catch (DateTimeParseException legacyEx) {
                throw invalidValue(field, value, file, ex);
            }
        }
    }

    static LocalDateTime parseNullableDateTime(String value, String field, Path file) {
        return value == null || value.isBlank() ? null : parseDateTime(value, field, file);
    }

    static String formatDateTime(LocalDateTime value) {
        return value.format(DATE_TIME_FORMATTER);
    }

    static String formatNullableDateTime(LocalDateTime value) {
        return value == null ? "" : formatDateTime(value);
    }

    static String nullable(String value) {
        return value == null ? "" : value;
    }

    static String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }

    private static IllegalStateException invalidValue(
            String field, String value, Path file, Exception cause) {
        String message = "Valore non valido per '" + field + "' nel file "
                + file + ": " + value;
        return cause == null
                ? new IllegalStateException(message)
                : new IllegalStateException(message, cause);
    }

    private static void appendRow(StringBuilder output, List<String> row) {
        for (int i = 0; i < row.size(); i++) {
            if (i > 0) {
                output.append(SEPARATOR);
            }
            output.append(escape(row.get(i)));
        }
        output.append(System.lineSeparator());
    }

    private static String escape(String value) {
        String safe = value == null ? "" : value;
        boolean quote = safe.indexOf(SEPARATOR) >= 0
                || safe.indexOf('"') >= 0
                || safe.indexOf('\n') >= 0
                || safe.indexOf('\r') >= 0;
        if (!quote) {
            return safe;
        }
        return '"' + safe.replace("\"", "\"\"") + '"';
    }

    private static List<List<String>> parse(String content) {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);

            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < content.length() && content.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    field.append(c);
                }
                continue;
            }

            if (c == '"') {
                if (field.length() != 0) {
                    throw new IllegalStateException("Virgolette CSV in posizione non valida.");
                }
                inQuotes = true;
            } else if (c == SEPARATOR) {
                row.add(field.toString());
                field.setLength(0);
            } else if (c == '\n') {
                row.add(field.toString());
                field.setLength(0);
                rows.add(row);
                row = new ArrayList<>();
            } else if (c == '\r') {
                if (i + 1 < content.length() && content.charAt(i + 1) == '\n') {
                    continue;
                }
                row.add(field.toString());
                field.setLength(0);
                rows.add(row);
                row = new ArrayList<>();
            } else {
                field.append(c);
            }
        }

        if (inQuotes) {
            throw new IllegalStateException("Campo CSV con virgolette non chiuse.");
        }

        if (field.length() > 0 || !row.isEmpty()) {
            row.add(field.toString());
            rows.add(row);
        }

        return rows;
    }
}
