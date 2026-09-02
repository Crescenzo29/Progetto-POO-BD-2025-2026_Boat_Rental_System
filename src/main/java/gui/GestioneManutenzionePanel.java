package gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.Barca;
import model.Manutenzione;
import model.StatoManutenzione;

/**
 * Pannello admin per gestire le manutenzioni.
 */
public class GestioneManutenzionePanel extends JPanel {
    private final MainFrame mainFrame;
    private final JLabel nomeBarcaLabel = new JLabel("-");
    private final JLabel matricolaLabel = new JLabel("-");
    private final JLabel statoBarcaLabel = new JLabel("-");
    private final JTextField dataInizioField = new JTextField(10);
    private final JTextField dataFineField = new JTextField(10);
    private final JTextArea descrizioneArea = new JTextArea(3, 24);
    private final List<Manutenzione> manutenzioni = new ArrayList<>();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Data Inizio", "Data Fine", "Descrizione", "Stato"},
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public GestioneManutenzionePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 35, 20, 35));

        JLabel titolo = new JLabel("GESTIONE MANUTENZIONE", SwingConstants.CENTER);
        titolo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        add(titolo, BorderLayout.NORTH);

        JPanel contenuto = new JPanel(new BorderLayout(10, 10));
        contenuto.add(creaForm(), BorderLayout.NORTH);

        JTable tabella = new JTable(tableModel);
        tabella.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                selezionaManutenzione(tabella.getSelectedRow());
            }
        });
        contenuto.add(new JScrollPane(tabella), BorderLayout.CENTER);
        add(contenuto, BorderLayout.CENTER);

        JPanel pulsanti = new JPanel();
        JButton programmaButton = new JButton("Programma Manutenzione");
        JButton avviaButton = new JButton("Avvia Manutenzione");
        JButton completaButton = new JButton("Completa Manutenzione");
        JButton indietroButton = new JButton("Indietro");
        programmaButton.addActionListener(event -> programmaManutenzione());
        avviaButton.addActionListener(event -> avviaManutenzione());
        completaButton.addActionListener(event -> completaManutenzione());
        indietroButton.addActionListener(event -> mainFrame.mostraManutenzioneBarche());
        pulsanti.add(programmaButton);
        pulsanti.add(avviaButton);
        pulsanti.add(completaButton);
        pulsanti.add(indietroButton);
        add(pulsanti, BorderLayout.SOUTH);
    }

    public void aggiornaDati() {
        Barca barca = barcaCorrente();
        tableModel.setRowCount(0);
        manutenzioni.clear();

        if (barca == null) {
            nomeBarcaLabel.setText("-");
            matricolaLabel.setText("-");
            statoBarcaLabel.setText("-");
            return;
        }

        nomeBarcaLabel.setText(barca.getNome());
        matricolaLabel.setText(barca.getMatricola());
        statoBarcaLabel.setText(String.valueOf(barca.getStato()));

        manutenzioni.addAll(mainFrame.getController().getManutenzioniBarca(barca.getMatricola()));
        for (Manutenzione manutenzione : manutenzioni) {
            tableModel.addRow(new Object[]{
                    manutenzione.getIdManutenzione(),
                    mainFrame.formatData(manutenzione.getDataInizio()),
                    mainFrame.formatData(manutenzione.getDataFine()),
                    manutenzione.getDescrizione(),
                    manutenzione.getStato()
            });
        }
    }

    private JPanel creaForm() {
        JPanel form = new JPanel(new GridBagLayout());
        aggiungiRiga(form, 0, "Nome Barca", nomeBarcaLabel);
        aggiungiRiga(form, 1, "Matricola", matricolaLabel);
        aggiungiRiga(form, 2, "Stato", statoBarcaLabel);
        aggiungiRiga(form, 3, "Data Inizio", dataInizioField);
        aggiungiRiga(form, 4, "Data Fine", dataFineField);
        aggiungiRiga(form, 5, "Descrizione", new JScrollPane(descrizioneArea));
        return form;
    }

    private void programmaManutenzione() {
        try {
            Barca barca = barcaCorrente();
            if (barca == null) {
                throw new IllegalArgumentException("Barca non selezionata.");
            }
            Manutenzione manutenzione = new Manutenzione(
                    mainFrame.getController().prossimoIdManutenzione(),
                    mainFrame.parseData(dataInizioField.getText()),
                    mainFrame.parseData(dataFineField.getText()),
                    descrizioneArea.getText().trim(),
                    StatoManutenzione.PROGRAMMATA,
                    barca);

            mainFrame.getController().aggiungiManutenzione(manutenzione);
            mainFrame.setManutenzioneSelezionata(manutenzione);
            pulisciCampi();
            mainFrame.mostraMessaggio(this, "Manutenzione programmata correttamente.");
            aggiornaDati();
        } catch (RuntimeException ex) {
            mainFrame.mostraErrore(this, ex);
        }
    }

    private void avviaManutenzione() {
        try {
            Manutenzione manutenzione = manutenzioneSelezionata();
            mainFrame.getController().avviaManutenzione(manutenzione.getIdManutenzione());
            mainFrame.mostraMessaggio(this, "Manutenzione avviata correttamente.");
            aggiornaDati();
        } catch (RuntimeException ex) {
            mainFrame.mostraErrore(this, ex);
        }
    }

    private void completaManutenzione() {
        try {
            Manutenzione manutenzione = manutenzioneSelezionata();
            mainFrame.getController().completaManutenzione(manutenzione.getIdManutenzione());
            mainFrame.mostraMessaggio(this, "Manutenzione completata correttamente.");
            aggiornaDati();
        } catch (RuntimeException ex) {
            mainFrame.mostraErrore(this, ex);
        }
    }

    private Barca barcaCorrente() {
        Barca selezionata = mainFrame.getBarcaSelezionata();
        if (selezionata == null) {
            return null;
        }
        Barca aggiornata = mainFrame.getController()
                .getBarca(selezionata.getMatricola())
                .orElse(selezionata);
        mainFrame.setBarcaSelezionata(aggiornata);
        return aggiornata;
    }

    private Manutenzione manutenzioneSelezionata() {
        Manutenzione manutenzione = mainFrame.getManutenzioneSelezionata();
        if (manutenzione == null) {
            throw new IllegalArgumentException("Selezionare una manutenzione.");
        }
        return mainFrame.getController()
                .getManutenzione(manutenzione.getIdManutenzione())
                .orElse(manutenzione);
    }

    private void selezionaManutenzione(int riga) {
        if (riga >= 0 && riga < manutenzioni.size()) {
            mainFrame.setManutenzioneSelezionata(manutenzioni.get(riga));
        }
    }

    private void aggiungiRiga(JPanel form, int riga, String etichetta, java.awt.Component campo) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = riga;
        form.add(new JLabel(etichetta), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(campo, gbc);
    }

    private void pulisciCampi() {
        dataInizioField.setText("");
        dataFineField.setText("");
        descrizioneArea.setText("");
    }
}
