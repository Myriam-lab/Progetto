package model;

/**
 * Rappresenta un utente generico del sistema con credenziali di accesso.
 * Classe base per Utente_generico e Amministratore_sistema.
 */
public class Utente {
    private final String login;
    private String password;

    /**
     * Costruttore per creare un nuovo Utente.
     * @param login l'username dell'utente (non può essere modificato).
     * @param password la password dell'utente.
     */
    public Utente(String login, String password) {
        this.login = login;
        this.password = password;
    }

    /**
     * Restituisce il login dell'utente.
     * @return il login dell'utente.
     */
    public String getLogin() {
        return login;
    }

    /**
     * Imposta una nuova password per l'utente.
     * @param password la nuova password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Restituisce la password dell'utente.
     * @return la password.
     */
    public String getPassword() {
        return password;
    }
}
