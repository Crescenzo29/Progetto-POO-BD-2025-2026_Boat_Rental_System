import controller.NoleggioBarcheController;
import dao.impl.BarcaCsvDAO;
import dao.impl.ClienteCsvDAO;
import dao.impl.ManutenzioneCsvDAO;
import dao.impl.NoleggioCsvDAO;
import dao.impl.PrenotazioneCsvDAO;
import dao.impl.SedeCsvDAO;
import gui.MainFrame;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.SwingUtilities;

/**
 * Avvio del programma Gestione Noleggio Barche.
 */
public class Main {

    /**
     * Avvia la finestra principale.
     *
     * @param args argomenti non usati
     */
    public static void main(String[] args) {
        // Crea il controller e avvia la finestra
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(creaControllerCsv());
            frame.setVisible(true);
        });
    }

    // Collega i DAO CSV al controller
    private static NoleggioBarcheController creaControllerCsv() {
        Path dataDir = percorsoDati();

        SedeCsvDAO sedeDAO = new SedeCsvDAO(dataDir.resolve("sedi.csv"));
        ClienteCsvDAO clienteDAO = new ClienteCsvDAO(dataDir.resolve("clienti.csv"));
        BarcaCsvDAO barcaDAO = new BarcaCsvDAO(dataDir.resolve("barche.csv"), sedeDAO);
        PrenotazioneCsvDAO prenotazioneDAO = new PrenotazioneCsvDAO(
                dataDir.resolve("prenotazioni.csv"), clienteDAO, barcaDAO);
        NoleggioCsvDAO noleggioDAO = new NoleggioCsvDAO(
                dataDir.resolve("noleggi.csv"), prenotazioneDAO);
        ManutenzioneCsvDAO manutenzioneDAO = new ManutenzioneCsvDAO(
                dataDir.resolve("manutenzioni.csv"), barcaDAO);

        return new NoleggioBarcheController(
                clienteDAO,
                sedeDAO,
                barcaDAO,
                prenotazioneDAO,
                noleggioDAO,
                manutenzioneDAO
        );
    }

    // Cerca la cartella dei CSV
    private static Path percorsoDati() {
        Path sorgenti = Path.of("src/main/resources/data");
        if (Files.isDirectory(sorgenti)) {
            return sorgenti;
        }
        return Path.of("data");
    }
}
