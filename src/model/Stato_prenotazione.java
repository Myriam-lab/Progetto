package model;

/**
 * Enumera i possibili stati di una prenotazione.
 */
public enum Stato_prenotazione {
    /**
     * La prenotazione è confermata.
     */
    confermata,
    /**
     * La prenotazione è in attesa di conferma o pagamento.
     */
    in_attesa,
    /**
     * La prenotazione è stata cancellata.
     */
    cancellata;
}
