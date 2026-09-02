package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import exception.NoleggioNonTerminabileException;
import exception.TransizioneStatoNonValidaException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class NoleggioTest {
    @Test
    void creazioneValida() {
        Noleggio noleggio = ModelTestFactory.noleggio(StatoNoleggio.IN_CORSO);

        assertEquals(StatoNoleggio.IN_CORSO, noleggio.getStato());
    }

    @Test
    void restituzionePrecedenteAlRitiro() {
        assertThrows(IllegalArgumentException.class, () -> new Noleggio(
                1,
                LocalDateTime.of(2026, 8, 10, 9, 0),
                LocalDateTime.of(2026, 8, 10, 8, 59),
                null,
                null,
                false,
                StatoNoleggio.IN_CORSO,
                ModelTestFactory.prenotazione()));
    }

    @Test
    void inCorsoVersoSospeso() {
        Noleggio noleggio = ModelTestFactory.noleggio(StatoNoleggio.IN_CORSO);

        noleggio.setStato(StatoNoleggio.SOSPESO);

        assertEquals(StatoNoleggio.SOSPESO, noleggio.getStato());
    }

    @Test
    void sospesoVersoInCorso() {
        Noleggio noleggio = ModelTestFactory.noleggio(StatoNoleggio.SOSPESO);

        noleggio.setStato(StatoNoleggio.IN_CORSO);

        assertEquals(StatoNoleggio.IN_CORSO, noleggio.getStato());
    }

    @Test
    void terminazioneValida() {
        Noleggio noleggio = ModelTestFactory.noleggio(StatoNoleggio.IN_CORSO);
        noleggio.setDataOraRestituzione(LocalDateTime.of(2026, 8, 12, 17, 0));

        noleggio.terminaNoleggio();

        assertEquals(StatoNoleggio.TERMINATO, noleggio.getStato());
    }

    @Test
    void terminaRichiedeDataRestituzione() {
        Noleggio noleggio = ModelTestFactory.noleggio(StatoNoleggio.IN_CORSO);

        assertThrows(NoleggioNonTerminabileException.class, noleggio::terminaNoleggio);
    }

    @Test
    void statoInizialeTerminatoRichiedeDataRestituzione() {
        assertThrows(IllegalArgumentException.class, () -> new Noleggio(
                1,
                LocalDateTime.of(2026, 8, 10, 9, 0),
                null,
                null,
                null,
                false,
                StatoNoleggio.TERMINATO,
                ModelTestFactory.prenotazione()));
    }

    @Test
    void terminatoComeStatoTerminale() {
        Noleggio noleggio = ModelTestFactory.noleggio(StatoNoleggio.IN_CORSO);
        noleggio.setDataOraRestituzione(LocalDateTime.of(2026, 8, 12, 17, 0));
        noleggio.terminaNoleggio();

        assertThrows(TransizioneStatoNonValidaException.class, () -> noleggio.setStato(StatoNoleggio.IN_CORSO));
        assertThrows(NoleggioNonTerminabileException.class, noleggio::terminaNoleggio);
    }
}
