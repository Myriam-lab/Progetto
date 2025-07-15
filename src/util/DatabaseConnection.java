package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe di utilità per la gestione della connessione al database PostgreSQL.
 * Questa classe fornisce un metodo centralizzato per ottenere una connessione al database,
 * incapsulando i dettagli di connessione come URL, nome utente e password.
 */
public class DatabaseConnection {

    /**
     * L'URL JDBC per la connessione al database PostgreSQL.
     * Specifica il protocollo, il tipo di database, l'host, la porta e il nome del database.
     */
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";

    /**
     * Il nome utente per la connessione al database.
     */
    private static final String USER = "postgres";

    /**
     * La password per la connessione al database.
     */
    private static final String PASSWORD = "admin";

    /**
     * Stabilisce e restituisce una connessione al database.
     * Questo metodo statico utilizza l'URL, l'utente e la password predefiniti per
     * creare una nuova connessione al database.
     *
     * @return Un oggetto {@link Connection} al database.
     * @throws SQLException se si verifica un errore di accesso al database.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }
}