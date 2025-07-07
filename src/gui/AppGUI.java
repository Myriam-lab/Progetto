package gui;

import controller.AppController;
import model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppGUI extends JFrame {

    private final AppController controller;

    private final CardLayout cardLayout;
    private final JPanel mainPanel;

    private JTextField txtNomeUtenteLogin;
    private JPasswordField txtPasswordLogin;

    private JTextField adminSearchNomeField;
    private JTextField adminSearchCognomeField;

    private DefaultTableModel adminTableModel;

    private JTextField adminCreaVoloCodice;
    private JTextField adminCreaVoloCompagnia;
    private JTextField adminCreaVoloOrigine;
    private JTextField adminCreaVoloDestinazione;
    private JTextField adminCreaVoloData;
    private JTextField adminCreaVoloOrario;
    private JTextField adminCreaVoloGate;
    private JComboBox<String> adminCreaVoloTipo;
    private JComboBox<String> adminCreaVoloStatoComboBox;

    private final String[] STATI_VOLO_DISPLAY = {"In Orario", "In Ritardo", "Cancellato", "Atterrato", "Rinviato"};
    private static final int NUM_FILE_SEDILI = 5;
    private static final char ULTIMA_LETTERA_SEDILE = 'D';
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;


    public AppGUI(AppController controller) {
        this.controller = controller;
        this.controller.setGui(this);

        setTitle("Gestione Aeroporto");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 700);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setName("MainCardPanel");


        mainPanel.add(createVoloPanel(false), "Volo_Initial");
        mainPanel.add(createLoginPanel(), "Login");
        JPanel placeholderMain = new JPanel();
        placeholderMain.setName("MainContentArea_Placeholder");
        mainPanel.add(placeholderMain, "MainContentArea");


        add(mainPanel);
        cardLayout.show(mainPanel, "Volo_Initial");
    }


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
                    if (!loggedIn) {
                        mostraMessaggioInformativo("Devi accedere per prenotare un volo.", "Accesso Richiesto");
                    }
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
                    } else if (sourceTable.getModel() == modelPartenza && !(statoVolo == Stato_del_volo.in_orario || statoVolo == Stato_del_volo.in_ritardo || statoVolo == Stato_del_volo.rinviato)) {
                            canBook = false; reason = "il suo stato (" + controller.mapStatoDelVoloToString(statoVolo) + ") non permette la prenotazione";
                        }

                    if (!canBook) {
                        mostraMessaggioWarn("Impossibile prenotare il volo " + voloSelezionato.getCodice() + " perché " + reason + ".", "Prenotazione Non Disponibile");
                        return;
                    }

                    PrenotazioneDialog pDialog = new PrenotazioneDialog(AppGUI.this, voloSelezionato, controller);
                    pDialog.setVisible(true);
                    if (pDialog.isPrenotazioneConfermata()) {
                        aggiornaTabelleVoli();
                        if ("Utente".equals(controller.getRuoloUtente())) {
                            aggiornaVistaMiePrenotazioni();
                        }
                        if("Amministratore".equals(controller.getRuoloUtente()) && adminSearchNomeField != null) {
                            aggiornaVistaAdminPrenotazioni(adminSearchNomeField.getText(), adminSearchCognomeField.getText());
                        }

                        Prenotazione prenEffettuata = pDialog.getPrenotazione();
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

        if (!loggedIn) {
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

                Component oldMainContentArea = findComponentByName(mainPanel, "MainContentArea_Placeholder");
                if (oldMainContentArea == null) oldMainContentArea = findComponentByName(mainPanel, "MainContentArea_Actual");
                if (oldMainContentArea != null) mainPanel.remove(oldMainContentArea);

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
        container.setName("MainContentPanel_Container_LoggedIn");

        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnVolo = new JButton("Visualizza Voli");
        JButton btnMiePrenotazioni = new JButton("Le Mie Prenotazioni");
        JButton btnAdminCercaPrenotazioni = new JButton("Gestisci Prenotazioni");
        JButton btnAdminCreaVolo = new JButton("Crea Nuovo Volo");
        JButton btnLogout = new JButton("Logout");

        JPanel contentPanelCards = new JPanel(new CardLayout());
        contentPanelCards.setName("InnerContent_CardPanel");

        JPanel voliPanelInterno = createVoloPanel(true);

        contentPanelCards.add(voliPanelInterno, "Volo_LoggedIn_View");
        contentPanelCards.add(createMiePrenotazioniPanel(), "MiePrenotazioni_View");
        contentPanelCards.add(createAdminPrenotazioniPanel(), "AdminCercaPrenotazioni_View");
        contentPanelCards.add(createAdminCreaVoloPanel(), "AdminCreaVolo_View");

        CardLayout innerCardLayout = (CardLayout) contentPanelCards.getLayout();

        btnVolo.addActionListener(e -> {
            aggiornaTabelleVoli();
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
                resetAdminCreaVoloForm();
                innerCardLayout.show(contentPanelCards, "AdminCreaVolo_View");
            } else {
                mostraMessaggioWarn("Funzione disponibile solo per gli amministratori.", "Accesso Negato");
            }
        });

        btnLogout.addActionListener(e -> {
            controller.logout();
            txtNomeUtenteLogin.setText("");
            txtPasswordLogin.setText("");

            Component currentMainContent = findComponentByName(mainPanel, "MainContentArea_Actual");
            if (currentMainContent != null) {
                mainPanel.remove(currentMainContent);
            }
            JPanel placeholderMain = new JPanel();
            placeholderMain.setName("MainContentArea_Placeholder");
            mainPanel.add(placeholderMain, "MainContentArea");


            Component oldInitialVolo = findComponentByName(mainPanel, "VoloPanel_Initial_Instance");
            if(oldInitialVolo != null) mainPanel.remove(oldInitialVolo);
            mainPanel.add(createVoloPanel(false), "Volo_Initial", 0);

            cardLayout.show(mainPanel, "Volo_Initial");
            mainPanel.revalidate();
            mainPanel.repaint();
        });

        navBar.add(btnVolo);
        btnMiePrenotazioni.setVisible("Utente".equals(controller.getRuoloUtente()));
        btnAdminCercaPrenotazioni.setVisible("Amministratore".equals(controller.getRuoloUtente()));
        btnAdminCreaVolo.setVisible("Amministratore".equals(controller.getRuoloUtente()));

        navBar.add(btnMiePrenotazioni);
        navBar.add(btnAdminCercaPrenotazioni);
        navBar.add(btnAdminCreaVolo);
        navBar.add(btnLogout);

        container.add(navBar, BorderLayout.NORTH);
        container.add(contentPanelCards, BorderLayout.CENTER);
        innerCardLayout.show(contentPanelCards, "Volo_LoggedIn_View");
        return container;
    }


    private JPanel createMiePrenotazioniPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setName("MiePrenotazioniPanel_Internal");
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String nomePasseggeroLoggato = controller.getNomeUtenteLoggato();

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
        JTable adminPrenotazioniTable;
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
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);
        aggiornaVistaAdminPrenotazioni("", "");
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

        handleAdminCreaVoloTipoChange();

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
                aggiornaTabelleVoli();
            }
        });
        panel.add(btnCrea, gbc);
        return panel;
    }


    public void aggiornaTabelleVoli() {

        Component visibleCardInMain = getCurrentVisibleCard(mainPanel);
        if (visibleCardInMain != null) {
            if ("Volo_Initial".equals(visibleCardInMain.getName())) {
                mainPanel.remove(visibleCardInMain);
                mainPanel.add(createVoloPanel(false), "Volo_Initial");
                cardLayout.show(mainPanel, "Volo_Initial");
            } else if ("MainContentArea_Actual".equals(visibleCardInMain.getName())) {
                JPanel mainContentActual = (JPanel) visibleCardInMain;
                JPanel innerContentCards = (JPanel) findComponentByName(mainContentActual, "InnerContent_CardPanel");
                if (innerContentCards != null) {
                    Component voloLoggedInComp = findComponentByName(innerContentCards, "VoloPanel_LoggedIn");
                    if (voloLoggedInComp instanceof JPanel) {
                        innerContentCards.remove(voloLoggedInComp);
                        innerContentCards.add(createVoloPanel(true), "Volo_LoggedIn_View");
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
        JPanel mainContentActual = (JPanel) findComponentByName(mainPanel, "MainContentArea_Actual");
        if (mainContentActual != null) {
            JPanel innerContentCards = (JPanel) findComponentByName(mainContentActual, "InnerContent_CardPanel");
            if (innerContentCards != null) {
                Component oldMiePrenotazioniPanel = findComponentByName(innerContentCards, "MiePrenotazioniPanel_Internal");
                if (oldMiePrenotazioniPanel != null) {
                    innerContentCards.remove(oldMiePrenotazioniPanel);
                }
                innerContentCards.add(createMiePrenotazioniPanel(), "MiePrenotazioni_View");
                innerContentCards.revalidate();
                innerContentCards.repaint();
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
            String compagnia = "N/D";
            String origineDest = "N/D";
            String dataVolo = "N/D";
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
            if (v instanceof Volo_partenza vp && vp.gate != null && vp.gate.getGate() != 0) {
                    gateDisplay = String.valueOf(vp.gate.getGate());
                }

            dati[i] = new Object[]{
                    v.getCodice(), v.getCompagniaAerea(), v.getOrigine(), v.getDestinazione(),
                    v.getData() != null ? v.getData().format(DATE_FORMATTER) : "N/D",
                    v.getOrarioPrevisto() != null ? v.getOrarioPrevisto().format(TIME_FORMATTER) : "N/D",
                    controller.mapStatoDelVoloToString(v.getStato()),
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
            adminCreaVoloTipo.setSelectedIndex(0);
        } else {
            handleAdminCreaVoloTipoChange();
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

    public void mostraMessaggioInformativo(String messaggio, String titolo) {
        JOptionPane.showMessageDialog(this, messaggio, titolo, JOptionPane.INFORMATION_MESSAGE);
    }
    public void mostraMessaggioErrore(String messaggio, String titolo) {
        JOptionPane.showMessageDialog(this, messaggio, titolo, JOptionPane.ERROR_MESSAGE);
    }
    public void mostraMessaggioWarn(String messaggio, String titolo) {
        JOptionPane.showMessageDialog(this, messaggio, titolo, JOptionPane.WARNING_MESSAGE);
    }
    public void mostraMessaggioErroreDialogo(String messaggio, String titolo) {
        JOptionPane.showMessageDialog(this, messaggio, titolo, JOptionPane.ERROR_MESSAGE);
    }


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


    static class PrenotazioneDialog extends JDialog {
        private final Volo voloDialogo;
        private final AppController controllerDialog;
        private final Prenotazione prenotazioneDaConfermare;
        private boolean confermata = false;

        private JTextField txtNome;
        private JTextField txtCognome;
        private JTextField txtSSN;
        private JTextField txtEmail;
        private JTextField txtTelefono;
        private JCheckBox chkBagaglio;
        private JCheckBox chkAssicurazione;
        private JLabel lblPostoSelezionatoDisplay;
        private String postoAttualmenteSelezionato = null;
        private final Map<String, JButton> bottoniSediliMap = new HashMap<>();

        public PrenotazioneDialog(Frame owner, Volo volo, AppController controller) {
            super(owner, "Dettagli Prenotazione Volo: " + volo.getCodice(), true);
            this.voloDialogo = volo;
            this.controllerDialog = controller;
            this.prenotazioneDaConfermare = new Prenotazione();
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
                    boolean success = controllerDialog.creaNuovaPrenotazione(
                            this.voloDialogo,
                            txtNome.getText().trim(), txtCognome.getText().trim(), txtSSN.getText().trim(),
                            txtEmail.getText().trim(), txtTelefono.getText().trim(),
                            this.postoAttualmenteSelezionato,
                            chkBagaglio.isSelected(), chkAssicurazione.isSelected()
                    );
                    if (success) {
                        this.confermata = true;
                        Passeggero pTemp = new Passeggero();
                        pTemp.setNome(txtNome.getText().trim());
                        pTemp.setCognome(txtCognome.getText().trim());
                        pTemp.setPosto(this.postoAttualmenteSelezionato);
                        this.prenotazioneDaConfermare.setPasseggero(pTemp);
                        if(chkBagaglio.isSelected()) this.prenotazioneDaConfermare.updateBagaglio();
                        if(chkAssicurazione.isSelected()) this.prenotazioneDaConfermare.updateAssicurazione();

                        dispose();
                    } else {
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
            if (ssn.length() < 5 ) {
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


    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        SwingUtilities.invokeLater(() -> {
            AppController controller = new AppController();
            new AppGUI(controller).setVisible(true);
        });
    }
}