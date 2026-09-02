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
import model.Barca;

/**
 * Schermata con le barche da manutenere.
 */
public class ManutenzioneBarchePanel extends JPanel {
    private final MainFrame mainFrame;
    private final List<Barca> barche = new ArrayList<>();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Matricola", "Nome", "Sede", "Stato", "Indisponibile fino al"},
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public ManutenzioneBarchePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel titolo = new JLabel("MANUTENZIONE BARCHE", SwingConstants.CENTER);
        titolo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        add(titolo, BorderLayout.NORTH);

        JTable tabella = new JTable(tableModel);
        tabella.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    apriGestioneManutenzione(tabella.getSelectedRow());
                }
            }
        });
        add(new JScrollPane(tabella), BorderLayout.CENTER);

        JPanel pulsanti = new JPanel();
        JButton gestisciButton = new JButton("Gestisci Barca Selezionata");
        JButton indietroButton = new JButton("Indietro");
        gestisciButton.addActionListener(event -> apriGestioneManutenzione(tabella.getSelectedRow()));
        indietroButton.addActionListener(event -> mainFrame.mostraHomeAdmin());
        pulsanti.add(gestisciButton);
        pulsanti.add(indietroButton);
        add(pulsanti, BorderLayout.SOUTH);
    }

    public void aggiornaDati() {
        barche.clear();
        tableModel.setRowCount(0);
        barche.addAll(mainFrame.getController().getBarche());

        // Carica la tabella delle barche
        for (Barca barca : barche) {
            tableModel.addRow(new Object[]{
                    barca.getMatricola(),
                    barca.getNome(),
                    barca.getSede().getNome(),
                    barca.getStato(),
                    mainFrame.formatData(barca.getIndisponibileFinoAl())
            });
        }
    }

    private void apriGestioneManutenzione(int riga) {
        if (riga < 0 || riga >= barche.size()) {
            mainFrame.mostraErrore(this, new IllegalArgumentException("Selezionare una barca."));
            return;
        }
        mainFrame.setBarcaSelezionata(barche.get(riga));
        mainFrame.setManutenzioneSelezionata(null);
        mainFrame.mostraGestioneManutenzione();
    }
}
