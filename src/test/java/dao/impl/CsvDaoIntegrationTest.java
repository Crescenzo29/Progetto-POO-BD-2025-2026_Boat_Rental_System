package dao.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import model.Barca;
import model.BarcaMotore;
import model.BarcaVela;
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
import org.junit.jupiter.api.io.TempDir;

class CsvDaoIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    void clienteCrudEFindByEmail() {
        ClienteCsvDAO dao = new ClienteCsvDAO(tempDir.resolve("clienti.csv"));
        Cliente cliente = cliente(1, "Mario", "mario@example.it");

        dao.save(cliente);

        assertEquals(cliente, dao.findById(1).orElseThrow());
        assertEquals(cliente, dao.findByEmail("MARIO@example.it").orElseThrow());
        assertEquals(1, dao.findAll().size());

        Cliente aggiornato = new Cliente(
                1, "Mario", "Rossi", "nuova@example.it", "hash2",
                LocalDate.of(1990, 1, 1), null, null);
        dao.update(aggiornato);
        assertEquals("nuova@example.it", dao.findById(1).orElseThrow().getEmail());

        assertTrue(dao.deleteById(1));
        assertFalse(dao.deleteById(1));
        assertTrue(dao.findAll().isEmpty());
    }

    @Test
    void campiCsvConSeparatoreVirgoletteERitornoACapoSonoConservati() {
        SedeCsvDAO dao = new SedeCsvDAO(tempDir.resolve("sedi.csv"));
        Sede sede = new Sede(1, "Porto; \"Nord\"", "Napoli", "Via A\nMare 1");

        dao.save(sede);

        Sede letta = dao.findById(1).orElseThrow();
        assertEquals(sede.getNome(), letta.getNome());
        assertEquals(sede.getIndirizzo(), letta.getIndirizzo());
    }

    @Test
    void salvataggioDuplicatoVieneRifiutato() {
        ClienteCsvDAO dao = new ClienteCsvDAO(tempDir.resolve("clienti.csv"));
        dao.save(cliente(1, "Mario", "mario@example.it"));

        assertThrows(IllegalStateException.class,
                () -> dao.save(cliente(1, "Luigi", "luigi@example.it")));
    }

    @Test
    void aggiornamentoDiEntitaInesistenteVieneRifiutato() {
        SedeCsvDAO dao = new SedeCsvDAO(tempDir.resolve("sedi.csv"));

        assertThrows(IllegalStateException.class,
                () -> dao.update(new Sede(99, "Sede", "Napoli", "Via Mare 1")));
    }

    @Test
    void barcaMotoreRoundTripERicercaPerSede() {
        SedeCsvDAO sedeDAO = new SedeCsvDAO(tempDir.resolve("sedi.csv"));
        Sede sede = new Sede(1, "Molo Beverello", "Napoli", "Porto");
        sedeDAO.save(sede);
        BarcaCsvDAO barcaDAO = new BarcaCsvDAO(tempDir.resolve("barche.csv"), sedeDAO);

        BarcaMotore barca = new BarcaMotore(
                "NA001", "Blue", 6, 150.50, true,
                StatoBarca.DISPONIBILE, null, sede, 120, 200.0);
        barcaDAO.save(barca);

        Barca letta = barcaDAO.findById("NA001").orElseThrow();
        assertInstanceOf(BarcaMotore.class, letta);
        assertEquals(120, ((BarcaMotore) letta).getPotenzaMotoreCV());
        assertEquals(1, barcaDAO.findBySedeId(1).size());
    }

    @Test
    void barcaVelaRoundTrip() {
        SedeCsvDAO sedeDAO = new SedeCsvDAO(tempDir.resolve("sedi.csv"));
        Sede sede = new Sede(1, "Porto", "Napoli", "Molo 1");
        sedeDAO.save(sede);
        BarcaCsvDAO barcaDAO = new BarcaCsvDAO(tempDir.resolve("barche.csv"), sedeDAO);

        BarcaVela barca = new BarcaVela(
                "VE001", "Wind", 4, 110.0, false,
                StatoBarca.MANUTENZIONE, LocalDate.of(2026, 9, 5),
                sede, 35.5, 12.2);
        barcaDAO.save(barca);

        Barca letta = barcaDAO.findById("VE001").orElseThrow();
        assertInstanceOf(BarcaVela.class, letta);
        assertEquals(LocalDate.of(2026, 9, 5), letta.getIndisponibileFinoAl());
        assertEquals(35.5, ((BarcaVela) letta).getSuperficieVelica());
    }

    @Test
    void barcaConSedeMancanteNonVieneRicostruita() throws Exception {
        Path file = tempDir.resolve("barche.csv");
        Files.writeString(file,
                "matricola;tipo;nome;capacitaPasseggeri;tariffaGiornaliera;richiedePatente;stato;"
                        + "indisponibileFinoAl;idSede;potenzaMotoreCV;capacitaSerbatoio;superficieVelica;altezzaAlbero\n"
                        + "NA001;MOTORE;Blue;6;150.0;true;DISPONIBILE;;99;120;200.0;;\n");

        SedeCsvDAO sedeDAO = new SedeCsvDAO(tempDir.resolve("sedi.csv"));
        BarcaCsvDAO barcaDAO = new BarcaCsvDAO(file, sedeDAO);

        assertThrows(IllegalStateException.class, barcaDAO::findAll);
    }

    @Test
    void prenotazioneRoundTripERicercheSpecifiche() {
        Fixture f = fixture();

        Prenotazione prenotazione = new Prenotazione(
                10, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 9, 12), 4, true,
                StatoPrenotazione.CONFERMATA, f.cliente(), f.barca());
        f.prenotazioneDAO().save(prenotazione);

        Prenotazione letta = f.prenotazioneDAO().findById(10).orElseThrow();
        assertEquals(1, letta.getCliente().getIdCliente());
        assertEquals("NA001", letta.getBarca().getMatricola());
        assertEquals(1, f.prenotazioneDAO().findByClienteId(1).size());
        assertEquals(1, f.prenotazioneDAO().findByBarcaMatricola("NA001").size());
    }

    @Test
    void noleggioRoundTripConCampiOpzionali() {
        Fixture f = fixtureConPrenotazione();

        NoleggioCsvDAO dao = new NoleggioCsvDAO(tempDir.resolve("noleggi.csv"), f.prenotazioneDAO());
        Noleggio noleggio = new Noleggio(
                20, LocalDateTime.of(2026, 9, 10, 9, 0),
                null, "Ritiro regolare", null, false,
                StatoNoleggio.IN_CORSO, f.prenotazione());

        dao.save(noleggio);

        Noleggio letto = dao.findByPrenotazioneId(10).orElseThrow();
        assertEquals(20, letto.getIdNoleggio());
        assertNull(letto.getDataOraRestituzione());
        assertNull(letto.getNoteRestituzione());
    }

    @Test
    void noleggioTerminatoRoundTrip() {
        Fixture f = fixtureConPrenotazione();

        NoleggioCsvDAO dao = new NoleggioCsvDAO(tempDir.resolve("noleggi.csv"), f.prenotazioneDAO());
        Noleggio noleggio = new Noleggio(
                21, LocalDateTime.of(2026, 9, 10, 9, 0),
                LocalDateTime.of(2026, 9, 10, 18, 0),
                null, "Tutto regolare", true,
                StatoNoleggio.TERMINATO, f.prenotazione());

        dao.save(noleggio);

        Noleggio letto = dao.findById(21).orElseThrow();
        assertEquals(StatoNoleggio.TERMINATO, letto.getStato());
        assertTrue(letto.isCompletatoCorrettamente());
    }

    @Test
    void manutenzioneRoundTripERicercaPerBarca() {
        Fixture f = fixture();

        ManutenzioneCsvDAO dao = new ManutenzioneCsvDAO(
                tempDir.resolve("manutenzioni.csv"), f.barcaDAO());
        Manutenzione manutenzione = new Manutenzione(
                30, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3),
                "Controllo motore; filtri", StatoManutenzione.PROGRAMMATA, f.barca());

        dao.save(manutenzione);

        Manutenzione letta = dao.findById(30).orElseThrow();
        assertEquals("NA001", letta.getBarca().getMatricola());
        assertEquals(1, dao.findByBarcaMatricola("NA001").size());
    }

    @Test
    void iFileMancantiVengonoInizializzatiConHeader() throws Exception {
        Path file = tempDir.resolve("clienti.csv");

        new ClienteCsvDAO(file);

        List<String> lines = Files.readAllLines(file);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).startsWith("idCliente;nome;cognome"));
    }

    @Test
    void intestazioneErrataVieneRifiutata() throws Exception {
        Path file = tempDir.resolve("clienti.csv");
        Files.writeString(file, "header;errato\n");

        assertThrows(IllegalStateException.class, () -> new ClienteCsvDAO(file));
    }

    private Fixture fixture() {
        SedeCsvDAO sedeDAO = new SedeCsvDAO(tempDir.resolve("sedi.csv"));
        Sede sede = new Sede(1, "Porto", "Napoli", "Molo 1");
        sedeDAO.save(sede);

        ClienteCsvDAO clienteDAO = new ClienteCsvDAO(tempDir.resolve("clienti.csv"));
        Cliente cliente = cliente(1, "Mario", "mario@example.it");
        clienteDAO.save(cliente);

        BarcaCsvDAO barcaDAO = new BarcaCsvDAO(tempDir.resolve("barche.csv"), sedeDAO);
        Barca barca = new BarcaMotore(
                "NA001", "Blue", 6, 150.0, true,
                StatoBarca.DISPONIBILE, null, sede, 120, 200.0);
        barcaDAO.save(barca);

        PrenotazioneCsvDAO prenotazioneDAO = new PrenotazioneCsvDAO(
                tempDir.resolve("prenotazioni.csv"), clienteDAO, barcaDAO);

        return new Fixture(cliente, barca, clienteDAO, barcaDAO, prenotazioneDAO, null);
    }

    private Fixture fixtureConPrenotazione() {
        Fixture base = fixture();
        Prenotazione prenotazione = new Prenotazione(
                10, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 9, 12), 4, true,
                StatoPrenotazione.CONFERMATA, base.cliente(), base.barca());
        base.prenotazioneDAO().save(prenotazione);
        return new Fixture(
                base.cliente(), base.barca(), base.clienteDAO(),
                base.barcaDAO(), base.prenotazioneDAO(), prenotazione);
    }

    private Cliente cliente(int id, String nome, String email) {
        return new Cliente(
                id, nome, "Rossi", email, "hash",
                LocalDate.of(1990, 1, 1), null, null);
    }

    private record Fixture(
            Cliente cliente,
            Barca barca,
            ClienteCsvDAO clienteDAO,
            BarcaCsvDAO barcaDAO,
            PrenotazioneCsvDAO prenotazioneDAO,
            Prenotazione prenotazione) {
    }
}
