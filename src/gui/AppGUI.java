package gui; // Or your preferred package for GUI classes

import controller.AppController; // Assuming AppController is in a 'controller' package
import model.*; // Keep model imports

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// No need to import Collectors or Stream here if data processing is in Controller

public class AppGUI extends JFrame {

    private AppController controller; // Reference to the controller

    private CardLayout cardLayout;
    private JPanel mainPanel; // The top-level panel with CardLayout

    // Login Panel Components
    private JTextField txtNomeUtenteLogin;
    private JPasswordField txtPasswordLogin;

    // Admin - Prenotazioni Panel Components
    private JTextField adminSearchNomeField;
    private JTextField adminSearchCognomeField;
    private JTable adminPrenotazioniTable;
    private DefaultTableModel adminTableModel;

    // Admin - Crea Volo Panel Components
    private JTextField adminCreaVoloCodice, adminCreaVoloCompagnia, adminCreaVoloOrigine, adminCreaVoloDestinazione,
            adminCreaVoloData, adminCreaVoloOrario, adminCreaVoloGate;
    private JComboBox<String> adminCreaVoloTipo;
    private JComboBox<String> adminCreaVoloStatoComboBox;

    // Constants for UI
    private final String[] STATI_VOLO_DISPLAY = {"In Orario", "In Ritardo", "Cancellato", "Atterrato", "Rinviato"};
    private static final int NUM_FILE_SEDILI = 5;
    private static final char ULTIMA_LETTERA_SEDILE = 'D';
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;


    public AppGUI(AppController controller) {
        this.controller = controller;
        this.controller.setGui(this); // Provide GUI reference to controller

        setTitle("Gestione Aeroporto");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 700);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setName("MainCardPanel");


        // Initial panels (before login)
        mainPanel.add(createVoloPanel(false), "Volo_Initial"); // Panel for non-logged-in users
        mainPanel.add(createLoginPanel(), "Login");
        // Placeholder for the main content area after login, to be replaced
        JPanel placeholderMain = new JPanel();
        placeholderMain.setName("MainContentArea_Placeholder");
        mainPanel.add(placeholderMain, "MainContentArea");


        add(mainPanel);
        cardLayout.show(mainPanel, "Volo_Initial");
    }


    // --- Panel Creation Methods ---

    private JPanel createVoloPanel(boolean loggedIn) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setName(loggedIn ? "VoloPanel_LoggedIn" : "VoloPanel_Initial_Instance");
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));

        JLabel lblArrivi = new JLabel("Voli in Arrivo", SwingConstants.CENTER);
        lblArrivi.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel lblPartenze = new JLabel("Voli in Partenza", SwingConstants.CENTER);
        lblPartenze.setFont(new Font("Arial", Font.BOLD, 16));

        String[] colonne = {"Codice", "Compagnia", "Origine", "Destinazione", "Data", "Orario", "Stato", "Gate"};

        DefaultTableModel modelArrivo = new DefaultTableModel(getDatiVoliTable(controller.getVoliInArrivo()), colonne) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tableArrivo = new JTable(modelArrivo);

        DefaultTableModel modelPartenza = new DefaultTableModel(getDatiVoliTable(controller.getVoliInPartenza()), colonne) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tablePartenza = new JTable(modelPartenza);

        MouseAdapter prenotazioneListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!"Utente".equals(controller.getRuoloUtente())) {
                    if (!loggedIn) { // Only show login prompt if on initial view
                        mostraMessaggioInformativo("Devi accedere per prenotare un volo.", "Accesso Richiesto");
                    }
                    // If logged in but not as "Utente", do nothing or show specific admin message
                    return;
                }

                JTable sourceTable = (JTable) e.getSource();
                int selectedRowInView = sourceTable.getSelectedRow();
                if (selectedRowInView >= 0) {
                    int modelRow = sourceTable.convertRowIndexToModel(selectedRowInView);
                    Volo voloSelezionato;
                    List<Volo> sourceList;

                    if (sourceTable.getModel() == modelArrivo) {
                        sourceList = controller.getVoliInArrivo();
                    } else if (sourceTable.getModel() == modelPartenza) {
                        sourceList = controller.getVoliInPartenza();
                    } else {
                        return;
                    }
                    if (modelRow < sourceList.size()) voloSelezionato = sourceList.get(modelRow); else return;


                    Stato_del_volo statoVolo = voloSelezionato.getStato();
                    boolean canBook = true;
                    String reason = "";

                    if (statoVolo == Stato_del_volo.cancellato) {
                        canBook = false; reason = "è cancellato";
                    } else if (sourceTable.getModel() == modelArrivo && statoVolo == Stato_del_volo.atterrato) {
                        canBook = false; reason = "è già atterrato";
                    } else if (sourceTable.getModel() == modelPartenza) {
                        if (!(statoVolo == Stato_del_volo.in_orario ||
                                statoVolo == Stato_del_volo.in_ritardo ||
                                statoVolo == Stato_del_volo.rinviato)) {
                            canBook = false; reason = "il suo stato (" + controller.mapStatoDelVoloToString(statoVolo) + ") non permette la prenotazione";
                        }
                    }
                    if (!canBook) {
                        mostraMessaggioWarn("Impossibile prenotare il volo " + voloSelezionato.getCodice() + " perché " + reason + ".", "Prenotazione Non Disponibile");
                        return;
                    }

                    PrenotazioneDialog pDialog = new PrenotazioneDialog(AppGUI.this, voloSelezionato, controller);
                    pDialog.setVisible(true);
                    if (pDialog.isPrenotazioneConfermata()) {
                        // Controller already added the booking if successful.
                        // Now, update relevant views.
                        aggiornaTabelleVoli(); // In case seat availability changed, though not directly shown
                        if ("Utente".equals(controller.getRuoloUtente())) {
                            aggiornaVistaMiePrenotazioni(); // Update the "Mie Prenotazioni" tab
                        }
                        if("Amministratore".equals(controller.getRuoloUtente()) && adminSearchNomeField != null) {
                            aggiornaVistaAdminPrenotazioni(adminSearchNomeField.getText(), adminSearchCognomeField.getText());
                        }

                        Prenotazione prenEffettuata = pDialog.getPrenotazione(); // This comes from dialog's internal state after controller confirmed
                        mostraMessaggioInformativo(
                                "Prenotazione effettuata con successo per " + prenEffettuata.getPasseggero().getNome() + " " + prenEffettuata.getPasseggero().getCognome() +
                                        "\nPosto: " + pDialog.getPostoAttualmenteSelezionato() +
                                        (prenEffettuata.isBagaglio() ? "\nCon Bagaglio" : "") +
                                        (prenEffettuata.isAssicurazione() ? "\nCon Assicurazione" : ""),
                                "Prenotazione Confermata");
                    }
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
        panel.add(centerPanel, BorderLayout.CENTER);

        if (!loggedIn) { // Only show login button on the initial, non-logged-in Volo panel
            JButton btnLogin = new JButton("Accedi per Prenotare/Gestire");
            btnLogin.setName("LoginButton_VoloPanel_Initial");
            btnLogin.addActionListener(e -> cardLayout.show(mainPanel, "Login"));
            JPanel btnPanelSouth = new JPanel(new FlowLayout(FlowLayout.CENTER));
            btnPanelSouth.add(btnLogin);
            panel.add(btnPanelSouth, BorderLayout.SOUTH);
        }
        return panel;
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

        txtNomeUtenteLogin = new JTextField(15);
        txtPasswordLogin = new JPasswordField(15);
        JLabel lblUser = new JLabel("Nome Utente:");
        JLabel lblPass = new JLabel("Password:");
        lblUser.setFont(new Font("Arial", Font.PLAIN, 15));
        lblPass.setFont(new Font("Arial", Font.PLAIN, 15));

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST; inputPanel.add(lblUser, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; inputPanel.add(txtNomeUtenteLogin, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST; inputPanel.add(lblPass, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; inputPanel.add(txtPasswordLogin, gbc);

        JButton btnAccedi = new JButton("Accedi");
        btnAccedi.setPreferredSize(new Dimension(100, 30));
        btnAccedi.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAccedi.addActionListener(e -> {
            String user = txtNomeUtenteLogin.getText();
            String pass = new String(txtPasswordLogin.getPassword());
            boolean loginSuccess = controller.login(user, pass);

            if (loginSuccess) {
                mostraMessaggioInformativo("Accesso come " + controller.getRuoloUtente(), "Accesso Effettuato");

                // Remove old placeholder or previous main content
                Component oldMainContentArea = findComponentByName(mainPanel, "MainContentArea_Placeholder");
                if (oldMainContentArea == null) oldMainContentArea = findComponentByName(mainPanel, "MainContentArea_Actual");
                if (oldMainContentArea != null) mainPanel.remove(oldMainContentArea);

                // Add new main content panel for logged-in user
                JPanel newMainContentArea = createMainContentPanelForLoggedInUser();
                newMainContentArea.setName("MainContentArea_Actual");
                mainPanel.add(newMainContentArea, "MainContentArea");

                cardLayout.show(mainPanel, "MainContentArea");
            } else {
                mostraMessaggioErrore("Credenziali non valide", "Login Fallito");
            }
        });

        JButton btnBack = new JButton("Torna ai Voli");
        btnBack.setPreferredSize(new Dimension(120, 30));
        btnBack.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "Volo_Initial"));

        formPanel.add(inputPanel);
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(btnAccedi);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(btnBack);
        GridBagConstraints mainGbc = new GridBagConstraints();
        panel.add(formPanel, mainGbc);
        return panel;
    }


    private JPanel createMainContentPanelForLoggedInUser() {
        JPanel container = new JPanel(new BorderLayout(0, 0));
        container.setName("MainContentPanel_Container_LoggedIn"); // Name for the whole logged-in container

        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnVolo = new JButton("Visualizza Voli");
        JButton btnMiePrenotazioni = new JButton("Le Mie Prenotazioni");
        JButton btnAdminCercaPrenotazioni = new JButton("Gestisci Prenotazioni");
        JButton btnAdminCreaVolo = new JButton("Crea Nuovo Volo");
        JButton btnLogout = new JButton("Logout");

        JPanel contentPanelCards = new JPanel(new CardLayout());
        contentPanelCards.setName("InnerContent_CardPanel"); // Cards for different views post-login

        // Create the Volo panel that will be shown when logged in
        JPanel voliPanelInterno = createVoloPanel(true); // Pass true for logged-in version
        // No need to set name again here if createVoloPanel does it

        contentPanelCards.add(voliPanelInterno, "Volo_LoggedIn_View");
        contentPanelCards.add(createMiePrenotazioniPanel(), "MiePrenotazioni_View");
        contentPanelCards.add(createAdminPrenotazioniPanel(), "AdminCercaPrenotazioni_View");
        contentPanelCards.add(createAdminCreaVoloPanel(), "AdminCreaVolo_View");

        CardLayout innerCardLayout = (CardLayout) contentPanelCards.getLayout();

        btnVolo.addActionListener(e -> {
            aggiornaTabelleVoli(); // Ensure tables are up-to-date in the logged-in view
            innerCardLayout.show(contentPanelCards, "Volo_LoggedIn_View");
        });

        btnMiePrenotazioni.addActionListener(e -> {
            if ("Utente".equals(controller.getRuoloUtente())) {
                aggiornaVistaMiePrenotazioni();
                innerCardLayout.show(contentPanelCards, "MiePrenotazioni_View");
            } else {
                mostraMessaggioWarn("Funzione disponibile solo per gli utenti.", "Accesso Negato");
            }
        });

        btnAdminCercaPrenotazioni.addActionListener(e -> {
            if ("Amministratore".equals(controller.getRuoloUtente())) {
                if (adminSearchNomeField != null) aggiornaVistaAdminPrenotazioni(adminSearchNomeField.getText(), adminSearchCognomeField.getText());
                else aggiornaVistaAdminPrenotazioni("","");
                innerCardLayout.show(contentPanelCards, "AdminCercaPrenotazioni_View");
            } else {
                mostraMessaggioWarn("Funzione disponibile solo per gli amministratori.", "Accesso Negato");
            }
        });

        btnAdminCreaVolo.addActionListener(e -> {
            if ("Amministratore".equals(controller.getRuoloUtente())) {
                resetAdminCreaVoloForm(); // Clear and reset the form
                innerCardLayout.show(contentPanelCards, "AdminCreaVolo_View");
            } else {
                mostraMessaggioWarn("Funzione disponibile solo per gli amministratori.", "Accesso Negato");
            }
        });

        btnLogout.addActionListener(e -> {
            controller.logout();
            txtNomeUtenteLogin.setText("");
            txtPasswordLogin.setText("");

            // Remove the current logged-in main content area
            Component currentMainContent = findComponentByName(mainPanel, "MainContentArea_Actual");
            if (currentMainContent != null) {
                mainPanel.remove(currentMainContent);
            }
            // Add back a placeholder (or directly switch to Volo_Initial)
            JPanel placeholderMain = new JPanel();
            placeholderMain.setName("MainContentArea_Placeholder"); // Reuse name for consistency if needed
            mainPanel.add(placeholderMain, "MainContentArea");


            // Refresh the initial Volo panel (it might have been removed or become stale)
            // To be safe, remove and re-add the initial Volo panel
            Component oldInitialVolo = findComponentByName(mainPanel, "VoloPanel_Initial_Instance");
            if(oldInitialVolo != null) mainPanel.remove(oldInitialVolo);
            mainPanel.add(createVoloPanel(false), "Volo_Initial", 0); // Add at specific index if needed

            cardLayout.show(mainPanel, "Volo_Initial");
            mainPanel.revalidate();
            mainPanel.repaint();
        });

        navBar.add(btnVolo);
        // Visibility based on role
        btnMiePrenotazioni.setVisible("Utente".equals(controller.getRuoloUtente()));
        btnAdminCercaPrenotazioni.setVisible("Amministratore".equals(controller.getRuoloUtente()));
        btnAdminCreaVolo.setVisible("Amministratore".equals(controller.getRuoloUtente()));

        navBar.add(btnMiePrenotazioni);
        navBar.add(btnAdminCercaPrenotazioni);
        navBar.add(btnAdminCreaVolo);
        navBar.add(btnLogout);

        container.add(navBar, BorderLayout.NORTH);
        container.add(contentPanelCards, BorderLayout.CENTER);
        innerCardLayout.show(contentPanelCards, "Volo_LoggedIn_View"); // Default to Volo view after login
        return container;
    }


    private JPanel createMiePrenotazioniPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setName("MiePrenotazioniPanel_Internal");
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String nomePasseggeroLoggato = controller.getNomeUtenteLoggato(); // Get from controller

        JLabel titleLabel = new JLabel("Le Mie Prenotazioni" + (!nomePasseggeroLoggato.isEmpty() ? " (Passeggero: " + nomePasseggeroLoggato + ")" : ""), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        List<Prenotazione> prenotazioniUtente = controller.getPrenotazioniPerUtenteLoggato();

        if (prenotazioniUtente.isEmpty()) {
            JLabel noPrenotazioniLabel = new JLabel(
                    ("Utente".equals(controller.getRuoloUtente()) && !nomePasseggeroLoggato.isEmpty()) ?
                            "Nessuna prenotazione trovata per " + nomePasseggeroLoggato + "." :
                            "Nessuna prenotazione da visualizzare.", SwingConstants.CENTER);
            panel.add(noPrenotazioniLabel, BorderLayout.CENTER);
        } else {
            String[] colonne = {"SSN", "Nome", "Cognome", "Email", "Telefono",
                    "Cod. Volo", "Orig-Dest", "Data Volo", "Posto", "Bagaglio", "Assicurazione"};
            Object[][] dati = new Object[prenotazioniUtente.size()][colonne.length];

            for (int i = 0; i < prenotazioniUtente.size(); i++) {
                Prenotazione p = prenotazioniUtente.get(i);
                Passeggero pass = p.getPasseggero();
                Volo voloAssociato = controller.findVoloByCodice(p.getCodiceVolo());

                String origineDest = "N/D";
                String dataVolo = "N/D";
                if (voloAssociato != null) {
                    origineDest = voloAssociato.getOrigine() + "-" + voloAssociato.getDestinazione();
                    dataVolo = (voloAssociato.getData() != null ? voloAssociato.getData().format(DATE_FORMATTER) : "N/D") + " " +
                            (voloAssociato.getOrarioPrevisto() != null ? voloAssociato.getOrarioPrevisto().format(TIME_FORMATTER) : "");
                }

                dati[i] = new Object[]{
                        pass.getSsn(), pass.getNome(), pass.getCognome(), pass.getEmail(), pass.getTelefono(),
                        p.getCodiceVolo(),
                        origineDest,
                        dataVolo,
                        String.valueOf(pass.getPosto()),
                        p.isBagaglio() ? "Sì" : "No",
                        p.isAssicurazione() ? "Sì" : "No"
                };
            }
            JTable tablePrenotazioni = new JTable(new DefaultTableModel(dati, colonne) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            });
            tablePrenotazioni.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            JScrollPane scrollPane = new JScrollPane(tablePrenotazioni);
            panel.add(scrollPane, BorderLayout.CENTER);
        }
        return panel;
    }

    private JPanel createAdminPrenotazioniPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setName("AdminPrenotazioniPanel_Internal");
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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

        String[] colonneAdmin = {"SSN", "Nome", "Cognome", "Email", "Telefono",
                "Cod. Volo", "Compagnia", "Orig-Dest", "Data Volo", "Posto", "Bagaglio", "Assicurazione"};
        adminTableModel = new DefaultTableModel(colonneAdmin, 0){
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        adminPrenotazioniTable = new JTable(adminTableModel);
        adminPrenotazioniTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane scrollPane = new JScrollPane(adminPrenotazioniTable);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);
        aggiornaVistaAdminPrenotazioni("", ""); // Initial load
        return panel;
    }

    private JPanel createAdminCreaVoloPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setName("AdminCreaVoloPanel_Internal");
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; gbc.fill = GridBagConstraints.NONE; panel.add(new JLabel("Codice Volo:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; adminCreaVoloCodice = new JTextField(15); panel.add(adminCreaVoloCodice, gbc);
        gbc.weightx = 0; y++;

        gbc.gridx = 0; gbc.gridy = y; gbc.fill = GridBagConstraints.NONE; panel.add(new JLabel("Compagnia Aerea:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; adminCreaVoloCompagnia = new JTextField(15); panel.add(adminCreaVoloCompagnia, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; gbc.fill = GridBagConstraints.NONE; panel.add(new JLabel("Tipo Volo:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; adminCreaVoloTipo = new JComboBox<>(new String[]{"In Partenza", "In Arrivo"});
        adminCreaVoloTipo.addItemListener(e -> handleAdminCreaVoloTipoChange());
        panel.add(adminCreaVoloTipo, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; gbc.fill = GridBagConstraints.NONE; panel.add(new JLabel("Origine:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; adminCreaVoloOrigine = new JTextField(15); panel.add(adminCreaVoloOrigine, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; gbc.fill = GridBagConstraints.NONE; panel.add(new JLabel("Destinazione:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; adminCreaVoloDestinazione = new JTextField(15); panel.add(adminCreaVoloDestinazione, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; gbc.fill = GridBagConstraints.NONE; panel.add(new JLabel("Data (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; adminCreaVoloData = new JTextField(10); panel.add(adminCreaVoloData, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; gbc.fill = GridBagConstraints.NONE; panel.add(new JLabel("Orario (HH:MM):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; adminCreaVoloOrario = new JTextField(5); panel.add(adminCreaVoloOrario, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; gbc.fill = GridBagConstraints.NONE; panel.add(new JLabel("Stato Iniziale:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; adminCreaVoloStatoComboBox = new JComboBox<>(STATI_VOLO_DISPLAY);
        adminCreaVoloStatoComboBox.setSelectedItem("In Orario");
        panel.add(adminCreaVoloStatoComboBox, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; gbc.fill = GridBagConstraints.NONE; panel.add(new JLabel("Gate:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; adminCreaVoloGate = new JTextField(5); panel.add(adminCreaVoloGate, gbc);
        y++;

        handleAdminCreaVoloTipoChange(); // Initial setup

        gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;
        JButton btnCrea = new JButton("Crea Volo");
        btnCrea.addActionListener(e -> {
            boolean success = controller.creaNuovoVolo(
                    adminCreaVoloCodice.getText(), adminCreaVoloCompagnia.getText(),
                    (String) adminCreaVoloTipo.getSelectedItem(),
                    adminCreaVoloOrigine.getText(), adminCreaVoloDestinazione.getText(),
                    adminCreaVoloData.getText(), adminCreaVoloOrario.getText(),
                    (String) adminCreaVoloStatoComboBox.getSelectedItem(),
                    adminCreaVoloGate.getText()
            );
            if (success) {
                resetAdminCreaVoloForm();
                aggiornaTabelleVoli(); // Update flight tables in all relevant views
            }
            // Error messages handled by controller via mostraMessaggioErrore
        });
        panel.add(btnCrea, gbc);
        return panel;
    }

    // --- UI Update and Helper Methods ---

    public void aggiornaTabelleVoli() {
        // This method needs to find the JTables in *both* Volo_Initial and Volo_LoggedIn_View
        // and update their models. This can be complex due to panel recreation.
        // A more robust way is to ensure that whenever a Volo panel is shown, it's recreated or its table models are explicitly updated.

        // For simplicity, let's assume we rebuild the relevant visible Volo panel
        // or at least its tables.
        Component visibleCardInMain = getCurrentVisibleCard(mainPanel);
        if (visibleCardInMain != null) {
            if ("Volo_Initial".equals(visibleCardInMain.getName())) {
                // Rebuild or update Volo_Initial's tables
                // This might involve removing and re-adding the panel if it's the easiest way
                mainPanel.remove(visibleCardInMain);
                mainPanel.add(createVoloPanel(false), "Volo_Initial"); // Ensure correct constraint and position
                cardLayout.show(mainPanel, "Volo_Initial"); // Reshow it
            } else if ("MainContentArea_Actual".equals(visibleCardInMain.getName())) {
                // If the logged-in area is visible, update its internal Volo panel
                JPanel mainContentActual = (JPanel) visibleCardInMain;
                JPanel innerContentCards = (JPanel) findComponentByName(mainContentActual, "InnerContent_CardPanel");
                if (innerContentCards != null) {
                    // Find the Volo_LoggedIn_View and update its tables
                    Component voloLoggedInComp = findComponentByName(innerContentCards, "VoloPanel_LoggedIn");
                    if (voloLoggedInComp instanceof JPanel) {
                        // To refresh its tables, we might need to replace it or update its models directly
                        // Safest is often to replace the component within the card layout
                        innerContentCards.remove(voloLoggedInComp);
                        innerContentCards.add(createVoloPanel(true), "Volo_LoggedIn_View");
                        // If it was the visible card, re-show it. Otherwise, it'll update when next shown.
                        Component currentInnerCard = getCurrentVisibleCard(innerContentCards);
                        if (currentInnerCard != null && "VoloPanel_LoggedIn".equals(currentInnerCard.getName())) {
                            ((CardLayout)innerContentCards.getLayout()).show(innerContentCards, "Volo_LoggedIn_View");
                        }
                    }
                }
            }
        }
        mainPanel.revalidate();
        mainPanel.repaint();
    }


    private void aggiornaVistaMiePrenotazioni() {
        // Find the "MainContentArea_Actual" panel
        JPanel mainContentActual = (JPanel) findComponentByName(mainPanel, "MainContentArea_Actual");
        if (mainContentActual != null) {
            JPanel innerContentCards = (JPanel) findComponentByName(mainContentActual, "InnerContent_CardPanel");
            if (innerContentCards != null) {
                // Remove old "MiePrenotazioni_View"
                Component oldMiePrenotazioniPanel = findComponentByName(innerContentCards, "MiePrenotazioniPanel_Internal");
                if (oldMiePrenotazioniPanel != null) {
                    innerContentCards.remove(oldMiePrenotazioniPanel);
                }
                // Add new one
                innerContentCards.add(createMiePrenotazioniPanel(), "MiePrenotazioni_View");
                innerContentCards.revalidate();
                innerContentCards.repaint();
                // If it's the currently shown card, it will update.
                // If not, it will be updated when `show` is called next time for this card.
            }
        }
    }


    private void aggiornaVistaAdminPrenotazioni(String nomeFilter, String cognomeFilter) {
        if (adminTableModel == null) return;
        adminTableModel.setRowCount(0);

        List<Prenotazione> filteredPrenotazioni = controller.getPrenotazioniFiltrateAdmin(nomeFilter, cognomeFilter);

        for (Prenotazione p : filteredPrenotazioni) {
            Passeggero pass = p.getPasseggero();
            Volo voloAssociato = controller.findVoloByCodice(p.getCodiceVolo());
            String compagnia = "N/D", origineDest = "N/D", dataVolo = "N/D";
            if (voloAssociato != null) {
                compagnia = voloAssociato.getCompagniaAerea();
                origineDest = voloAssociato.getOrigine() + "-" + voloAssociato.getDestinazione();
                dataVolo = (voloAssociato.getData() != null ? voloAssociato.getData().format(DATE_FORMATTER) : "N/D") + " " +
                        (voloAssociato.getOrarioPrevisto() != null ? voloAssociato.getOrarioPrevisto().format(TIME_FORMATTER) : "");
            }
            adminTableModel.addRow(new Object[]{
                    pass.getSsn(), pass.getNome(), pass.getCognome(), pass.getEmail(), pass.getTelefono(),
                    p.getCodiceVolo(), compagnia, origineDest, dataVolo, String.valueOf(pass.getPosto()),
                    p.isBagaglio() ? "Sì" : "No", p.isAssicurazione() ? "Sì" : "No"
            });
        }
    }

    private Object[][] getDatiVoliTable(List<Volo> voli) {
        Object[][] dati = new Object[voli.size()][8];
        for (int i = 0; i < voli.size(); i++) {
            Volo v = voli.get(i);
            String gateDisplay = "N/A";
            if (v instanceof Volo_partenza) {
                Volo_partenza vp = (Volo_partenza) v;
                if (vp.gate != null && vp.gate.getGate() != 0) {
                    gateDisplay = String.valueOf(vp.gate.getGate());
                }
            }
            dati[i] = new Object[]{
                    v.getCodice(), v.getCompagniaAerea(), v.getOrigine(), v.getDestinazione(),
                    v.getData() != null ? v.getData().format(DATE_FORMATTER) : "N/D",
                    v.getOrarioPrevisto() != null ? v.getOrarioPrevisto().format(TIME_FORMATTER) : "N/D",
                    controller.mapStatoDelVoloToString(v.getStato()), // Use controller's mapping
                    gateDisplay
            };
        }
        return dati;
    }

    private void handleAdminCreaVoloTipoChange() {
        if (adminCreaVoloTipo == null || adminCreaVoloOrigine == null || adminCreaVoloDestinazione == null || adminCreaVoloGate == null) {
            return;
        }
        String tipoSelezionato = (String) adminCreaVoloTipo.getSelectedItem();
        JLabel gateLabel = findGateLabelForAdminCreaVolo();

        if ("In Partenza".equals(tipoSelezionato)) {
            adminCreaVoloOrigine.setText("Napoli NAP");
            adminCreaVoloOrigine.setEditable(false);
            adminCreaVoloDestinazione.setText("");
            adminCreaVoloDestinazione.setEditable(true);
            adminCreaVoloGate.setEditable(true);
            if(gateLabel != null) gateLabel.setEnabled(true);
            adminCreaVoloGate.setEnabled(true);
        } else if ("In Arrivo".equals(tipoSelezionato)) {
            adminCreaVoloOrigine.setText("");
            adminCreaVoloOrigine.setEditable(true);
            adminCreaVoloDestinazione.setText("Napoli NAP");
            adminCreaVoloDestinazione.setEditable(false);
            adminCreaVoloGate.setText("");
            adminCreaVoloGate.setEditable(false);
            if(gateLabel != null) gateLabel.setEnabled(false);
            adminCreaVoloGate.setEnabled(false);
        } else {
            adminCreaVoloOrigine.setEditable(true);
            adminCreaVoloDestinazione.setEditable(true);
            adminCreaVoloGate.setEditable(true);
            if(gateLabel != null) gateLabel.setEnabled(true);
            adminCreaVoloGate.setEnabled(true);
        }
    }
    private void resetAdminCreaVoloForm() {
        if(adminCreaVoloCodice != null) adminCreaVoloCodice.setText("");
        if(adminCreaVoloCompagnia != null) adminCreaVoloCompagnia.setText("");
        if(adminCreaVoloData != null) adminCreaVoloData.setText("");
        if(adminCreaVoloOrario != null) adminCreaVoloOrario.setText("");
        if(adminCreaVoloGate != null) adminCreaVoloGate.setText("");
        if (adminCreaVoloTipo != null) {
            adminCreaVoloTipo.setSelectedIndex(0); // This triggers handleAdminCreaVoloTipoChange
        } else {
            handleAdminCreaVoloTipoChange(); // Call manually if not triggered
        }
        if (adminCreaVoloStatoComboBox != null) {
            adminCreaVoloStatoComboBox.setSelectedItem("In Orario");
        }
    }


    private JLabel findGateLabelForAdminCreaVolo() {
        if (adminCreaVoloGate == null || adminCreaVoloGate.getParent() == null) return null;
        for(Component comp : adminCreaVoloGate.getParent().getComponents()){
            if(comp instanceof JLabel && "Gate:".equals(((JLabel) comp).getText())){
                return (JLabel) comp;
            }
        }
        return null;
    }

    // --- JOptionPane Wrappers for easier testing/mocking if needed ---
    public void mostraMessaggioInformativo(String messaggio, String titolo) {
        JOptionPane.showMessageDialog(this, messaggio, titolo, JOptionPane.INFORMATION_MESSAGE);
    }
    public void mostraMessaggioErrore(String messaggio, String titolo) {
        JOptionPane.showMessageDialog(this, messaggio, titolo, JOptionPane.ERROR_MESSAGE);
    }
    public void mostraMessaggioWarn(String messaggio, String titolo) {
        JOptionPane.showMessageDialog(this, messaggio, titolo, JOptionPane.WARNING_MESSAGE);
    }
    // For dialog specific messages
    public void mostraMessaggioErroreDialogo(String messaggio, String titolo) {
        JOptionPane.showMessageDialog(this, messaggio, titolo, JOptionPane.ERROR_MESSAGE); // 'this' refers to AppGUI frame
    }


    // --- Component Traversal Helpers (from original MainFrame) ---
    private Component getCurrentVisibleCard(JPanel cardPanel) {
        for (Component comp : cardPanel.getComponents()) {
            if (comp.isVisible()) return comp;
        }
        return null;
    }

    private Component findComponentByName(Container container, String name) {
        if (name == null || container == null) return null;
        for (Component comp : container.getComponents()) {
            if (name.equals(comp.getName())) return comp;
            if (comp instanceof Container) {
                Component found = findComponentByName((Container) comp, name);
                if (found != null) return found;
            }
        }
        return null;
    }


    // --- Inner class PrenotazioneDialog (Dialog for booking) ---
    class PrenotazioneDialog extends JDialog {
        private Volo voloDialogo;
        private AppController controllerDialog; // Controller reference
        private Prenotazione prenotazioneDaConfermare; // Holds the booking data before final confirmation
        private boolean confermata = false;

        private JTextField txtNome, txtCognome, txtSSN, txtEmail, txtTelefono;
        private JCheckBox chkBagaglio, chkAssicurazione;
        private JLabel lblPostoSelezionatoDisplay;
        private String postoAttualmenteSelezionato = null;
        private Map<String, JButton> bottoniSediliMap = new HashMap<>();

        public PrenotazioneDialog(Frame owner, Volo volo, AppController controller) {
            super(owner, "Dettagli Prenotazione Volo: " + volo.getCodice(), true);
            this.voloDialogo = volo;
            this.controllerDialog = controller; // Use the passed controller
            this.prenotazioneDaConfermare = new Prenotazione(); // Initialize a temporary booking object
            this.prenotazioneDaConfermare.setCodiceVolo(volo.getCodice());


            setSize(Math.min(owner.getWidth() - 50, 600), Math.min(owner.getHeight() - 50, 530));
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout(10, 10));
            ((JPanel)getContentPane()).setBorder(new EmptyBorder(10,10,10,10));

            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.addTab("Dati Passeggero", createPanelDatiPersonaliDialog());
            tabbedPane.addTab("Scelta Posto", createPanelSceltaPostoDialog());
            tabbedPane.addTab("Opzioni Extra", createPanelOpzioniExtraDialog());
            add(tabbedPane, BorderLayout.CENTER);

            JPanel panelBottoni = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnConferma = new JButton("Conferma Prenotazione");
            btnConferma.addActionListener(e -> {
                if (validaDatiPrenotazione()) {
                    // All data seems okay, try to create through controller
                    boolean success = controllerDialog.creaNuovaPrenotazione(
                            this.voloDialogo,
                            txtNome.getText().trim(), txtCognome.getText().trim(), txtSSN.getText().trim(),
                            txtEmail.getText().trim(), txtTelefono.getText().trim(),
                            this.postoAttualmenteSelezionato,
                            chkBagaglio.isSelected(), chkAssicurazione.isSelected()
                    );
                    if (success) {
                        this.confermata = true;
                        // Populate prenotazioneDaConfermare with final details for getPrenotazione() if needed by caller.
                        // This is a bit redundant if controller handles adding it to the main list.
                        // The main purpose is to signal success and provide data for the confirmation message.
                        Passeggero pTemp = new Passeggero();
                        pTemp.setNome(txtNome.getText().trim());
                        pTemp.setCognome(txtCognome.getText().trim());
                        pTemp.setPosto(this.postoAttualmenteSelezionato);
                        this.prenotazioneDaConfermare.setPasseggero(pTemp);
                        if(chkBagaglio.isSelected()) this.prenotazioneDaConfermare.updateBagaglio();
                        if(chkAssicurazione.isSelected()) this.prenotazioneDaConfermare.updateAssicurazione();

                        dispose();
                    } else {
                        // Error message already shown by controller via AppGUI.mostraMessaggioErroreDialogo
                    }
                }
            });
            JButton btnAnnulla = new JButton("Annulla");
            btnAnnulla.addActionListener(e -> dispose());
            panelBottoni.add(btnAnnulla);
            panelBottoni.add(btnConferma);
            add(panelBottoni, BorderLayout.SOUTH);
        }

        private boolean validaDatiPrenotazione() {
            String nome = txtNome.getText().trim();
            String cognome = txtCognome.getText().trim();
            String ssn = txtSSN.getText().trim();
            String email = txtEmail.getText().trim();
            String telefono = txtTelefono.getText().trim();

            if (nome.isEmpty() || cognome.isEmpty() || ssn.isEmpty() || email.isEmpty() || telefono.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tutti i campi dei dati personali (incluso SSN) sono obbligatori.", "Dati Mancanti", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (ssn.length() < 5 ) { // Simplified
                JOptionPane.showMessageDialog(this, "Il formato SSN/Codice Fiscale non è valido.", "Errore Dati", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (!email.contains("@") || !email.contains(".")) {
                JOptionPane.showMessageDialog(this, "Formato email non valido.", "Errore Dati", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (postoAttualmenteSelezionato == null) {
                JOptionPane.showMessageDialog(this, "Devi selezionare un posto.", "Posto Mancante", JOptionPane.ERROR_MESSAGE);
                if (getContentPane().getComponent(0) instanceof JTabbedPane) {
                    ((JTabbedPane) getContentPane().getComponent(0)).setSelectedIndex(1);
                }
                return false;
            }
            return true;
        }


        private JPanel createPanelDatiPersonaliDialog() {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            txtNome = new JTextField(20); txtCognome = new JTextField(20);
            txtSSN = new JTextField(16); txtEmail = new JTextField(25);
            txtTelefono = new JTextField(15);

            int y = 0;
            gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Nome:"), gbc);
            gbc.gridx = 1; panel.add(txtNome, gbc); y++;
            gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Cognome:"), gbc);
            gbc.gridx = 1; panel.add(txtCognome, gbc); y++;
            gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("SSN/Codice Fiscale:"), gbc);
            gbc.gridx = 1; panel.add(txtSSN, gbc); y++;
            gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Email:"), gbc);
            gbc.gridx = 1; panel.add(txtEmail, gbc); y++;
            gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Telefono:"), gbc);
            gbc.gridx = 1; panel.add(txtTelefono, gbc);
            return panel;
        }

        private JPanel createPanelSceltaPostoDialog() {
            JPanel panel = new JPanel(new BorderLayout(5,5));
            JPanel mappaPostiPanel = new JPanel(new GridLayout(NUM_FILE_SEDILI, (ULTIMA_LETTERA_SEDILE - 'A') + 1, 3, 3));
            mappaPostiPanel.setBorder(BorderFactory.createTitledBorder("Mappa Sedili Aereo (Semplificata)"));

            List<String> postiOccupati = controllerDialog.getPostiOccupatiPerVolo(voloDialogo.getCodice());
            bottoniSediliMap.clear();

            for (int i = 1; i <= NUM_FILE_SEDILI; i++) {
                for (char c = 'A'; c <= ULTIMA_LETTERA_SEDILE; c++) {
                    String nomePosto = i + "" + c;
                    JButton btnPosto = new JButton(nomePosto);
                    btnPosto.setMargin(new Insets(2,2,2,2));
                    btnPosto.setFont(new Font("Arial", Font.PLAIN, 10));
                    btnPosto.setOpaque(true); btnPosto.setBorderPainted(false);

                    if (postiOccupati.contains(nomePosto)) {
                        btnPosto.setBackground(Color.RED);
                        btnPosto.setEnabled(false);
                    } else {
                        btnPosto.setBackground(Color.GREEN.brighter());
                        btnPosto.setEnabled(true);
                        btnPosto.addActionListener(ev -> selezionaPostoDialog(nomePosto));
                    }
                    mappaPostiPanel.add(btnPosto);
                    bottoniSediliMap.put(nomePosto, btnPosto);
                }
            }
            panel.add(mappaPostiPanel, BorderLayout.CENTER);
            lblPostoSelezionatoDisplay = new JLabel("Nessun posto selezionato");
            lblPostoSelezionatoDisplay.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(lblPostoSelezionatoDisplay, BorderLayout.SOUTH);
            return panel;
        }

        private void selezionaPostoDialog(String nomePosto) {
            if (postoAttualmenteSelezionato != null && bottoniSediliMap.containsKey(postoAttualmenteSelezionato)) {
                JButton vecchioBottone = bottoniSediliMap.get(postoAttualmenteSelezionato);
                if(vecchioBottone.isEnabled()) vecchioBottone.setBackground(Color.GREEN.brighter());
            }
            postoAttualmenteSelezionato = nomePosto;
            if (bottoniSediliMap.containsKey(nomePosto)) {
                bottoniSediliMap.get(nomePosto).setBackground(Color.ORANGE);
            }
            lblPostoSelezionatoDisplay.setText("Posto selezionato: " + nomePosto);
        }

        private JPanel createPanelOpzioniExtraDialog() {
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


        public boolean isPrenotazioneConfermata() { return confermata; }
        public Prenotazione getPrenotazione() { return prenotazioneDaConfermare; }
        public String getPostoAttualmenteSelezionato() { return postoAttualmenteSelezionato; }
    }


    // --- Main Method (Entry Point) ---
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Use default L&F
        }
        SwingUtilities.invokeLater(() -> {
            AppController controller = new AppController();
            new AppGUI(controller).setVisible(true);
        });
    }
}