package gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import model.Noleggio;
import model.Prenotazione;

/**
 * Pannello admin per gestire un noleggio.
 */
public class GestioneNoleggioPanel extends JPanel {
    private final MainFrame mainFrame;
    private final JLabel idPrenotazioneLabel = new JLabel("-");
    private final JLabel clienteLabel = new JLabel("-");
    private final JLabel barcaLabel = new JLabel("-");
    private final JLabel matricolaLabel = new JLabel("-");
    private final JLabel periodoLabel = new JLabel("-");
    private final JLabel statoPrenotazioneLabel = new JLabel("-");
    private final JLabel noleggioAssociatoLabel = new JLabel("-");
    private final JTextField dataRitiroField = new JTextField(10);
    private final JTextField oraRitiroField = new JTextField(6);
    private final JTextArea noteRitiroArea = new JTextArea(3, 24);
    private final JTextField dataRestituzioneField = new JTextField(10);
    private final JTextField oraRestituzioneField = new JTextField(6);
    private final JTextArea noteRestituzioneArea = new JTextArea(3, 24);
    private final JComboBox<String> completatoCombo = new JComboBox<>(new String[]{"Si", "No"});

    public GestioneNoleggioPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 40, 20, 40));

        JLabel titolo = new JLabel("GESTIONE NOLEGGIO", SwingConstants.CENTER);
        titolo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        add(titolo, BorderLayout.NORTH);

        JPanel contenuto = new JPanel(new GridBagLayout());
        aggiungiRiga(contenuto, 0, "ID Prenotazione", idPrenotazioneLabel);
        aggiungiRiga(contenuto, 1, "Cliente", clienteLabel);
        aggiungiRiga(contenuto, 2, "Barca", barcaLabel);
        aggiungiRiga(contenuto, 3, "Matricola", matricolaLabel);
        aggiungiRiga(contenuto, 4, "Periodo", periodoLabel);
        aggiungiRiga(contenuto, 5, "Stato Prenotazione", statoPrenotazioneLabel);
        aggiungiRiga(contenuto, 6, "Noleggio associato", noleggioAssociatoLabel);

        JLabel ritiro = new JLabel("RITIRO");
        ritiro.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        aggiungiRiga(contenuto, 7, "", ritiro);
        aggiungiRiga(contenuto, 8, "Data Ritiro", dataRitiroField);
        aggiungiRiga(contenuto, 9, "Ora Ritiro", oraRitiroField);
        aggiungiRiga(contenuto, 10, "Note Ritiro", new JScrollPane(noteRitiroArea));

        JButton avviaButton = new JButton("Avvia Noleggio");
        avviaButton.addActionListener(event -> avviaNoleggio());
        aggiungiRiga(contenuto, 11, "", avviaButton);

        JLabel restituzione = new JLabel("RESTITUZIONE");
        restituzione.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        aggiungiRiga(contenuto, 12, "", restituzione);
        aggiungiRiga(contenuto, 13, "Data Restituzione", dataRestituzioneField);
        aggiungiRiga(contenuto, 14, "Ora Restituzione", oraRestituzioneField);
        aggiungiRiga(contenuto, 15, "Note Restituzione", new JScrollPane(noteRestituzioneArea));
        aggiungiRiga(contenuto, 16, "Completato correttamente", completatoCombo);

        JButton terminaButton = new JButton("Termina Noleggio");
        terminaButton.addActionListener(event -> terminaNoleggio());
        aggiungiRiga(contenuto, 17, "", terminaButton);

        add(new JScrollPane(contenuto), BorderLayout.CENTER);

        JPanel pulsanti = new JPanel();
        JButton indietroButton = new JButton("Indietro");
        indietroButton.addActionListener(event -> mainFrame.mostraHomeAdmin());
        pulsanti.add(indietroButton);
        add(pulsanti, BorderLayout.SOUTH);
    }

    public void aggiornaDati() {
        Prenotazione prenotazione = prenotazioneCorrente();
        if (prenotazione == null) {
            pulisciDati();
            return;
        }

        idPrenotazioneLabel.setText(String.valueOf(prenotazione.getIdPrenotazione()));
        clienteLabel.setText(prenotazione.getCliente().getNome() + " " + prenotazione.getCliente().getCognome());
        barcaLabel.setText(prenotazione.getBarca().getNome());
        matricolaLabel.setText(prenotazione.getBarca().getMatricola());
        periodoLabel.setText(mainFrame.formatData(prenotazione.getDataInizio())
                + " - " + mainFrame.formatData(prenotazione.getDataFine()));
        statoPrenotazioneLabel.setText(String.valueOf(prenotazione.getStato()));
        noleggioAssociatoLabel.setText(mainFrame.getController()
                .getNoleggioPerPrenotazione(prenotazione.getIdPrenotazione())
                .map(noleggio -> noleggio.getIdNoleggio() + " - " + noleggio.getStato())
                .orElse("Nessuno"));
    }

    private void avviaNoleggio() {
        try {
            Prenotazione prenotazione = prenotazioneCorrente();
            if (prenotazione == null) {
                throw new IllegalArgumentException("Prenotazione non selezionata.");
            }
            LocalDateTime dataOraRitiro = mainFrame.parseDataOra(
                    dataRitiroField.getText(), oraRitiroField.getText());
            mainFrame.getController().avviaNoleggio(
                    mainFrame.getController().prossimoIdNoleggio(),
                    prenotazione.getIdPrenotazione(),
                    dataOraRitiro,
                    noteRitiroArea.getText());

            mainFrame.mostraMessaggio(this, "Noleggio avviato correttamente.");
            aggiornaDati();
        } catch (RuntimeException ex) {
            mainFrame.mostraErrore(this, ex);
        }
    }

    private void terminaNoleggio() {
        try {
            Prenotazione prenotazione = prenotazioneCorrente();
            if (prenotazione == null) {
                throw new IllegalArgumentException("Prenotazione non selezionata.");
            }
            Noleggio noleggio = mainFrame.getController()
                    .getNoleggioPerPrenotazione(prenotazione.getIdPrenotazione())
                    .orElseThrow(() -> new IllegalArgumentException("Noleggio non trovato."));
            LocalDateTime dataOraRestituzione = mainFrame.parseDataOra(
                    dataRestituzioneField.getText(), oraRestituzioneField.getText());
            boolean completato = "Si".equals(completatoCombo.getSelectedItem());

            mainFrame.getController().terminaNoleggio(
                    noleggio.getIdNoleggio(),
                    dataOraRestituzione,
                    noteRestituzioneArea.getText(),
                    completato);

            mainFrame.mostraMessaggio(this, "Noleggio terminato correttamente.");
            aggiornaDati();
        } catch (RuntimeException ex) {
            mainFrame.mostraErrore(this, ex);
        }
    }

    private Prenotazione prenotazioneCorrente() {
        Prenotazione selezionata = mainFrame.getPrenotazioneSelezionata();
        if (selezionata == null) {
            return null;
        }
        Prenotazione aggiornata = mainFrame.getController()
                .getPrenotazione(selezionata.getIdPrenotazione())
                .orElse(selezionata);
        mainFrame.setPrenotazioneSelezionata(aggiornata);
        return aggiornata;
    }

    private void aggiungiRiga(JPanel panel, int riga, String etichetta, java.awt.Component campo) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = riga;
        panel.add(new JLabel(etichetta), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(campo, gbc);
    }

    private void pulisciDati() {
        idPrenotazioneLabel.setText("-");
        clienteLabel.setText("-");
        barcaLabel.setText("-");
        matricolaLabel.setText("-");
        periodoLabel.setText("-");
        statoPrenotazioneLabel.setText("-");
        noleggioAssociatoLabel.setText("-");
    }
}
