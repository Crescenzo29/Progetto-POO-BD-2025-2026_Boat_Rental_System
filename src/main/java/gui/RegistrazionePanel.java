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
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import model.Cliente;

/**
 * Pannello per registrare un nuovo cliente.
 */
public class RegistrazionePanel extends JPanel {
    private final MainFrame mainFrame;
    private final JTextField nomeField = new JTextField(22);
    private final JTextField cognomeField = new JTextField(22);
    private final JTextField emailField = new JTextField(22);
    private final JPasswordField passwordField = new JPasswordField(22);
    private final JTextField dataNascitaField = new JTextField(22);
    private final JTextField numeroPatenteField = new JTextField(22);
    private final JTextField scadenzaPatenteField = new JTextField(22);

    public RegistrazionePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(10, 20));
        setBorder(new EmptyBorder(30, 80, 30, 80));

        JLabel titolo = new JLabel("REGISTRATI", SwingConstants.CENTER);
        titolo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        add(titolo, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        aggiungiRiga(form, 0, "Nome", nomeField);
        aggiungiRiga(form, 1, "Cognome", cognomeField);
        aggiungiRiga(form, 2, "Email", emailField);
        aggiungiRiga(form, 3, "Password", passwordField);
        aggiungiRiga(form, 4, "Data di nascita", dataNascitaField);
        aggiungiRiga(form, 5, "Numero patente nautica", numeroPatenteField);
        aggiungiRiga(form, 6, "Data scadenza patente", scadenzaPatenteField);

        JPanel pulsanti = new JPanel();
        JButton registratiButton = new JButton("Registrati");
        JButton indietroButton = new JButton("Indietro");
        registratiButton.addActionListener(event -> registra());
        indietroButton.addActionListener(event -> mainFrame.mostraStart());
        pulsanti.add(registratiButton);
        pulsanti.add(indietroButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 8, 8, 8);
        form.add(pulsanti, gbc);

        add(form, BorderLayout.CENTER);
    }

    private void registra() {
        try {
            String password = new String(passwordField.getPassword());
            if (password.isBlank()) {
                throw new IllegalArgumentException("La password non puo' essere vuota.");
            }

            LocalDate dataNascita = mainFrame.parseData(dataNascitaField.getText());
            String numeroPatente = numeroPatenteField.getText().trim();
            LocalDate scadenzaPatente = mainFrame.parseDataOpzionale(scadenzaPatenteField.getText());
            if (numeroPatente.isBlank()) {
                numeroPatente = null;
            }

            Cliente cliente = new Cliente(
                    mainFrame.getController().prossimoIdCliente(),
                    nomeField.getText().trim(),
                    cognomeField.getText().trim(),
                    emailField.getText().trim(),
                    mainFrame.getController().generaPasswordHash(password),
                    dataNascita,
                    numeroPatente,
                    scadenzaPatente);

            mainFrame.getController().aggiungiCliente(cliente);
            mainFrame.setClienteAutenticato(cliente);
            pulisciCampi();
            mainFrame.mostraHomeCliente();
        } catch (RuntimeException ex) {
            mainFrame.mostraErrore(this, ex);
        }
    }

    private void aggiungiRiga(JPanel form, int riga, String etichetta, JTextField campo) {
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
        nomeField.setText("");
        cognomeField.setText("");
        emailField.setText("");
        passwordField.setText("");
        dataNascitaField.setText("");
        numeroPatenteField.setText("");
        scadenzaPatenteField.setText("");
    }
}
