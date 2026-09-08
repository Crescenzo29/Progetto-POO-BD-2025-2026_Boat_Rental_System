package gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.Barca;
import model.Sede;

/**
 * Pannello per cercare e scegliere una barca.
 */
public class CatalogoPanel extends JPanel {
    private final MainFrame mainFrame;
    private final JComboBox<String> tipoCombo = new JComboBox<>(new String[]{"Tutti", "Barca a vela", "Barca a motore"});
    private final JComboBox<String> patenteCombo = new JComboBox<>(new String[]{"Tutti", "Si", "No"});
    private final JComboBox<String> sedeCombo = new JComboBox<>();
    private final JTextField passeggeriField = new JTextField(8);
    private final JTextField tariffaField = new JTextField(8);
    private final JTextField dataInizioField = new JTextField(10);
    private final JTextField dataFineField = new JTextField(10);
    private final List<Barca> risultati = new ArrayList<>();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{
                    "Matricola", "Nome", "Capacita passeggeri", "Tariffa giornaliera",
                    "Richiede patente", "Stato", "Indisponibile fino al", "Sede"
            },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public CatalogoPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel titolo = new JLabel("SCEGLI LA TUA IMBARCAZIONE DA NOLEGGIARE", SwingConstants.CENTER);
        titolo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        add(titolo, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(10, 10));
        centro.add(creaFiltri(), BorderLayout.NORTH);

        JTable tabella = new JTable(tableModel);
        tabella.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    apriPrenotazione(tabella.getSelectedRow());
                }
            }
        });
        centro.add(new JScrollPane(tabella), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        JPanel pulsanti = new JPanel();
        JButton prenotaButton = new JButton("Prenota Barca Selezionata");
        JButton indietroButton = new JButton("Indietro");
        prenotaButton.addActionListener(event -> apriPrenotazione(tabella.getSelectedRow()));
        indietroButton.addActionListener(event -> mainFrame.mostraHomeCliente());
        pulsanti.add(prenotaButton);
        pulsanti.add(indietroButton);
        add(pulsanti, BorderLayout.SOUTH);
    }

    public void aggiornaDati() {
        caricaSedi();
        cerca();
    }

    private JPanel creaFiltri() {
        JPanel filtri = new JPanel(new GridBagLayout());
        aggiungiRigaFiltro(filtri, 0, "Tipo imbarcazione", tipoCombo);
        aggiungiRigaFiltro(filtri, 1, "Patente", patenteCombo);
        aggiungiRigaFiltro(filtri, 2, "Numero passeggeri", passeggeriField);
        aggiungiRigaFiltro(filtri, 3, "Tariffa giornaliera massima", tariffaField);
        aggiungiRigaFiltro(filtri, 4, "Sede", sedeCombo);
        aggiungiRigaFiltro(filtri, 5, "Data Inizio", dataInizioField);
        aggiungiRigaFiltro(filtri, 6, "Data Fine", dataFineField);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.insets = new Insets(8, 8, 8, 8);
        JButton cercaButton = new JButton("Cerca");
        cercaButton.addActionListener(event -> cerca());
        filtri.add(cercaButton, gbc);
        return filtri;
    }

    private void aggiungiRigaFiltro(JPanel filtri, int riga, String etichetta, java.awt.Component campo) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = riga;
        filtri.add(new JLabel(etichetta), gbc);

        gbc.gridx = 1;
        filtri.add(campo, gbc);
    }

    private void cerca() {
        try {
            risultati.clear();
            tableModel.setRowCount(0);

            String tipo = (String) tipoCombo.getSelectedItem();
            Boolean patente = valorePatente();
            Integer passeggeri = interoOpzionale(passeggeriField.getText());
            Double tariffaMassima = doubleOpzionale(tariffaField.getText());
            Integer idSede = idSedeSelezionata();
            LocalDate dataInizio = mainFrame.parseDataOpzionale(dataInizioField.getText());
            LocalDate dataFine = mainFrame.parseDataOpzionale(dataFineField.getText());

            List<Barca> barche = mainFrame.getController().cercaBarche(
                    tipo, patente, passeggeri, tariffaMassima, idSede, dataInizio, dataFine);
            risultati.addAll(barche);

            for (Barca barca : risultati) {
                tableModel.addRow(new Object[]{
                        barca.getMatricola(),
                        barca.getNome(),
                        barca.getCapacitaPasseggeri(),
                        barca.getTariffaGiornaliera(),
                        barca.isRichiedePatente() ? "Si" : "No",
                        barca.getStato(),
                        mainFrame.formatData(barca.getIndisponibileFinoAl()),
                        barca.getSede().getNome()
                });
            }
        } catch (RuntimeException ex) {
            mainFrame.mostraErrore(this, ex);
        }
    }

    private void apriPrenotazione(int riga) {
        if (riga < 0 || riga >= risultati.size()) {
            mainFrame.mostraErrore(this, new IllegalArgumentException("Selezionare una barca."));
            return;
        }
        mainFrame.setBarcaSelezionata(risultati.get(riga));
        mainFrame.mostraPrenotazione();
    }

    private void caricaSedi() {
        Object selezione = sedeCombo.getSelectedItem();
        sedeCombo.removeAllItems();
        sedeCombo.addItem("Tutte le sedi");
        for (Sede sede : mainFrame.getController().getSedi()) {
            sedeCombo.addItem(sede.getIdSede() + " - " + sede.getNome());
        }
        if (selezione != null) {
            sedeCombo.setSelectedItem(selezione);
        }
    }

    private Boolean valorePatente() {
        String valore = (String) patenteCombo.getSelectedItem();
        if ("Si".equals(valore)) {
            return true;
        }
        if ("No".equals(valore)) {
            return false;
        }
        return null;
    }

    private Integer idSedeSelezionata() {
        String valore = (String) sedeCombo.getSelectedItem();
        if (valore == null || valore.equals("Tutte le sedi")) {
            return null;
        }
        return Integer.parseInt(valore.substring(0, valore.indexOf(" - ")));
    }

    private Integer interoOpzionale(String testo) {
        if (testo == null || testo.trim().isEmpty()) {
            return null;
        }
        return Integer.parseInt(testo.trim());
    }

    private Double doubleOpzionale(String testo) {
        if (testo == null || testo.trim().isEmpty()) {
            return null;
        }
        return Double.parseDouble(testo.trim());
    }
}
