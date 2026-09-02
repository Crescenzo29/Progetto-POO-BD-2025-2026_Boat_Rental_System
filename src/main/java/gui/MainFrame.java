package gui;

import controller.NoleggioBarcheController;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import model.Barca;
import model.Cliente;
import model.Manutenzione;
import model.Prenotazione;

/**
 * Finestra principale dell'applicazione.
 */
public class MainFrame extends JFrame {
    private static final String START = "start";
    private static final String LOGIN = "login";
    private static final String REGISTRAZIONE = "registrazione";
    private static final String HOME_CLIENTE = "homeCliente";
    private static final String CATALOGO = "catalogo";
    private static final String PRENOTAZIONE = "prenotazione";
    private static final String HOME_ADMIN = "homeAdmin";
    private static final String GESTIONE_NOLEGGIO = "gestioneNoleggio";
    private static final String MANUTENZIONE_BARCHE = "manutenzioneBarche";
    private static final String GESTIONE_MANUTENZIONE = "gestioneManutenzione";

    private static final DateTimeFormatter DATA_SLASH = DateTimeFormatter
            .ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DATA_DASH = DateTimeFormatter
            .ofPattern("dd-MM-uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter ORA_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final NoleggioBarcheController controller;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contenitore = new JPanel(cardLayout);
    private final HomeClientePanel homeClientePanel;
    private final CatalogoPanel catalogoPanel;
    private final PrenotazionePanel prenotazionePanel;
    private final HomeAdminPanel homeAdminPanel;
    private final GestioneNoleggioPanel gestioneNoleggioPanel;
    private final ManutenzioneBarchePanel manutenzioneBarchePanel;
    private final GestioneManutenzionePanel gestioneManutenzionePanel;

    private Cliente clienteAutenticato;
    private Barca barcaSelezionata;
    private Prenotazione prenotazioneSelezionata;
    private Manutenzione manutenzioneSelezionata;

    public MainFrame(NoleggioBarcheController controller) {
        super("Gestione Noleggio Barche");
        this.controller = controller;

        homeClientePanel = new HomeClientePanel(this);
        catalogoPanel = new CatalogoPanel(this);
        prenotazionePanel = new PrenotazionePanel(this);
        homeAdminPanel = new HomeAdminPanel(this);
        gestioneNoleggioPanel = new GestioneNoleggioPanel(this);
        manutenzioneBarchePanel = new ManutenzioneBarchePanel(this);
        gestioneManutenzionePanel = new GestioneManutenzionePanel(this);

        contenitore.add(new StartPanel(this), START);
        contenitore.add(new LoginPanel(this), LOGIN);
        contenitore.add(new RegistrazionePanel(this), REGISTRAZIONE);
        contenitore.add(homeClientePanel, HOME_CLIENTE);
        contenitore.add(catalogoPanel, CATALOGO);
        contenitore.add(prenotazionePanel, PRENOTAZIONE);
        contenitore.add(homeAdminPanel, HOME_ADMIN);
        contenitore.add(gestioneNoleggioPanel, GESTIONE_NOLEGGIO);
        contenitore.add(manutenzioneBarchePanel, MANUTENZIONE_BARCHE);
        contenitore.add(gestioneManutenzionePanel, GESTIONE_MANUTENZIONE);

        setLayout(new BorderLayout());
        add(contenitore, BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        mostraStart();
    }


    public NoleggioBarcheController getController() {
        return controller;
    }

    public Cliente getClienteAutenticato() {
        return clienteAutenticato;
    }

    public void setClienteAutenticato(Cliente clienteAutenticato) {
        this.clienteAutenticato = clienteAutenticato;
    }

    public Barca getBarcaSelezionata() {
        return barcaSelezionata;
    }

    public void setBarcaSelezionata(Barca barcaSelezionata) {
        this.barcaSelezionata = barcaSelezionata;
    }

    public Prenotazione getPrenotazioneSelezionata() {
        return prenotazioneSelezionata;
    }

    public void setPrenotazioneSelezionata(Prenotazione prenotazioneSelezionata) {
        this.prenotazioneSelezionata = prenotazioneSelezionata;
    }

    public Manutenzione getManutenzioneSelezionata() {
        return manutenzioneSelezionata;
    }

    public void setManutenzioneSelezionata(Manutenzione manutenzioneSelezionata) {
        this.manutenzioneSelezionata = manutenzioneSelezionata;
    }

    public void mostraStart() {
        pulisciSessione();
        cardLayout.show(contenitore, START);
    }

    public void mostraLogin() {
        cardLayout.show(contenitore, LOGIN);
    }

    public void mostraRegistrazione() {
        cardLayout.show(contenitore, REGISTRAZIONE);
    }

    public void mostraHomeCliente() {
        homeClientePanel.aggiornaDati();
        cardLayout.show(contenitore, HOME_CLIENTE);
    }

    public void mostraCatalogo() {
        catalogoPanel.aggiornaDati();
        cardLayout.show(contenitore, CATALOGO);
    }

    public void mostraPrenotazione() {
        prenotazionePanel.aggiornaDati();
        cardLayout.show(contenitore, PRENOTAZIONE);
    }

    public void mostraHomeAdmin() {
        homeAdminPanel.aggiornaDati();
        cardLayout.show(contenitore, HOME_ADMIN);
    }

    public void mostraGestioneNoleggio() {
        gestioneNoleggioPanel.aggiornaDati();
        cardLayout.show(contenitore, GESTIONE_NOLEGGIO);
    }

    public void mostraManutenzioneBarche() {
        manutenzioneBarchePanel.aggiornaDati();
        cardLayout.show(contenitore, MANUTENZIONE_BARCHE);
    }

    public void mostraGestioneManutenzione() {
        gestioneManutenzionePanel.aggiornaDati();
        cardLayout.show(contenitore, GESTIONE_MANUTENZIONE);
    }

    public LocalDate parseData(String testo) {
        String valore = testo == null ? "" : testo.trim();
        if (valore.isEmpty()) {
            throw new IllegalArgumentException("La data non puo' essere vuota.");
        }
        try {
            return LocalDate.parse(valore, valore.contains("/") ? DATA_SLASH : DATA_DASH);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Formato data non valido. Usa GG/MM/AAAA o GG-MM-AAAA.");
        }
    }

    public LocalDate parseDataOpzionale(String testo) {
        if (testo == null || testo.trim().isEmpty()) {
            return null;
        }
        return parseData(testo);
    }

    public String formatData(LocalDate data) {
        return data == null ? "" : data.format(DATA_SLASH);
    }

    public LocalDateTime parseDataOra(String data, String ora) {
        try {
            return LocalDateTime.of(parseData(data), LocalTime.parse(ora.trim(), ORA_FORMAT));
        } catch (DateTimeParseException | NullPointerException ex) {
            throw new IllegalArgumentException("Formato ora non valido. Usa HH:mm.");
        }
    }

    public void mostraMessaggio(Component parent, String messaggio) {
        JOptionPane.showMessageDialog(parent, messaggio, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public void mostraErrore(Component parent, Exception ex) {
        JOptionPane.showMessageDialog(parent, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
    }

    private void pulisciSessione() {
        clienteAutenticato = null;
        barcaSelezionata = null;
        prenotazioneSelezionata = null;
        manutenzioneSelezionata = null;
    }

}
