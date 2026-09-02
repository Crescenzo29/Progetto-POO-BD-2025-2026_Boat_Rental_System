package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SedeTest {
    @Test
    void creazioneValida() {
        Sede sede = ModelTestFactory.sede();

        assertEquals("Genova", sede.getCitta());
    }

    @Test
    void campiObbligatoriNonBlank() {
        assertThrows(IllegalArgumentException.class, () -> new Sede(1, "", "Genova", "Molo 1"));
        assertThrows(IllegalArgumentException.class, () -> new Sede(1, "Porto", " ", "Molo 1"));
        assertThrows(IllegalArgumentException.class, () -> new Sede(1, "Porto", "Genova", null));
    }
}
