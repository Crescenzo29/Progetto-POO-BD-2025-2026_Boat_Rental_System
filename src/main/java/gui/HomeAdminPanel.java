package gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.Prenotazione;

/**
 * Home dell'amministratore.
 */
public class HomeAdminPanel extends JPanel {
    private final MainFrame mainFrame;
    private final List<Prenotazione> prenotazioni = new ArrayList<>();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{
                    "ID Prenotazione", "Cliente", "Barca", "Data Prenotazione",
                    "Data Inizio", "Data Fine", "Numero Passeggeri", "Con Patente", "Stato"
            },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public HomeAdminPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel intestazione = new JPanel(new BorderLayout());
        JLabel titolo = new JLabel("HOME AMMINISTRATORE", SwingConstants.CENTER);
        titolo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        JLabel sottotitolo = new JLabel("Noleggi da gestire", SwingConstants.CENTER);
        intestazione.add(titolo, BorderLayout.NORTH);
        intestazione.add(sottotitolo, BorderLayout.SOUTH);
        add(intestazione, BorderLayout.NORTH);

        JTable tabella = new JTable(tableModel);
        tabella.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    apriGestioneNoleggio(tabella.getSelectedRow());
                }
            }
        });
        add(new JScrollPane(tabella), BorderLayout.CENTER);

        JPanel pulsanti = new JPanel();
        JButton gestisciButton = new JButton("Gestisci Prenotazione");
        JButton manutenzioneButton = new JButton("Manutenzione Barche");
        JButton esciButton = new JButton("Esci");
        gestisciButton.addActionListener(event -> apriGestioneNoleggio(tabella.getSelectedRow()));
        manutenzioneButton.addActionListener(event -> mainFrame.mostraManutenzioneBarche());
        esciButton.addActionListener(event -> mainFrame.mostraStart());
        pulsanti.add(gestisciButton);
        pulsanti.add(manutenzioneButton);
        pulsanti.add(esciButton);
        add(pulsanti, BorderLayout.SOUTH);
    }

    public void aggiornaDati() {
        prenotazioni.clear();
        tableModel.setRowCount(0);
        prenotazioni.addAll(mainFrame.getController().getPrenotazioni());

        // Carica le prenotazioni
        for (Prenotazione prenotazione : prenotazioni) {
            tableModel.addRow(new Object[]{
                    prenotazione.getIdPrenotazione(),
                    prenotazione.getCliente().getNome() + " " + prenotazione.getCliente().getCognome(),
                    prenotazione.getBarca().getNome(),
                    mainFrame.formatData(prenotazione.getDataPrenotazione()),
                    mainFrame.formatData(prenotazione.getDataInizio()),
                    mainFrame.formatData(prenotazione.getDataFine()),
                    prenotazione.getNumeroPasseggeri(),
                    prenotazione.isConPatente() ? "Si" : "No",
                    prenotazione.getStato()
            });
        }
    }

    private void apriGestioneNoleggio(int riga) {
        if (riga < 0 || riga >= prenotazioni.size()) {
            mainFrame.mostraErrore(this, new IllegalArgumentException("Selezionare una prenotazione."));
            return;
        }
        mainFrame.setPrenotazioneSelezionata(prenotazioni.get(riga));
        mainFrame.mostraGestioneNoleggio();
    }
}
