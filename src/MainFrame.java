
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JTextField txtNomeUtente, txtCodiceVolo, txtGate;
    private JPasswordField txtPassword;
    private String ruoloUtente = "Utente";
    private List<Volo> voliInArrivo;
    private List<Volo> voliInPartenza;
    private List<Prenotazione> prenotazioni;

    public MainFrame() {
        setTitle("Gestione Aeroporto");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        voliInArrivo = new ArrayList<>();
        voliInPartenza = new ArrayList<>();
        prenotazioni = new ArrayList<>();

        mainPanel.add(createVoloPanel(), "Volo");
        mainPanel.add(createLoginPanel(), "Login");
        mainPanel.add(createMainContentPanel(), "Main");

        add(mainPanel);
        cardLayout.show(mainPanel, "Volo");
    }

    private JPanel createVoloPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));

        JPanel arrivoPanel = new JPanel(new BorderLayout());
        String[] colonne = {"Codice", "Compagnia", "Origine", "Destinazione", "Data", "Orario", "Stato", "Gate"};
        Object[][] datiArrivo = new Object[voliInArrivo.size()][8];

        for (int i = 0; i < voliInArrivo.size(); i++) {
            Volo volo = voliInArrivo.get(i);
            datiArrivo[i] = new Object[]{
                    volo.getCodice(), volo.getCompagnia(), volo.getOrigine(), volo.getDestinazione(),
                    volo.getData(), volo.getOrario(), volo.getStato(), volo.getGate()
            };
        }

        JTable tableArrivo = new JTable(datiArrivo, colonne);
        JScrollPane scrollPaneArrivo = new JScrollPane(tableArrivo);
        arrivoPanel.add(new JLabel("Voli in Arrivo:"), BorderLayout.NORTH);
        arrivoPanel.add(scrollPaneArrivo, BorderLayout.CENTER);

        JPanel partenzaPanel = new JPanel(new BorderLayout());
        Object[][] datiPartenza = new Object[voliInPartenza.size()][8];

        for (int i = 0; i < voliInPartenza.size(); i++) {
            Volo volo = voliInPartenza.get(i);
            datiPartenza[i] = new Object[]{
                    volo.getCodice(), volo.getCompagnia(), volo.getOrigine(), volo.getDestinazione(),
                    volo.getData(), volo.getOrario(), volo.getStato(), volo.getGate()
            };
        }

        JTable tablePartenza = new JTable(datiPartenza, colonne);
        JScrollPane scrollPanePartenza = new JScrollPane(tablePartenza);
        partenzaPanel.add(new JLabel("Voli in Partenza:"), BorderLayout.NORTH);
        partenzaPanel.add(scrollPanePartenza, BorderLayout.CENTER);

        centerPanel.add(arrivoPanel);
        centerPanel.add(partenzaPanel);

        JButton btnLogin = new JButton("Accedi");
        btnLogin.setPreferredSize(new Dimension(100, 30));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnLogin.addActionListener(e -> cardLayout.show(mainPanel, "Login"));
        buttonPanel.add(btnLogin);

        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLoginPanel() {
        JPanel loginPanel = new JPanel(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        txtNomeUtente = new JTextField();
        txtPassword = new JPasswordField();

        String[] ruoli = {"Utente", "Amministratore"};
        JComboBox<String> cmbRuolo = new JComboBox<>(ruoli);

        inputPanel.add(new JLabel("Nome Utente:"));
        inputPanel.add(txtNomeUtente);
        inputPanel.add(new JLabel("Password:"));
        inputPanel.add(txtPassword);
        inputPanel.add(new JLabel("Ruolo:"));
        inputPanel.add(cmbRuolo);

        loginPanel.add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnAccedi = new JButton("Accedi");
        btnAccedi.addActionListener(e -> {
            ruoloUtente = (String) cmbRuolo.getSelectedItem();
            verificaCredenziali();
        });
        buttonPanel.add(btnAccedi);

        loginPanel.add(buttonPanel, BorderLayout.SOUTH);

        return loginPanel;
    }

    private JPanel createMainContentPanel() {
        JPanel container = new JPanel(new BorderLayout());

        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnInfo = new JButton("Info Personali");
        JButton btnVolo = new JButton("Voli");
        JButton btnPrenotazione = new JButton("Prenotazioni");
        JButton btnLogout = new JButton("Logout");

        JPanel contentPanel = new JPanel(new CardLayout());

        contentPanel.add(createInfoPanel(), "Info");
        contentPanel.add(createVoloPanel(), "Volo");
        contentPanel.add(createPrenotazionePanel(), "Prenotazione");

        btnInfo.addActionListener(e -> switchContent(contentPanel, "Info"));
        btnVolo.addActionListener(e -> switchContent(contentPanel, "Volo"));
        btnPrenotazione.addActionListener(e -> switchContent(contentPanel, "Prenotazione"));
        btnLogout.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Logout effettuato.");
            cardLayout.show(mainPanel, "Login");
        });

        navBar.add(btnInfo);
        navBar.add(btnVolo);
        navBar.add(btnPrenotazione);
        navBar.add(btnLogout);

        container.add(navBar, BorderLayout.NORTH);
        container.add(contentPanel, BorderLayout.CENTER);

        return container;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));

        JTextField txtNomePasseggero = new JTextField();
        JTextField txtCognome = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtSSN = new JTextField();
        JTextField txtPosto = new JTextField();
        JTextField txtTelefono = new JTextField();

        panel.add(new JLabel("Nome:"));
        panel.add(txtNomePasseggero);
        panel.add(new JLabel("Cognome:"));
        panel.add(txtCognome);
        panel.add(new JLabel("Email:"));
        panel.add(txtEmail);
        panel.add(new JLabel("SSN:"));
        panel.add(txtSSN);
        panel.add(new JLabel("Posto:"));
        panel.add(txtPosto);
        panel.add(new JLabel("Telefono:"));
        panel.add(txtTelefono);

        return panel;
    }

    private JPanel createPrenotazionePanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));

        JTextField txtPNR = new JTextField();
        JTextField txtBiglietto = new JTextField();
        JCheckBox chkBagaglio = new JCheckBox("Bagaglio");
        JCheckBox chkAssicurazione = new JCheckBox("Assicurazione");

        panel.add(new JLabel("PNR:"));
        panel.add(txtPNR);
        panel.add(new JLabel("Numero Biglietto:"));
        panel.add(txtBiglietto);
        panel.add(chkBagaglio);
        panel.add(new JLabel());
        panel.add(chkAssicurazione);
        panel.add(new JLabel());

        return panel;
    }

    private void switchContent(JPanel contentPanel, String name) {
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, name);
    }

    private void verificaCredenziali() {
        String utente = txtNomeUtente.getText();
        String password = new String(txtPassword.getPassword());

        JOptionPane.showMessageDialog(this, "Login effettuato con successo!");
        cardLayout.show(mainPanel, "Main");

        if (ruoloUtente.equals("Amministratore")) {
            aggiungiEsempiVoli();
        }
    }

    private void aggiungiEsempiVoli() {
        voliInArrivo.add(new Volo("V001", "Alitalia", "Roma", "Napoli", "2025-06-01", "12:30", "Programmato", "Gate 1"));
        voliInArrivo.add(new Volo("V002", "Ryanair", "Milano", "Napoli", "2025-06-01", "14:30", "In Ritardo", "Gate 2"));
        voliInPartenza.add(new Volo("V003", "EasyJet", "Napoli", "Londra", "2025-06-01", "16:00", "Cancellato", "Gate 3"));
        voliInPartenza.add(new Volo("V004", "Vueling", "Napoli", "Barcellona", "2025-06-01", "18:00", "Programmato", "Gate 4"));
    }

    class Volo {
        private String codice;
        private String compagnia;
        private String origine;
        private String destinazione;
        private String data;
        private String orario;
        private String stato;
        private String gate;

        public Volo(String codice, String compagnia, String origine, String destinazione, String data,
                    String orario, String stato, String gate) {
            this.codice = codice;
            this.compagnia = compagnia;
            this.origine = origine;
            this.destinazione = destinazione;
            this.data = data;
            this.orario = orario;
            this.stato = stato;
            this.gate = gate;
        }

        public String getCodice() { return codice; }
        public String getCompagnia() { return compagnia; }
        public String getOrigine() { return origine; }
        public String getDestinazione() { return destinazione; }
        public String getData() { return data; }
        public String getOrario() { return orario; }
        public String getStato() { return stato; }
        public String getGate() { return gate; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
