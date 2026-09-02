package model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ClienteTest {
    @Test
    void patenteAssente() {
        Cliente cliente = ModelTestFactory.clienteSenzaPatente();

        assertFalse(cliente.haPatenteNautica());
    }

    @Test
    void patentePresente() {
        Cliente cliente = ModelTestFactory.clienteConPatente(LocalDate.now().plusDays(30));

        assertTrue(cliente.haPatenteNautica());
    }

    @Test
    void patenteValidaAncheNelGiornoDiScadenza() {
        LocalDate scadenza = LocalDate.of(2026, 9, 1);
        Cliente cliente = ModelTestFactory.clienteConPatente(scadenza);

        assertTrue(cliente.patenteValida(scadenza));
    }

    @Test
    void patenteScaduta() {
        Cliente cliente = ModelTestFactory.clienteConPatente(LocalDate.of(2026, 8, 31));

        assertFalse(cliente.patenteValida(LocalDate.of(2026, 9, 1)));
    }

    @Test
    void dataNascitaFuturaNonValida() {
        assertThrows(IllegalArgumentException.class, () -> new Cliente(
                1,
                "Mario",
                "Rossi",
                "mario.rossi@example.com",
                "hash",
                LocalDate.now().plusDays(1),
                null,
                null));
    }

    @Test
    void numeroPatenteEScadenzaDevonoEssereCoerenti() {
        assertThrows(IllegalArgumentException.class, () -> new Cliente(
                1,
                "Mario",
                "Rossi",
                "mario.rossi@example.com",
                "hash",
                LocalDate.of(1990, 1, 10),
                "PAT123",
                null));
    }
}
