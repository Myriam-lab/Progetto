package dao;

import model.Passeggero;
import model.Prenotazione;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrenotazioneDAOImpl implements PrenotazioneDAO {

    private final PasseggeroDAO passeggeroDAO = new PasseggeroDAOImpl();

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

    @Override
    public List<Prenotazione> getPrenotazioniPerUtente(String nomeUtente) {
        return getPrenotazioniFiltrateAdmin(nomeUtente, "");
    }

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