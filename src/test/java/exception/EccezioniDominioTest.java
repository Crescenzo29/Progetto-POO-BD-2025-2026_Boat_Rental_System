package exception;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import model.BarcaMotore;
import model.Cliente;
import model.Manutenzione;
import model.Noleggio;
import model.Prenotazione;
import model.Sede;
import model.StatoBarca;
import model.StatoManutenzione;
import model.StatoNoleggio;
import model.StatoPrenotazione;
import org.junit.jupiter.api.Test;

class EccezioniDominioTest {
    private static final List<Class<? extends NoleggioBarcheException>> ECCEZIONI = List.of(
            ClienteMinorenneException.class,
            PatenteNauticaRichiestaException.class,
            PatenteNauticaScadutaException.class,
            CapacitaPasseggeriSuperataException.class,
            PeriodoPrenotazioneNonValidoException.class,
            BarcaNonDisponibileException.class,
            PrenotazioneSovrappostaException.class,
            PrenotazioneNonAnnullabileException.class,
            NoleggioNonAvviabileException.class,
            NoleggioGiaEsistenteException.class,
            NoleggioNonTerminabileException.class,
            ManutenzioneInConflittoException.class,
            ManutenzioneNonAvviabileException.class,
            TransizioneStatoNonValidaException.class);

    @Test
    void noleggioBarcheExceptionEstendeRuntimeException() {
        assertTrue(RuntimeException.class.isAssignableFrom(NoleggioBarcheException.class));
    }

    @Test
    void tutteLeQuattordiciEccezioniEstendonoNoleggioBarcheException() {
        for (Class<? extends NoleggioBarcheException> eccezione : ECCEZIONI) {
            assertTrue(NoleggioBarcheException.class.isAssignableFrom(eccezione));
        }
    }

    @Test
    void capacitaPasseggeriSuperataVieneLanciata() {
        assertThrows(CapacitaPasseggeriSuperataException.class, () -> new Prenotazione(
                1,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 12),
                7,
                true,
                StatoPrenotazione.CONFERMATA,
                cliente(),
                barca()));
    }

    @Test
    void periodoPrenotazioneNonValidoVieneLanciata() {
        assertThrows(PeriodoPrenotazioneNonValidoException.class, () -> new Prenotazione(
                1,
                LocalDate.of(2026, 8, 11),
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 12),
                2,
                true,
                StatoPrenotazione.CONFERMATA,
                cliente(),
                barca()));
    }

    @Test
    void prenotazioneNonAnnullabileVieneLanciata() {
        Prenotazione prenotazione = prenotazioneValida();
        prenotazione.avviaNoleggio();

        assertThrows(PrenotazioneNonAnnullabileException.class, prenotazione::annulla);
    }

    @Test
    void transizioneStatoNonValidaVieneLanciataDaPrenotazione() {
        Prenotazione prenotazione = prenotazioneValida();

        assertThrows(TransizioneStatoNonValidaException.class, prenotazione::completa);
    }

    @Test
    void transizioneStatoNonValidaVieneLanciataDaNoleggio() {
        Noleggio noleggio = noleggioTerminato();

        assertThrows(TransizioneStatoNonValidaException.class, () -> noleggio.setStato(StatoNoleggio.IN_CORSO));
    }

    @Test
    void noleggioNonTerminabileVieneLanciata() {
        Noleggio noleggio = new Noleggio(
                1,
                LocalDateTime.of(2026, 8, 10, 9, 0),
                null,
                null,
                null,
                false,
                StatoNoleggio.IN_CORSO,
                prenotazioneValida());

        assertThrows(NoleggioNonTerminabileException.class, noleggio::terminaNoleggio);
    }

    @Test
    void manutenzioneNonAvviabileVieneLanciata() {
        Manutenzione manutenzione = new Manutenzione(
                1,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 3),
                "Controllo motore",
                StatoManutenzione.IN_CORSO,
                barca());

        assertThrows(ManutenzioneNonAvviabileException.class, manutenzione::avvia);
    }

    private static Noleggio noleggioTerminato() {
        return new Noleggio(
                1,
                LocalDateTime.of(2026, 8, 10, 9, 0),
                LocalDateTime.of(2026, 8, 12, 17, 0),
                null,
                null,
                true,
                StatoNoleggio.TERMINATO,
                prenotazioneValida());
    }

    private static Prenotazione prenotazioneValida() {
        return new Prenotazione(
                1,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 12),
                4,
                true,
                StatoPrenotazione.CONFERMATA,
                cliente(),
                barca());
    }

    private static Cliente cliente() {
        return new Cliente(
                1,
                "Mario",
                "Rossi",
                "mario.rossi@example.com",
                "hash",
                LocalDate.of(1990, 1, 10),
                "PAT123",
                LocalDate.of(2027, 1, 1));
    }

    private static BarcaMotore barca() {
        return new BarcaMotore(
                "BM001",
                "Azzurra",
                6,
                120.0,
                true,
                StatoBarca.DISPONIBILE,
                null,
                new Sede(1, "Sede Porto", "Genova", "Molo 1"),
                150,
                80.0);
    }
}
