package dao;

import model.Passeggero;
import model.Prenotazione;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione dell'interfaccia {@link PrenotazioneDAO} per un database PostgreSQL.
 * Contiene la logica per interagire con la tabella delle prenotazioni e dei passeggeri.
 */
public class PrenotazioneDAOImpl implements PrenotazioneDAO {

    private final PasseggeroDAO passeggeroDAO = new PasseggeroDAOImpl();

    /**
     * Salva una nuova prenotazione nel database.
     * Prima di salvare la prenotazione, si assicura che il passeggero associato
     * sia presente nel database invocando il metodo {@code save} del passeggeroDAO.
     *
     * @param prenotazione L'oggetto {@link Prenotazione} da salvare.
     * @return {@code true} se l'inserimento ha successo, {@code false} altrimenti.
     */
    @Override
    public boolean creaPrenotazione(Prenotazione prenotazione) {
        passeggeroDAO.save(prenotazione.getPasseggero());

        String sql = "INSERT INTO Prenotazioni (codice_volo_fk, ssn_passeggero_fk, posto, assicurazione, bagaglio) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, prenotazione.getCodiceVolo());
            pstmt.setString(2, prenotazione.getPasseggero().getSsn());
            pstmt.setString(3, prenotazione.getPasseggero().getPosto());
            pstmt.setBoolean(4, prenotazione.isAssicurazione());
            pstmt.setBoolean(5, prenotazione.isBagaglio());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Recupera le prenotazioni per un dato nome utente.
     * Questo metodo riutilizza la logica di {@code getPrenotazioniFiltrateAdmin},
     * passando il nome utente come filtro per il nome e una stringa vuota per il cognome.
     *
     * @param nomeUtente il nome utente per cui recuperare le prenotazioni.
     * @return una lista di {@link Prenotazione}.
     */
    @Override
    public List<Prenotazione> getPrenotazioniPerUtente(String nomeUtente) {
        return getPrenotazioniFiltrateAdmin(nomeUtente, "");
    }

    /**
     * Recupera una lista di prenotazioni dal database, filtrando per nome e cognome del passeggero.
     * Esegue una JOIN con la tabella dei passeggeri. La ricerca è case-insensitive e parziale (LIKE %).
     *
     * @param nomeFilter Il filtro per il nome del passeggero.
     * @param cognomeFilter Il filtro per il cognome del passeggero.
     * @return Una lista di oggetti {@link Prenotazione} corrispondenti ai filtri.
     */
    @Override
    public List<Prenotazione> getPrenotazioniFiltrateAdmin(String nomeFilter, String cognomeFilter) {
        List<Prenotazione> prenotazioni = new ArrayList<>();
        String sql = "SELECT * FROM Prenotazioni pr JOIN Passeggeri p ON pr.ssn_passeggero_fk = p.ssn WHERE LOWER(p.nome) LIKE ? AND LOWER(p.cognome) LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + nomeFilter.toLowerCase() + "%");
            pstmt.setString(2, "%" + cognomeFilter.toLowerCase() + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Passeggero p = new Passeggero();
                    p.setSsn(rs.getString("ssn"));
                    p.setNome(rs.getString("nome"));
                    p.setCognome(rs.getString("cognome"));
                    p.setEmail(rs.getString("email"));
                    p.setTelefono(rs.getString("telefono"));
                    p.setPosto(rs.getString("posto"));

                    Prenotazione pr = new Prenotazione();
                    pr.setPasseggero(p);
                    pr.setCodiceVolo(rs.getString("codice_volo_fk"));
                    if(rs.getBoolean("assicurazione")) pr.updateAssicurazione();
                    if(rs.getBoolean("bagaglio")) pr.updateBagaglio();

                    prenotazioni.add(pr);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return prenotazioni;
    }

    /**
     * Recupera la lista dei posti occupati per un volo specifico.
     *
     * @param codiceVolo il codice del volo di cui si vogliono conoscere i posti occupati.
     * @return una lista di stringhe che rappresentano i posti occupati.
     */
    @Override
    public List<String> getPostiOccupatiPerVolo(String codiceVolo) {
        List<String> posti = new ArrayList<>();
        String sql = "SELECT posto FROM Prenotazioni WHERE codice_volo_fk = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, codiceVolo);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    posti.add(rs.getString("posto"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posti;
    }
}