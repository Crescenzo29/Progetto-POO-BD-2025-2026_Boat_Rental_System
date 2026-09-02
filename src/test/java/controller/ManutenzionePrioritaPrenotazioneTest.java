package controller;

import static org.junit.jupiter.api.Assertions.*;

import dao.impl.BarcaCsvDAO;
import dao.impl.ClienteCsvDAO;
import dao.impl.ManutenzioneCsvDAO;
import dao.impl.NoleggioCsvDAO;
import dao.impl.PrenotazioneCsvDAO;
import dao.impl.SedeCsvDAO;
import exception.ManutenzioneInConflittoException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import model.Barca;
import model.BarcaMotore;
import model.Cliente;
import model.Manutenzione;
import model.Prenotazione;
import model.Sede;
import model.StatoBarca;
import model.StatoManutenzione;
import model.StatoPrenotazione;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ManutenzionePrioritaPrenotazioneTest {
    private static final LocalDate DATA_PRENOTAZIONE = LocalDate.of(2026, 9, 1);
    private static final LocalDate PRENOTAZIONE_INIZIO = LocalDate.of(2026, 9, 10);
    private static final LocalDate PRENOTAZIONE_FINE = LocalDate.of(2026, 9, 15);
    private static final LocalDate MANUTENZIONE_INIZIO = LocalDate.of(2026, 9, 12);
    private static final LocalDate MANUTENZIONE_FINE = LocalDate.of(2026, 9, 14);

    @TempDir
    Path tempDir;

    private NoleggioBarcheController controller;
    private ClienteCsvDAO clienteDAO;
    private PrenotazioneCsvDAO prenotazioneDAO;
    private NoleggioCsvDAO noleggioDAO;
    private ManutenzioneCsvDAO manutenzioneDAO;
    private Path prenotazioniCsv;
    private Path manutenzioniCsv;
    private Sede sede;
    private Barca barca;

    @BeforeEach
    void setUp() {
        SedeCsvDAO sedeDAO = new SedeCsvDAO(tempDir.resolve("sedi.csv"));
        clienteDAO = new ClienteCsvDAO(tempDir.resolve("clienti.csv"));
        BarcaCsvDAO barcaDAO = new BarcaCsvDAO(tempDir.resolve("barche.csv"), sedeDAO);
        prenotazioniCsv = tempDir.resolve("prenotazioni.csv");
        manutenzioniCsv = tempDir.resolve("manutenzioni.csv");
        prenotazioneDAO = new PrenotazioneCsvDAO(prenotazioniCsv, clienteDAO, barcaDAO);
        noleggioDAO = new NoleggioCsvDAO(tempDir.resolve("noleggi.csv"), prenotazioneDAO);
        manutenzioneDAO = new ManutenzioneCsvDAO(manutenzioniCsv, barcaDAO);
        controller = new NoleggioBarcheController(
                clienteDAO, sedeDAO, barcaDAO, prenotazioneDAO, noleggioDAO, manutenzioneDAO);

        sede = new Sede(1, "Porto Test", "Napoli", "Molo 1");
        barca = new BarcaMotore(
                "PR-M001", "Priorita", 6, 180.0, false,
                StatoBarca.DISPONIBILE, null, sede, 120, 250.0);
        controller.aggiungiSede(sede);
        controller.aggiungiBarca(barca);
    }

    @Test
    void manutenzioneSovrappostaAnnullaPrenotazioneConfermata() {
        salvaCliente(1);
        prenotazioneDAO.save(prenotazione(
                10, 1, PRENOTAZIONE_INIZIO, PRENOTAZIONE_FINE, StatoPrenotazione.CONFERMATA));

        controller.aggiungiManutenzione(manutenzione(20, MANUTENZIONE_INIZIO, MANUTENZIONE_FINE));

        assertEquals(StatoManutenzione.PROGRAMMATA,
                manutenzioneDAO.findById(20).orElseThrow().getStato());
        assertEquals(StatoPrenotazione.ANNULLATA,
                prenotazioneDAO.findById(10).orElseThrow().getStato());
    }

    @Test
    void manutenzioneSovrappostaAnnullaPiuPrenotazioniConfermate() {
        salvaCliente(1);
        prenotazioneDAO.save(prenotazione(
                11, 1, LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 12),
                StatoPrenotazione.CONFERMATA));
        prenotazioneDAO.save(prenotazione(
                12, 1, LocalDate.of(2026, 9, 13), LocalDate.of(2026, 9, 15),
                StatoPrenotazione.CONFERMATA));

        controller.aggiungiManutenzione(manutenzione(21, MANUTENZIONE_INIZIO, MANUTENZIONE_FINE));

        assertEquals(StatoPrenotazione.ANNULLATA,
                prenotazioneDAO.findById(11).orElseThrow().getStato());
        assertEquals(StatoPrenotazione.ANNULLATA,
                prenotazioneDAO.findById(12).orElseThrow().getStato());
        assertEquals(StatoManutenzione.PROGRAMMATA,
                manutenzioneDAO.findById(21).orElseThrow().getStato());
    }

    @Test
    void manutenzioneNonSovrappostaNonAnnullaPrenotazioneConfermata() {
        salvaCliente(1);
        prenotazioneDAO.save(prenotazione(
                13, 1, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 3),
                StatoPrenotazione.CONFERMATA));

        controller.aggiungiManutenzione(manutenzione(22, MANUTENZIONE_INIZIO, MANUTENZIONE_FINE));

        assertEquals(StatoPrenotazione.CONFERMATA,
                prenotazioneDAO.findById(13).orElseThrow().getStato());
        assertEquals(StatoManutenzione.PROGRAMMATA,
                manutenzioneDAO.findById(22).orElseThrow().getStato());
    }

    @Test
    void noleggioInCorsoSovrappostoRifiutaManutenzioneSenzaModificarePrenotazione() {
        salvaCliente(1);
        controller.aggiungiPrenotazione(prenotazione(
                14, 1, PRENOTAZIONE_INIZIO, PRENOTAZIONE_FINE, StatoPrenotazione.CONFERMATA));
        controller.avviaNoleggio(
                30, 14, LocalDateTime.of(2026, 9, 10, 9, 0), "Ritiro");

        assertThrows(ManutenzioneInConflittoException.class,
                () -> controller.aggiungiManutenzione(manutenzione(
                        23, MANUTENZIONE_INIZIO, MANUTENZIONE_FINE)));

        assertTrue(manutenzioneDAO.findById(23).isEmpty());
        assertEquals(StatoPrenotazione.NOLEGGIATA,
                prenotazioneDAO.findById(14).orElseThrow().getStato());
        assertTrue(noleggioDAO.findByPrenotazioneId(14).isPresent());
    }

    @Test
    void altraManutenzioneSovrappostaRifiutaNuovaManutenzione() {
        controller.aggiungiManutenzione(manutenzione(
                24, PRENOTAZIONE_INIZIO, PRENOTAZIONE_FINE));

        assertThrows(ManutenzioneInConflittoException.class,
                () -> controller.aggiungiManutenzione(manutenzione(
                        25, MANUTENZIONE_INIZIO, MANUTENZIONE_FINE)));

        assertTrue(manutenzioneDAO.findById(25).isEmpty());
    }

    @Test
    void prenotazioneGiaAnnullataSovrappostaNonBloccaManutenzione() {
        salvaCliente(1);
        prenotazioneDAO.save(prenotazione(
                15, 1, PRENOTAZIONE_INIZIO, PRENOTAZIONE_FINE, StatoPrenotazione.ANNULLATA));

        controller.aggiungiManutenzione(manutenzione(26, MANUTENZIONE_INIZIO, MANUTENZIONE_FINE));

        assertEquals(StatoPrenotazione.ANNULLATA,
                prenotazioneDAO.findById(15).orElseThrow().getStato());
        assertEquals(StatoManutenzione.PROGRAMMATA,
                manutenzioneDAO.findById(26).orElseThrow().getStato());
    }

    @Test
    void programmazioneManutenzionePersisteAnnullamentoPrenotazioneESalvataggioManutenzione() throws Exception {
        salvaCliente(1);
        prenotazioneDAO.save(prenotazione(
                16, 1, PRENOTAZIONE_INIZIO, PRENOTAZIONE_FINE, StatoPrenotazione.CONFERMATA));

        controller.aggiungiManutenzione(manutenzione(27, MANUTENZIONE_INIZIO, MANUTENZIONE_FINE));

        String prenotazioni = Files.readString(prenotazioniCsv);
        String manutenzioni = Files.readString(manutenzioniCsv);
        assertTrue(prenotazioni.contains(
                "16;01/09/2026;10/09/2026;15/09/2026;3;no;ANNULLATA;1;PR-M001"));
        assertTrue(manutenzioni.contains(
                "27;12/09/2026;14/09/2026;Controllo tecnico;PROGRAMMATA;PR-M001"));
    }

    private void salvaCliente(int id) {
        clienteDAO.save(new Cliente(
                id,
                "Cliente" + id,
                "Test",
                "cliente" + id + "@example.it",
                "hash" + id,
                LocalDate.of(1990, 1, 1),
                null,
                null));
    }

    private Prenotazione prenotazione(
            int id,
            int idCliente,
            LocalDate inizio,
            LocalDate fine,
            StatoPrenotazione stato) {
        return new Prenotazione(
                id,
                DATA_PRENOTAZIONE,
                inizio,
                fine,
                3,
                false,
                stato,
                clienteDAO.findById(idCliente).orElseThrow(),
                barca);
    }

    private Manutenzione manutenzione(int id, LocalDate inizio, LocalDate fine) {
        return new Manutenzione(
                id,
                inizio,
                fine,
                "Controllo tecnico",
                StatoManutenzione.PROGRAMMATA,
                barca);
    }
}
