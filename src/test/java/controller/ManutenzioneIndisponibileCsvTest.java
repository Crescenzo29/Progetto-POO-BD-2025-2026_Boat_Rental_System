package controller;

import static org.junit.jupiter.api.Assertions.*;

import dao.impl.BarcaCsvDAO;
import dao.impl.ClienteCsvDAO;
import dao.impl.ManutenzioneCsvDAO;
import dao.impl.NoleggioCsvDAO;
import dao.impl.PrenotazioneCsvDAO;
import dao.impl.SedeCsvDAO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import model.Barca;
import model.BarcaMotore;
import model.Manutenzione;
import model.Sede;
import model.StatoBarca;
import model.StatoManutenzione;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ManutenzioneIndisponibileCsvTest {
    @TempDir
    Path tempDir;

    @Test
    void avvioECompletamentoManutenzionePersistonoIndisponibileFinoAlNelCsv() throws Exception {
        Path barcheCsv = tempDir.resolve("barche.csv");

        SedeCsvDAO sedeDAO = new SedeCsvDAO(tempDir.resolve("sedi.csv"));
        ClienteCsvDAO clienteDAO = new ClienteCsvDAO(tempDir.resolve("clienti.csv"));
        BarcaCsvDAO barcaDAO = new BarcaCsvDAO(barcheCsv, sedeDAO);
        PrenotazioneCsvDAO prenotazioneDAO = new PrenotazioneCsvDAO(
                tempDir.resolve("prenotazioni.csv"), clienteDAO, barcaDAO);
        NoleggioCsvDAO noleggioDAO = new NoleggioCsvDAO(
                tempDir.resolve("noleggi.csv"), prenotazioneDAO);
        ManutenzioneCsvDAO manutenzioneDAO = new ManutenzioneCsvDAO(
                tempDir.resolve("manutenzioni.csv"), barcaDAO);
        NoleggioBarcheController controller = new NoleggioBarcheController(
                clienteDAO, sedeDAO, barcaDAO, prenotazioneDAO, noleggioDAO, manutenzioneDAO);

        Sede sede = new Sede(1, "Porto Test", "Napoli", "Molo 1");
        controller.aggiungiSede(sede);
        Barca barca = new BarcaMotore(
                "MN-C001", "Azzurra", 6, 180.0, false,
                StatoBarca.DISPONIBILE, null, sede, 120, 250.0);
        controller.aggiungiBarca(barca);

        LocalDate dataInizio = LocalDate.of(2026, 9, 2);
        LocalDate dataFine = LocalDate.of(2026, 9, 5);
        Manutenzione manutenzione = new Manutenzione(
                1, dataInizio, dataFine, "Controllo motore",
                StatoManutenzione.PROGRAMMATA, barca);
        controller.aggiungiManutenzione(manutenzione);

        Barca iniziale = barcaDAO.findById("MN-C001").orElseThrow();
        assertEquals(StatoBarca.DISPONIBILE, iniziale.getStato());
        assertNull(iniziale.getIndisponibileFinoAl());
        assertEquals(StatoManutenzione.PROGRAMMATA,
                manutenzioneDAO.findById(1).orElseThrow().getStato());

        controller.avviaManutenzione(1);

        Barca dopoAvvio = barcaDAO.findById("MN-C001").orElseThrow();
        assertEquals(StatoManutenzione.IN_CORSO,
                manutenzioneDAO.findById(1).orElseThrow().getStato());
        assertEquals(StatoBarca.MANUTENZIONE, dopoAvvio.getStato());
        assertEquals(dataFine, dopoAvvio.getIndisponibileFinoAl());
        assertTrue(Files.readString(barcheCsv).contains(
                "MN-C001;MOTORE;Azzurra;6;180.0;no;MANUTENZIONE;05/09/2026;1;120;250.0;;"));

        controller.completaManutenzione(1);

        Barca dopoCompletamento = barcaDAO.findById("MN-C001").orElseThrow();
        assertEquals(StatoManutenzione.COMPLETATA,
                manutenzioneDAO.findById(1).orElseThrow().getStato());
        assertEquals(StatoBarca.DISPONIBILE, dopoCompletamento.getStato());
        assertNull(dopoCompletamento.getIndisponibileFinoAl());
        assertTrue(Files.readString(barcheCsv).contains(
                "MN-C001;MOTORE;Azzurra;6;180.0;no;DISPONIBILE;;1;120;250.0;;"));
    }
}
