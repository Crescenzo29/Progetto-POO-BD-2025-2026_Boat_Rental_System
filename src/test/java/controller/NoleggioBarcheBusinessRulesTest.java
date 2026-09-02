package controller;

import static org.junit.jupiter.api.Assertions.*;

import dao.impl.BarcaCsvDAO;
import dao.impl.ClienteCsvDAO;
import dao.impl.ManutenzioneCsvDAO;
import dao.impl.NoleggioCsvDAO;
import dao.impl.PrenotazioneCsvDAO;
import dao.impl.SedeCsvDAO;
import exception.BarcaNonDisponibileException;
import exception.CapacitaPasseggeriSuperataException;
import exception.ClienteMinorenneException;
import exception.ManutenzioneInConflittoException;
import exception.ManutenzioneNonAvviabileException;
import exception.NoleggioGiaEsistenteException;
import exception.NoleggioNonAvviabileException;
import exception.PatenteNauticaRichiestaException;
import exception.PatenteNauticaScadutaException;
import exception.PrenotazioneNonAnnullabileException;
import exception.PrenotazioneSovrappostaException;
import exception.TransizioneStatoNonValidaException;
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

class NoleggioBarcheBusinessRulesTest {
    private static final LocalDate DATA_PRENOTAZIONE = LocalDate.of(2026, 9, 1);
    private static final LocalDate INIZIO = LocalDate.of(2026, 10, 10);
    private static final LocalDate FINE = LocalDate.of(2026, 10, 12);

    @TempDir
    Path tempDir;

    private NoleggioBarcheController controller;
    private Sede sede;

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

        sede = new Sede(1, "Porto Centrale", "Napoli", "Molo 1");
        controller.aggiungiSede(sede);
    }

    @Test
    void clienteConEmailDuplicataVieneRifiutato() {
        controller.aggiungiCliente(cliente(1, "mario@example.it"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.aggiungiCliente(cliente(2, "MARIO@example.it")));

        assertEquals("Esiste già un cliente registrato con questa email.", ex.getMessage());
    }

    @Test
    void aggiornamentoClienteMantieneLaPropriaEmail() {
        controller.aggiungiCliente(cliente(1, "mario@example.it"));

        controller.aggiornaCliente(cliente(1, "MARIO@example.it"));

        assertEquals("MARIO@example.it", controller.getCliente(1).orElseThrow().getEmail());
    }

    @Test
    void aggiornamentoClienteNonUsaEmailDiUnAltroCliente() {
        controller.aggiungiCliente(cliente(1, "mario@example.it"));
        controller.aggiungiCliente(cliente(2, "lucia@example.it"));

        assertThrows(IllegalArgumentException.class,
                () -> controller.aggiornaCliente(cliente(2, "MARIO@example.it")));
    }

    @Test
    void prenotazioneDiClienteMaggiorenneVieneSalvataConfermata() {
        Cliente cliente = cliente(1, "adulto@example.it");
        Barca barca = barca("BR001", false);
        salvaClienteEBarca(cliente, barca);

        controller.aggiungiPrenotazione(prenotazione(10, cliente, barca));

        assertEquals(StatoPrenotazione.CONFERMATA,
                controller.getPrenotazione(10).orElseThrow().getStato());
    }

    @Test
    void prenotazioneDiClienteMinorenneVieneRifiutata() {
        Cliente cliente = cliente(1, "minore@example.it", LocalDate.of(2010, 1, 1), null);
        Barca barca = barca("BR002", false);
        salvaClienteEBarca(cliente, barca);

        assertThrows(ClienteMinorenneException.class,
                () -> controller.aggiungiPrenotazione(prenotazione(11, cliente, barca)));
    }

    @Test
    void prenotazioneSenzaPatenteSuBarcaCheNonLaRichiedeVieneAccettata() {
        Cliente cliente = cliente(1, "senza-patente@example.it");
        Barca barca = barca("BR003", false);
        salvaClienteEBarca(cliente, barca);

        controller.aggiungiPrenotazione(prenotazione(12, cliente, barca, 3, false));

        assertTrue(controller.getPrenotazione(12).isPresent());
    }

    @Test
    void barcaConPatenteRichiestaRifiutaFlagPatenteAssente() {
        Cliente cliente = cliente(1, "patentato@example.it", LocalDate.of(1990, 1, 1), FINE.plusYears(2));
        Barca barca = barca("BR004", true);
        salvaClienteEBarca(cliente, barca);

        assertThrows(PatenteNauticaRichiestaException.class,
                () -> controller.aggiungiPrenotazione(prenotazione(13, cliente, barca, 3, false)));
    }

    @Test
    void barcaConPatenteRichiestaRifiutaClienteSenzaPatente() {
        Cliente cliente = cliente(1, "nessuna-patente@example.it");
        Barca barca = barca("BR005", true);
        salvaClienteEBarca(cliente, barca);

        assertThrows(PatenteNauticaRichiestaException.class,
                () -> controller.aggiungiPrenotazione(prenotazione(14, cliente, barca, 3, true)));
    }

    @Test
    void barcaConPatenteRichiestaRifiutaPatenteScadutaNelPeriodo() {
        Cliente cliente = cliente(1, "patente-scaduta@example.it", LocalDate.of(1990, 1, 1), FINE.minusDays(1));
        Barca barca = barca("BR006", true);
        salvaClienteEBarca(cliente, barca);

        assertThrows(PatenteNauticaScadutaException.class,
                () -> controller.aggiungiPrenotazione(prenotazione(15, cliente, barca, 3, true)));
    }

    @Test
    void barcaConPatenteRichiestaAccettaPatenteValida() {
        Cliente cliente = cliente(1, "patente-valida@example.it", LocalDate.of(1990, 1, 1), FINE.plusYears(1));
        Barca barca = barca("BR007", true);
        salvaClienteEBarca(cliente, barca);

        controller.aggiungiPrenotazione(prenotazione(16, cliente, barca, 3, true));

        assertTrue(controller.getPrenotazione(16).isPresent());
    }

    @Test
    void patenteDichiarataRichiedePatenteRealeAncheSeLaBarcaNonLaRichiede() {
        Cliente cliente = cliente(1, "flag-patente@example.it");
        Barca barca = barca("BR008", false);
        salvaClienteEBarca(cliente, barca);

        assertThrows(PatenteNauticaRichiestaException.class,
                () -> controller.aggiungiPrenotazione(prenotazione(17, cliente, barca, 3, true)));
    }

    @Test
    void prenotazioneOltreCapacitaVieneRifiutataDalController() {
        Cliente cliente = cliente(1, "capacita@example.it");
        Barca barca = barca("BR009", false, StatoBarca.DISPONIBILE, null, 6);
        Prenotazione prenotazione = prenotazione(18, cliente, barca, 4, false);
        barca.setCapacitaPasseggeri(3);
        salvaClienteEBarca(cliente, barca);

        assertThrows(CapacitaPasseggeriSuperataException.class,
                () -> controller.aggiungiPrenotazione(prenotazione));
    }

    @Test
    void prenotazioneRifiutaBarcaFuoriServizio() {
        Cliente cliente = cliente(1, "fuori-servizio@example.it");
        Barca barca = barca("BR010", false, StatoBarca.FUORI_SERVIZIO, null, 6);
        salvaClienteEBarca(cliente, barca);

        assertThrows(BarcaNonDisponibileException.class,
                () -> controller.aggiungiPrenotazione(prenotazione(19, cliente, barca)));
    }

    @Test
    void prenotazioneRifiutaBarcaIndisponibileFinoAllaDataDiInizio() {
        Cliente cliente = cliente(1, "indisponibile@example.it");
        Barca barca = barca("BR011", false, StatoBarca.DISPONIBILE, INIZIO, 6);
        salvaClienteEBarca(cliente, barca);

        assertThrows(BarcaNonDisponibileException.class,
                () -> controller.aggiungiPrenotazione(prenotazione(20, cliente, barca)));
    }

    @Test
    void prenotazioneSovrappostaVieneRifiutata() {
        Cliente cliente = cliente(1, "overlap@example.it");
        Barca barca = barca("BR012", false);
        salvaClienteEBarca(cliente, barca);
        controller.aggiungiPrenotazione(prenotazione(21, cliente, barca));

        assertThrows(PrenotazioneSovrappostaException.class,
                () -> controller.aggiungiPrenotazione(prenotazione(
                        22, cliente, barca, INIZIO.plusDays(2), FINE.plusDays(3), 3, false,
                        StatoPrenotazione.CONFERMATA)));
    }

    @Test
    void prenotazioneAnnullataNonBloccaNuovaPrenotazione() {
        Cliente cliente = cliente(1, "annullata@example.it");
        Barca barca = barca("BR013", false);
        salvaClienteEBarca(cliente, barca);
        controller.aggiungiPrenotazione(prenotazione(23, cliente, barca));
        controller.annullaPrenotazione(23);

        controller.aggiungiPrenotazione(prenotazione(24, cliente, barca));

        assertEquals(StatoPrenotazione.CONFERMATA,
                controller.getPrenotazione(24).orElseThrow().getStato());
    }

    @Test
    void prenotazioneRifiutaPeriodoConManutenzioneProgrammata() {
        Cliente cliente = cliente(1, "manutenzione-prenotazione@example.it");
        Barca barca = barca("BR014", false);
        salvaClienteEBarca(cliente, barca);
        controller.aggiungiManutenzione(manutenzione(30, barca, INIZIO, FINE, StatoManutenzione.PROGRAMMATA));

        assertThrows(BarcaNonDisponibileException.class,
                () -> controller.aggiungiPrenotazione(prenotazione(25, cliente, barca)));
    }

    @Test
    void annullaPrenotazioneConfermataAggiornaLoStato() {
        Cliente cliente = cliente(1, "annulla@example.it");
        Barca barca = barca("BR015", false);
        salvaClienteEBarca(cliente, barca);
        controller.aggiungiPrenotazione(prenotazione(26, cliente, barca));

        controller.annullaPrenotazione(26);

        assertEquals(StatoPrenotazione.ANNULLATA,
                controller.getPrenotazione(26).orElseThrow().getStato());
    }

    @Test
    void annullaPrenotazioneNonConfermataVieneRifiutata() {
        Cliente cliente = cliente(1, "annulla-invalid@example.it");
        Barca barca = barca("BR016", false);
        salvaClienteEBarca(cliente, barca);
        controller.aggiungiPrenotazione(prenotazione(27, cliente, barca));
        controller.annullaPrenotazione(27);

        assertThrows(PrenotazioneNonAnnullabileException.class,
                () -> controller.annullaPrenotazione(27));
    }

    @Test
    void nuovaPrenotazioneNonConfermataVieneRifiutata() {
        Cliente cliente = cliente(1, "stato-prenotazione@example.it");
        Barca barca = barca("BR017", false);
        salvaClienteEBarca(cliente, barca);

        assertThrows(TransizioneStatoNonValidaException.class,
                () -> controller.aggiungiPrenotazione(prenotazione(
                        28, cliente, barca, INIZIO, FINE, 3, false, StatoPrenotazione.ANNULLATA)));
    }

    @Test
    void avviaNoleggioDaPrenotazioneConfermata() {
        Cliente cliente = cliente(1, "start@example.it");
        Barca barca = barca("NR001", false);
        salvaPrenotazioneBase(40, cliente, barca);

        Noleggio noleggio = controller.avviaNoleggio(50, 40, LocalDateTime.of(2026, 10, 10, 9, 0), "Ritiro");

        assertEquals(StatoNoleggio.IN_CORSO, noleggio.getStato());
        assertEquals(StatoPrenotazione.NOLEGGIATA,
                controller.getPrenotazione(40).orElseThrow().getStato());
        assertEquals(StatoBarca.NOLEGGIATA, controller.getBarca("NR001").orElseThrow().getStato());
    }

    @Test
    void avviaNoleggioRifiutaPrenotazioneNonConfermata() {
        Cliente cliente = cliente(1, "start-invalid@example.it");
        Barca barca = barca("NR002", false);
        Prenotazione prenotazione = salvaPrenotazioneBase(41, cliente, barca);
        prenotazione.avviaNoleggio();
        controller.aggiornaPrenotazione(prenotazione);

        assertThrows(NoleggioNonAvviabileException.class,
                () -> controller.avviaNoleggio(51, 41, LocalDateTime.of(2026, 10, 10, 9, 0), "Ritiro"));
    }

    @Test
    void avviaNoleggioRifiutaSecondoNoleggioSullaStessaPrenotazione() {
        Cliente cliente = cliente(1, "secondo-noleggio@example.it");
        Barca barca = barca("NR003", false);
        Prenotazione prenotazione = salvaPrenotazioneBase(42, cliente, barca);
        controller.aggiungiNoleggio(new Noleggio(
                52, LocalDateTime.of(2026, 10, 10, 9, 0),
                null, "Ritiro", null, false, StatoNoleggio.IN_CORSO, prenotazione));

        assertThrows(NoleggioGiaEsistenteException.class,
                () -> controller.avviaNoleggio(53, 42, LocalDateTime.of(2026, 10, 10, 10, 0), "Ritiro"));
    }

    @Test
    void avviaNoleggioRifiutaBarcaNonOperativa() {
        Cliente cliente = cliente(1, "barca-non-operativa@example.it");
        Barca barca = barca("NR004", false);
        salvaPrenotazioneBase(43, cliente, barca);
        barca.setStato(StatoBarca.FUORI_SERVIZIO);
        controller.aggiornaBarca(barca);

        assertThrows(BarcaNonDisponibileException.class,
                () -> controller.avviaNoleggio(54, 43, LocalDateTime.of(2026, 10, 10, 9, 0), "Ritiro"));
    }

    @Test
    void avviaNoleggioRifiutaRitiroFuoriPeriodo() {
        Cliente cliente = cliente(1, "ritiro-fuori-periodo@example.it");
        Barca barca = barca("NR005", false);
        salvaPrenotazioneBase(44, cliente, barca);

        assertThrows(NoleggioNonAvviabileException.class,
                () -> controller.avviaNoleggio(55, 44, LocalDateTime.of(2026, 10, 9, 9, 0), "Ritiro"));
    }

    @Test
    void sospendiERiprendiNoleggioAggiornanoSoloLoStatoDelNoleggio() {
        Cliente cliente = cliente(1, "sospendi@example.it");
        Barca barca = barca("NR006", false);
        salvaPrenotazioneBase(45, cliente, barca);
        controller.avviaNoleggio(56, 45, LocalDateTime.of(2026, 10, 10, 9, 0), "Ritiro");

        controller.sospendiNoleggio(56);
        assertEquals(StatoNoleggio.SOSPESO, controller.getNoleggio(56).orElseThrow().getStato());
        assertEquals(StatoBarca.NOLEGGIATA, controller.getBarca("NR006").orElseThrow().getStato());

        controller.riprendiNoleggio(56);
        assertEquals(StatoNoleggio.IN_CORSO, controller.getNoleggio(56).orElseThrow().getStato());
    }

    @Test
    void terminaNoleggioCorrettoCompletaPrenotazioneENoleggioERendeBarcaDisponibile() {
        Cliente cliente = cliente(1, "termina-ok@example.it");
        Barca barca = barca("NR007", false);
        salvaPrenotazioneBase(46, cliente, barca);
        controller.avviaNoleggio(57, 46, LocalDateTime.of(2026, 10, 10, 9, 0), "Ritiro");

        controller.terminaNoleggio(57, LocalDateTime.of(2026, 10, 12, 18, 0), "Ok", true);

        assertEquals(StatoNoleggio.TERMINATO, controller.getNoleggio(57).orElseThrow().getStato());
        assertEquals(StatoPrenotazione.COMPLETATA, controller.getPrenotazione(46).orElseThrow().getStato());
        assertEquals(StatoBarca.DISPONIBILE, controller.getBarca("NR007").orElseThrow().getStato());
    }

    @Test
    void terminaNoleggioConProblemaMetteBarcaFuoriServizio() {
        Cliente cliente = cliente(1, "termina-problema@example.it");
        Barca barca = barca("NR008", false);
        salvaPrenotazioneBase(47, cliente, barca);
        controller.avviaNoleggio(58, 47, LocalDateTime.of(2026, 10, 10, 9, 0), "Ritiro");

        controller.terminaNoleggio(58, LocalDateTime.of(2026, 10, 12, 18, 0), "Danno", false);

        assertEquals(StatoNoleggio.TERMINATO, controller.getNoleggio(58).orElseThrow().getStato());
        assertEquals(StatoBarca.FUORI_SERVIZIO, controller.getBarca("NR008").orElseThrow().getStato());
    }

    @Test
    void manutenzioneProgrammataValidaVieneSalvata() {
        Barca barca = barca("MN001", false);
        controller.aggiungiBarca(barca);

        controller.aggiungiManutenzione(manutenzione(60, barca, INIZIO, FINE, StatoManutenzione.PROGRAMMATA));

        assertEquals(StatoManutenzione.PROGRAMMATA,
                controller.getManutenzione(60).orElseThrow().getStato());
    }

    @Test
    void manutenzioneSovrappostaAnnullaPrenotazioneConfermata() {
        Cliente cliente = cliente(1, "conflitto-prenotazione@example.it");
        Barca barca = barca("MN002", false);
        salvaPrenotazioneBase(61, cliente, barca);

        controller.aggiungiManutenzione(manutenzione(
                62, barca, INIZIO.plusDays(1), FINE.plusDays(1), StatoManutenzione.PROGRAMMATA));

        assertEquals(StatoManutenzione.PROGRAMMATA, controller.getManutenzione(62).orElseThrow().getStato());
        assertEquals(StatoPrenotazione.ANNULLATA, controller.getPrenotazione(61).orElseThrow().getStato());
    }

    @Test
    void manutenzioneInConflittoConAltraManutenzioneVieneRifiutata() {
        Barca barca = barca("MN003", false);
        controller.aggiungiBarca(barca);
        controller.aggiungiManutenzione(manutenzione(63, barca, INIZIO, FINE, StatoManutenzione.PROGRAMMATA));

        assertThrows(ManutenzioneInConflittoException.class,
                () -> controller.aggiungiManutenzione(manutenzione(
                        64, barca, INIZIO.plusDays(2), FINE.plusDays(2), StatoManutenzione.PROGRAMMATA)));
    }

    @Test
    void avviaManutenzioneValidaImpostaBarcaInManutenzione() {
        Barca barca = barca("MN004", false);
        controller.aggiungiBarca(barca);
        controller.aggiungiManutenzione(manutenzione(65, barca, INIZIO, FINE, StatoManutenzione.PROGRAMMATA));

        controller.avviaManutenzione(65);

        assertEquals(StatoManutenzione.IN_CORSO, controller.getManutenzione(65).orElseThrow().getStato());
        assertEquals(StatoBarca.MANUTENZIONE, controller.getBarca("MN004").orElseThrow().getStato());
    }

    @Test
    void avviaManutenzioneRifiutaBarcaNoleggiata() {
        Cliente cliente = cliente(1, "manutenzione-noleggiata@example.it");
        Barca barca = barca("MN005", false);
        salvaPrenotazioneBase(66, cliente, barca);
        controller.aggiungiManutenzione(manutenzione(
                67, barca, FINE.plusDays(10), FINE.plusDays(12), StatoManutenzione.PROGRAMMATA));
        controller.avviaNoleggio(68, 66, LocalDateTime.of(2026, 10, 10, 9, 0), "Ritiro");

        assertThrows(ManutenzioneNonAvviabileException.class,
                () -> controller.avviaManutenzione(67));
    }

    @Test
    void avviaManutenzioneRifiutaBarcaFuoriServizio() {
        Barca barca = barca("MN006", false);
        controller.aggiungiBarca(barca);
        controller.aggiungiManutenzione(manutenzione(69, barca, INIZIO, FINE, StatoManutenzione.PROGRAMMATA));
        controller.mettiBarcaFuoriServizio("MN006", FINE.plusDays(3));

        assertThrows(ManutenzioneNonAvviabileException.class,
                () -> controller.avviaManutenzione(69));
    }

    @Test
    void completaManutenzioneAggiornaStatoERendeBarcaDisponibile() {
        Barca barca = barca("MN007", false);
        controller.aggiungiBarca(barca);
        controller.aggiungiManutenzione(manutenzione(70, barca, INIZIO, FINE, StatoManutenzione.PROGRAMMATA));
        controller.avviaManutenzione(70);

        controller.completaManutenzione(70);

        assertEquals(StatoManutenzione.COMPLETATA, controller.getManutenzione(70).orElseThrow().getStato());
        assertEquals(StatoBarca.DISPONIBILE, controller.getBarca("MN007").orElseThrow().getStato());
    }

    @Test
    void nuovaManutenzioneNonProgrammataVieneRifiutata() {
        Barca barca = barca("MN008", false);
        controller.aggiungiBarca(barca);

        assertThrows(TransizioneStatoNonValidaException.class,
                () -> controller.aggiungiManutenzione(manutenzione(
                        71, barca, INIZIO, FINE, StatoManutenzione.IN_CORSO)));
    }

    @Test
    void metteBarcaFuoriServizioConDataDiBlocco() {
        Barca barca = barca("FS001", false);
        controller.aggiungiBarca(barca);

        controller.mettiBarcaFuoriServizio("FS001", FINE.plusDays(5));

        Barca aggiornata = controller.getBarca("FS001").orElseThrow();
        assertEquals(StatoBarca.FUORI_SERVIZIO, aggiornata.getStato());
        assertEquals(FINE.plusDays(5), aggiornata.getIndisponibileFinoAl());
    }

    @Test
    void fuoriServizioSospendeNoleggioAttivo() {
        Cliente cliente = cliente(1, "fuori-servizio-noleggio@example.it");
        Barca barca = barca("FS002", false);
        salvaPrenotazioneBase(80, cliente, barca);
        controller.avviaNoleggio(81, 80, LocalDateTime.of(2026, 10, 10, 9, 0), "Ritiro");

        controller.mettiBarcaFuoriServizio("FS002", FINE.plusDays(2));

        assertEquals(StatoNoleggio.SOSPESO, controller.getNoleggio(81).orElseThrow().getStato());
        assertEquals(StatoBarca.FUORI_SERVIZIO, controller.getBarca("FS002").orElseThrow().getStato());
    }

    @Test
    void ripristinaBarcaSenzaBlocchiAttivi() {
        Barca barca = barca("FS003", false);
        controller.aggiungiBarca(barca);
        controller.mettiBarcaFuoriServizio("FS003", FINE.plusDays(2));

        controller.ripristinaBarca("FS003");

        Barca ripristinata = controller.getBarca("FS003").orElseThrow();
        assertEquals(StatoBarca.DISPONIBILE, ripristinata.getStato());
        assertNull(ripristinata.getIndisponibileFinoAl());
    }

    @Test
    void ripristinaBarcaRifiutaNoleggioAttivo() {
        Cliente cliente = cliente(1, "ripristino-noleggio@example.it");
        Barca barca = barca("FS004", false);
        salvaPrenotazioneBase(82, cliente, barca);
        controller.avviaNoleggio(83, 82, LocalDateTime.of(2026, 10, 10, 9, 0), "Ritiro");
        controller.mettiBarcaFuoriServizio("FS004", FINE.plusDays(2));

        assertThrows(BarcaNonDisponibileException.class,
                () -> controller.ripristinaBarca("FS004"));
    }

    @Test
    void ripristinaBarcaRifiutaManutenzioneAttiva() {
        Barca barca = barca("FS005", false);
        controller.aggiungiBarca(barca);
        controller.aggiungiManutenzione(manutenzione(84, barca, INIZIO, FINE, StatoManutenzione.PROGRAMMATA));
        controller.avviaManutenzione(84);
        controller.mettiBarcaFuoriServizio("FS005", FINE.plusDays(2));

        assertThrows(BarcaNonDisponibileException.class,
                () -> controller.ripristinaBarca("FS005"));
    }

    private Prenotazione salvaPrenotazioneBase(int idPrenotazione, Cliente cliente, Barca barca) {
        salvaClienteEBarca(cliente, barca);
        Prenotazione prenotazione = prenotazione(idPrenotazione, cliente, barca);
        controller.aggiungiPrenotazione(prenotazione);
        return prenotazione;
    }

    private void salvaClienteEBarca(Cliente cliente, Barca barca) {
        controller.aggiungiCliente(cliente);
        controller.aggiungiBarca(barca);
    }

    private Cliente cliente(int id, String email) {
        return cliente(id, email, LocalDate.of(1990, 1, 1), null);
    }

    private Cliente cliente(int id, String email, LocalDate dataNascita, LocalDate scadenzaPatente) {
        String numeroPatente = scadenzaPatente == null ? null : "PAT-" + id;
        return new Cliente(
                id,
                "Nome" + id,
                "Cognome" + id,
                email,
                "hash" + id,
                dataNascita,
                numeroPatente,
                scadenzaPatente);
    }

    private Barca barca(String matricola, boolean richiedePatente) {
        return barca(matricola, richiedePatente, StatoBarca.DISPONIBILE, null, 6);
    }

    private Barca barca(
            String matricola,
            boolean richiedePatente,
            StatoBarca stato,
            LocalDate indisponibileFinoAl,
            int capacita) {
        return new BarcaMotore(
                matricola,
                "Barca " + matricola,
                capacita,
                120.0,
                richiedePatente,
                stato,
                indisponibileFinoAl,
                sede,
                100,
                200.0);
    }

    private Prenotazione prenotazione(int id, Cliente cliente, Barca barca) {
        return prenotazione(id, cliente, barca, INIZIO, FINE, 3, false, StatoPrenotazione.CONFERMATA);
    }

    private Prenotazione prenotazione(int id, Cliente cliente, Barca barca, int passeggeri, boolean conPatente) {
        return prenotazione(id, cliente, barca, INIZIO, FINE, passeggeri, conPatente, StatoPrenotazione.CONFERMATA);
    }

    private Prenotazione prenotazione(
            int id,
            Cliente cliente,
            Barca barca,
            LocalDate inizio,
            LocalDate fine,
            int passeggeri,
            boolean conPatente,
            StatoPrenotazione stato) {
        return new Prenotazione(
                id,
                DATA_PRENOTAZIONE,
                inizio,
                fine,
                passeggeri,
                conPatente,
                stato,
                cliente,
                barca);
    }

    private Manutenzione manutenzione(
            int id,
            Barca barca,
            LocalDate inizio,
            LocalDate fine,
            StatoManutenzione stato) {
        return new Manutenzione(id, inizio, fine, "Controllo tecnico", stato, barca);
    }
}
