package dao.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import model.Barca;
import model.Cliente;
import model.Manutenzione;
import model.Noleggio;
import model.Prenotazione;
import model.StatoBarca;
import model.StatoManutenzione;
import model.StatoNoleggio;
import model.StatoPrenotazione;
import org.junit.jupiter.api.Test;

class DemoDataIntegrationTest {
    private static final Path DATA = Path.of("src", "main", "resources", "data");

    private Fixture load() {
        SedeCsvDAO sedi = new SedeCsvDAO(DATA.resolve("sedi.csv"));
        ClienteCsvDAO clienti = new ClienteCsvDAO(DATA.resolve("clienti.csv"));
        BarcaCsvDAO barche = new BarcaCsvDAO(DATA.resolve("barche.csv"), sedi);
        PrenotazioneCsvDAO prenotazioni = new PrenotazioneCsvDAO(
                DATA.resolve("prenotazioni.csv"), clienti, barche);
        NoleggioCsvDAO noleggi = new NoleggioCsvDAO(
                DATA.resolve("noleggi.csv"), prenotazioni);
        ManutenzioneCsvDAO manutenzioni = new ManutenzioneCsvDAO(
                DATA.resolve("manutenzioni.csv"), barche);
        return new Fixture(clienti, sedi, barche, prenotazioni, noleggi, manutenzioni);
    }

    @Test
    void tuttiIDatiDemoSonoCaricabili() {
        Fixture f = load();
        assertTrue(f.clienti().findAll().size() >= 6);
        assertEquals(3, f.sedi().findAll().size());
        assertEquals(6, f.barche().findAll().size());
        assertTrue(f.prenotazioni().findAll().size() >= 8);
        assertTrue(f.noleggi().findAll().size() >= 3);
        assertTrue(f.manutenzioni().findAll().size() >= 4);
    }

    @Test
    void matricoleEmailEIdentificativiSonoUnivoci() {
        Fixture f = load();
        assertEquals(f.clienti().findAll().size(),
                f.clienti().findAll().stream().map(Cliente::getIdCliente).distinct().count());
        assertEquals(f.clienti().findAll().size(),
                f.clienti().findAll().stream().map(c -> c.getEmail().toLowerCase()).distinct().count());
        assertEquals(6, f.barche().findAll().stream().map(Barca::getMatricola).distinct().count());
        assertEquals(f.prenotazioni().findAll().size(),
                f.prenotazioni().findAll().stream().map(Prenotazione::getIdPrenotazione).distinct().count());
        assertEquals(f.noleggi().findAll().size(),
                f.noleggi().findAll().stream().map(Noleggio::getIdNoleggio).distinct().count());
        assertEquals(f.manutenzioni().findAll().size(),
                f.manutenzioni().findAll().stream().map(Manutenzione::getIdManutenzione).distinct().count());
    }

    @Test
    void capacitaPasseggeriSempreRispettata() {
        for (Prenotazione p : load().prenotazioni().findAll()) {
            assertTrue(p.getNumeroPasseggeri() <= p.getBarca().getCapacitaPasseggeri());
        }
    }

    @Test
    void patenteValidaPerPrenotazioniAttiveDiBarcheCheLaRichiedono() {
        for (Prenotazione p : load().prenotazioni().findAll()) {
            if (p.getStato() != StatoPrenotazione.ANNULLATA && p.getBarca().isRichiedePatente()) {
                assertTrue(p.isConPatente());
                assertTrue(p.getCliente().patenteValida(p.getDataFine()));
            }
        }
    }

    @Test
    void prenotazioniNonAnnullateNonSiSovrappongonoPerLaStessaBarca() {
        List<Prenotazione> prenotazioni = load().prenotazioni().findAll().stream()
                .filter(p -> p.getStato() != StatoPrenotazione.ANNULLATA)
                .sorted(Comparator.comparing(Prenotazione::getDataInizio))
                .toList();

        for (int i = 0; i < prenotazioni.size(); i++) {
            for (int j = i + 1; j < prenotazioni.size(); j++) {
                Prenotazione a = prenotazioni.get(i);
                Prenotazione b = prenotazioni.get(j);
                if (a.getBarca().getMatricola().equals(b.getBarca().getMatricola())) {
                    boolean overlap = !a.getDataInizio().isAfter(b.getDataFine())
                            && !b.getDataInizio().isAfter(a.getDataFine());
                    assertFalse(overlap,
                            () -> "Sovrapposizione demo tra " + a.getIdPrenotazione()
                                    + " e " + b.getIdPrenotazione());
                }
            }
        }
    }

    @Test
    void noleggioAttivoCoerenteConPrenotazioneEBarca() {
        Fixture f = load();
        for (Noleggio noleggio : f.noleggi().findAll()) {
            if (noleggio.getStato() == StatoNoleggio.IN_CORSO) {
                assertEquals(StatoPrenotazione.NOLEGGIATA, noleggio.getPrenotazione().getStato());
                assertEquals(StatoBarca.NOLEGGIATA, noleggio.getPrenotazione().getBarca().getStato());
            }
        }
    }

    @Test
    void prenotazioniCompletateHannoNoleggioTerminato() {
        Fixture f = load();
        for (Prenotazione p : f.prenotazioni().findAll()) {
            if (p.getStato() == StatoPrenotazione.COMPLETATA) {
                Noleggio n = f.noleggi().findByPrenotazioneId(p.getIdPrenotazione()).orElseThrow();
                assertEquals(StatoNoleggio.TERMINATO, n.getStato());
                assertNotNull(n.getDataOraRestituzione());
            }
        }
    }

    @Test
    void manutenzioneAttivaCoerenteConStatoBarca() {
        Fixture f = load();
        for (Manutenzione m : f.manutenzioni().findAll()) {
            if (m.getStato() == StatoManutenzione.IN_CORSO) {
                assertEquals(StatoBarca.MANUTENZIONE, m.getBarca().getStato());
            }
        }
    }

    @Test
    void clienteConPatenteScadutaDisponibilePerFuturiTest() {
        Cliente cliente = load().clienti().findById(5).orElseThrow();
        assertFalse(cliente.patenteValida(LocalDate.of(2026, 8, 29)));
    }

    private record Fixture(
            ClienteCsvDAO clienti,
            SedeCsvDAO sedi,
            BarcaCsvDAO barche,
            PrenotazioneCsvDAO prenotazioni,
            NoleggioCsvDAO noleggi,
            ManutenzioneCsvDAO manutenzioni) {
    }
}
