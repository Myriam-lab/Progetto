package dao;

import model.Passeggero;
import util.DatabaseConnection;
import java.sql.*;
import java.util.Optional;

/**
 * Implementazione dell'interfaccia {@link PasseggeroDAO} per un database PostgreSQL.
 * Fornisce la logica concreta per eseguire operazioni CRUD sulla tabella dei passeggeri.
 */
public class PasseggeroDAOImpl implements PasseggeroDAO {

    /**
     * Trova un passeggero nel database tramite il suo codice fiscale (SSN).
     *
     * @param ssn Il codice fiscale univoco del passeggero da cercare.
     * @return un {@link Optional} che contiene l'oggetto {@link Passeggero} se viene trovato,
     * altrimenti un Optional vuoto.
     */
    @Override
    public Optional<Passeggero> findBySsn(String ssn) {
        String sql = "SELECT * FROM Passeggeri WHERE ssn = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ssn);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Passeggero p = new Passeggero();
                    p.setSsn(rs.getString("ssn"));
                    p.setNome(rs.getString("nome"));
                    p.setCognome(rs.getString("cognome"));
                    p.setEmail(rs.getString("email"));
                    p.setTelefono(rs.getString("telefono"));
                    return Optional.of(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Salva un oggetto {@link Passeggero} nel database.
     * Prima di inserire, controlla se un passeggero con lo stesso SSN è già presente
     * per evitare duplicati. Se esiste, l'operazione termina.
     *
     * @param passeggero L'oggetto Passeggero da persistere.
     */
    @Override
    public void save(Passeggero passeggero) {
        if (findBySsn(passeggero.getSsn()).isPresent()) return;

        String sql = "INSERT INTO Passeggeri (ssn, nome, cognome, email, telefono) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, passeggero.getSsn());
            pstmt.setString(2, passeggero.getNome());
            pstmt.setString(3, passeggero.getCognome());
            pstmt.setString(4, passeggero.getEmail());
            pstmt.setString(5, passeggero.getTelefono());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}