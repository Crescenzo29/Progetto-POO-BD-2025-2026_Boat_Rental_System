package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import exception.CapacitaPasseggeriSuperataException;
import exception.PeriodoPrenotazioneNonValidoException;
import exception.PrenotazioneNonAnnullabileException;
import exception.TransizioneStatoNonValidaException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class PrenotazioneTest {
    @Test
    void creazioneValida() {
        Prenotazione prenotazione = ModelTestFactory.prenotazione();

        assertEquals(StatoPrenotazione.CONFERMATA, prenotazione.getStato());
        assertEquals(4, prenotazione.getNumeroPasseggeri());
    }

    @Test
    void dateNonValide() {
        assertThrows(PeriodoPrenotazioneNonValidoException.class, () -> new Prenotazione(
                1,
                LocalDate.of(2026, 8, 11),
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 12),
                2,
                true,
                StatoPrenotazione.CONFERMATA,
                ModelTestFactory.clienteSenzaPatente(),
                ModelTestFactory.barcaMotore()));
        assertThrows(PeriodoPrenotazioneNonValidoException.class, () -> new Prenotazione(
                1,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 9),
                2,
                true,
                StatoPrenotazione.CONFERMATA,
                ModelTestFactory.clienteSenzaPatente(),
                ModelTestFactory.barcaMotore()));
    }

    @Test
    void numeroPasseggeriNonValido() {
        assertThrows(IllegalArgumentException.class, () -> new Prenotazione(
                1,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 12),
                0,
                true,
                StatoPrenotazione.CONFERMATA,
                ModelTestFactory.clienteSenzaPatente(),
                ModelTestFactory.barcaMotore()));
    }

    @Test
    void passeggeriOltreCapacita() {
        assertThrows(CapacitaPasseggeriSuperataException.class, () -> new Prenotazione(
                1,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 12),
                7,
                true,
                StatoPrenotazione.CONFERMATA,
                ModelTestFactory.clienteSenzaPatente(),
                ModelTestFactory.barcaMotore()));
    }

    @Test
    void calcoloCostoPerPiuGiorni() {
        Prenotazione prenotazione = ModelTestFactory.prenotazione();

        assertEquals(360.0, prenotazione.calcolaCostoTotale());
    }

    @Test
    void stessoGiornoValeUnGiorno() {
        Prenotazione prenotazione = new Prenotazione(
                1,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 10),
                2,
                true,
                StatoPrenotazione.CONFERMATA,
                ModelTestFactory.clienteSenzaPatente(),
                ModelTestFactory.barcaMotore());

        assertEquals(120.0, prenotazione.calcolaCostoTotale());
    }

    @Test
    void confermataVersoNoleggiata() {
        Prenotazione prenotazione = ModelTestFactory.prenotazione();

        prenotazione.avviaNoleggio();

        assertEquals(StatoPrenotazione.NOLEGGIATA, prenotazione.getStato());
    }

    @Test
    void confermataVersoAnnullata() {
        Prenotazione prenotazione = ModelTestFactory.prenotazione();

        prenotazione.annulla();

        assertEquals(StatoPrenotazione.ANNULLATA, prenotazione.getStato());
    }

    @Test
    void noleggiataVersoCompletata() {
        Prenotazione prenotazione = ModelTestFactory.prenotazione();

        prenotazione.avviaNoleggio();
        prenotazione.completa();

        assertEquals(StatoPrenotazione.COMPLETATA, prenotazione.getStato());
    }

    @Test
    void transizioniIllegali() {
        Prenotazione annullata = ModelTestFactory.prenotazione();
        annullata.annulla();

        Prenotazione noleggiata = ModelTestFactory.prenotazione();
        noleggiata.avviaNoleggio();

        assertThrows(TransizioneStatoNonValidaException.class, annullata::avviaNoleggio);
        assertThrows(PrenotazioneNonAnnullabileException.class, noleggiata::annulla);
        assertThrows(TransizioneStatoNonValidaException.class, ModelTestFactory.prenotazione()::completa);
    }

    @Test
    void contieneAvviaNoleggioENonConferma() {
        assertEquals(1, Arrays.stream(Prenotazione.class.getDeclaredMethods())
                .map(Method::getName)
                .filter("avviaNoleggio"::equals)
                .count());
        assertFalse(Arrays.stream(Prenotazione.class.getDeclaredMethods())
                .map(Method::getName)
                .anyMatch("conferma"::equals));
    }
}
