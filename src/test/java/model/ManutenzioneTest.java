package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import exception.ManutenzioneNonAvviabileException;
import exception.TransizioneStatoNonValidaException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ManutenzioneTest {
    @Test
    void periodoValido() {
        Manutenzione manutenzione = new Manutenzione(
                1,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 3),
                "Controllo motore",
                StatoManutenzione.PROGRAMMATA,
                ModelTestFactory.barcaMotore());

        assertEquals(StatoManutenzione.PROGRAMMATA, manutenzione.getStato());
    }

    @Test
    void periodoNonValido() {
        assertThrows(IllegalArgumentException.class, () -> new Manutenzione(
                1,
                LocalDate.of(2026, 9, 3),
                LocalDate.of(2026, 9, 1),
                "Controllo motore",
                StatoManutenzione.PROGRAMMATA,
                ModelTestFactory.barcaMotore()));
    }

    @Test
    void programmataVersoInCorso() {
        Manutenzione manutenzione = manutenzioneProgrammata();

        manutenzione.avvia();

        assertEquals(StatoManutenzione.IN_CORSO, manutenzione.getStato());
    }

    @Test
    void inCorsoVersoCompletata() {
        Manutenzione manutenzione = manutenzioneProgrammata();

        manutenzione.avvia();
        manutenzione.completa();

        assertEquals(StatoManutenzione.COMPLETATA, manutenzione.getStato());
    }

    @Test
    void transizioniIllegali() {
        Manutenzione programmata = manutenzioneProgrammata();
        Manutenzione completata = manutenzioneProgrammata();
        completata.avvia();
        completata.completa();

        assertThrows(TransizioneStatoNonValidaException.class, programmata::completa);
        assertThrows(ManutenzioneNonAvviabileException.class, completata::avvia);
        assertThrows(TransizioneStatoNonValidaException.class, completata::completa);
    }

    private Manutenzione manutenzioneProgrammata() {
        return new Manutenzione(
                1,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 3),
                "Controllo motore",
                StatoManutenzione.PROGRAMMATA,
                ModelTestFactory.barcaMotore());
    }
}
