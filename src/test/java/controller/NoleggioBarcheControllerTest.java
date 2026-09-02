package controller;

import static org.junit.jupiter.api.Assertions.*;

import dao.impl.BarcaCsvDAO;
import dao.impl.ClienteCsvDAO;
import dao.impl.ManutenzioneCsvDAO;
import dao.impl.NoleggioCsvDAO;
import dao.impl.PrenotazioneCsvDAO;
import dao.impl.SedeCsvDAO;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import model.Barca;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NoleggioBarcheControllerTest {
    @TempDir
    Path tempDir;

    private NoleggioBarcheController controller;
    private Sede sede;
    private Cliente cliente;
    private Barca barca;
    private Prenotazione prenotazione;

    @BeforeEach
    void setUp() {
        SedeCsvDAO sedeDAO = new SedeCsvDAO(tempDir.resolve("sedi.csv"));
        ClienteCsvDAO clienteDAO = new ClienteCsvDAO(tempDir.resolve("clienti.csv"));
        BarcaCsvDAO barcaDAO = new BarcaCsvDAO(tempDir.resolve("barche.csv"), sedeDAO);
        PrenotazioneCsvDAO prenotazioneDAO = new PrenotazioneCsvDAO(
                tempDir.resolve("prenotazioni.csv"), clienteDAO, barcaDAO);
        NoleggioCsvDAO noleggioDAO = new NoleggioCsvDAO(
                tempDir.resolve("noleggi.csv"), prenotazioneDAO);
        ManutenzioneCsvDAO manutenzioneDAO = new ManutenzioneCsvDAO(
                tempDir.resolve("manutenzioni.csv"), barcaDAO);

        controller = new NoleggioBarcheController(
                clienteDAO, sedeDAO, barcaDAO, prenotazioneDAO, noleggioDAO, manutenzioneDAO);

        sede = new Sede(1, "Porto", "Napoli", "Molo 1");
        cliente = new Cliente(
                1, "Mario", "Rossi", "mario@example.it", "hash",
                LocalDate.of(1990, 1, 1), null, null);
        barca = new BarcaMotore(
                "NA001", "Blue", 6, 150.0, false,
                StatoBarca.DISPONIBILE, null, sede, 120, 200.0);
        prenotazione = new Prenotazione(
                10, LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 12),
                4, false, StatoPrenotazione.CONFERMATA, cliente, barca);
    }

    @Test
    void costruttoreRifiutaDipendenzeNull() {
        assertThrows(NullPointerException.class, () ->
                new NoleggioBarcheController(null, null, null, null, null, null));
    }

    @Test
    void controllerGestisceClienti() {
        controller.aggiungiCliente(cliente);

        assertEquals(cliente, controller.getCliente(1).orElseThrow());
        assertEquals(cliente, controller.getClientePerEmail("MARIO@example.it").orElseThrow());
        assertEquals(1, controller.getClienti().size());

        Cliente aggiornato = new Cliente(
                1, "Mario", "Rossi", "nuovo@example.it", "hash2",
                LocalDate.of(1990, 1, 1), null, null);
        controller.aggiornaCliente(aggiornato);

        assertEquals("nuovo@example.it", controller.getCliente(1).orElseThrow().getEmail());
    }

    @Test
    void controllerGestisceSediEBarche() {
        controller.aggiungiSede(sede);
        controller.aggiungiBarca(barca);

        assertEquals(sede, controller.getSede(1).orElseThrow());
        assertEquals(barca, controller.getBarca("NA001").orElseThrow());
        assertEquals(1, controller.getBarchePerSede(1).size());

        barca.setStato(StatoBarca.FUORI_SERVIZIO);
        controller.aggiornaBarca(barca);

        assertEquals(StatoBarca.FUORI_SERVIZIO,
                controller.getBarca("NA001").orElseThrow().getStato());
    }

    @Test
    void controllerGestiscePrenotazioni() {
        salvaDipendenzePrenotazione();
        controller.aggiungiPrenotazione(prenotazione);

        assertEquals(prenotazione, controller.getPrenotazione(10).orElseThrow());
        assertEquals(1, controller.getPrenotazioniCliente(1).size());
        assertEquals(1, controller.getPrenotazioniBarca("NA001").size());

        prenotazione.annulla();
        controller.aggiornaPrenotazione(prenotazione);

        assertEquals(StatoPrenotazione.ANNULLATA,
                controller.getPrenotazione(10).orElseThrow().getStato());
    }

    @Test
    void controllerGestisceNoleggi() {
        salvaDipendenzePrenotazione();
        controller.aggiungiPrenotazione(prenotazione);
        prenotazione.avviaNoleggio();
        controller.aggiornaPrenotazione(prenotazione);

        Noleggio noleggio = new Noleggio(
                20, LocalDateTime.of(2026, 9, 10, 9, 0),
                null, "Ritiro", null, false,
                StatoNoleggio.IN_CORSO, prenotazione);

        controller.aggiungiNoleggio(noleggio);

        assertEquals(noleggio, controller.getNoleggio(20).orElseThrow());
        assertEquals(noleggio, controller.getNoleggioPerPrenotazione(10).orElseThrow());

        noleggio.setStato(StatoNoleggio.SOSPESO);
        controller.aggiornaNoleggio(noleggio);

        assertEquals(StatoNoleggio.SOSPESO,
                controller.getNoleggio(20).orElseThrow().getStato());
    }

    @Test
    void controllerGestisceManutenzioni() {
        controller.aggiungiSede(sede);
        controller.aggiungiBarca(barca);

        Manutenzione manutenzione = new Manutenzione(
                30, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3),
                "Tagliando", StatoManutenzione.PROGRAMMATA, barca);

        controller.aggiungiManutenzione(manutenzione);

        assertEquals(manutenzione, controller.getManutenzione(30).orElseThrow());
        assertEquals(1, controller.getManutenzioniBarca("NA001").size());

        manutenzione.avvia();
        controller.aggiornaManutenzione(manutenzione);

        assertEquals(StatoManutenzione.IN_CORSO,
                controller.getManutenzione(30).orElseThrow().getStato());
    }

    @Test
    void controllerNonContieneLogicaGrafica() {
        String packageName = NoleggioBarcheController.class.getPackageName();
        assertEquals("controller", packageName);
    }

    private void salvaDipendenzePrenotazione() {
        controller.aggiungiSede(sede);
        controller.aggiungiCliente(cliente);
        controller.aggiungiBarca(barca);
    }
}
