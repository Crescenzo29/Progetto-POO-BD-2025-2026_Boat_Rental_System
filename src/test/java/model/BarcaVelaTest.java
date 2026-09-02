package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BarcaVelaTest {
    @Test
    void estendeBarca() {
        assertTrue(Barca.class.isAssignableFrom(BarcaVela.class));
    }

    @Test
    void superficieValida() {
        BarcaVela barca = ModelTestFactory.barcaVela();

        assertEquals(35.0, barca.getSuperficieVelica());
    }

    @Test
    void superficieNonValida() {
        BarcaVela barca = ModelTestFactory.barcaVela();

        assertThrows(IllegalArgumentException.class, () -> barca.setSuperficieVelica(0.0));
    }

    @Test
    void altezzaValida() {
        BarcaVela barca = ModelTestFactory.barcaVela();

        assertEquals(12.0, barca.getAltezzaAlbero());
    }

    @Test
    void altezzaNonValida() {
        BarcaVela barca = ModelTestFactory.barcaVela();

        assertThrows(IllegalArgumentException.class, () -> barca.setAltezzaAlbero(0.0));
    }
}
