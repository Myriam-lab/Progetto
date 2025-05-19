
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JTextField txtNomeUtente;
    private JPasswordField txtPassword;
    private String ruoloUtente = "Utente";
    private List<Volo> voliInArrivo = new ArrayList<>();
    private List<Volo> voliInPartenza = new ArrayList<>();
    private List<Prenotazione> prenotazioni = new ArrayList<>(); // Lista delle prenotazioni

    public MainFrame() {
        setTitle("Gestione Aeroporto");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        aggiungiEsempiVoli(); // Popola i voli prima della visualizzazione

        mainPanel.add(createVoloPanel(), "Volo");
        mainPanel.add(createLoginPanel(), "Login");
        mainPanel.add(createMainContentPanel(), "Main");

        add(mainPanel);
        cardLayout.show(mainPanel, "Volo");
    }

    private JPanel createVoloPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));

        String[] colonne = {"Codice", "Compagnia", "Origine", "Destinazione", "Data", "Orario", "Stato", "Gate"};

        JTable tableArrivo = new JTable(getDatiVoli(voliInArrivo), colonne);
        JTable tablePartenza = new JTable(getDatiVoli(voliInPartenza), colonne);

        // Disabilitiamo l'interazione con le tabelle per gli utenti
        if (ruoloUtente.equals("Utente")) {
            tableArrivo.setEnabled(false);
            tablePartenza.setEnabled(false);
        }

        // Aggiungi il listener per prenotare un volo
        tableArrivo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (ruoloUtente.equals("Utente")) {
                    int row = tableArrivo.rowAtPoint(e.getPoint());
                    Volo voloSelezionato = voliInArrivo.get(row);
                    prenotaVolo(voloSelezionato);
                }
            }
        });

        centerPanel.add(new JScrollPane(tableArrivo));
        centerPanel.add(new JScrollPane(tablePartenza));

        JButton btnLogin = new JButton("Accedi");
        btnLogin.addActionListener(e -> cardLayout.show(mainPanel, "Login"));
        JPanel btnPanel = new JPanel();
        btnPanel.add(btnLogin);

        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private Object[][] getDatiVoli(List<Volo> voli) {
        Object[][] dati = new Object[voli.size()][8];
        for (int i = 0; i < voli.size(); i++) {
            Volo v = voli.get(i);
            dati[i] = new Object[]{v.codice, v.compagnia, v.origine, v.destinazione, v.data, v.orario, v.stato, v.gate};
        }
        return dati;
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        txtNomeUtente = new JTextField();
        txtPassword = new JPasswordField();

        Dimension smallField = new Dimension(150, 45);
        txtNomeUtente.setPreferredSize(smallField);
        txtPassword.setPreferredSize(smallField);

        JLabel lblUser = new JLabel("Nome Utente:");
        JLabel lblPass = new JLabel("Password:");
        lblUser.setFont(new Font("Arial", Font.PLAIN, 15));
        lblPass.setFont(new Font("Arial", Font.PLAIN, 15));

        inputPanel.add(lblUser);
        inputPanel.add(txtNomeUtente);
        inputPanel.add(lblPass);
        inputPanel.add(txtPassword);

        JButton btnAccedi = new JButton("Accedi");
        btnAccedi.setPreferredSize(new Dimension(100, 30));
        btnAccedi.addActionListener(e -> {
            String user = txtNomeUtente.getText();
            String pass = new String(txtPassword.getPassword());
            if (user.equals("admin") && pass.equals("admin")) {
                ruoloUtente = "Amministratore";
                JOptionPane.showMessageDialog(this, "Accesso come Amministratore");
                cardLayout.show(mainPanel, "Main");
            } else if (user.equals("utente") && pass.equals("utente")) {
                ruoloUtente = "Utente";
                JOptionPane.showMessageDialog(this, "Accesso come Utente");
                cardLayout.show(mainPanel, "Main");
            } else {
                JOptionPane.showMessageDialog(this, "Credenziali non valide");
            }
        });

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.add(inputPanel);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(btnAccedi);

        panel.add(formPanel, new GridBagConstraints());
        return panel;
    }
    private JPanel createMainContentPanel() {
        JPanel container = new JPanel(new BorderLayout());
        JPanel navBar = new JPanel();

        JButton btnVolo = new JButton("Voli");
        JButton btnLogout = new JButton("Logout");

        JPanel contentPanel = new JPanel(new CardLayout());
        contentPanel.add(createVoloPanel(), "Volo");

        btnVolo.addActionListener(e -> switchContent(contentPanel, "Volo"));
        btnLogout.addActionListener(e -> cardLayout.show(mainPanel, "Login"));

        navBar.add(btnVolo);
        navBar.add(btnLogout);

        container.add(navBar, BorderLayout.NORTH);
        container.add(contentPanel, BorderLayout.CENTER);

        return container;
    }

    private void switchContent(JPanel panel, String name) {
        CardLayout cl = (CardLayout) panel.getLayout();
        cl.show(panel, name);
    }

    private void aggiungiEsempiVoli() {
        voliInArrivo.add(new Volo("V001", "Alitalia", "Roma", "Napoli", "2025-06-01", "12:30", "Programmato", "Gate 1"));
        voliInArrivo.add(new Volo("V002", "Ryanair", "Milano", "Napoli", "2025-06-01", "14:30", "In Ritardo", "Gate 2"));
        voliInPartenza.add(new Volo("V003", "EasyJet", "Napoli", "Londra", "2025-06-01", "16:00", "Cancellato", "Gate 3"));
        voliInPartenza.add(new Volo("V004", "Vueling", "Napoli", "Barcellona", "2025-06-01", "18:00", "Programmato", "Gate 4"));
    }

    private void prenotaVolo(Volo volo) {
        // Finestra di dialogo per raccogliere i dati della prenotazione
        JTextField txtNome = new JTextField();
        JTextField txtCognome = new JTextField();

        Object[] message = {
                "Nome:", txtNome,
                "Cognome:", txtCognome
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Prenotazione Volo", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String nome = txtNome.getText();
            String cognome = txtCognome.getText();

            if (!nome.isEmpty() && !cognome.isEmpty()) {
                // Crea la prenotazione
                Prenotazione prenotazione = new Prenotazione(nome, cognome, volo);
                prenotazioni.add(prenotazione);

                // Aggiorna lo stato del volo
                volo.stato = "Prenotato";

                // Mostra un messaggio di conferma
                JOptionPane.showMessageDialog(this, "Prenotazione effettuata per " + nome + " " + cognome + " su volo " + volo.codice);
            } else {
                JOptionPane.showMessageDialog(this, "Inserisci tutti i dati.");
            }
        }
    }

    class Volo {
        String codice, compagnia, origine, destinazione, data, orario, stato, gate;
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
    }

    class Prenotazione {
        String nome, cognome;
        Volo volo;
        public Prenotazione(String nome, String cognome, Volo volo) {
            this.nome = nome;
            this.cognome = cognome;
            this.volo = volo;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}
