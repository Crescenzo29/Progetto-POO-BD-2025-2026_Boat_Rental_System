package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

final class ModelTestFactory {
    private ModelTestFactory() {
    }

    static Sede sede() {
        return new Sede(1, "Sede Porto", "Genova", "Molo 1");
    }

    static Cliente clienteSenzaPatente() {
        return new Cliente(
                1,
                "Mario",
                "Rossi",
                "mario.rossi@example.com",
                "hash",
                LocalDate.of(1990, 1, 10),
                null,
                null);
    }

    static Cliente clienteConPatente(LocalDate scadenza) {
        return new Cliente(
                2,
                "Laura",
                "Bianchi",
                "laura.bianchi@example.com",
                "hash",
                LocalDate.of(1988, 6, 5),
                "PAT123",
                scadenza);
    }

    static BarcaMotore barcaMotore() {
        return new BarcaMotore(
                "BM001",
                "Azzurra",
                6,
                120.0,
                true,
                StatoBarca.DISPONIBILE,
                null,
                sede(),
                150,
                80.0);
    }

    static BarcaVela barcaVela() {
        return new BarcaVela(
                "BV001",
                "Maestrale",
                4,
                90.0,
                false,
                StatoBarca.DISPONIBILE,
                null,
                sede(),
                35.0,
                12.0);
    }

    static Prenotazione prenotazione() {
        return new Prenotazione(
                1,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 12),
                4,
                true,
                StatoPrenotazione.CONFERMATA,
                clienteConPatente(LocalDate.of(2027, 1, 1)),
                barcaMotore());
    }

    static Noleggio noleggio(StatoNoleggio stato) {
        return new Noleggio(
                1,
                LocalDateTime.of(2026, 8, 10, 9, 0),
                null,
                "Ritiro regolare",
                null,
                false,
                stato,
                prenotazione());
    }
}
