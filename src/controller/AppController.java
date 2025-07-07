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

public class AppController {

    private Utente utenteCorrente;
    private AppGUI gui;

    private final VoloDAO voloDAO;
    private final PrenotazioneDAO prenotazioneDAO;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;

    public AppController() {
        this.voloDAO = new VoloDAOImpl();
        this.prenotazioneDAO = new PrenotazioneDAOImpl();

    }

    public void setGui(AppGUI gui) {
        this.gui = gui;
    }

    public List<Volo> getVoliInArrivo() {
        return voloDAO.getVoliInArrivo();
    }

    public List<Volo> getVoliInPartenza() {
        return voloDAO.getVoliInPartenza();
    }

    public Volo findVoloByCodice(String codiceVolo) {
        return voloDAO.findVoloByCodice(codiceVolo);
    }

    public List<Prenotazione> getPrenotazioniFiltrateAdmin(String nomeFilter, String cognomeFilter) {
        return prenotazioneDAO.getPrenotazioniFiltrateAdmin(nomeFilter, cognomeFilter);
    }

    public List<String> getPostiOccupatiPerVolo(String codiceVolo) {
        return prenotazioneDAO.getPostiOccupatiPerVolo(codiceVolo);
    }

    public List<Prenotazione> getPrenotazioniPerUtenteLoggato() {
        if (utenteCorrente != null && "utente".equalsIgnoreCase(utenteCorrente.getLogin())) {
            return prenotazioneDAO.getPrenotazioniPerUtente(utenteCorrente.getLogin());
        }
        return new ArrayList<>();
    }

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

    public void logout() {
        utenteCorrente = null;
    }

    public String getRuoloUtente() {
        if (utenteCorrente instanceof Amministratore_sistema) {
            return "Amministratore";
        } else if (utenteCorrente instanceof Utente_generico) {
            return "Utente";
        }
        return "Nessuno";
    }

    public String getNomeUtenteLoggato() {
        if (utenteCorrente != null && "utente".equalsIgnoreCase(utenteCorrente.getLogin())) {
            return utenteCorrente.getLogin();
        }
        return "";
    }

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