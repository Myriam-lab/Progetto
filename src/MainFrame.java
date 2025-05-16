import javax.swing.*;
import java.awt.*;

/**
 * MainFrame è la finestra principale dell'applicazione.
 * Usa un CardLayout per passare dal pannello di login alla sezione principale.
 */
public class MainFrame extends JFrame {

    // CardLayout consente di cambiare "scheda" nella finestra, es: da Login a Main
    private CardLayout cardLayout;

    // Pannello contenitore che gestisce tutte le schermate (login, main)
    private JPanel mainPanel;

    // Campi per l'inserimento dell'utente e password nel login
    private JTextField txtNomeUtente;
    private JPasswordField txtPassword;

    // Campi per il pannello delle info personali
    private JTextField txtNomePasseggero, txtCognome, txtEmail, txtSSN, txtPosto, txtTelefono;

    /**
     * Costruttore della finestra principale: inizializza GUI, layout e pannelli.
     */
    public MainFrame() {
        setTitle("Gestione Volo"); // Titolo finestra
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Chiudi applicazione al click su "X"
        setSize(600, 600); // Dimensione finestra
        setLocationRelativeTo(null); // Centra la finestra sullo schermo

        // Inizializza il layout a schede
        cardLayout = new CardLayout();

        // Pannello contenitore principale
        mainPanel = new JPanel(cardLayout);

        // Aggiunge le due schermate principali
        mainPanel.add(createLoginPanel(), "Login");   // Schermata di login
        mainPanel.add(createMainContentPanel(), "Main"); // Area dopo il login

        add(mainPanel); // Aggiunge il pannello principale alla finestra
        cardLayout.show(mainPanel, "Login"); // Mostra schermata di login all'avvio
    }

    /**
     * Crea il pannello per il login dell'utente.
     * @return JPanel con i campi utente, password e pulsante di accesso.
     */
    private JPanel createLoginPanel() {
        // Layout a griglia 4 righe, 2 colonne, spaziatura 10px
        JPanel loginPanel = new JPanel(new GridLayout(4, 2, 10, 10));

        // Crea i campi di input
        txtNomeUtente = new JTextField();
        txtPassword = new JPasswordField();

        // Riduce l'altezza dei campi con dimensione preferita
        Dimension smallField = new Dimension(50, 25);
        txtNomeUtente.setPreferredSize(smallField);
        txtPassword.setPreferredSize(smallField);

        // Etichette + campi
        loginPanel.add(new JLabel("Nome Utente:"));
        loginPanel.add(txtNomeUtente);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(txtPassword);

        // Pulsante per accedere all'app
        JButton btnAccedi = new JButton("Accedi");

        // Associa azione al click del pulsante
        btnAccedi.addActionListener(e -> verificaCredenziali());

        loginPanel.add(new JLabel()); // Spazio vuoto per il layout
        loginPanel.add(btnAccedi); // Aggiunge il bottone di accesso

        return loginPanel;
    }

    /**
     * Crea il pannello principale dell'app, che appare dopo il login.
     * Contiene la barra di navigazione e i contenuti dinamici.
     * @return JPanel principale
     */
    private JPanel createMainContentPanel() {
        JPanel container = new JPanel(new BorderLayout()); // Layout con Nord (barra) e Centro (contenuto)

        // Barra in alto con i pulsanti di navigazione
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Crea i pulsanti
        JButton btnInfo = new JButton("Info Personali");
        JButton btnVolo = new JButton("Volo");
        JButton btnPrenotazione = new JButton("Prenotazione");
        JButton btnLogout = new JButton("Logout");

        // Pannello centrale che cambia in base al pulsante cliccato
        JPanel contentPanel = new JPanel(new CardLayout());

        // Aggiunge i pannelli dinamici
        contentPanel.add(createInfoPanel(), "Info");
        contentPanel.add(createVoloPanel(), "Volo");
        contentPanel.add(createPrenotazionePanel(), "Prenotazione");

        // Azioni associate ai pulsanti
        btnInfo.addActionListener(e -> switchContent(contentPanel, "Info"));
        btnVolo.addActionListener(e -> switchContent(contentPanel, "Volo"));
        btnPrenotazione.addActionListener(e -> switchContent(contentPanel, "Prenotazione"));

        btnLogout.addActionListener(e -> {
            // Messaggio e ritorno al login
            JOptionPane.showMessageDialog(this, "Logout effettuato.");
            cardLayout.show(mainPanel, "Login");
        });

        // Aggiunge i pulsanti alla barra
        navBar.add(btnInfo);
        navBar.add(btnVolo);
        navBar.add(btnPrenotazione);
        navBar.add(btnLogout);

        // Aggiunge la barra e l’area centrale al contenitore
        container.add(navBar, BorderLayout.NORTH);
        container.add(contentPanel, BorderLayout.CENTER);

        return container;
    }

    /**
     * Crea il pannello con le informazioni personali del passeggero.
     * @return JPanel con campi info personali
     */
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));

        // Campi per le info personali
        txtNomePasseggero = new JTextField();
        txtCognome = new JTextField();
        txtEmail = new JTextField();
        txtSSN = new JTextField();
        txtPosto = new JTextField();
        txtTelefono = new JTextField();

        // Etichette + campi
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

    /**
     * Crea il pannello con i dati del volo.
     * @return JPanel con campi del volo
     */
    private JPanel createVoloPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));

        // Campi del volo
        JTextField txtOrigine = new JTextField();
        JTextField txtDestinazione = new JTextField();
        JTextField txtCodiceVolo = new JTextField();

        panel.add(new JLabel("Origine:"));
        panel.add(txtOrigine);
        panel.add(new JLabel("Destinazione:"));
        panel.add(txtDestinazione);
        panel.add(new JLabel("Codice Volo:"));
        panel.add(txtCodiceVolo);

        return panel;
    }

    /**
     * Crea il pannello con le info di prenotazione.
     * @return JPanel con PNR, biglietto e opzioni
     */
    private JPanel createPrenotazionePanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));

        // Campi e opzioni prenotazione
        JTextField txtPNR = new JTextField();
        JTextField txtBiglietto = new JTextField();
        JCheckBox chkBagaglio = new JCheckBox("Bagaglio");
        JCheckBox chkAssicurazione = new JCheckBox("Assicurazione");

        panel.add(new JLabel("PNR:"));
        panel.add(txtPNR);
        panel.add(new JLabel("Numero Biglietto:"));
        panel.add(txtBiglietto);
        panel.add(chkBagaglio); // Checkbox senza etichetta a destra
        panel.add(new JLabel());
        panel.add(chkAssicurazione);
        panel.add(new JLabel());

        return panel;
    }

    /**
     * Mostra un pannello interno specificato all'interno del contenuto centrale.
     * @param contentPanel Il pannello con layout a schede
     * @param name Il nome della scheda da mostrare
     */
    private void switchContent(JPanel contentPanel, String name) {
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, name);
    }

    /**
     * Simula la verifica delle credenziali: accetta qualunque input.
     */
    private void verificaCredenziali() {
        // Ottiene nome utente e password inseriti
        String utente = txtNomeUtente.getText();
        String password = new String(txtPassword.getPassword());

        // In questa versione si accetta sempre l'accesso
        JOptionPane.showMessageDialog(this, "Login effettuato con successo!");
        cardLayout.show(mainPanel, "Main");
    }

    /**
     * Metodo principale: avvia l'interfaccia grafica.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true); // Mostra la finestra
        });
    }
}
