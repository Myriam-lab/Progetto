package controller;

import model.*;
import gui.AppGUI;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AppController {

    private List<Volo> voliInArrivo = new ArrayList<>();
    private List<Volo> voliInPartenza = new ArrayList<>();
    private List<Prenotazione> prenotazioni = new ArrayList<>();
    private Utente utenteCorrente;
    private AppGUI gui;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;


    public AppController() {
        aggiungiEsempiVoli();
        aggiungiEsempiPrenotazioni();
    }

    public void setGui(AppGUI gui) {
        this.gui = gui;
    }

    public List<Volo> getVoliInArrivo() {
        return voliInArrivo;
    }

    public List<Volo> getVoliInPartenza() {
        return voliInPartenza;
    }

    public List<Prenotazione> getAllPrenotazioni() {
        return prenotazioni;
    }

    public String getRuoloUtente() {
        if (utenteCorrente instanceof Amministratore_sistema) {
            return "Amministratore";
        } else if (utenteCorrente instanceof Utente_generico) {
            return "Utente";
        }
        return "Nessuno";
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


    public boolean creaNuovoVolo(String codice, String compagnia, String tipoVolo, String origine, String destinazione,
                                 String dataStr, String orarioStr, String statoDisplay, String gateStr) {
        if (codice.isEmpty() || compagnia.isEmpty() || origine.isEmpty() || destinazione.isEmpty() ||
                dataStr.isEmpty() || orarioStr.isEmpty() || statoDisplay == null || statoDisplay.isEmpty()) {
            gui.mostraMessaggioErrore("Tutti i campi tranne Gate (per arrivi) sono obbligatori.", "Errore Input");
            return false;
        }
        if ("In Partenza".equals(tipoVolo) && gateStr.isEmpty()) {
            gui.mostraMessaggioErrore("Il Gate è obbligatorio per i voli in partenza.", "Errore Input");
            return false;
        }

        LocalDate data;
        try {
            data = LocalDate.parse(dataStr, DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            gui.mostraMessaggioErrore("Formato data non valido. Usare YYYY-MM-DD.", "Errore Input");
            return false;
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
            gui.mostraMessaggioErrore("Formato orario non valido. Usare HH:MM o HH:MM:SS.", "Errore Input");
            return false;
        }
        Stato_del_volo statoEnum = mapStringToStatoDelVolo(statoDisplay);

        Volo nuovoVolo;
        if (utenteCorrente instanceof Amministratore_sistema) {
            if ("In Partenza".equals(tipoVolo)) {
                if (!origine.equalsIgnoreCase("Napoli NAP")) {
                    gui.mostraMessaggioErrore("Per i voli in partenza da questo aeroporto, l'origine è 'Napoli NAP'.", "Errore Input");
                    return false;
                }
                Volo_partenza vp = new Volo_partenza(destinazione, compagnia);
                vp.setCodice(codice);
                vp.setData(data);
                vp.setOrarioPrevisto(orario);
                vp.setStato(statoEnum);
                try {
                    vp.gate.setGate(Integer.parseInt(gateStr));
                } catch (NumberFormatException ex) {
                    gui.mostraMessaggioErrore("Gate non valido. Inserire un numero.", "Errore Input");
                    return false;
                }
                nuovoVolo = vp;
                voliInPartenza.add(nuovoVolo);
            } else {
                if (!destinazione.equalsIgnoreCase("Napoli NAP")) {
                    gui.mostraMessaggioErrore("Per i voli in arrivo a questo aeroporto, la destinazione è 'Napoli NAP'.", "Errore Input");
                    return false;
                }
                Volo_arrivo va = new Volo_arrivo(origine, compagnia);
                va.setCodice(codice);
                va.setData(data);
                va.setOrarioPrevisto(orario);
                va.setStato(statoEnum);
                nuovoVolo = va;
                voliInArrivo.add(nuovoVolo);
            }
            gui.mostraMessaggioInformativo("Volo " + codice + " creato con successo!", "Volo Creato");
            return true;
        } else {
            gui.mostraMessaggioErrore("Azione non permessa.", "Errore Autorizzazione");
            return false;
        }
    }

    public boolean creaNuovaPrenotazione(Volo voloSelezionato, String nome, String cognome, String ssn,
                                         String email, String telefono, String postoSelezionatoStr,
                                         boolean bagaglio, boolean assicurazione) {

        if (nome.isEmpty() || cognome.isEmpty() || ssn.isEmpty() || email.isEmpty() || telefono.isEmpty()) {
            gui.mostraMessaggioErroreDialogo("Tutti i campi dei dati personali (incluso SSN) sono obbligatori.", "Dati Mancanti");
            return false;
        }
        if (ssn.length() < 5 ) {
            gui.mostraMessaggioErroreDialogo("Il formato SSN/Codice Fiscale non è valido.", "Errore Dati");
            return false;
        }
        if (!email.contains("@") || !email.contains(".")) {
            gui.mostraMessaggioErroreDialogo("Formato email non valido.", "Errore Dati");
            return false;
        }
        if (postoSelezionatoStr == null || postoSelezionatoStr.isEmpty()) {
            gui.mostraMessaggioErroreDialogo("Devi selezionare un posto.", "Posto Mancante");
            return false;
        }

        if (utenteCorrente instanceof Utente_generico) {
            Prenotazione nuovaPrenotazione = new Prenotazione();
            Passeggero p = new Passeggero();
            p.setNome(nome);
            p.setCognome(cognome);
            p.setSsn(ssn);
            p.setEmail(email);
            p.setTelefono(telefono);
            p.setPosto(postoSelezionatoStr);

            nuovaPrenotazione.setPasseggero(p);
            nuovaPrenotazione.setCodiceVolo(voloSelezionato.getCodice());

            if (bagaglio) nuovaPrenotazione.updateBagaglio();
            if (assicurazione) nuovaPrenotazione.updateAssicurazione();

            prenotazioni.add(nuovaPrenotazione);
            return true;
        } else {
            gui.mostraMessaggioErroreDialogo("Azione non permessa o utente non loggato.", "Errore Autorizzazione");
            return false;
        }
    }

    public List<Prenotazione> getPrenotazioniPerUtenteLoggato() {
        List<Prenotazione> prenotazioniUtente = new ArrayList<>();
        if (utenteCorrente != null && "utente".equalsIgnoreCase(utenteCorrente.getLogin())) {
            final String nomePasseggeroLoggato = utenteCorrente.getLogin();
            prenotazioniUtente = prenotazioni.stream()
                    .filter(p -> p.getPasseggero() != null && nomePasseggeroLoggato.equalsIgnoreCase(p.getPasseggero().getNome()))
                    .collect(Collectors.toList());
        }
        return prenotazioniUtente;
    }

    public List<Prenotazione> getPrenotazioniFiltrateAdmin(String nomeFilter, String cognomeFilter) {
        return prenotazioni.stream()
                .filter(p -> p.getPasseggero() != null &&
                        (nomeFilter == null || nomeFilter.trim().isEmpty() || p.getPasseggero().getNome().toLowerCase().contains(nomeFilter.trim().toLowerCase())) &&
                        (cognomeFilter == null || cognomeFilter.trim().isEmpty() || p.getPasseggero().getCognome().toLowerCase().contains(cognomeFilter.trim().toLowerCase())))
                .collect(Collectors.toList());
    }


    public Volo findVoloByCodice(String codiceVolo) {
        if (codiceVolo == null) return null;
        return Stream.concat(voliInArrivo.stream(), voliInPartenza.stream())
                .filter(v -> codiceVolo.equals(v.getCodice()))
                .findFirst()
                .orElse(null);
    }

    public Stato_del_volo mapStringToStatoDelVolo(String statoStr) {
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
    public String mapStatoDelVoloToString(Stato_del_volo stato) {
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

            Volo_arrivo arrivo5 = new Volo_arrivo("Parigi CDG", "Air France");
            arrivo5.setCodice("AF1320");
            arrivo5.setData(LocalDate.parse("2025-05-21"));
            arrivo5.setOrarioPrevisto(LocalTime.parse("14:00:00"));
            arrivo5.setStato(Stato_del_volo.in_orario);
            voliInArrivo.add(arrivo5);

            Volo_arrivo arrivo6 = new Volo_arrivo("Barcellona BCN", "Vueling");
            arrivo6.setCodice("VY6210");
            arrivo6.setData(LocalDate.parse("2025-05-21"));
            arrivo6.setOrarioPrevisto(LocalTime.parse("16:45:00"));
            arrivo6.setStato(Stato_del_volo.in_ritardo);
            arrivo6.setRitardo(15);
            voliInArrivo.add(arrivo6);

            Volo_arrivo arrivo7 = new Volo_arrivo("Amsterdam AMS", "KLM");
            arrivo7.setCodice("KL1677");
            arrivo7.setData(LocalDate.parse("2025-05-22"));
            arrivo7.setOrarioPrevisto(LocalTime.parse("08:50:00"));
            arrivo7.setStato(Stato_del_volo.in_orario);
            voliInArrivo.add(arrivo7);


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

            Volo_partenza partenza4 = new Volo_partenza("Parigi ORY", "EasyJet");
            partenza4.setCodice("U24321");
            partenza4.setData(LocalDate.parse("2025-05-21"));
            partenza4.setOrarioPrevisto(LocalTime.parse("13:15:00"));
            partenza4.setStato(Stato_del_volo.in_orario);
            partenza4.gate.setGate(4);
            voliInPartenza.add(partenza4);

            Volo_partenza partenza5 = new Volo_partenza("Madrid MAD", "Iberia");
            partenza5.setCodice("IB3251");
            partenza5.setData(LocalDate.parse("2025-05-21"));
            partenza5.setOrarioPrevisto(LocalTime.parse("17:30:00"));
            partenza5.setStato(Stato_del_volo.in_orario);
            partenza5.gate.setGate(5);
            voliInPartenza.add(partenza5);

            Volo_partenza partenza6 = new Volo_partenza("Berlino BER", "Lufthansa");
            partenza6.setCodice("LH1899");
            partenza6.setData(LocalDate.parse("2025-05-22"));
            partenza6.setOrarioPrevisto(LocalTime.parse("09:20:00"));
            partenza6.setStato(Stato_del_volo.rinviato);
            partenza6.setData(LocalDate.parse("2025-05-23"));
            partenza6.setOrarioPrevisto(LocalTime.parse("10:00:00"));
            partenza6.gate.setGate(6);
            voliInPartenza.add(partenza6);

            Volo_partenza partenza7 = new Volo_partenza("New York JFK", "Delta");
            partenza7.setCodice("DL077");
            partenza7.setData(LocalDate.parse("2025-05-22"));
            partenza7.setOrarioPrevisto(LocalTime.parse("11:45:00"));
            partenza7.setStato(Stato_del_volo.in_orario);
            partenza7.gate.setGate(7);
            voliInPartenza.add(partenza7);


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
        Volo voloPerP1 = voliInPartenza.stream().filter(v -> v.getStato() == Stato_del_volo.in_orario || v.getStato() == Stato_del_volo.in_ritardo || v.getStato() == Stato_del_volo.rinviato).findFirst().orElse(null);
        if (voloPerP1 == null && !voliInPartenza.isEmpty()) voloPerP1 = voliInPartenza.get(0);

        if (voloPerP1 != null) {
            p1.setCodiceVolo(voloPerP1.getCodice());
            p1.getPasseggero().setPosto("1A");
            p1.updateAssicurazione();
            p1.updateBagaglio();
            prenotazioni.add(p1);
        }


        Prenotazione p2 = new Prenotazione();
        p2.getPasseggero().setNome("utente");
        p2.getPasseggero().setCognome("Test");
        p2.getPasseggero().setSsn("TSTUSR90C02H501C");
        p2.getPasseggero().setEmail("utente.test@example.com");
        p2.getPasseggero().setTelefono("3471122334");

        Volo voloPerP2 = voliInArrivo.stream().filter(v -> v.getStato() == Stato_del_volo.in_orario || v.getStato() == Stato_del_volo.in_ritardo).findFirst().orElse(null);
        if (voloPerP2 == null && !voliInArrivo.isEmpty()) voloPerP2 = voliInArrivo.get(0);

        if (voloPerP2 != null) {
            p2.setCodiceVolo(voloPerP2.getCodice());
            p2.getPasseggero().setPosto("4D");
            p2.updateAssicurazione();
            prenotazioni.add(p2);
        } else if (voloPerP1 != null) {
            p2.setCodiceVolo(voloPerP1.getCodice());
            p2.getPasseggero().setPosto("4D");
            p2.updateAssicurazione();
            prenotazioni.add(p2);
        }


        Volo voloPerP3 = null;
        for(Volo v : voliInPartenza){
            if((v.getStato() == Stato_del_volo.in_orario || v.getStato() == Stato_del_volo.rinviato || v.getStato() == Stato_del_volo.in_ritardo) && (voloPerP1 == null || !v.getCodice().equals(voloPerP1.getCodice())) ){
                voloPerP3 = v;
                break;
            }
        }
        if(voloPerP3 == null && voliInPartenza.size() > 1 && (voloPerP1 == null || !voliInPartenza.get(1).getCodice().equals(voloPerP1.getCodice()))) {
            voloPerP3 = voliInPartenza.get(1);
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

    public List<String> getPostiOccupatiPerVolo(String codiceVolo) {
        List<String> postiOccupati = new ArrayList<>();
        for (Prenotazione p : prenotazioni) {
            if (p.getCodiceVolo().equals(codiceVolo) && p.getPasseggero() != null && p.getPasseggero().getPosto() != null) {
                postiOccupati.add(p.getPasseggero().getPosto());
            }
        }
        return postiOccupati;
    }

    public String getNomeUtenteLoggato() {
        if (utenteCorrente != null && "utente".equalsIgnoreCase(utenteCorrente.getLogin())) {
            return utenteCorrente.getLogin();
        }
        return "";
    }
}