package dao;

import model.Prenotazione;
import java.util.List;

/**
 * Interfaccia per l'accesso ai dati delle prenotazioni (Data Access Object).
 * Definisce i metodi per la gestione della persistenza delle prenotazioni.
 */
public interface PrenotazioneDAO {

    /**
     * Crea una nuova prenotazione nel sistema di persistenza.
     *
     * @param prenotazione l'oggetto Prenotazione da salvare.
     * @return {@code true} se la prenotazione è stata creata con successo, {@code false} altrimenti.
     */
    boolean creaPrenotazione(Prenotazione prenotazione);

    /**
     * Recupera tutte le prenotazioni associate a un nome utente specifico.
     *
     * @param nomeUtente Il nome dell'utente per cui cercare le prenotazioni.
     * @return una lista di oggetti {@link Prenotazione}.
     */
    List<Prenotazione> getPrenotazioniPerUtente(String nomeUtente);

    /**
     * Recupera le prenotazioni filtrando per nome e cognome del passeggero.
     * La ricerca non è case-sensitive e funziona anche per corrispondenze parziali.
     *
     * @param nomeFilter Il filtro per il nome del passeggero.
     * @param cognomeFilter Il filtro per il cognome del passeggero.
     * @return una lista di {@link Prenotazione} che corrispondono ai criteri di ricerca.
     */
    List<Prenotazione> getPrenotazioniFiltrateAdmin(String nomeFilter, String cognomeFilter);

    /**
     * Recupera la lista dei posti già occupati per un determinato volo.
     *
     * @param codiceVolo Il codice del volo per cui verificare i posti.
     * @return una lista di stringhe, dove ogni stringa è un posto occupato (es. "3A").
     */
    List<String> getPostiOccupatiPerVolo(String codiceVolo);
}