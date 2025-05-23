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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import model.*;

public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JTextField txtNomeUtente;
    private JPasswordField txtPassword;
    private String ruoloUtente = "Utente";

    private List<Volo> voliInArrivo = new ArrayList<>();
    private List<Volo> voliInPartenza = new ArrayList<>();
    private List<Prenotazione> prenotazioni = new ArrayList<>();

    private JTextField adminSearchNomeField;
    private JTextField adminSearchCognomeField;
    private JTable adminPrenotazioniTable;
    private DefaultTableModel adminTableModel;

    private JTextField adminCreaVoloCodice, adminCreaVoloCompagnia, adminCreaVoloOrigine, adminCreaVoloDestinazione,
            adminCreaVoloData, adminCreaVoloOrario, adminCreaVoloGate;
    private JComboBox<String> adminCreaVoloTipo;
    private JComboBox<String> adminCreaVoloStatoComboBox;

    private final String[] STATI_VOLO_DISPLAY = {"In Orario", "In Ritardo", "Cancellato", "Atterrato", "Rinviato"};


    private static final int NUM_FILE_SEDILI = 5;
    private static final char ULTIMA_LETTERA_SEDİLE = 'D';

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;

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
        JPanel placeholderMain = new JPanel();
        placeholderMain.setName("MainContainer_Placeholder_Initial");
        mainPanel.add(placeholderMain, "Main");

        add(mainPanel);
        cardLayout.show(mainPanel, "Volo");
    }

    private Stato_del_volo mapStringToStatoDelVolo(String statoStr) {
        if (statoStr == null) return Stato_del_volo.in_orario;
        switch (statoStr) {
            case "In Orario": return Stato_del_volo.in_orario;
            case "In Ritardo": return Stato_del_volo.in_ritardo;
            case "Cancellato": return Stato_del_volo.cancellato;
            case "Atterrato": return Stato_del_volo.atterrato;
            case "Rinviato": return Stato_del_volo.rinviato;
            default: return Stato_del_volo.in_orario;
        }
    }

    private String mapStatoDelVoloToString(Stato_del_volo stato) {
        if (stato == null) return "N/D";
        switch (stato) {
            case in_orario: return "In Orario";
            case in_ritardo: return "In Ritardo";
            case cancellato: return "Cancellato";
            case atterrato: return "Atterrato";
            case rinviato: return "Rinviato";
            default: return "N/D";
        }
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

                        Stato_del_volo statoVolo = voloSelezionato.getStato();
                        boolean canBook = true;
                        String reason = "";

                        if (statoVolo == Stato_del_volo.cancellato) {
                            canBook = false;
                            reason = "è cancellato";
                        } else if (sourceTable.getModel() == modelArrivo && statoVolo == Stato_del_volo.atterrato) {
                            canBook = false;
                            reason = "è già atterrato";
                        } else if (sourceTable.getModel() == modelPartenza) {
                            if (!(statoVolo == Stato_del_volo.in_orario ||
                                    statoVolo == Stato_del_volo.in_ritardo ||
                                    statoVolo == Stato_del_volo.rinviato)) {
                                canBook = false;
                                reason = "il suo stato (" + mapStatoDelVoloToString(statoVolo) + ") non permette la prenotazione";
                            }
                        }
                        if (!canBook) {
                            JOptionPane.showMessageDialog(MainFrame.this,
                                    "Impossibile prenotare il volo " + voloSelezionato.getCodice() +
                                            " perché " + reason + ".",
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
                            Prenotazione prenEffettuata = pDialog.getPrenotazione();
                            JOptionPane.showMessageDialog(MainFrame.this,
                                    "Prenotazione effettuata con successo per " + prenEffettuata.getPasseggero().getNome() + " " + prenEffettuata.getPasseggero().getCognome() +
                                            "\nPosto: " + pDialog.getPostoAttualmenteSelezionato() +
                                            (prenEffettuata.isBagaglio() ? "\nCon Bagaglio" : "") +
                                            (prenEffettuata.isAssicurazione() ? "\nCon Assicurazione" : ""),
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

        Component currentTopLevelCard = getCurrentVisibleCard(mainPanel);
        if (currentTopLevelCard != null && "MainContainer".equals(currentTopLevelCard.getName())) {
            btnLogin.setVisible(false);
        } else {
            btnLogin.setVisible(true);
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
            String gateDisplay = "N/A";
            if (v instanceof Volo_partenza) {
                Volo_partenza vp = (Volo_partenza) v;
                if (vp.gate != null && vp.gate.getGate() != 0) {
                    gateDisplay = String.valueOf(vp.gate.getGate());
                }
            }
            dati[i] = new Object[]{
                    v.getCodice(),
                    v.getCompagniaAerea(),
                    v.getOrigine(),
                    v.getDestinazione(),
                    v.getData() != null ? v.getData().format(DATE_FORMATTER) : "N/D",
                    v.getOrarioPrevisto() != null ? v.getOrarioPrevisto().format(TIME_FORMATTER) : "N/D",
                    mapStatoDelVoloToString(v.getStato()),
                    gateDisplay
            };
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
                Component oldMainOrPlaceholder = findComponentByName(mainPanel, "MainContainer_Placeholder_Initial");
                if(oldMainOrPlaceholder == null) oldMainOrPlaceholder = findComponentByName(mainPanel, "MainContainer_Placeholder_After_Logout");
                if(oldMainOrPlaceholder == null) oldMainOrPlaceholder = findComponentByName(mainPanel, "MainContainer");

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
        JButton btnAdminCercaPrenotazioni = new JButton("Gestisci Prenotazioni");
        JButton btnAdminCreaVolo = new JButton("Crea Nuovo Volo");
        JButton btnLogout = new JButton("Logout");

        JPanel contentPanel = new JPanel(new CardLayout());
        contentPanel.setName("MainContentPanel_Card");

        JPanel voliPanelInterno = createVoloPanel();
        voliPanelInterno.setName("VoloPanel_Interno");
        setButtonVisibilityInPanel(voliPanelInterno, "LoginButton_VoloPanel", false);

        contentPanel.add(voliPanelInterno, "Volo");
        contentPanel.add(createMiePrenotazioniPanel(), "MiePrenotazioni");
        contentPanel.add(createAdminPrenotazioniPanel(), "AdminCercaPrenotazioni");
        contentPanel.add(createAdminCreaVoloPanel(), "AdminCreaVolo");

        btnVolo.addActionListener(e -> {
            refreshVoloTables();
            switchContent(contentPanel, "Volo");
        });

        btnMiePrenotazioni.addActionListener(e -> {
            if (ruoloUtente.equals("Utente")) {
                aggiornaVistaMiePrenotazioni();
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
                if (adminCreaVoloTipo != null) {
                    adminCreaVoloTipo.setSelectedIndex(0);
                    handleTipoVoloChange();
                }
                if (adminCreaVoloStatoComboBox != null) {
                    adminCreaVoloStatoComboBox.setSelectedItem("In Orario");
                }
                if(adminCreaVoloCodice != null) adminCreaVoloCodice.setText("");
                if(adminCreaVoloCompagnia != null) adminCreaVoloCompagnia.setText("");
                if(adminCreaVoloData != null) adminCreaVoloData.setText("");
                if(adminCreaVoloOrario != null) adminCreaVoloOrario.setText("");
                if(adminCreaVoloGate != null) adminCreaVoloGate.setText("");

                switchContent(contentPanel, "AdminCreaVolo");
            } else {
                JOptionPane.showMessageDialog(this, "Funzione disponibile solo per gli amministratori.", "Accesso Negato", JOptionPane.WARNING_MESSAGE);
            }
        });


        btnLogout.addActionListener(e -> {
            ruoloUtente = "Utente";
            txtNomeUtente.setText("");
            txtPassword.setText("");

            Component oldMainContainer = findComponentByName(mainPanel, "MainContainer");
            if (oldMainContainer != null) {
                mainPanel.remove(oldMainContainer);
            }
            JPanel placeholderMain = new JPanel();
            placeholderMain.setName("MainContainer_Placeholder_After_Logout");
            mainPanel.add(placeholderMain, "Main");

            updateVoloPanelForRole();
            cardLayout.show(mainPanel, "Volo");
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
        ((CardLayout)contentPanel.getLayout()).show(contentPanel, "Volo");
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
                contentPanel.revalidate();
                contentPanel.repaint();
            }
        }
    }


    private JPanel createMiePrenotazioniPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setName("MiePrenotazioniPanel_Internal");
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String nomePasseggeroLoggato = (txtNomeUtente != null && "utente".equalsIgnoreCase(txtNomeUtente.getText()) && ruoloUtente.equals("Utente"))
                ? "utente"
                : "";

        JLabel titleLabel = new JLabel("Le Mie Prenotazioni" + (!nomePasseggeroLoggato.isEmpty() ? " (Passeggero: " + nomePasseggeroLoggato + ")" : ""), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        List<Prenotazione> prenotazioniUtente = new ArrayList<>();
        if (!nomePasseggeroLoggato.isEmpty()) {
            final String filterName = nomePasseggeroLoggato;
            prenotazioniUtente = prenotazioni.stream()
                    .filter(p -> p.getPasseggero() != null && filterName.equalsIgnoreCase(p.getPasseggero().getNome()))
                    .collect(Collectors.toList());
        }


        if (prenotazioniUtente.isEmpty()) {
            JLabel noPrenotazioniLabel = new JLabel(
                    (ruoloUtente.equals("Utente") && !nomePasseggeroLoggato.isEmpty()) ?
                            "Nessuna prenotazione trovata per " + nomePasseggeroLoggato + "." :
                            "Accedi come utente specifico per vedere le tue prenotazioni.", SwingConstants.CENTER);
            panel.add(noPrenotazioniLabel, BorderLayout.CENTER);
        } else {
            String[] colonne = {"SSN", "Nome", "Cognome", "Email", "Telefono",
                    "Cod. Volo", "Orig-Dest", "Data Volo", "Posto", "Bagaglio", "Assicurazione"};
            Object[][] dati = new Object[prenotazioniUtente.size()][colonne.length];

            for (int i = 0; i < prenotazioniUtente.size(); i++) {
                Prenotazione p = prenotazioniUtente.get(i);
                Passeggero pass = p.getPasseggero();
                Volo voloAssociato = findVoloByCodice(p.getCodiceVolo());

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

    private Volo findVoloByCodice(String codiceVolo) {
        if (codiceVolo == null) return null;
        return Stream.concat(voliInArrivo.stream(), voliInPartenza.stream())
                .filter(v -> codiceVolo.equals(v.getCodice()))
                .findFirst()
                .orElse(null);
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
        aggiornaVistaAdminPrenotazioni("", "");
        return panel;
    }

    private void aggiornaVistaAdminPrenotazioni(String nomeFilter, String cognomeFilter) {
        if (adminTableModel == null) return;
        adminTableModel.setRowCount(0);

        List<Prenotazione> filteredPrenotazioni = prenotazioni.stream()
                .filter(p -> p.getPasseggero() != null &&
                        (nomeFilter == null || nomeFilter.trim().isEmpty() || p.getPasseggero().getNome().toLowerCase().contains(nomeFilter.trim().toLowerCase())) &&
                        (cognomeFilter == null || cognomeFilter.trim().isEmpty() || p.getPasseggero().getCognome().toLowerCase().contains(cognomeFilter.trim().toLowerCase())))
                .collect(Collectors.toList());

        for (Prenotazione p : filteredPrenotazioni) {
            Passeggero pass = p.getPasseggero();
            Volo voloAssociato = findVoloByCodice(p.getCodiceVolo());

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
                    p.getCodiceVolo(),
                    compagnia,
                    origineDest,
                    dataVolo,
                    String.valueOf(pass.getPosto()),
                    p.isBagaglio() ? "Sì" : "No",
                    p.isAssicurazione() ? "Sì" : "No"
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

        gbc.gridx = 0; gbc.gridy = y; gbc.fill = GridBagConstraints.NONE; panel.add(new JLabel("Tipo Volo:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; adminCreaVoloTipo = new JComboBox<>(new String[]{"In Partenza", "In Arrivo"});
        adminCreaVoloTipo.addItemListener(e -> handleTipoVoloChange());
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

        handleTipoVoloChange();

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
                String dataStr = adminCreaVoloData.getText().trim();
                String orarioStr = adminCreaVoloOrario.getText().trim();
                String statoDisplay = (String) adminCreaVoloStatoComboBox.getSelectedItem();
                String gateStr = adminCreaVoloGate.getText().trim();
                String tipo = (String) adminCreaVoloTipo.getSelectedItem();

                if (codice.isEmpty() || compagnia.isEmpty() || origine.isEmpty() || destinazione.isEmpty() ||
                        dataStr.isEmpty() || orarioStr.isEmpty() || statoDisplay == null || statoDisplay.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Tutti i campi tranne Gate (per arrivi) sono obbligatori.", "Errore Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if ("In Partenza".equals(tipo) && gateStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Il Gate è obbligatorio per i voli in partenza.", "Errore Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }


                LocalDate data;
                try {
                    data = LocalDate.parse(dataStr, DATE_FORMATTER);
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this, "Formato data non valido. Usare YYYY-MM-DD.", "Errore Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                LocalTime orario;
                try {
                    if (orarioStr.matches("\\d{2}:\\d{2}")) {
                        orario = LocalTime.parse(orarioStr + ":00", TIME_FORMATTER);
                    } else if (orarioStr.matches("\\d{2}:\\d{2}:\\d{2}")){
                        orario = LocalTime.parse(orarioStr, TIME_FORMATTER);
                    } else {
                        throw new DateTimeParseException("Formato orario non valido", orarioStr, 0);
                    }
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this, "Formato orario non valido. Usare HH:MM o HH:MM:SS.", "Errore Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Stato_del_volo statoEnum = mapStringToStatoDelVolo(statoDisplay);

                Volo nuovoVolo;
                if ("In Partenza".equals(tipo)) {
                    if (!origine.equalsIgnoreCase("Napoli NAP")) {
                        JOptionPane.showMessageDialog(this, "Per i voli in partenza da questo aeroporto, l'origine è 'Napoli NAP'.", "Errore Input", JOptionPane.ERROR_MESSAGE);
                        adminCreaVoloOrigine.setText("Napoli NAP");
                        return;
                    }
                    Volo_partenza vp = new Volo_partenza(destinazione, compagnia);
                    vp.setCodice(codice);
                    vp.setData(data);
                    vp.setOrarioPrevisto(orario);
                    vp.setStato(statoEnum);
                    try {
                        vp.gate.setGate(Integer.parseInt(gateStr));
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Gate non valido. Inserire un numero.", "Errore Input", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    nuovoVolo = vp;
                    voliInPartenza.add(nuovoVolo);
                } else {
                    if (!destinazione.equalsIgnoreCase("Napoli NAP")) {
                        JOptionPane.showMessageDialog(this, "Per i voli in arrivo a questo aeroporto, la destinazione è 'Napoli NAP'.", "Errore Input", JOptionPane.ERROR_MESSAGE);
                        adminCreaVoloDestinazione.setText("Napoli NAP");
                        return;
                    }
                    Volo_arrivo va = new Volo_arrivo(origine, compagnia);
                    va.setCodice(codice);
                    va.setData(data);
                    va.setOrarioPrevisto(orario);
                    va.setStato(statoEnum);
                    nuovoVolo = va;
                    voliInArrivo.add(nuovoVolo);
                }

                JOptionPane.showMessageDialog(this, "Volo " + codice + " creato con successo!", "Volo Creato", JOptionPane.INFORMATION_MESSAGE);

                adminCreaVoloCodice.setText(""); adminCreaVoloCompagnia.setText("");
                adminCreaVoloData.setText(""); adminCreaVoloOrario.setText("");
                adminCreaVoloGate.setText("");
                adminCreaVoloTipo.setSelectedIndex(0);
                adminCreaVoloStatoComboBox.setSelectedItem("In Orario");


                refreshVoloTables();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Errore durante la creazione del volo: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        panel.add(btnCrea, gbc);
        return panel;
    }

    private void handleTipoVoloChange() {
        if (adminCreaVoloTipo == null || adminCreaVoloOrigine == null || adminCreaVoloDestinazione == null || adminCreaVoloGate == null) {
            return;
        }
        String tipoSelezionato = (String) adminCreaVoloTipo.getSelectedItem();
        JLabel gateLabel = null;
        for(Component comp : adminCreaVoloGate.getParent().getComponents()){
            if(comp instanceof JLabel && ((JLabel) comp).getText().equals("Gate:")){
                gateLabel = (JLabel) comp;
                break;
            }
        }

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

    private void switchContent(JPanel panel, String name) {
        CardLayout cl = (CardLayout) panel.getLayout();
        cl.show(panel, name);
    }

    private void aggiungiEsempiVoli() {
        try {
            Volo_arrivo arrivo1 = new Volo_arrivo("Roma FCO", "Alitalia");
            arrivo1.setCodice("AZ204");
            arrivo1.setData(LocalDate.parse("2025-05-21"));
            arrivo1.setOrarioPrevisto(LocalTime.parse("09:30:00"));
            arrivo1.setStato(Stato_del_volo.in_orario);
            voliInArrivo.add(arrivo1);

            Volo_arrivo arrivo2 = new Volo_arrivo("Milano BGY", "Ryanair");
            arrivo2.setCodice("FR1822");
            arrivo2.setData(LocalDate.parse("2025-05-21"));
            arrivo2.setOrarioPrevisto(LocalTime.parse("10:15:00"));
            arrivo2.setStato(Stato_del_volo.in_orario);
            voliInArrivo.add(arrivo2);

            Volo_arrivo arrivo3 = new Volo_arrivo("Venezia VCE", "EasyJet");
            arrivo3.setCodice("U22851");
            arrivo3.setData(LocalDate.parse("2025-05-21"));
            arrivo3.setOrarioPrevisto(LocalTime.parse("11:00:00"));
            arrivo3.setStato(Stato_del_volo.atterrato);
            voliInArrivo.add(arrivo3);

            Volo_arrivo arrivo4 = new Volo_arrivo("Monaco MUC", "Lufthansa");
            arrivo4.setCodice("LH1778");
            arrivo4.setData(LocalDate.parse("2025-05-21"));
            arrivo4.setOrarioPrevisto(LocalTime.parse("12:30:00"));
            arrivo4.setStato(Stato_del_volo.in_ritardo);
            arrivo4.setRitardo(30);
            voliInArrivo.add(arrivo4);


            Volo_partenza partenza1 = new Volo_partenza("Roma FCO", "Alitalia");
            partenza1.setCodice("AZ205");
            partenza1.setData(LocalDate.parse("2025-05-21"));
            partenza1.setOrarioPrevisto(LocalTime.parse("10:00:00"));
            partenza1.setStato(Stato_del_volo.in_orario);
            partenza1.gate.setGate(1);
            voliInPartenza.add(partenza1);

            Volo_partenza partenza2 = new Volo_partenza("Milano BGY", "Ryanair");
            partenza2.setCodice("FR1823");
            partenza2.setData(LocalDate.parse("2025-05-21"));
            partenza2.setOrarioPrevisto(LocalTime.parse("11:05:00"));
            partenza2.setStato(Stato_del_volo.cancellato);
            partenza2.gate.setGate(2);
            voliInPartenza.add(partenza2);

            Volo_partenza partenza3 = new Volo_partenza("Londra LHR", "British Airways");
            partenza3.setCodice("BA2609");
            partenza3.setData(LocalDate.parse("2025-05-22"));
            partenza3.setOrarioPrevisto(LocalTime.parse("15:00:00"));
            partenza3.setStato(Stato_del_volo.rinviato);
            partenza3.gate.setGate(3);
            voliInPartenza.add(partenza3);

        } catch (DateTimeParseException e) {
            System.err.println("Error parsing date/time in example flights: " + e.getMessage());
        }
    }

    private void aggiungiEsempiPrenotazioni() {
        if (voliInPartenza.isEmpty() || voliInArrivo.isEmpty()) {
            System.err.println("Numero insufficiente di voli di esempio per creare prenotazioni.");
            return;
        }

        Prenotazione p1 = new Prenotazione();
        p1.getPasseggero().setNome("Mario");
        p1.getPasseggero().setCognome("Rossi");
        p1.getPasseggero().setSsn("RSSMRA80A01H501A");
        p1.getPasseggero().setEmail("mario.rossi@example.com");
        p1.getPasseggero().setTelefono("3331234567");
        p1.setCodiceVolo(voliInPartenza.get(0).getCodice());
        p1.getPasseggero().setPosto("1A");
        p1.updateAssicurazione();
        p1.updateBagaglio();
        prenotazioni.add(p1);

        Prenotazione p2 = new Prenotazione();
        p2.getPasseggero().setNome("utente");
        p2.getPasseggero().setCognome("Test");
        p2.getPasseggero().setSsn("TSTUSR90C02H501C");
        p2.getPasseggero().setEmail("utente.test@example.com");
        p2.getPasseggero().setTelefono("3471122334");
        if (!voliInArrivo.isEmpty()) {
            p2.setCodiceVolo(voliInArrivo.get(0).getCodice());
        } else if (!voliInPartenza.isEmpty()) {
            p2.setCodiceVolo(voliInPartenza.get(0).getCodice());
        } else { return; }
        p2.getPasseggero().setPosto("4D");
        p2.updateAssicurazione();
        prenotazioni.add(p2);

        if (voliInPartenza.size() > 1) {
            Volo voloPerP3 = null;
            for(Volo v : voliInPartenza){
                if(v.getStato() == Stato_del_volo.in_orario || v.getStato() == Stato_del_volo.rinviato || v.getStato() == Stato_del_volo.in_ritardo){
                    voloPerP3 = v;
                    break;
                }
            }
            if(voloPerP3 != null){
                Prenotazione p3 = new Prenotazione();
                p3.getPasseggero().setNome("Laura");
                p3.getPasseggero().setCognome("Bianchi");
                p3.getPasseggero().setSsn("BNCLRA85M41H501B");
                p3.getPasseggero().setEmail("laura.bianchi@example.com");
                p3.getPasseggero().setTelefono("3387654321");
                p3.setCodiceVolo(voloPerP3.getCodice());
                p3.getPasseggero().setPosto("2C");
                p3.updateBagaglio();
                prenotazioni.add(p3);
            }
        }
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
            if(visible && button.getParent()!=null && !button.getParent().isVisible()){
                button.getParent().setVisible(true);
            }
        } else if (button != null) {
            button.setVisible(visible);
        }
    }

    class PrenotazioneDialog extends JDialog {
        private Volo voloDialogo;
        private Prenotazione prenotazioneEffettuata;
        private boolean confermata = false;

        private JTextField txtNome, txtCognome, txtSSN, txtEmail, txtTelefono;
        private JCheckBox chkBagaglio, chkAssicurazione;
        private JLabel lblPostoSelezionatoDisplay;
        private String postoAttualmenteSelezionato = null;
        private Map<String, JButton> bottoniSediliMap = new HashMap<>();


        public PrenotazioneDialog(Frame owner, Volo volo) {
            super(owner, "Dettagli Prenotazione Volo: " + volo.getCodice(), true);
            this.voloDialogo = volo;
            setSize(Math.min(owner.getWidth() - 50, 600), Math.min(owner.getHeight() - 50, 530));
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

            List<Integer> postiOccupatiInt = new ArrayList<>();
            List<String> postiOccupatiString = new ArrayList<>();
            for (Prenotazione pExisting : MainFrame.this.prenotazioni) {
                if (pExisting.getCodiceVolo().equals(this.voloDialogo.getCodice())) {
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

                    if (postiOccupatiString.contains(nomePosto)) {
                        btnPosto.setBackground(Color.RED);
                        btnPosto.setEnabled(false);
                    } else {
                        btnPosto.setBackground(Color.GREEN.brighter());
                        btnPosto.setEnabled(true);
                        btnPosto.addActionListener(ev -> selezionaPosto(nomePosto));
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
                if(vecchioBottoneSelezionato.isEnabled()) {
                    vecchioBottoneSelezionato.setBackground(Color.GREEN.brighter());
                }
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
            String email = txtEmail.getText().trim();
            String telefono = txtTelefono.getText().trim();

            if (nome.isEmpty() || cognome.isEmpty() || ssn.isEmpty() || email.isEmpty() || telefono.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tutti i campi dei dati personali (incluso SSN) sono obbligatori.", "Dati Mancanti", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (ssn.length() < 5 ) {
                JOptionPane.showMessageDialog(this, "Il formato SSN/Codice Fiscale non è valido.", "Errore Dati", JOptionPane.ERROR_MESSAGE);
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

            this.prenotazioneEffettuata = new Prenotazione();
            this.prenotazioneEffettuata.getPasseggero().setNome(nome);
            this.prenotazioneEffettuata.getPasseggero().setCognome(cognome);
            this.prenotazioneEffettuata.getPasseggero().setSsn(ssn);
            this.prenotazioneEffettuata.getPasseggero().setEmail(email);
            this.prenotazioneEffettuata.getPasseggero().setTelefono(telefono);
            this.prenotazioneEffettuata.setCodiceVolo(this.voloDialogo.getCodice());

            String seatNumberInt = "";
            if (postoAttualmenteSelezionato != null && !postoAttualmenteSelezionato.isEmpty()) {
                try {
                    seatNumberInt = (postoAttualmenteSelezionato.replaceAll("[^0-9]", ""));
                } catch (NumberFormatException ex) {
                    System.err.println("Could not parse seat number from string: " + postoAttualmenteSelezionato);
                }
            }
            this.prenotazioneEffettuata.getPasseggero().setPosto(seatNumberInt);


            if (bagaglio) this.prenotazioneEffettuata.updateBagaglio();
            if (assicurazione) this.prenotazioneEffettuata.updateAssicurazione();

            this.confermata = true;
            dispose();
        }

        public boolean isPrenotazioneConfermata() { return confermata; }
        public Prenotazione getPrenotazione() { return prenotazioneEffettuata; }
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
        }
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}