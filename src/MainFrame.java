import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JTextField txtNomeUtente;
    private JPasswordField txtPassword;
    private String ruoloUtente = "Utente"; // Ruolo di default
    private List<Volo> voliInArrivo = new ArrayList<>();
    private List<Volo> voliInPartenza = new ArrayList<>();
    private List<Prenotazione> prenotazioni = new ArrayList<>(); // Lista di tutte le prenotazioni

    // Campi per la ricerca admin
    private JTextField adminSearchNomeField;
    private JTextField adminSearchCognomeField;
    private JTable adminPrenotazioniTable;
    private DefaultTableModel adminTableModel;


    // Per la mappa dei sedili semplificata
    private static final int NUM_FILE_SEDILI = 5;
    private static final char ULTIMA_LETTERA_SEDİLE = 'D'; // A, B, C, D

    public MainFrame() {
        setTitle("Gestione Aeroporto");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 700); // Leggermente aumentato per la vista admin
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        aggiungiEsempiVoli();
        aggiungiEsempiPrenotazioni(); // Popola con prenotazioni di esempio

        mainPanel.add(createVoloPanel(), "Volo");
        mainPanel.add(createLoginPanel(), "Login");
        mainPanel.add(createMainContentPanel(), "Main");

        add(mainPanel);
        cardLayout.show(mainPanel, "Volo");
    }

    private JPanel createVoloPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setName("VoloPanel_Instance_" + System.identityHashCode(panel));
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));

        JLabel lblArrivi = new JLabel("Voli in Arrivo", SwingConstants.CENTER);
        lblArrivi.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel lblPartenze = new JLabel("Voli in Partenza", SwingConstants.CENTER);
        lblPartenze.setFont(new Font("Arial", Font.BOLD, 16));

        String[] colonne = {"Codice", "Compagnia", "Origine", "Destinazione", "Data", "Orario", "Stato", "Gate"};

        JTable tableArrivo = new JTable(getDatiVoli(voliInArrivo), colonne);
        JTable tablePartenza = new JTable(getDatiVoli(voliInPartenza), colonne);

        if (ruoloUtente.equals("Utente")) {
            tableArrivo.setEnabled(false);
            tablePartenza.setEnabled(false);
        } else if (ruoloUtente.equals("Amministratore")) {
            tableArrivo.setEnabled(true); // L'admin potrebbe voler interagire (non implementato)
            tablePartenza.setEnabled(true);
        }

        MouseAdapter prenotazioneListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                boolean inMainPanel = false;
                Component currentCard = getCurrentVisibleCard(mainPanel);
                if (currentCard != null && "MainContainer".equals(currentCard.getName())) {
                    inMainPanel = true;
                }

                if (ruoloUtente.equals("Utente") && inMainPanel) {
                    JTable sourceTable = (JTable) e.getSource();
                    int row = sourceTable.rowAtPoint(e.getPoint());
                    if (row >= 0) { // Assicura che una riga valida sia stata cliccata
                        Volo voloSelezionato;
                        if (sourceTable == tableArrivo) {
                            if(row < voliInArrivo.size()) voloSelezionato = voliInArrivo.get(row); else return;
                        } else {
                            if(row < voliInPartenza.size()) voloSelezionato = voliInPartenza.get(row); else return;
                        }

                        PrenotazioneDialog pDialog = new PrenotazioneDialog(MainFrame.this, voloSelezionato);
                        pDialog.setVisible(true);
                        if (pDialog.isPrenotazioneConfermata()) {
                            prenotazioni.add(pDialog.getPrenotazione());
                            aggiornaVistaMiePrenotazioni();
                            // Aggiorna anche la vista admin se l'admin è loggato e sta visualizzando le prenotazioni
                            if(ruoloUtente.equals("Amministratore")) aggiornaVistaAdminPrenotazioni(adminSearchNomeField.getText(), adminSearchCognomeField.getText());

                            JOptionPane.showMessageDialog(MainFrame.this,
                                    "Prenotazione effettuata con successo per " + pDialog.getPrenotazione().nome + " " + pDialog.getPrenotazione().cognome +
                                            "\nPosto: " + pDialog.getPrenotazione().postoSelezionato +
                                            (pDialog.getPrenotazione().conBagaglio ? "\nCon Bagaglio" : "") +
                                            (pDialog.getPrenotazione().conAssicurazione ? "\nCon Assicurazione" : ""),
                                    "Prenotazione Confermata", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                } else if (ruoloUtente.equals("Utente") && !inMainPanel) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Devi accedere per prenotare un volo.", "Accesso Richiesto", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        };

        tableArrivo.addMouseListener(prenotazioneListener);
        tablePartenza.addMouseListener(prenotazioneListener);

        JPanel arriviPanel = new JPanel(new BorderLayout());
        arriviPanel.add(lblArrivi, BorderLayout.NORTH);
        arriviPanel.add(new JScrollPane(tableArrivo), BorderLayout.CENTER);

        JPanel partenzePanel = new JPanel(new BorderLayout());
        partenzePanel.add(lblPartenze, BorderLayout.NORTH);
        partenzePanel.add(new JScrollPane(tablePartenza), BorderLayout.CENTER);

        centerPanel.add(arriviPanel);
        centerPanel.add(partenzePanel);

        JButton btnLogin = new JButton("Accedi per Prenotare/Gestire");
        btnLogin.setName("LoginButton_VoloPanel");
        boolean isLoggedIn = false;
        Component currentVisibleCard = getCurrentVisibleCard(mainPanel);
        if (currentVisibleCard != null && "MainContainer".equals(currentVisibleCard.getName())) {
            isLoggedIn = true;
        }
        btnLogin.setVisible(!isLoggedIn);

        btnLogin.addActionListener(e -> cardLayout.show(mainPanel, "Login"));
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        if (btnLogin.isVisible()) {
            btnPanel.add(btnLogin);
        }

        panel.add(centerPanel, BorderLayout.CENTER);
        if (btnLogin.isVisible() && btnPanel.getComponentCount() > 0) {
            panel.add(btnPanel, BorderLayout.SOUTH);
        }
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
        panel.setName("LoginPanel");
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtNomeUtente = new JTextField(15);
        txtPassword = new JPasswordField(15);
        JLabel lblUser = new JLabel("Nome Utente:");
        JLabel lblPass = new JLabel("Password:");
        lblUser.setFont(new Font("Arial", Font.PLAIN, 15));
        lblPass.setFont(new Font("Arial", Font.PLAIN, 15));

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST; inputPanel.add(lblUser, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; inputPanel.add(txtNomeUtente, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST; inputPanel.add(lblPass, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; inputPanel.add(txtPassword, gbc);

        JButton btnAccedi = new JButton("Accedi");
        btnAccedi.setPreferredSize(new Dimension(100, 30));
        btnAccedi.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAccedi.addActionListener(e -> {
            String user = txtNomeUtente.getText();
            String pass = new String(txtPassword.getPassword());
            String targetPanelConstraint = "Main";
            if (user.equals("admin") && pass.equals("admin")) {
                ruoloUtente = "Amministratore";
                JOptionPane.showMessageDialog(this, "Accesso come Amministratore");
            } else if (user.equals("utente") && pass.equals("utente")) {
                ruoloUtente = "Utente";
                JOptionPane.showMessageDialog(this, "Accesso come Utente");
            } else {
                JOptionPane.showMessageDialog(this, "Credenziali non valide");
                return;
            }

            Component oldMainContainer = findComponentByName(mainPanel, "MainContainer");
            if (oldMainContainer != null) {
                mainPanel.remove(oldMainContainer);
            }

            mainPanel.add(createMainContentPanel(), targetPanelConstraint); // Aggiunge il nuovo
            updateVoloPanelForRole();
            cardLayout.show(mainPanel, targetPanelConstraint);
        });

        JButton btnBack = new JButton("Torna ai Voli");
        btnBack.setPreferredSize(new Dimension(120, 30));
        btnBack.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "Volo"));

        formPanel.add(inputPanel);
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(btnAccedi);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(btnBack);
        GridBagConstraints mainGbc = new GridBagConstraints();
        panel.add(formPanel, mainGbc);
        return panel;
    }

    private void updateVoloPanelForRole() {
        Component oldInitialVoloPanel = null;
        if (mainPanel.getComponentCount() > 0) {
            Component firstComp = mainPanel.getComponent(0);
            if (firstComp.getName() != null && firstComp.getName().startsWith("VoloPanel_Instance_")) {
                oldInitialVoloPanel = firstComp;
            }
        }
        if(oldInitialVoloPanel != null) mainPanel.remove(oldInitialVoloPanel);
        mainPanel.add(createVoloPanel(), "Volo", 0);

        Component mainContentWrapper = findComponentByName(mainPanel, "MainContainer");
        if (mainContentWrapper instanceof Container) {
            JPanel contentPanelCards = (JPanel) findComponentByName((Container) mainContentWrapper, "MainContentPanel_Card");
            if (contentPanelCards != null) {
                Component oldVoloPanelInterno = findComponentByName(contentPanelCards, "VoloPanel_Interno");
                if (oldVoloPanelInterno != null) {
                    contentPanelCards.remove(oldVoloPanelInterno);
                }
                JPanel nuovoVoloPanelInterno = createVoloPanel();
                nuovoVoloPanelInterno.setName("VoloPanel_Interno");
                setButtonVisibilityInPanel(nuovoVoloPanelInterno, "LoginButton_VoloPanel", false);
                contentPanelCards.add(nuovoVoloPanelInterno, "Volo");
            }
        }
        mainPanel.revalidate();
        mainPanel.repaint();
    }


    private JPanel createMainContentPanel() {
        JPanel container = new JPanel(new BorderLayout(0, 0));
        container.setName("MainContainer");
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnVolo = new JButton("Visualizza Voli");
        JButton btnMiePrenotazioni = new JButton("Le Mie Prenotazioni");
        JButton btnAdminCercaPrenotazioni = new JButton("Gestisci Prenotazioni"); // Nuovo bottone Admin
        JButton btnLogout = new JButton("Logout");

        JPanel contentPanel = new JPanel(new CardLayout());
        contentPanel.setName("MainContentPanel_Card");

        JPanel voliPanelInterno = createVoloPanel();
        voliPanelInterno.setName("VoloPanel_Interno");
        setButtonVisibilityInPanel(voliPanelInterno, "LoginButton_VoloPanel", false);

        contentPanel.add(voliPanelInterno, "Volo");
        contentPanel.add(createMiePrenotazioniPanel(), "MiePrenotazioni");
        contentPanel.add(createAdminPrenotazioniPanel(), "AdminCercaPrenotazioni"); // Aggiungi pannello admin

        btnVolo.addActionListener(e -> switchContent(contentPanel, "Volo"));

        btnMiePrenotazioni.addActionListener(e -> {
            if (ruoloUtente.equals("Utente")) {
                aggiornaVistaMiePrenotazioni();
            } else {
                JOptionPane.showMessageDialog(this, "Funzione disponibile solo per gli utenti.", "Accesso Negato", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnAdminCercaPrenotazioni.addActionListener(e -> {
            if (ruoloUtente.equals("Amministratore")) {
                aggiornaVistaAdminPrenotazioni("", ""); // Mostra tutte inizialmente o l'ultima ricerca
                switchContent(contentPanel, "AdminCercaPrenotazioni");
            } else {
                JOptionPane.showMessageDialog(this, "Funzione disponibile solo per gli amministratori.", "Accesso Negato", JOptionPane.WARNING_MESSAGE);
            }
        });


        btnLogout.addActionListener(e -> {
            ruoloUtente = "Utente";
            txtNomeUtente.setText("");
            txtPassword.setText("");
            // Non pulire `prenotazioni` globali qui, l'admin potrebbe ancora averne bisogno.
            // Le prenotazioni visualizzate dall'utente saranno filtrate.
            updateVoloPanelForRole();
            cardLayout.show(mainPanel, "Volo");
        });

        navBar.add(btnVolo);
        // Visibilità condizionale dei bottoni
        btnMiePrenotazioni.setVisible(ruoloUtente.equals("Utente"));
        btnAdminCercaPrenotazioni.setVisible(ruoloUtente.equals("Amministratore"));

        navBar.add(btnMiePrenotazioni);
        navBar.add(btnAdminCercaPrenotazioni);
        navBar.add(btnLogout);

        container.add(navBar, BorderLayout.NORTH);
        container.add(contentPanel, BorderLayout.CENTER);
        return container;
    }

    private void aggiornaVistaMiePrenotazioni() {
        Component mainContainerComp = findComponentByName(mainPanel, "MainContainer");
        if (mainContainerComp instanceof Container) {
            Container mainContainer = (Container) mainContainerComp;
            Component contentPanelComp = findComponentByName(mainContainer, "MainContentPanel_Card");

            if (contentPanelComp instanceof JPanel) {
                JPanel contentPanel = (JPanel) contentPanelComp;
                Component currentPrenotazioniPanel = findComponentByName(contentPanel, "MiePrenotazioniPanel_Internal");
                if (currentPrenotazioniPanel != null) {
                    contentPanel.remove(currentPrenotazioniPanel);
                }
                JPanel nuovoPanelPrenotazioni = createMiePrenotazioniPanel();
                contentPanel.add(nuovoPanelPrenotazioni, "MiePrenotazioni");
                switchContent(contentPanel, "MiePrenotazioni");
                contentPanel.revalidate();
                contentPanel.repaint();
            }
        }
    }


    private JPanel createMiePrenotazioniPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setName("MiePrenotazioniPanel_Internal");
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("Le Mie Prenotazioni (" + txtNomeUtente.getText() + ")", SwingConstants.CENTER); // Mostra nome utente loggato
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Filtra le prenotazioni per l'utente attualmente loggato (basato su nome e cognome, per semplicità)
        // In un sistema reale, ci sarebbe un ID utente.
        // Qui assumiamo che il nome utente del login corrisponda al campo "nome" della prenotazione.
        // Questo è una semplificazione e potrebbe non essere robusto.
        String utenteLoggato = txtNomeUtente.getText(); // Usato come pseudo-ID
        List<Prenotazione> prenotazioniUtente = prenotazioni.stream()
                .filter(p -> p.nome.equalsIgnoreCase(utenteLoggato)) // Semplificazione: filtra per nome
                .collect(Collectors.toList());


        if (prenotazioniUtente.isEmpty()) {
            JLabel noPrenotazioniLabel = new JLabel("Nessuna prenotazione effettuata.", SwingConstants.CENTER);
            panel.add(noPrenotazioniLabel, BorderLayout.CENTER);
        } else {
            String[] colonne = {"SSN", "Nome", "Cognome", "Data Nascita", "Email", "Telefono",
                    "Cod. Volo", "Orig-Dest", "Data Volo", "Posto", "Bagaglio", "Assicurazione"};
            Object[][] dati = new Object[prenotazioniUtente.size()][colonne.length];
            for (int i = 0; i < prenotazioniUtente.size(); i++) {
                Prenotazione p = prenotazioniUtente.get(i);
                dati[i] = new Object[]{
                        p.ssn, p.nome, p.cognome, p.dataNascita, p.email, p.telefono,
                        p.volo.codice, p.volo.origine + "-" + p.volo.destinazione, p.volo.data + " " + p.volo.orario,
                        p.postoSelezionato, p.conBagaglio ? "Sì" : "No", p.conAssicurazione ? "Sì" : "No"
                };
            }
            JTable tablePrenotazioni = new JTable(dati, colonne);
            tablePrenotazioni.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            // ... (imposta larghezze colonne come prima) ...
            tablePrenotazioni.getColumnModel().getColumn(0).setPreferredWidth(120); // SSN
            tablePrenotazioni.getColumnModel().getColumn(1).setPreferredWidth(100); // Nome
            // ... altre colonne ...
            tablePrenotazioni.getColumnModel().getColumn(10).setPreferredWidth(80); // Assicurazione


            tablePrenotazioni.setEnabled(false);
            JScrollPane scrollPane = new JScrollPane(tablePrenotazioni);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            panel.add(scrollPane, BorderLayout.CENTER);
        }
        return panel;
    }

    private JPanel createAdminPrenotazioniPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setName("AdminPrenotazioniPanel_Internal");
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Pannello di ricerca
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Nome:"));
        adminSearchNomeField = new JTextField(15);
        searchPanel.add(adminSearchNomeField);
        searchPanel.add(new JLabel("Cognome:"));
        adminSearchCognomeField = new JTextField(15);
        searchPanel.add(adminSearchCognomeField);
        JButton btnCercaAdmin = new JButton("Cerca Prenotazioni");
        btnCercaAdmin.addActionListener(e -> aggiornaVistaAdminPrenotazioni(adminSearchNomeField.getText(), adminSearchCognomeField.getText()));
        searchPanel.add(btnCercaAdmin);
        panel.add(searchPanel, BorderLayout.NORTH);

        // Tabella per visualizzare le prenotazioni
        String[] colonneAdmin = {"SSN", "Nome", "Cognome", "Data Nascita", "Email", "Telefono",
                "Cod. Volo", "Compagnia", "Orig-Dest", "Data Volo", "Posto", "Bagaglio", "Assicurazione"};
        adminTableModel = new DefaultTableModel(colonneAdmin, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Rendi la tabella non editabile
            }
        };
        adminPrenotazioniTable = new JTable(adminTableModel);
        adminPrenotazioniTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // Imposta larghezze colonne per admin table
        adminPrenotazioniTable.getColumnModel().getColumn(0).setPreferredWidth(130); // SSN
        adminPrenotazioniTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Nome
        adminPrenotazioniTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Cognome
        adminPrenotazioniTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Data Nascita
        adminPrenotazioniTable.getColumnModel().getColumn(4).setPreferredWidth(160); // Email
        adminPrenotazioniTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Telefono
        adminPrenotazioniTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Cod. Volo
        adminPrenotazioniTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Compagnia
        adminPrenotazioniTable.getColumnModel().getColumn(8).setPreferredWidth(120); // Orig-Dest
        adminPrenotazioniTable.getColumnModel().getColumn(9).setPreferredWidth(120); // Data Volo
        adminPrenotazioniTable.getColumnModel().getColumn(10).setPreferredWidth(60); // Posto
        adminPrenotazioniTable.getColumnModel().getColumn(11).setPreferredWidth(70); // Bagaglio
        adminPrenotazioniTable.getColumnModel().getColumn(12).setPreferredWidth(90); // Assicurazione


        JScrollPane scrollPane = new JScrollPane(adminPrenotazioniTable);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Popola inizialmente con tutte le prenotazioni o nessuna
        aggiornaVistaAdminPrenotazioni("", ""); // Mostra tutte se i campi sono vuoti

        return panel;
    }

    private void aggiornaVistaAdminPrenotazioni(String nomeFilter, String cognomeFilter) {
        if (adminTableModel == null) return; // Se il pannello non è ancora stato creato pienamente
        adminTableModel.setRowCount(0); // Pulisci la tabella

        List<Prenotazione> filteredPrenotazioni = prenotazioni.stream()
                .filter(p -> (nomeFilter == null || nomeFilter.trim().isEmpty() || p.nome.toLowerCase().contains(nomeFilter.trim().toLowerCase())) &&
                        (cognomeFilter == null || cognomeFilter.trim().isEmpty() || p.cognome.toLowerCase().contains(cognomeFilter.trim().toLowerCase())))
                .collect(Collectors.toList());

        for (Prenotazione p : filteredPrenotazioni) {
            adminTableModel.addRow(new Object[]{
                    p.ssn, p.nome, p.cognome, p.dataNascita, p.email, p.telefono,
                    p.volo.codice, p.volo.compagnia, p.volo.origine + "-" + p.volo.destinazione,
                    p.volo.data + " " + p.volo.orario,
                    p.postoSelezionato, p.conBagaglio ? "Sì" : "No", p.conAssicurazione ? "Sì" : "No"
            });
        }
    }


    private void switchContent(JPanel panel, String name) {
        CardLayout cl = (CardLayout) panel.getLayout();
        cl.show(panel, name);
    }

    private void aggiungiEsempiVoli() {
        // VOLI IN ARRIVO
        voliInArrivo.add(new Volo("AZ204", "Alitalia", "Roma FCO", "Napoli NAP", "2025-05-21", "09:30", "In Orario", "A01"));
        voliInArrivo.add(new Volo("FR1822", "Ryanair", "Milano BGY", "Napoli NAP", "2025-05-21", "10:15", "Previsto", "A02"));
        voliInArrivo.add(new Volo("U22851", "EasyJet", "Venezia VCE", "Napoli NAP", "2025-05-21", "11:00", "Atterrato", "A03"));
        voliInArrivo.add(new Volo("LH1778", "Lufthansa", "Monaco MUC", "Napoli NAP", "2025-05-21", "12:30", "In Ritardo", "A04"));
        voliInArrivo.add(new Volo("AF1578", "Air France", "Parigi CDG", "Napoli NAP", "2025-05-21", "13:45", "In Orario", "A05"));
        voliInArrivo.add(new Volo("BA2608", "British Airways", "Londra LHR", "Napoli NAP", "2025-05-21", "14:20", "Previsto", "B01"));
        voliInArrivo.add(new Volo("VY6720", "Vueling", "Barcellona BCN", "Napoli NAP", "2025-05-21", "15:00", "In Orario", "B02"));
        voliInArrivo.add(new Volo("EK097", "Emirates", "Dubai DXB", "Napoli NAP", "2025-05-21", "16:30", "Cancellato", "B03"));
        voliInArrivo.add(new Volo("TK1879", "Turkish Airlines", "Istanbul IST", "Napoli NAP", "2025-05-21", "17:55", "In Orario", "B04"));
        voliInArrivo.add(new Volo("IB3270", "Iberia", "Madrid MAD", "Napoli NAP", "2025-05-21", "19:10", "Previsto", "B05"));

        // VOLI IN PARTENZA
        voliInPartenza.add(new Volo("AZ205", "Alitalia", "Napoli NAP", "Roma FCO", "2025-05-21", "10:00", "Imbarco", "C01"));
        voliInPartenza.add(new Volo("FR1823", "Ryanair", "Napoli NAP", "Milano BGY", "2025-05-21", "11:05", "Ultima Chiamata", "C02"));
        voliInPartenza.add(new Volo("U22852", "EasyJet", "Napoli NAP", "Venezia VCE", "2025-05-21", "11:50", "Partito", "C03"));
        voliInPartenza.add(new Volo("LH1779", "Lufthansa", "Napoli NAP", "Monaco MUC", "2025-05-21", "13:15", "In Ritardo", "C04"));
        voliInPartenza.add(new Volo("AF1579", "Air France", "Napoli NAP", "Parigi CDG", "2025-05-21", "14:30", "Previsto", "D01"));
        voliInPartenza.add(new Volo("BA2609", "British Airways", "Napoli NAP", "Londra LHR", "2025-05-21", "15:00", "Imbarco", "D02"));
        voliInPartenza.add(new Volo("VY6721", "Vueling", "Napoli NAP", "Barcellona BCN", "2025-05-21", "15:45", "Previsto", "D03"));
        voliInPartenza.add(new Volo("EK098", "Emirates", "Napoli NAP", "Dubai DXB", "2025-05-21", "17:10", "Cancellato", "D04"));
        voliInPartenza.add(new Volo("TK1880", "Turkish Airlines", "Napoli NAP", "Istanbul IST", "2025-05-21", "18:30", "Previsto", "E01"));
        voliInPartenza.add(new Volo("IB3271", "Iberia", "Napoli NAP", "Madrid MAD", "2025-05-21", "19:50", "Ultima Chiamata", "E02"));
        voliInPartenza.add(new Volo("W65728", "Wizz Air", "Napoli NAP", "Bucarest OTP", "2025-05-21", "20:30", "Previsto", "E03"));
        voliInPartenza.add(new Volo("KL1682", "KLM", "Napoli NAP", "Amsterdam AMS", "2025-05-21", "21:15", "Imbarco", "E04"));
    }

    private void aggiungiEsempiPrenotazioni() {
        if (voliInPartenza.isEmpty() || voliInArrivo.isEmpty()) {
            // Assicurati che i voli siano stati caricati
            System.err.println("Voli di esempio non caricati, impossibile creare prenotazioni di esempio.");
            return;
        }
        // Esempio 1
        prenotazioni.add(new Prenotazione("Mario", "Rossi", "RSSMRA80A01H501A", "01/01/1980", "mario.rossi@example.com", "3331234567",
                voliInPartenza.get(0), "1A", true, true)); // Volo AZ205
        // Esempio 2
        prenotazioni.add(new Prenotazione("Laura", "Bianchi", "BNCLRA85M41H501B", "21/08/1985", "laura.bianchi@example.com", "3387654321",
                voliInPartenza.get(1), "2B", false, true)); // Volo FR1823
        // Esempio 3 (stesso nome di login utente per test vista "Le mie prenotazioni")
        prenotazioni.add(new Prenotazione("utente", "Test", "TSTUSR90C02H501C", "02/03/1990", "utente.test@example.com", "3471122334",
                voliInArrivo.get(0), "3C", true, false)); // Volo AZ204
        // Esempio 4
        prenotazioni.add(new Prenotazione("Giovanni", "Verdi", "VRDGNN75P03H501D", "03/09/1975", "giovanni.verdi@example.com", "3294455667",
                voliInPartenza.get(4), "4D", false, false)); // Volo AF1579
        // Esempio 5 (altro utente per test ricerca admin)
        prenotazioni.add(new Prenotazione("Mario", "Gialli", "GLLMRA82A01H501E", "10/01/1982", "mario.gialli@example.com", "3359876543",
                voliInArrivo.get(1), "1B", true, true)); // Volo FR1822
    }


    private Component getCurrentVisibleCard(JPanel cardPanel) {
        for (Component comp : cardPanel.getComponents()) {
            if (comp.isVisible()) {
                return comp;
            }
        }
        return null;
    }

    private Component findComponentByName(Container container, String name) {
        if (name == null || container == null) return null;
        for (Component comp : container.getComponents()) {
            if (name.equals(comp.getName())) {
                return comp;
            }
            if (comp instanceof Container) {
                Component found = findComponentByName((Container) comp, name);
                if (found != null) return found;
            }
        }
        return null;
    }

    private void setButtonVisibilityInPanel(JPanel panel, String buttonName, boolean visible) {
        Component button = findComponentByName(panel, buttonName);
        if (button instanceof JButton) {
            button.setVisible(visible);
        }
    }

    class Volo {
        String codice, compagnia, origine, destinazione, data, orario, stato, gate;
        public Volo(String codice, String compagnia, String origine, String destinazione, String data,
                    String orario, String stato, String gate) {
            this.codice = codice; this.compagnia = compagnia; this.origine = origine;
            this.destinazione = destinazione; this.data = data; this.orario = orario;
            this.stato = stato; this.gate = gate;
        }
    }

    // Classe Prenotazione AGGIORNATA con SSN
    class Prenotazione {
        String nome, cognome, ssn, dataNascita, email, telefono; // Aggiunto ssn
        Volo volo;
        String postoSelezionato;
        boolean conBagaglio;
        boolean conAssicurazione;

        public Prenotazione(String nome, String cognome, String ssn, String dataNascita, String email, String telefono, Volo volo,
                            String postoSelezionato, boolean conBagaglio, boolean conAssicurazione) {
            this.nome = nome; this.cognome = cognome; this.ssn = ssn;
            this.dataNascita = dataNascita; this.email = email; this.telefono = telefono;
            this.volo = volo; this.postoSelezionato = postoSelezionato;
            this.conBagaglio = conBagaglio; this.conAssicurazione = conAssicurazione;
        }
    }

    // Classe PrenotazioneDialog AGGIORNATA con SSN
    class PrenotazioneDialog extends JDialog {
        private Volo volo;
        private Prenotazione prenotazioneEffettuata;
        private boolean confermata = false;

        private JTextField txtNome, txtCognome, txtSSN, txtDataNascita, txtEmail, txtTelefono; // Aggiunto txtSSN
        private JCheckBox chkBagaglio, chkAssicurazione;
        private JLabel lblPostoSelezionatoDisplay;
        private String postoAttualmenteSelezionato = null;
        private Map<String, JButton> bottoniSedili = new HashMap<>();


        public PrenotazioneDialog(Frame owner, Volo volo) {
            super(owner, "Dettagli Prenotazione Volo: " + volo.codice, true);
            this.volo = volo;
            setSize(Math.min(owner.getWidth() - 50, 600), Math.min(owner.getHeight() - 50, 580)); // Aumentato per SSN
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout(10, 10));
            ((JPanel)getContentPane()).setBorder(new EmptyBorder(10,10,10,10));

            JTabbedPane tabbedPane = new JTabbedPane();

            tabbedPane.addTab("Dati Passeggero", createPanelDatiPersonali());
            tabbedPane.addTab("Scelta Posto", createPanelSceltaPosto());
            tabbedPane.addTab("Opzioni Extra", createPanelOpzioniExtra());
            add(tabbedPane, BorderLayout.CENTER);

            JPanel panelBottoni = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnConferma = new JButton("Conferma Prenotazione");
            btnConferma.addActionListener(e -> confermaPrenotazione());
            JButton btnAnnulla = new JButton("Annulla");
            btnAnnulla.addActionListener(e -> dispose());
            panelBottoni.add(btnAnnulla);
            panelBottoni.add(btnConferma);
            add(panelBottoni, BorderLayout.SOUTH);
        }

        private JPanel createPanelDatiPersonali() {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            txtNome = new JTextField(20);
            txtCognome = new JTextField(20);
            txtSSN = new JTextField(16); // Campo per SSN/Codice Fiscale
            txtDataNascita = new JTextField(10);
            txtEmail = new JTextField(25);
            txtTelefono = new JTextField(15);

            int y = 0;
            gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Nome:"), gbc);
            gbc.gridx = 1; panel.add(txtNome, gbc);
            y++;
            gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Cognome:"), gbc);
            gbc.gridx = 1; panel.add(txtCognome, gbc);
            y++;
            gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("SSN/Codice Fiscale:"), gbc); // Etichetta per SSN
            gbc.gridx = 1; panel.add(txtSSN, gbc); // Campo SSN
            y++;
            gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Data Nascita (GG/MM/AAAA):"), gbc);
            gbc.gridx = 1; panel.add(txtDataNascita, gbc);
            y++;
            gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Email:"), gbc);
            gbc.gridx = 1; panel.add(txtEmail, gbc);
            y++;
            gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Telefono:"), gbc);
            gbc.gridx = 1; panel.add(txtTelefono, gbc);
            return panel;
        }

        private JPanel createPanelSceltaPosto() {
            // ... (invariato rispetto a prima) ...
            JPanel panel = new JPanel(new BorderLayout(5,5));
            JPanel mappaPostiPanel = new JPanel(new GridLayout(NUM_FILE_SEDILI, (ULTIMA_LETTERA_SEDİLE - 'A') + 1, 3, 3));
            mappaPostiPanel.setBorder(BorderFactory.createTitledBorder("Mappa Sedili Aereo (Semplificata)"));

            for (int i = 1; i <= NUM_FILE_SEDILI; i++) {
                for (char c = 'A'; c <= ULTIMA_LETTERA_SEDİLE; c++) {
                    String nomePosto = i + "" + c;
                    JButton btnPosto = new JButton(nomePosto);
                    btnPosto.setMargin(new Insets(2,2,2,2));
                    btnPosto.setFont(new Font("Arial", Font.PLAIN, 10));
                    btnPosto.setBackground(Color.GREEN.brighter());
                    btnPosto.setOpaque(true);
                    btnPosto.setBorderPainted(false);
                    btnPosto.addActionListener(e -> selezionaPosto(nomePosto));
                    mappaPostiPanel.add(btnPosto);
                    bottoniSedili.put(nomePosto, btnPosto);
                }
            }
            panel.add(mappaPostiPanel, BorderLayout.CENTER);
            lblPostoSelezionatoDisplay = new JLabel("Nessun posto selezionato");
            lblPostoSelezionatoDisplay.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(lblPostoSelezionatoDisplay, BorderLayout.SOUTH);
            return panel;
        }

        private void selezionaPosto(String nomePosto) {
            // ... (invariato rispetto a prima) ...
            if (postoAttualmenteSelezionato != null && bottoniSedili.containsKey(postoAttualmenteSelezionato)) {
                bottoniSedili.get(postoAttualmenteSelezionato).setBackground(Color.GREEN.brighter());
            }
            postoAttualmenteSelezionato = nomePosto;
            if (bottoniSedili.containsKey(nomePosto)) {
                bottoniSedili.get(nomePosto).setBackground(Color.ORANGE);
            }
            lblPostoSelezionatoDisplay.setText("Posto selezionato: " + nomePosto);
        }


        private JPanel createPanelOpzioniExtra() {
            // ... (invariato rispetto a prima) ...
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(new EmptyBorder(15,15,15,15));
            chkBagaglio = new JCheckBox("Includi bagaglio da stiva (costo aggiuntivo)");
            chkAssicurazione = new JCheckBox("Aggiungi assicurazione viaggio (costo aggiuntivo)");
            panel.add(chkBagaglio);
            panel.add(Box.createVerticalStrut(10));
            panel.add(chkAssicurazione);
            return panel;
        }

        private void confermaPrenotazione() {
            String nome = txtNome.getText().trim();
            String cognome = txtCognome.getText().trim();
            String ssn = txtSSN.getText().trim(); // Recupera SSN
            String dataNascita = txtDataNascita.getText().trim();
            String email = txtEmail.getText().trim();
            String telefono = txtTelefono.getText().trim();

            // Validazione campi, incluso SSN
            if (nome.isEmpty() || cognome.isEmpty() || ssn.isEmpty() || dataNascita.isEmpty() || email.isEmpty() || telefono.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tutti i campi dei dati personali (incluso SSN) sono obbligatori.", "Dati Mancanti", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (ssn.length() != 16) { // Esempio validazione lunghezza SSN (Codice Fiscale Italiano)
                JOptionPane.showMessageDialog(this, "Il formato SSN/Codice Fiscale non è valido (richiesti 16 caratteri).", "Errore Dati", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!email.contains("@") || !email.contains(".")) {
                JOptionPane.showMessageDialog(this, "Formato email non valido.", "Errore Dati", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (postoAttualmenteSelezionato == null) {
                JOptionPane.showMessageDialog(this, "Devi selezionare un posto.", "Posto Mancante", JOptionPane.ERROR_MESSAGE);
                if (getContentPane().getComponent(0) instanceof JTabbedPane) {
                    ((JTabbedPane) getContentPane().getComponent(0)).setSelectedIndex(1);
                }
                return;
            }

            boolean bagaglio = chkBagaglio.isSelected();
            boolean assicurazione = chkAssicurazione.isSelected();

            // Crea prenotazione con SSN
            this.prenotazioneEffettuata = new Prenotazione(nome, cognome, ssn, dataNascita, email, telefono, volo,
                    postoAttualmenteSelezionato, bagaglio, assicurazione);
            this.confermata = true;
            dispose();
        }

        public boolean isPrenotazioneConfermata() {
            return confermata;
        }

        public Prenotazione getPrenotazione() {
            return prenotazioneEffettuata;
        }
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Usa il default
        }
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}