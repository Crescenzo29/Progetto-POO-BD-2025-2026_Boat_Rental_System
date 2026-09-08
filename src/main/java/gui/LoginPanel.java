package gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import model.Cliente;

/**
 * Pannello di login.
 */
public class LoginPanel extends JPanel {
    private final MainFrame mainFrame;
    private final JTextField emailField = new JTextField(25);
    private final JPasswordField passwordField = new JPasswordField(25);

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(10, 20));
        setBorder(new EmptyBorder(40, 80, 40, 80));

        JLabel titolo = new JLabel("ACCEDI AL TUO ACCOUNT", SwingConstants.CENTER);
        titolo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        add(titolo, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Email"), gbc);
        gbc.gridx = 1;
        form.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Password"), gbc);
        gbc.gridx = 1;
        form.add(passwordField, gbc);

        JPanel pulsanti = new JPanel();
        JButton accediButton = new JButton("Accedi");
        JButton indietroButton = new JButton("Indietro");
        accediButton.addActionListener(event -> accedi());
        indietroButton.addActionListener(event -> mainFrame.mostraStart());
        pulsanti.add(accediButton);
        pulsanti.add(indietroButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        form.add(pulsanti, gbc);

        add(form, BorderLayout.CENTER);
    }

    private void accedi() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (mainFrame.getController().isAdmin(email, password)) {
            mainFrame.setClienteAutenticato(null);
            mainFrame.mostraHomeAdmin();
            pulisciCampi();
            return;
        }

        mainFrame.getController().autenticaCliente(email, password)
                .ifPresentOrElse(this::loginCliente, this::mostraErroreLogin);
    }

    private void loginCliente(Cliente cliente) {
        mainFrame.setClienteAutenticato(cliente);
        pulisciCampi();
        mainFrame.mostraHomeCliente();
    }

    private void mostraErroreLogin() {
        mainFrame.mostraErrore(this, new IllegalArgumentException("Email o password non corretti."));
    }

    private void pulisciCampi() {
        emailField.setText("");
        passwordField.setText("");
    }
}
