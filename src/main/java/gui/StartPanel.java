package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Schermata iniziale.
 */
public class StartPanel extends JPanel {
    public StartPanel(MainFrame mainFrame) {
        setLayout(new BorderLayout(10, 20));
        setBorder(new EmptyBorder(80, 120, 80, 120));

        JLabel titolo = new JLabel("NOLEGGIO BARCHE", SwingConstants.CENTER);
        titolo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        add(titolo, BorderLayout.NORTH);

        JPanel pulsanti = new JPanel(new GridLayout(2, 1, 10, 15));
        JButton loginButton = new JButton("Login");
        JButton registratiButton = new JButton("Registrati");

        loginButton.addActionListener(event -> mainFrame.mostraLogin());
        registratiButton.addActionListener(event -> mainFrame.mostraRegistrazione());

        pulsanti.add(loginButton);
        pulsanti.add(registratiButton);

        JPanel centro = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centro.add(pulsanti);
        add(centro, BorderLayout.CENTER);
    }
}
