package gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import model.Barca;
import model.Cliente;
import model.Prenotazione;
import model.StatoPrenotazione;

/**
 * Pannello per creare una prenotazione.
 */
public class PrenotazionePanel extends JPanel {
    private final MainFrame mainFrame;
    private final JLabel nomeBarcaLabel = new JLabel("-");
    private final JLabel matricolaLabel = new JLabel("-");
    private final JLabel tariffaLabel = new JLabel("-");
    private final JLabel capacitaLabel = new JLabel("-");
    private final JLabel patenteLabel = new JLabel("-");
    private final JTextField dataInizioField = new JTextField(12);
    private final JTextField dataFineField = new JTextField(12);
    private final JTextField passeggeriField = new JTextField(12);

    public PrenotazionePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(10, 20));
        setBorder(new EmptyBorder(25, 80, 25, 80));

        JLabel titolo = new JLabel("PRENOTA LA TUA IMBARCAZIONE", SwingConstants.CENTER);
        titolo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        add(titolo, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        aggiungiRiga(form, 0, "Nome Barca", nomeBarcaLabel);
        aggiungiRiga(form, 1, "Matricola", matricolaLabel);
        aggiungiRiga(form, 2, "Tariffa giornaliera", tariffaLabel);
        aggiungiRiga(form, 3, "Capacita passeggeri", capacitaLabel);
        aggiungiRiga(form, 4, "Richiede patente", patenteLabel);
        aggiungiRiga(form, 5, "Data Inizio", dataInizioField);
        aggiungiRiga(form, 6, "Data Fine", dataFineField);
        aggiungiRiga(form, 7, "Numero Passeggeri", passeggeriField);

        JPanel pulsanti = new JPanel();
        JButton noleggiaButton = new JButton("Noleggia");
        JButton indietroButton = new JButton("Indietro");
        noleggiaButton.addActionListener(event -> prenota());
        indietroButton.addActionListener(event -> mainFrame.mostraCatalogo());
        pulsanti.add(noleggiaButton);
        pulsanti.add(indietroButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 8, 8, 8);
        form.add(pulsanti, gbc);

        add(form, BorderLayout.CENTER);
    }

    public void aggiornaDati() {
        Barca barca = mainFrame.getBarcaSelezionata();
        if (barca == null) {
            nomeBarcaLabel.setText("-");
            matricolaLabel.setText("-");
            tariffaLabel.setText("-");
            capacitaLabel.setText("-");
            patenteLabel.setText("-");
            return;
        }

        nomeBarcaLabel.setText(barca.getNome());
        matricolaLabel.setText(barca.getMatricola());
        tariffaLabel.setText(String.valueOf(barca.getTariffaGiornaliera()));
        capacitaLabel.setText(String.valueOf(barca.getCapacitaPasseggeri()));
        patenteLabel.setText(barca.isRichiedePatente() ? "Si" : "No");
    }

    private void prenota() {
        try {
            Cliente cliente = mainFrame.getClienteAutenticato();
            Barca barca = mainFrame.getBarcaSelezionata();
            if (cliente == null) {
                throw new IllegalArgumentException("Cliente non autenticato.");
            }
            if (barca == null) {
                throw new IllegalArgumentException("Barca non selezionata.");
            }

            LocalDate dataInizio = mainFrame.parseData(dataInizioField.getText());
            LocalDate dataFine = mainFrame.parseData(dataFineField.getText());
            int passeggeri = Integer.parseInt(passeggeriField.getText().trim());

            Prenotazione prenotazione = new Prenotazione(
                    mainFrame.getController().prossimoIdPrenotazione(),
                    LocalDate.now(),
                    dataInizio,
                    dataFine,
                    passeggeri,
                    cliente.haPatenteNautica(),
                    StatoPrenotazione.CONFERMATA,
                    cliente,
                    barca);

            mainFrame.getController().aggiungiPrenotazione(prenotazione);
            pulisciCampi();
            mainFrame.mostraMessaggio(this, "Prenotazione effettuata correttamente.");
            mainFrame.mostraHomeCliente();
        } catch (RuntimeException ex) {
            mainFrame.mostraErrore(this, ex);
        }
    }

    private void aggiungiRiga(JPanel form, int riga, String etichetta, java.awt.Component campo) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = riga;
        form.add(new JLabel(etichetta), gbc);

        gbc.gridx = 1;
        form.add(campo, gbc);
    }

    private void pulisciCampi() {
        dataInizioField.setText("");
        dataFineField.setText("");
        passeggeriField.setText("");
    }
}
