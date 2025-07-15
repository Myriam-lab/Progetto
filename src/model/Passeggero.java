package model;

/**
 * Rappresenta un passeggero con i suoi dati anagrafici.
 */
public class Passeggero {
    private String nome;
    private String cognome;
    private String email;
    private String ssn;
    private String posto;
    private String telefono;


    /**
     * Costruttore di default per un Passeggero.
     * Inizializza tutti i campi a null.
     */
    public Passeggero() {
        nome = null;
        cognome = null;
        email = null;
        ssn = null;
        posto = null;
        telefono = null;
    }

    /**
     * Restituisce il nome del passeggero.
     * @return il nome del passeggero.
     */
    public String getNome() {
        return nome;
    }

    /**
     * Imposta il nome del passeggero.
     * @param nome il nuovo nome del passeggero.
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Restituisce il cognome del passeggero.
     * @return il cognome del passeggero.
     */
    public String getCognome() {
        return cognome;
    }

    /**
     * Imposta il cognome del passeggero.
     * @param cognome il nuovo cognome del passeggero.
     */
    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    /**
     * Restituisce l'email del passeggero.
     * @return l'email del passeggero.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Imposta l'email del passeggero.
     * @param email la nuova email del passeggero.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Restituisce il codice fiscale (SSN) del passeggero.
     * @return il codice fiscale del passeggero.
     */
    public String getSsn() {
        return ssn;
    }

    /**
     * Imposta il codice fiscale (SSN) del passeggero.
     * @param ssn il nuovo codice fiscale del passeggero.
     */
    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    /**
     * Restituisce il posto assegnato al passeggero.
     * @return il posto assegnato.
     */
    public String getPosto() {
        return posto;
    }

    /**
     * Imposta il posto del passeggero.
     * @param posto il nuovo posto del passeggero.
     */
    public void setPosto(String posto) {
        this.posto = posto;
    }

    /**
     * Restituisce il numero di telefono del passeggero.
     * @return il numero di telefono.
     */
    public String getTelefono() {
        return telefono;
    }

    /**
     * Imposta il numero di telefono del passeggero.
     * @param telefono il nuovo numero di telefono.
     */
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

}
