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
import java.util.List;
import model.Barca;
import model.BarcaMotore;
import model.BarcaVela;
import model.Cliente;
import model.Prenotazione;
import model.Sede;
import model.StatoBarca;
import model.StatoPrenotazione;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NoleggioBarcheGuiControllerTest {
    @TempDir
    Path tempDir;

    private NoleggioBarcheController controller;
    private Sede sedeNapoli;
    private Sede sedePozzuoli;

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

        sedeNapoli = new Sede(1, "Porto di Napoli", "Napoli", "Molo Beverello");
        sedePozzuoli = new Sede(2, "Marina di Pozzuoli", "Pozzuoli", "Via Napoli 1");
        controller.aggiungiSede(sedeNapoli);
        controller.aggiungiSede(sedePozzuoli);
    }

    @Test
    void credenzialiAdminCorretteSonoAccettate() {
        assertTrue(controller.isAdmin("admin@noleggiobarche.it", "Admin123!"));
        assertTrue(controller.isAdmin("ADMIN@noleggiobarche.it", "Admin123!"));
    }

    @Test
    void credenzialiAdminErrateSonoRifiutate() {
        assertFalse(controller.isAdmin("admin@noleggiobarche.it", "sbagliata"));
        assertFalse(controller.isAdmin("utente@noleggiobarche.it", "Admin123!"));
    }

    @Test
    void autenticazioneClienteCorrettaRestituisceCliente() {
        Cliente cliente = cliente(1, "cliente@example.it", "Demo123!");
        controller.aggiungiCliente(cliente);

        assertEquals(cliente, controller.autenticaCliente("CLIENTE@example.it", "Demo123!").orElseThrow());
    }

    @Test
    void autenticazioneClienteErrataRestituisceEmpty() {
        controller.aggiungiCliente(cliente(1, "cliente@example.it", "Demo123!"));

        assertTrue(controller.autenticaCliente("cliente@example.it", "passwordErrata").isEmpty());
        assertTrue(controller.autenticaCliente("assente@example.it", "Demo123!").isEmpty());
    }

    @Test
    void hashPasswordDemoRimaneCompatibileConCsvDemo() {
        assertEquals(
                "588c55f3ce2b8569b153c5abbf13f9f74308b88a20017cc699b835cc93195d16",
                controller.generaPasswordHash("Demo123!"));
    }

    @Test
    void prossimiIdSonoCalcolatiDaMassimoEsistente() {
        Cliente cliente = cliente(5, "cliente@example.it", "Demo123!");
        Barca barca = barcaMotore("BR001", sedeNapoli, 6, 180.0, false);
        controller.aggiungiCliente(cliente);
        controller.aggiungiBarca(barca);
        controller.aggiungiPrenotazione(new Prenotazione(
                9,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 12),
                3,
                false,
                StatoPrenotazione.CONFERMATA,
                cliente,
                barca));

        assertEquals(6, controller.prossimoIdCliente());
        assertEquals(10, controller.prossimoIdPrenotazione());
        assertEquals(1, controller.prossimoIdNoleggio());
        assertEquals(1, controller.prossimoIdManutenzione());
    }

    @Test
    void ricercaBarcheFiltraTipoPatentePostiTariffaSedeEPeriodo() {
        Cliente cliente = cliente(1, "cliente@example.it", "Demo123!");
        Barca motore = barcaMotore("M001", sedeNapoli, 8, 220.0, true);
        Barca vela = barcaVela("V001", sedePozzuoli, 5, 150.0, false);
        controller.aggiungiCliente(cliente);
        controller.aggiungiBarca(motore);
        controller.aggiungiBarca(vela);
        controller.aggiungiPrenotazione(new Prenotazione(
                20,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 12),
                3,
                false,
                StatoPrenotazione.CONFERMATA,
                cliente,
                vela));

        List<Barca> risultato = controller.cercaBarche(
                "Barca a motore",
                true,
                6,
                250.0,
                sedeNapoli.getIdSede(),
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 12));

        assertEquals(List.of(motore), risultato);
    }

    private Cliente cliente(int id, String email, String password) {
        return new Cliente(
                id,
                "Nome",
                "Cognome",
                email,
                controller.generaPasswordHash(password),
                LocalDate.of(1990, 1, 1),
                null,
                null);
    }

    private Barca barcaMotore(String matricola, Sede sede, int capacita, double tariffa, boolean patente) {
        return new BarcaMotore(
                matricola,
                "Motore " + matricola,
                capacita,
                tariffa,
                patente,
                StatoBarca.DISPONIBILE,
                null,
                sede,
                120,
                250.0);
    }

    private Barca barcaVela(String matricola, Sede sede, int capacita, double tariffa, boolean patente) {
        return new BarcaVela(
                matricola,
                "Vela " + matricola,
                capacita,
                tariffa,
                patente,
                StatoBarca.DISPONIBILE,
                null,
                sede,
                45.0,
                14.0);
    }
}
