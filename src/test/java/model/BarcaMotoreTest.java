package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BarcaMotoreTest {
    @Test
    void estendeBarca() {
        assertTrue(Barca.class.isAssignableFrom(BarcaMotore.class));
    }

    @Test
    void potenzaValida() {
        BarcaMotore barca = ModelTestFactory.barcaMotore();

        assertEquals(150, barca.getPotenzaMotoreCV());
    }

    @Test
    void potenzaNonValida() {
        BarcaMotore barca = ModelTestFactory.barcaMotore();

        assertThrows(IllegalArgumentException.class, () -> barca.setPotenzaMotoreCV(0));
    }

    @Test
    void serbatoioValido() {
        BarcaMotore barca = ModelTestFactory.barcaMotore();

        assertEquals(80.0, barca.getCapacitaSerbatoio());
    }

    @Test
    void serbatoioNonValido() {
        BarcaMotore barca = ModelTestFactory.barcaMotore();

        assertThrows(IllegalArgumentException.class, () -> barca.setCapacitaSerbatoio(0.0));
    }
}
