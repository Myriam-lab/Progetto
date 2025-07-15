package model;

/**
 * Rappresenta la prenotazione di un posto su un volo da parte di un passeggero.
 */
public class Prenotazione {
    private String pnr;
    private String num_biglietto;
    private Stato_prenotazione stato;
    private boolean assicurazione;
    private boolean bagaglio;
    protected Passeggero passeggero;
    private String codiceVolo;

    /**
     * Costruttore di default per una Prenotazione.
     * Inizializza i valori di default per PNR, numero biglietto, stato, assicurazione e bagaglio.
     * Crea un nuovo oggetto Passeggero associato.
     */
    public Prenotazione() {
        pnr = "AA000";
        num_biglietto = "BB000";
        stato = Stato_prenotazione.confermata;
        assicurazione = false;
        bagaglio = false;
        passeggero = new Passeggero();
    }

    /**
     * Restituisce il codice PNR della prenotazione.
     * @return il codice PNR.
     */
    public String getPnr() {
        return pnr;
    }

    /**
     * Imposta il codice PNR della prenotazione.
     * @param pnr il nuovo codice PNR.
     */
    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    /**
     * Restituisce il numero del biglietto.
     * @return il numero del biglietto.
     */
    public String getNum_biglietto() {
        return num_biglietto;
    }

    /**
     * Imposta il numero del biglietto.
     * @param num_biglietto il nuovo numero del biglietto.
     */
    public void setNum_biglietto(String num_biglietto) {
        this.num_biglietto = num_biglietto;
    }

    /**
     * Restituisce lo stato della prenotazione.
     * @return lo stato della prenotazione.
     */
    public Stato_prenotazione getStato() {
        return stato;
    }

    /**
     * Imposta lo stato della prenotazione.
     * @param stato il nuovo stato della prenotazione.
     */
    public void setStato(Stato_prenotazione stato) {
        this.stato = stato;
    }

    /**
     * Verifica se l'assicurazione è inclusa.
     * @return true se l'assicurazione è inclusa, false altrimenti.
     */
    public boolean isAssicurazione() {
        return assicurazione;
    }

    /**
     * Aggiorna lo stato dell'assicurazione (la inverte).
     */
    public void updateAssicurazione() {
        this.assicurazione = !assicurazione;
    }

    /**
     * Verifica se il bagaglio è incluso.
     * @return true se il bagaglio è incluso, false altrimenti.
     */
    public boolean isBagaglio() {
        return bagaglio;
    }

    /**
     * Aggiorna lo stato del bagaglio (lo inverte).
     */
    public void updateBagaglio() {
        this.bagaglio = !bagaglio;
    }

    /**
     * Restituisce il passeggero associato alla prenotazione.
     * @return il passeggero.
     */
    public Passeggero getPasseggero() {
        return passeggero;
    }

    /**
     * Imposta il passeggero per questa prenotazione.
     * @param passeggero il nuovo passeggero.
     */
    public void setPasseggero(Passeggero passeggero) {
        this.passeggero = passeggero;
    }

    /**
     * Restituisce il codice del volo associato alla prenotazione.
     * @return il codice del volo.
     */
    public String getCodiceVolo() {
        return codiceVolo;
    }

    /**
     * Imposta il codice del volo per questa prenotazione.
     * @param codiceVolo il nuovo codice del volo.
     */
    public void setCodiceVolo(String codiceVolo) {
        this.codiceVolo = codiceVolo;
    }

}
