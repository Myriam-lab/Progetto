import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
// import javax.swing.table.TableRowSorter; // Non usato attivamente
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
    private List<Prenotazione> prenotazioni = new ArrayList<>();

    private JTextField adminSearchNomeField;
    private JTextField adminSearchCognomeField;
    private JTable adminPrenotazioniTable;
    private DefaultTableModel adminTableModel;

    private JTextField adminCreaVoloCodice, adminCreaVoloCompagnia, adminCreaVoloOrigine, adminCreaVoloDestinazione,
            adminCreaVoloData, adminCreaVoloOrario, adminCreaVoloStato, adminCreaVoloGate;
    private JComboBox<String> adminCreaVoloTipo;

    private static final int NUM_FILE_SEDILI = 5;
    private static final char ULTIMA_LETTERA_SEDİLE = 'D';

    public MainFrame() {
        setTitle("Gestione Aeroporto");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 700);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        aggiungiEsempiVoli();
        aggiungiEsempiPrenotazioni();

        mainPanel.add(createVoloPanel(), "Volo");
        mainPanel.add(createLoginPanel(), "Login");
        // Aggiunge un placeholder per "Main" che sarà sostituito al login
        JPanel placeholderMain = new JPanel();
        placeholderMain.setName("MainContainer_Placeholder_Initial");
        mainPanel.add(placeholderMain, "Main");


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

        DefaultTableModel modelArrivo = new DefaultTableModel(getDatiVoli(voliInArrivo), colonne) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tableArrivo = new JTable(modelArrivo);

        DefaultTableModel modelPartenza = new DefaultTableModel(getDatiVoli(voliInPartenza), colonne) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tablePartenza = new JTable(modelPartenza);

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
                    int selectedRowInView = sourceTable.getSelectedRow();
                    if (selectedRowInView >= 0) {
                        int modelRow = sourceTable.convertRowIndexToModel(selectedRowInView);
                        Volo voloSelezionato;
                        if (sourceTable.getModel() == modelArrivo) {
                            if(modelRow < voliInArrivo.size()) voloSelezionato = voliInArrivo.get(modelRow); else return;
                        } else if (sourceTable.getModel() == modelPartenza) {
                            if(modelRow < voliInPartenza.size()) voloSelezionato = voliInPartenza.get(modelRow); else return;
                        } else {
                            return;
                        }

                        if (voloSelezionato.stato.equalsIgnoreCase("Cancellato") ||
                                voloSelezionato.stato.equalsIgnoreCase("Partito") ||
                                voloSelezionato.stato.equalsIgnoreCase("Atterrato")) {
                            JOptionPane.showMessageDialog(MainFrame.this,
                                    "Impossibile prenotare il volo " + voloSelezionato.codice +
                                            " perché è " + voloSelezionato.stato.toLowerCase() + ".",
                                    "Prenotazione Non Disponibile", JOptionPane.WARNING_MESSAGE);
                            return;
                        }

                        PrenotazioneDialog pDialog = new PrenotazioneDialog(MainFrame.this, voloSelezionato);
                        pDialog.setVisible(true);
                        if (pDialog.isPrenotazioneConfermata()) {
                            prenotazioni.add(pDialog.getPrenotazione());
                            aggiornaVistaMiePrenotazioni();
                            if(ruoloUtente.equals("Amministratore") && adminSearchNomeField != null) {
                                aggiornaVistaAdminPrenotazioni(adminSearchNomeField.getText(), adminSearchCognomeField.getText());
                            }
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

        // Determina se l'utente è loggato controllando se il pannello "MainContainer" è attualmente visibile nel mainPanel.
        // Se non è loggato (cioè, il VoloPanel iniziale è visibile), il bottone Accedi deve essere mostrato.
        boolean isLoggedIn = false;
        Component currentTopLevelCard = getCurrentVisibleCard(mainPanel);
        if (currentTopLevelCard != null && "MainContainer".equals(currentTopLevelCard.getName())) {
            isLoggedIn = true;
        }
        // Questo controllo è per il VoloPanel *interno* al MainContentPanel.
        // Se il parent di questo panel è il contentPanel del MainContainer, allora siamo loggati.
        // Per il VoloPanel iniziale, il bottone deve essere visibile se non siamo in "MainContainer".
        Container parent = panel.getParent();
        while(parent != null && !(parent instanceof JFrame)){
            if("MainContentPanel_Card".equals(parent.getName())){
                isLoggedIn = true; // Se siamo dentro il content panel, siamo loggati
                break;
            }
            parent = parent.getParent();
        }
        // Se il ruolo è "Utente" ma non siamo loggati (cioè, stiamo vedendo il VoloPanel iniziale)
        // O se il ruolo è "Utente" e siamo loggati ma questo pannello è quello iniziale (non dovrebbe accadere con la logica attuale)
        // La logica più semplice è: il bottone è visibile se non siamo nel "MainContainer"
        if (currentTopLevelCard != null && "MainContainer".equals(currentTopLevelCard.getName())) {
            btnLogin.setVisible(false); // Se siamo nel MainContainer (loggati), nascondi nel VoloPanel interno
        } else {
            btnLogin.setVisible(true); // Se siamo nel VoloPanel iniziale (non loggati), mostra
        }


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

    private void refreshVoloTables() {
        updateVoloPanelForRole();
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
            boolean loginSuccess = false;
            if (user.equals("admin") && pass.equals("admin")) {
                ruoloUtente = "Amministratore";
                JOptionPane.showMessageDialog(this, "Accesso come Amministratore");
                loginSuccess = true;
            } else if (user.equals("utente") && pass.equals("utente")) {
                ruoloUtente = "Utente";
                JOptionPane.showMessageDialog(this, "Accesso come Utente");
                loginSuccess = true;
            } else {
                JOptionPane.showMessageDialog(this, "Credenziali non valide");
            }

            if (loginSuccess) {
                // Rimuovi il placeholder o il vecchio MainContainer
                Component oldMainOrPlaceholder = null;
                for(Component comp : mainPanel.getComponents()){
                    if("Main".equals(cardLayout.toString()) || "MainContainer_Placeholder_Initial".equals(comp.getName()) || "MainContainer".equals(comp.getName())){
                        // Questo modo di trovare il componente "Main" è impreciso
                        // Meglio affidarsi al nome o avere un riferimento.
                        // Per ora, cerchiamo per nome se possibile.
                        if("MainContainer_Placeholder_Initial".equals(comp.getName()) || "MainContainer".equals(comp.getName())){
                            oldMainOrPlaceholder = comp;
                            break;
                        }
                    }
                }
                if (oldMainOrPlaceholder != null) {
                    mainPanel.remove(oldMainOrPlaceholder);
                }

                mainPanel.add(createMainContentPanel(), targetPanelConstraint);
                updateVoloPanelForRole();
                cardLayout.show(mainPanel, targetPanelConstraint);
            }
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
        if(oldInitialVoloPanel != null) {
            mainPanel.remove(oldInitialVoloPanel);
        }
        mainPanel.add(createVoloPanel(), "Volo", 0); // Questo VoloPanel userà il `ruoloUtente` corrente

        Component mainContentWrapper = findComponentByName(mainPanel, "MainContainer");
        if (mainContentWrapper instanceof Container) {
            JPanel contentPanelCards = (JPanel) findComponentByName((Container) mainContentWrapper, "MainContentPanel_Card");
            if (contentPanelCards != null) {
                Component oldVoloPanelInterno = findComponentByName(contentPanelCards, "VoloPanel_Interno");
                if (oldVoloPanelInterno != null) {
                    contentPanelCards.remove(oldVoloPanelInterno);
                }
                JPanel nuovoVoloPanelInterno = createVoloPanel(); // Questo userà il `ruoloUtente` corrente
                nuovoVoloPanelInterno.setName("VoloPanel_Interno");
                setButtonVisibilityInPanel(nuovoVoloPanelInterno, "LoginButton_VoloPanel", false); // Nascondi login se interno
                contentPanelCards.add(nuovoVoloPanelInterno, "Volo");
            }
        }
        mainPanel.revalidate();
        mainPanel.repaint();
    }


    private JPanel createMainContentPanel() {
        JPanel container = new JPanel(new BorderLayout(0, 0));
        container.setName("MainContainer"); // Nome importante per trovarlo
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnVolo = new JButton("Visualizza Voli");
        JButton btnMiePrenotazioni = new JButton("Le Mie Prenotazioni");
        JButton btnAdminCercaPrenotazioni = new JButton("Gestisci Prenotazioni");
        JButton btnAdminCreaVolo = new JButton("Crea Nuovo Volo");
        JButton btnLogout = new JButton("Logout");

        JPanel contentPanel = new JPanel(new CardLayout());
        contentPanel.setName("MainContentPanel_Card");

        JPanel voliPanelInterno = createVoloPanel(); // Creato con il ruoloUtente corrente
        voliPanelInterno.setName("VoloPanel_Interno");
        setButtonVisibilityInPanel(voliPanelInterno, "LoginButton_VoloPanel", false); // Nascondi login nel panel interno

        contentPanel.add(voliPanelInterno, "Volo");
        contentPanel.add(createMiePrenotazioniPanel(), "MiePrenotazioni");
        contentPanel.add(createAdminPrenotazioniPanel(), "AdminCercaPrenotazioni");
        contentPanel.add(createAdminCreaVoloPanel(), "AdminCreaVolo");

        btnVolo.addActionListener(e -> {
            // updateVoloPanelForRole(); // Chiamato per sicurezza, ma il VoloPanel interno dovrebbe essere già ok
            switchContent(contentPanel, "Volo");
        });

        btnMiePrenotazioni.addActionListener(e -> {
            if (ruoloUtente.equals("Utente")) {
                aggiornaVistaMiePrenotazioni(); // Ricrea il pannello con dati aggiornati
                switchContent(contentPanel, "MiePrenotazioni");
            } else {
                JOptionPane.showMessageDialog(this, "Funzione disponibile solo per gli utenti.", "Accesso Negato", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnAdminCercaPrenotazioni.addActionListener(e -> {
            if (ruoloUtente.equals("Amministratore")) {
                if (adminSearchNomeField != null) aggiornaVistaAdminPrenotazioni(adminSearchNomeField.getText(), adminSearchCognomeField.getText());
                else aggiornaVistaAdminPrenotazioni("","");
                switchContent(contentPanel, "AdminCercaPrenotazioni");
            } else {
                JOptionPane.showMessageDialog(this, "Funzione disponibile solo per gli amministratori.", "Accesso Negato", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnAdminCreaVolo.addActionListener(e -> {
            if (ruoloUtente.equals("Amministratore")) {
                switchContent(contentPanel, "AdminCreaVolo");
            } else {
                JOptionPane.showMessageDialog(this, "Funzione disponibile solo per gli amministratori.", "Accesso Negato", JOptionPane.WARNING_MESSAGE);
            }
        });


        btnLogout.addActionListener(e -> {
            ruoloUtente = "Utente"; // Imposta ruolo a utente non loggato (o generico)
            txtNomeUtente.setText(""); // Pulisci campi login
            txtPassword.setText("");

            // Rimuovi il pannello MainContainer corrente
            Component oldMainContainer = findComponentByName(mainPanel, "MainContainer");
            if (oldMainContainer != null) {
                mainPanel.remove(oldMainContainer);
            }
            // Aggiungi un placeholder per il constraint "Main" per evitare problemi con CardLayout
            // Questo placeholder sarà sostituito al prossimo login.
            JPanel placeholderMain = new JPanel();
            placeholderMain.setName("MainContainer_Placeholder_After_Logout"); // Nome per debug, se necessario
            mainPanel.add(placeholderMain, "Main"); // "Main" è il constraint usato dal CardLayout per questo slot

            updateVoloPanelForRole(); // Questo ricrea il VoloPanel iniziale (all'indice 0)
            // usando il `ruoloUtente` appena resettato.
            // Il VoloPanel iniziale mostrerà il pulsante "Accedi..."
            cardLayout.show(mainPanel, "Volo"); // Mostra la schermata Voli iniziale
        });

        navBar.add(btnVolo);
        btnMiePrenotazioni.setVisible(ruoloUtente.equals("Utente"));
        btnAdminCercaPrenotazioni.setVisible(ruoloUtente.equals("Amministratore"));
        btnAdminCreaVolo.setVisible(ruoloUtente.equals("Amministratore"));

        navBar.add(btnMiePrenotazioni);
        navBar.add(btnAdminCercaPrenotazioni);
        navBar.add(btnAdminCreaVolo);
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
                // switchContent(contentPanel, "MiePrenotazioni"); // Lo switch avviene nell'action listener del bottone
                contentPanel.revalidate();
                contentPanel.repaint();
            }
        }
    }

    private JPanel createMiePrenotazioniPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setName("MiePrenotazioniPanel_Internal");
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String nomeUtenteLoggato = (txtNomeUtente != null && !txtNomeUtente.getText().isEmpty() && ruoloUtente.equals("Utente")) ? txtNomeUtente.getText() : "";
        JLabel titleLabel = new JLabel("Le Mie Prenotazioni" + (!nomeUtenteLoggato.isEmpty() ? " ("+ nomeUtenteLoggato +")" : ""), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        List<Prenotazione> prenotazioniUtente = new ArrayList<>();
        if (!nomeUtenteLoggato.isEmpty()) {
            prenotazioniUtente = prenotazioni.stream()
                    .filter(p -> p.nome.equalsIgnoreCase(nomeUtenteLoggato))
                    .collect(Collectors.toList());
        }

        if (prenotazioniUtente.isEmpty()) {
            JLabel noPrenotazioniLabel = new JLabel(ruoloUtente.equals("Utente") && !nomeUtenteLoggato.isEmpty() ? "Nessuna prenotazione effettuata." : "Accedi come utente per vedere le tue prenotazioni.", SwingConstants.CENTER);
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
            tablePrenotazioni.getColumnModel().getColumn(0).setPreferredWidth(120);
            tablePrenotazioni.getColumnModel().getColumn(1).setPreferredWidth(100);
            tablePrenotazioni.getColumnModel().getColumn(3).setPreferredWidth(150);
            tablePrenotazioni.getColumnModel().getColumn(6).setPreferredWidth(120);
            tablePrenotazioni.getColumnModel().getColumn(7).setPreferredWidth(120);
            tablePrenotazioni.getColumnModel().getColumn(11).setPreferredWidth(90);

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

        String[] colonneAdmin = {"SSN", "Nome", "Cognome", "Data Nascita", "Email", "Telefono",
                "Cod. Volo", "Compagnia", "Orig-Dest", "Data Volo", "Posto", "Bagaglio", "Assicurazione"};
        adminTableModel = new DefaultTableModel(colonneAdmin, 0){
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        adminPrenotazioniTable = new JTable(adminTableModel);
        adminPrenotazioniTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        adminPrenotazioniTable.getColumnModel().getColumn(0).setPreferredWidth(130);
        adminPrenotazioniTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        adminPrenotazioniTable.getColumnModel().getColumn(4).setPreferredWidth(160);
        adminPrenotazioniTable.getColumnModel().getColumn(8).setPreferredWidth(120);
        adminPrenotazioniTable.getColumnModel().getColumn(9).setPreferredWidth(120);
        adminPrenotazioniTable.getColumnModel().getColumn(12).setPreferredWidth(90);

        JScrollPane scrollPane = new JScrollPane(adminPrenotazioniTable);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);
        aggiornaVistaAdminPrenotazioni("", "");
        return panel;
    }

    private void aggiornaVistaAdminPrenotazioni(String nomeFilter, String cognomeFilter) {
        if (adminTableModel == null) return;
        adminTableModel.setRowCount(0);

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
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; adminCreaVoloStato = new JTextField(10); adminCreaVoloStato.setText("Programmato"); panel.add(adminCreaVoloStato, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; gbc.fill = GridBagConstraints.NONE; panel.add(new JLabel("Gate:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; adminCreaVoloGate = new JTextField(5); panel.add(adminCreaVoloGate, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; gbc.fill = GridBagConstraints.NONE; panel.add(new JLabel("Tipo Volo:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; adminCreaVoloTipo = new JComboBox<>(new String[]{"In Partenza", "In Arrivo"}); panel.add(adminCreaVoloTipo, gbc);
        y++;

        gbc.gridy = y;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        JButton btnCrea = new JButton("Crea Volo");
        btnCrea.addActionListener(e -> {
            try {
                String codice = adminCreaVoloCodice.getText().trim();
                String compagnia = adminCreaVoloCompagnia.getText().trim();
                String origine = adminCreaVoloOrigine.getText().trim();
                String destinazione = adminCreaVoloDestinazione.getText().trim();
                String data = adminCreaVoloData.getText().trim();
                String orario = adminCreaVoloOrario.getText().trim();
                String stato = adminCreaVoloStato.getText().trim();
                String gate = adminCreaVoloGate.getText().trim();
                String tipo = (String) adminCreaVoloTipo.getSelectedItem();

                if (codice.isEmpty() || compagnia.isEmpty() || origine.isEmpty() || destinazione.isEmpty() ||
                        data.isEmpty() || orario.isEmpty() || stato.isEmpty() || gate.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Tutti i campi sono obbligatori.", "Errore Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!data.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    JOptionPane.showMessageDialog(this, "Formato data non valido. Usare YYYY-MM-DD.", "Errore Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!orario.matches("\\d{2}:\\d{2}")) {
                    JOptionPane.showMessageDialog(this, "Formato orario non valido. Usare HH:MM.", "Errore Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Volo nuovoVolo = new Volo(codice, compagnia, origine, destinazione, data, orario, stato, gate);
                if ("In Partenza".equals(tipo)) {
                    voliInPartenza.add(nuovoVolo);
                } else {
                    voliInArrivo.add(nuovoVolo);
                }
                JOptionPane.showMessageDialog(this, "Volo " + codice + " creato con successo!", "Volo Creato", JOptionPane.INFORMATION_MESSAGE);

                adminCreaVoloCodice.setText(""); adminCreaVoloCompagnia.setText(""); adminCreaVoloOrigine.setText("");
                adminCreaVoloDestinazione.setText(""); adminCreaVoloData.setText(""); adminCreaVoloOrario.setText("");
                adminCreaVoloStato.setText("Programmato"); adminCreaVoloGate.setText("");

                refreshVoloTables();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Errore durante la creazione del volo: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(btnCrea, gbc);
        return panel;
    }


    private void switchContent(JPanel panel, String name) {
        CardLayout cl = (CardLayout) panel.getLayout();
        cl.show(panel, name);
    }

    private void aggiungiEsempiVoli() {
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
        if (voliInPartenza.size() < 5 || voliInArrivo.size() < 2) {
            System.err.println("Numero insufficiente di voli di esempio per creare tutte le prenotazioni di esempio.");
            return;
        }
        // Prenotazioni per AZ205 (Napoli NAP -> Roma FCO) - voloPartenza.get(0)
        prenotazioni.add(new Prenotazione("Mario", "Rossi", "RSSMRA80A01H501A", "01/01/1980", "mario.rossi@example.com", "3331234567",
                voliInPartenza.get(0), "1A", true, true));
        prenotazioni.add(new Prenotazione("Giulia", "Neri", "NERGLI85M41H501Z", "20/08/1985", "giulia.neri@example.com", "3339876543",
                voliInPartenza.get(0), "1B", false, true));

        // Prenotazioni per FR1823 (Napoli NAP -> Milano BGY) - voloPartenza.get(1)
        prenotazioni.add(new Prenotazione("Laura", "Bianchi", "BNCLRA85M41H501B", "21/08/1985", "laura.bianchi@example.com", "3387654321",
                voliInPartenza.get(1), "2B", false, true));
        prenotazioni.add(new Prenotazione("Paolo", "Gallo", "GLLPLA70A01F205X", "01/01/1970", "paolo.gallo@example.com", "3201234567",
                voliInPartenza.get(1), "2A", true, false));

        // Prenotazioni per utente "utente"
        prenotazioni.add(new Prenotazione("utente", "Test", "TSTUSR90C02H501C", "02/03/1990", "utente.test@example.com", "3471122334",
                voliInArrivo.get(0), "3C", true, false)); // Volo AZ204
        prenotazioni.add(new Prenotazione("utente", "Test", "TSTUSR90C02H501C", "02/03/1990", "utente.test@example.com", "3471122334",
                voliInPartenza.get(2), "1D", false, true)); // Volo U22852


        prenotazioni.add(new Prenotazione("Giovanni", "Verdi", "VRDGNN75P03H501D", "03/09/1975", "giovanni.verdi@example.com", "3294455667",
                voliInPartenza.get(4), "4D", false, false)); // Volo AF1579
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

    class Prenotazione {
        String nome, cognome, ssn, dataNascita, email, telefono;
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

    class PrenotazioneDialog extends JDialog {
        private Volo voloDialogo;
        private Prenotazione prenotazioneEffettuata;
        private boolean confermata = false;

        private JTextField txtNome, txtCognome, txtSSN, txtDataNascita, txtEmail, txtTelefono;
        private JCheckBox chkBagaglio, chkAssicurazione;
        private JLabel lblPostoSelezionatoDisplay;
        private String postoAttualmenteSelezionato = null;
        private Map<String, JButton> bottoniSediliMap = new HashMap<>();


        public PrenotazioneDialog(Frame owner, Volo volo) {
            super(owner, "Dettagli Prenotazione Volo: " + volo.codice, true);
            this.voloDialogo = volo;
            setSize(Math.min(owner.getWidth() - 50, 600), Math.min(owner.getHeight() - 50, 580));
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
            txtSSN = new JTextField(16);
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
            gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("SSN/Codice Fiscale:"), gbc);
            gbc.gridx = 1; panel.add(txtSSN, gbc);
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
            JPanel panel = new JPanel(new BorderLayout(5,5));
            JPanel mappaPostiPanel = new JPanel(new GridLayout(NUM_FILE_SEDILI, (ULTIMA_LETTERA_SEDİLE - 'A') + 1, 3, 3));
            mappaPostiPanel.setBorder(BorderFactory.createTitledBorder("Mappa Sedili Aereo (Semplificata)"));

            List<String> postiOccupatiPerQuestoVolo = new ArrayList<>();
            for (Prenotazione p : MainFrame.this.prenotazioni) {
                if (p.volo.codice.equals(this.voloDialogo.codice)) {
                    postiOccupatiPerQuestoVolo.add(p.postoSelezionato);
                }
            }

            bottoniSediliMap.clear();

            for (int i = 1; i <= NUM_FILE_SEDILI; i++) {
                for (char c = 'A'; c <= ULTIMA_LETTERA_SEDİLE; c++) {
                    String nomePosto = i + "" + c;
                    JButton btnPosto = new JButton(nomePosto);
                    btnPosto.setMargin(new Insets(2,2,2,2));
                    btnPosto.setFont(new Font("Arial", Font.PLAIN, 10));
                    btnPosto.setOpaque(true);
                    btnPosto.setBorderPainted(false);

                    if (postiOccupatiPerQuestoVolo.contains(nomePosto)) {
                        btnPosto.setBackground(Color.RED);
                        btnPosto.setEnabled(false);
                    } else {
                        btnPosto.setBackground(Color.GREEN.brighter());
                        btnPosto.setEnabled(true);
                        btnPosto.addActionListener(e -> selezionaPosto(nomePosto));
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

        private void selezionaPosto(String nomePosto) {
            if (postoAttualmenteSelezionato != null && bottoniSediliMap.containsKey(postoAttualmenteSelezionato)) {
                JButton vecchioBottoneSelezionato = bottoniSediliMap.get(postoAttualmenteSelezionato);
                vecchioBottoneSelezionato.setBackground(Color.GREEN.brighter());
            }
            postoAttualmenteSelezionato = nomePosto;
            if (bottoniSediliMap.containsKey(nomePosto)) {
                bottoniSediliMap.get(nomePosto).setBackground(Color.ORANGE);
            }
            lblPostoSelezionatoDisplay.setText("Posto selezionato: " + nomePosto);
        }


        private JPanel createPanelOpzioniExtra() {
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
            String ssn = txtSSN.getText().trim();
            String dataNascita = txtDataNascita.getText().trim();
            String email = txtEmail.getText().trim();
            String telefono = txtTelefono.getText().trim();

            if (nome.isEmpty() || cognome.isEmpty() || ssn.isEmpty() || dataNascita.isEmpty() || email.isEmpty() || telefono.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tutti i campi dei dati personali (incluso SSN) sono obbligatori.", "Dati Mancanti", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (ssn.length() != 16) {
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

            this.prenotazioneEffettuata = new Prenotazione(nome, cognome, ssn, dataNascita, email, telefono, this.voloDialogo,
                    postoAttualmenteSelezionato, bagaglio, assicurazione);
            this.confermata = true;
            dispose();
        }

        public boolean isPrenotazioneConfermata() { return confermata; }
        public Prenotazione getPrenotazione() { return prenotazioneEffettuata; }
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