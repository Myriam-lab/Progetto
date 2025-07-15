package controller;

import model.*;
import gui.AppGUI;
import dao.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Il Controller dell'applicazione, segue il pattern MVC.
 * Gestisce la logica di business e funge da intermediario tra la View (AppGUI) e il Model.
 * Orchesta le operazioni richieste dall'utente, interagendo con lo strato DAO per la persistenza dei dati.
 */
public class AppController {

    private Utente utenteCorrente;
    private AppGUI gui;

    private final VoloDAO voloDAO;
    private final PrenotazioneDAO prenotazioneDAO;

    /**
     * Formatter per le date in formato ISO (YYYY-MM-DD).
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Formatter per gli orari in formato ISO (HH:MM:SS).
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;

    /**
     * Costruttore dell'AppController.
     * Inizializza gli oggetti DAO per l'accesso ai dati di voli e prenotazioni.
     */
    public AppController() {
        this.voloDAO = new VoloDAOImpl();
        this.prenotazioneDAO = new PrenotazioneDAOImpl();

    }

    /**
     * Imposta l'istanza della GUI (View) per permettere al controller di comunicare con essa.
     * @param gui l'istanza di AppGUI.
     */
    public void setGui(AppGUI gui) {
        this.gui = gui;
    }

    /**
     * Recupera la lista di tutti i voli in arrivo.
     * @return una lista di oggetti Volo.
     */
    public List<Volo> getVoliInArrivo() {
        return voloDAO.getVoliInArrivo();
    }

    /**
     * Recupera la lista di tutti i voli in partenza.
     * @return una lista di oggetti Volo.
     */
    public List<Volo> getVoliInPartenza() {
        return voloDAO.getVoliInPartenza();
    }

    /**
     * Trova un volo specifico tramite il suo codice.
     * @param codiceVolo il codice del volo da cercare.
     * @return l'oggetto Volo corrispondente, o null se non trovato.
     */
    public Volo findVoloByCodice(String codiceVolo) {
        return voloDAO.findVoloByCodice(codiceVolo);
    }

    /**
     * Recupera una lista di prenotazioni filtrate per nome e cognome del passeggero (funzione per admin).
     * @param nomeFilter il nome (o parte di esso) da usare come filtro.
     * @param cognomeFilter il cognome (o parte di esso) da usare come filtro.
     * @return una lista di oggetti Prenotazione.
     */
    public List<Prenotazione> getPrenotazioniFiltrateAdmin(String nomeFilter, String cognomeFilter) {
        return prenotazioneDAO.getPrenotazioniFiltrateAdmin(nomeFilter, cognomeFilter);
    }

    /**
     * Recupera la lista dei posti già occupati per un determinato volo.
     * @param codiceVolo il codice del volo.
     * @return una lista di stringhe rappresentanti i posti occupati.
     */
    public List<String> getPostiOccupatiPerVolo(String codiceVolo) {
        return prenotazioneDAO.getPostiOccupatiPerVolo(codiceVolo);
    }

    /**
     * Recupera le prenotazioni effettuate dall'utente attualmente loggato.
     * @return una lista di oggetti Prenotazione, o una lista vuota se nessun utente è loggato.
     */
    public List<Prenotazione> getPrenotazioniPerUtenteLoggato() {
        if (utenteCorrente != null && "utente".equalsIgnoreCase(utenteCorrente.getLogin())) {
            return prenotazioneDAO.getPrenotazioniPerUtente(utenteCorrente.getLogin());
        }
        return new ArrayList<>();
    }

    /**
     * Crea un nuovo volo nel sistema.
     * Effettua la validazione dei dati di input e mostra messaggi di errore sulla GUI in caso di problemi.
     * @param codice Codice del nuovo volo.
     * @param compagnia Compagnia aerea.
     * @param tipoVolo "In Partenza" o "In Arrivo".
     * @param origine Luogo di origine del volo.
     * @param destinazione Luogo di destinazione del volo.
     * @param dataStr Data del volo in formato stringa (YYYY-MM-DD).
     * @param orarioStr Orario del volo in formato stringa (HH:MM).
     * @param statoDisplay Stato iniziale del volo.
     * @param gateStr Numero del gate (per i voli in partenza).
     * @return {@code true} se il volo è stato creato con successo, {@code false} altrimenti.
     */
    public boolean creaNuovoVolo(String codice, String compagnia, String tipoVolo, String origine, String destinazione, String dataStr, String orarioStr, String statoDisplay, String gateStr) {
        if (codice.isEmpty() || compagnia.isEmpty() || origine.isEmpty() || destinazione.isEmpty() || dataStr.isEmpty() || orarioStr.isEmpty() || statoDisplay == null) {
            gui.mostraMessaggioErrore("Tutti i campi obbligatori devono essere compilati.", "Errore Input");
            return false;
        }

        LocalDate data;
        LocalTime orario;
        try {
            data = LocalDate.parse(dataStr, DATE_FORMATTER);
            if (orarioStr.matches("\\d{2}:\\d{2}")) {
                orario = LocalTime.parse(orarioStr + ":00", TIME_FORMATTER);
            } else {
                orario = LocalTime.parse(orarioStr, TIME_FORMATTER);
            }
        } catch (DateTimeParseException e) {
            gui.mostraMessaggioErrore("Formato data (YYYY-MM-DD) o orario (HH:MM) non valido.", "Errore Parsing");
            return false;
        }

        Volo nuovoVolo;
        if ("In Partenza".equals(tipoVolo)) {
            Volo_partenza vp = new Volo_partenza(destinazione, compagnia);
            try {
                vp.gate.setGate(Integer.parseInt(gateStr));
            } catch (NumberFormatException e) {
                gui.mostraMessaggioErrore("Il Gate deve essere un numero.", "Errore Input");
                return false;
            }
            nuovoVolo = vp;
        } else {
            nuovoVolo = new Volo_arrivo(origine, compagnia);
        }

        nuovoVolo.setCodice(codice);
        nuovoVolo.setOrigine(origine);
        nuovoVolo.setDestinazione(destinazione);
        nuovoVolo.setData(data);
        nuovoVolo.setOrarioPrevisto(orario);
        nuovoVolo.setStato(mapStringToStatoDelVolo(statoDisplay));

        return voloDAO.creaNuovoVolo(nuovoVolo);
    }

    /**
     * Crea una nuova prenotazione per un volo.
     * @param voloSelezionato L'oggetto Volo per cui si sta prenotando.
     * @param nome Nome del passeggero.
     * @param cognome Cognome del passeggero.
     * @param ssn Codice fiscale del passeggero.
     * @param email Email del passeggero.
     * @param telefono Telefono del passeggero.
     * @param postoSelezionatoStr Posto scelto dal passeggero.
     * @param bagaglio {@code true} se è stato aggiunto il bagaglio.
     * @param assicurazione {@code true} se è stata aggiunta l'assicurazione.
     * @return {@code true} se la prenotazione è stata creata con successo, {@code false} altrimenti.
     */
    public boolean creaNuovaPrenotazione(Volo voloSelezionato, String nome, String cognome, String ssn, String email, String telefono, String postoSelezionatoStr, boolean bagaglio, boolean assicurazione) {
        Passeggero p = new Passeggero();
        p.setNome(nome);
        p.setCognome(cognome);
        p.setSsn(ssn);
        p.setEmail(email);
        p.setTelefono(telefono);
        p.setPosto(postoSelezionatoStr);

        Prenotazione nuovaPrenotazione = new Prenotazione();
        nuovaPrenotazione.setPasseggero(p);
        nuovaPrenotazione.setCodiceVolo(voloSelezionato.getCodice());
        if (bagaglio) nuovaPrenotazione.updateBagaglio();
        if (assicurazione) nuovaPrenotazione.updateAssicurazione();

        return prenotazioneDAO.creaPrenotazione(nuovaPrenotazione);
    }

    /**
     * Effettua il login dell'utente.
     * @param username l'username inserito.
     * @param password la password inserita.
     * @return {@code true} se le credenziali sono corrette, {@code false} altrimenti.
     */
    public boolean login(String username, String password) {
        if ("admin".equals(username) && "admin".equals(password)) {
            utenteCorrente = new Amministratore_sistema(username, password);
            return true;
        } else if ("utente".equals(username) && "utente".equals(password)) {
            utenteCorrente = new Utente_generico(username, password);
            return true;
        }
        utenteCorrente = null;
        return false;
    }

    /**
     * Effettua il logout, resettando l'utente corrente.
     */
    public void logout() {
        utenteCorrente = null;
    }

    /**
     * Restituisce il ruolo dell'utente attualmente loggato.
     * @return "Amministratore", "Utente" o "Nessuno".
     */
    public String getRuoloUtente() {
        if (utenteCorrente instanceof Amministratore_sistema) {
            return "Amministratore";
        } else if (utenteCorrente instanceof Utente_generico) {
            return "Utente";
        }
        return "Nessuno";
    }

    /**
     * Restituisce il nome dell'utente loggato.
     * @return il nome dell'utente, o una stringa vuota se non c'è un utente loggato.
     */
    public String getNomeUtenteLoggato() {
        if (utenteCorrente != null && "utente".equalsIgnoreCase(utenteCorrente.getLogin())) {
            return utenteCorrente.getLogin();
        }
        return "";
    }

    /**
     * Converte una stringa rappresentante lo stato di un volo nel corrispondente valore dell'enum Stato_del_volo.
     * @param statoStr la stringa da convertire.
     * @return l'oggetto Stato_del_volo corrispondente.
     */
    public Stato_del_volo mapStringToStatoDelVolo(String statoStr) {
        if (statoStr == null) return Stato_del_volo.in_orario;
        return switch (statoStr) {
            case "In Ritardo" -> Stato_del_volo.in_ritardo;
            case "Cancellato" -> Stato_del_volo.cancellato;
            case "Atterrato" -> Stato_del_volo.atterrato;
            case "Rinviato" -> Stato_del_volo.rinviato;
            default -> Stato_del_volo.in_orario;
        };
    }

    /**
     * Converte un valore dell'enum Stato_del_volo nella sua rappresentazione testuale per la GUI.
     * @param stato l'enum da convertire.
     * @return la stringa corrispondente.
     */
    public String mapStatoDelVoloToString(Stato_del_volo stato) {
        if (stato == null) return "N/D";
        return switch (stato) {
            case in_orario -> "In Orario";
            case in_ritardo -> "In Ritardo";
            case cancellato -> "Cancellato";
            case atterrato -> "Atterrato";
            case rinviato -> "Rinviato";
            default -> "N/D";
        };
    }
}