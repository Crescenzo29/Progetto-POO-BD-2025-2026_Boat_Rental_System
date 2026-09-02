package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import model.Barca;
import model.Cliente;
import model.Manutenzione;
import model.Noleggio;
import model.Prenotazione;
import model.Sede;
import org.junit.jupiter.api.Test;

class DaoContractTest {
    @Test
    void daoSonoInterfacce() {
        assertTrue(CrudDAO.class.isInterface());
        assertTrue(ClienteDAO.class.isInterface());
        assertTrue(SedeDAO.class.isInterface());
        assertTrue(BarcaDAO.class.isInterface());
        assertTrue(PrenotazioneDAO.class.isInterface());
        assertTrue(NoleggioDAO.class.isInterface());
        assertTrue(ManutenzioneDAO.class.isInterface());
    }

    @Test
    void daoSpecificiEstendonoCrudDaoConGenericsCorretti() {
        assertCrudDao(ClienteDAO.class, Cliente.class, Integer.class);
        assertCrudDao(SedeDAO.class, Sede.class, Integer.class);
        assertCrudDao(BarcaDAO.class, Barca.class, String.class);
        assertCrudDao(PrenotazioneDAO.class, Prenotazione.class, Integer.class);
        assertCrudDao(NoleggioDAO.class, Noleggio.class, Integer.class);
        assertCrudDao(ManutenzioneDAO.class, Manutenzione.class, Integer.class);
    }

    @Test
    void crudDaoDefinisceMetodiCrud() throws NoSuchMethodException {
        assertEquals(Optional.class, CrudDAO.class.getMethod("findById", Object.class).getReturnType());
        assertEquals(List.class, CrudDAO.class.getMethod("findAll").getReturnType());
        assertEquals(void.class, CrudDAO.class.getMethod("save", Object.class).getReturnType());
        assertEquals(void.class, CrudDAO.class.getMethod("update", Object.class).getReturnType());
        assertEquals(boolean.class, CrudDAO.class.getMethod("deleteById", Object.class).getReturnType());
    }

    @Test
    void metodiSpecificiSonoPresenti() throws NoSuchMethodException {
        assertEquals(Optional.class, ClienteDAO.class.getMethod("findByEmail", String.class).getReturnType());
        assertEquals(List.class, BarcaDAO.class.getMethod("findBySedeId", int.class).getReturnType());
        assertEquals(List.class, PrenotazioneDAO.class.getMethod("findByClienteId", int.class).getReturnType());
        assertEquals(List.class, PrenotazioneDAO.class.getMethod("findByBarcaMatricola", String.class).getReturnType());
        assertEquals(Optional.class, NoleggioDAO.class.getMethod("findByPrenotazioneId", int.class).getReturnType());
        assertEquals(List.class, ManutenzioneDAO.class.getMethod("findByBarcaMatricola", String.class).getReturnType());
    }

    private static void assertCrudDao(Class<?> daoClass, Class<?> entityClass, Class<?> idClass) {
        Type crudType = daoClass.getGenericInterfaces()[0];
        assertTrue(crudType instanceof ParameterizedType);

        ParameterizedType parameterizedType = (ParameterizedType) crudType;
        assertEquals(CrudDAO.class, parameterizedType.getRawType());
        assertEquals(entityClass, parameterizedType.getActualTypeArguments()[0]);
        assertEquals(idClass, parameterizedType.getActualTypeArguments()[1]);
    }
}
