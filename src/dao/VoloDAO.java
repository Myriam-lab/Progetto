package dao;

import model.Volo;
import java.util.List;

/**
 * Interfaccia per l'accesso ai dati dei voli (Data Access Object).
 * Definisce le operazioni standard per la gestione della persistenza dei dati dei voli.
 */
public interface VoloDAO {

    /**
     * Recupera dal database la lista di tutti i voli in arrivo.
     *
     * @return una lista di oggetti {@link Volo} che sono voli in arrivo.
     */
    List<Volo> getVoliInArrivo();

    /**
     * Recupera dal database la lista di tutti i voli in partenza.
     *
     * @return una lista di oggetti {@link Volo} che sono voli in partenza.
     */
    List<Volo> getVoliInPartenza();

    /**
     * Cerca e restituisce un volo specifico basandosi sul suo codice univoco.
     *
     * @param codiceVolo il codice del volo da cercare.
     * @return l'oggetto {@link Volo} corrispondente al codice, o {@code null} se non trovato.
     */
    Volo findVoloByCodice(String codiceVolo);

    /**
     * Inserisce un nuovo volo nel database.
     *
     * @param volo l'oggetto {@link Volo} (o una sua sottoclasse) da salvare.
     * @return {@code true} se il volo è stato creato con successo, {@code false} altrimenti.
     */
    boolean creaNuovoVolo(Volo volo);
}