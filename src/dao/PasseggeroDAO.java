package dao;

import model.Passeggero;
import java.util.Optional;

/**
 * Interfaccia per l'accesso ai dati dei passeggeri (Data Access Object).
 * Definisce i metodi standard per interagire con la persistenza dei dati dei passeggeri,
 * astraendo l'implementazione specifica del database.
 */
public interface PasseggeroDAO {

    /**
     * Cerca e restituisce un passeggero basato sul suo codice fiscale (SSN).
     *
     * @param ssn Il codice fiscale del passeggero da trovare.
     * @return un {@link Optional} contenente il {@link Passeggero} se trovato,
     * altrimenti un Optional vuoto.
     */
    Optional<Passeggero> findBySsn(String ssn);

    /**
     * Salva un nuovo passeggero nel database.
     * Se un passeggero con lo stesso SSN esiste già, l'operazione potrebbe non avere effetto
     * per evitare duplicati.
     *
     * @param passeggero l'oggetto Passeggero da salvare.
     */
    void save(Passeggero passeggero);
}