package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class BarcaTest {
    @Test
    void barcaEAbstract() {
        assertTrue(Modifier.isAbstract(Barca.class.getModifiers()));
    }

    @Test
    void calcoloCosto() {
        Barca barca = ModelTestFactory.barcaMotore();

        assertEquals(360.0, barca.calcolaCosto(3));
    }

    @Test
    void giorniNonValidi() {
        Barca barca = ModelTestFactory.barcaMotore();

        assertThrows(IllegalArgumentException.class, () -> barca.calcolaCosto(0));
        assertThrows(IllegalArgumentException.class, () -> barca.calcolaCosto(-1));
    }

    @Test
    void disponibileQuandoStatoDisponibileENessunaIndisponibilita() {
        Barca barca = ModelTestFactory.barcaMotore();

        assertTrue(barca.isDisponibile());
    }

    @Test
    void indisponibileSeDataIndisponibilitaCoincideConOggi() {
        Barca barca = new BarcaMotore(
                "BM002",
                "Blu",
                6,
                100.0,
                true,
                StatoBarca.DISPONIBILE,
                LocalDate.now(),
                ModelTestFactory.sede(),
                100,
                60.0);

        assertFalse(barca.isDisponibile());
    }

    @Test
    void indisponibileSeStatoDiversoDaDisponibile() {
        Barca barca = ModelTestFactory.barcaMotore();

        barca.setStato(StatoBarca.MANUTENZIONE);

        assertFalse(barca.isDisponibile());
    }

    @Test
    void attributiNumericiNonValidi() {
        assertThrows(IllegalArgumentException.class, () -> new BarcaMotore(
                "BM003",
                "Rossa",
                0,
                100.0,
                true,
                StatoBarca.DISPONIBILE,
                null,
                ModelTestFactory.sede(),
                100,
                60.0));
        assertThrows(IllegalArgumentException.class, () -> new BarcaMotore(
                "BM004",
                "Verde",
                6,
                0.0,
                true,
                StatoBarca.DISPONIBILE,
                null,
                ModelTestFactory.sede(),
                100,
                60.0));
    }
}
