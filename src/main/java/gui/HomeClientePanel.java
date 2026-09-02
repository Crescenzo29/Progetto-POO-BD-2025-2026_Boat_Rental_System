package gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.Cliente;
import model.Prenotazione;

/**
 * Home del cliente.
 */
public class HomeClientePanel extends JPanel {
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final MainFrame mainFrame;
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Matricola", "Nome Barca", "Data Inizio", "Data Fine", "Numero Passeggeri", "Stato"},
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public HomeClientePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel intestazione = new JPanel(new BorderLayout());
        JLabel titolo = new JLabel("HOME", SwingConstants.CENTER);
        titolo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        JLabel sottotitolo = new JLabel("Prenotazioni effettuate", SwingConstants.CENTER);
        intestazione.add(titolo, BorderLayout.NORTH);
        intestazione.add(sottotitolo, BorderLayout.SOUTH);
        add(intestazione, BorderLayout.NORTH);

        JTable tabella = new JTable(tableModel);
        add(new JScrollPane(tabella), BorderLayout.CENTER);

        JPanel pulsanti = new JPanel();
        JButton catalogoButton = new JButton("Catalogo");
        JButton esciButton = new JButton("Esci");
        catalogoButton.addActionListener(event -> mainFrame.mostraCatalogo());
        esciButton.addActionListener(event -> mainFrame.mostraStart());
        pulsanti.add(catalogoButton);
        pulsanti.add(esciButton);
        add(pulsanti, BorderLayout.SOUTH);
    }

    public void aggiornaDati() {
        tableModel.setRowCount(0);
        Cliente cliente = mainFrame.getClienteAutenticato();
        if (cliente == null) {
            return;
        }

        // Carica le prenotazioni del cliente
        for (Prenotazione prenotazione : mainFrame.getController().getPrenotazioniCliente(cliente.getIdCliente())) {
            tableModel.addRow(new Object[]{
                    prenotazione.getBarca().getMatricola(),
                    prenotazione.getBarca().getNome(),
                    prenotazione.getDataInizio().format(FORMATO_DATA),
                    prenotazione.getDataFine().format(FORMATO_DATA),
                    prenotazione.getNumeroPasseggeri(),
                    prenotazione.getStato()
            });
        }
    }
}
