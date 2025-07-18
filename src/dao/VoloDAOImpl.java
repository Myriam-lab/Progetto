package dao;

import model.*;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione dell'interfaccia {@link VoloDAO} per un database PostgreSQL.
 * Gestisce le operazioni di lettura e scrittura per gli oggetti Volo sulla tabella dei voli.
 */
public class VoloDAOImpl implements VoloDAO {

    /**
     * Recupera una lista di tutti i voli il cui tipo è 'Arrivo'.
     *
     * @return una lista di oggetti {@link Volo} (specificamente {@link Volo_arrivo}).
     */
    @Override
    public List<Volo> getVoliInArrivo() {
        List<Volo> voli = new ArrayList<>();
        String sql = "SELECT * FROM Voli WHERE tipo_volo = 'Arrivo'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Volo_arrivo volo = new Volo_arrivo(rs.getString("origine"), rs.getString("compagnia_aerea"));
                populateVoloFromResultSet(volo, rs);
                voli.add(volo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return voli;
    }

    /**
     * Recupera una lista di tutti i voli il cui tipo è 'Partenza'.
     *
     * @return una lista di oggetti {@link Volo} (specificamente {@link Volo_partenza}).
     */
    @Override
    public List<Volo> getVoliInPartenza() {
        List<Volo> voli = new ArrayList<>();
        String sql = "SELECT * FROM Voli WHERE tipo_volo = 'Partenza'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Volo_partenza volo = new Volo_partenza(rs.getString("destinazione"), rs.getString("compagnia_aerea"));
                populateVoloFromResultSet(volo, rs);
                volo.gate.setGate(rs.getInt("gate_numero"));
                voli.add(volo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return voli;
    }

    /**
     * Trova un singolo volo nel database usando il suo codice univoco.
     * Determina se il volo è di tipo 'Partenza' o 'Arrivo' e istanzia l'oggetto corretto.
     *
     * @param codiceVolo il codice del volo da trovare.
     * @return un oggetto {@link Volo} (o una sua sottoclasse) se trovato, altrimenti {@code null}.
     */
    @Override
    public Volo findVoloByCodice(String codiceVolo) {
        String sql = "SELECT * FROM Voli WHERE codice_volo = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, codiceVolo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String tipoVolo = rs.getString("tipo_volo");
                    Volo volo;
                    if ("Partenza".equalsIgnoreCase(tipoVolo)) {
                        Volo_partenza vp = new Volo_partenza(rs.getString("destinazione"), rs.getString("compagnia_aerea"));
                        vp.gate.setGate(rs.getInt("gate_numero"));
                        volo = vp;
                    } else {
                        volo = new Volo_arrivo(rs.getString("origine"), rs.getString("compagnia_aerea"));
                    }
                    populateVoloFromResultSet(volo, rs);
                    return volo;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Salva un nuovo volo nel database.
     * Utilizza {@code instanceof} per determinare il tipo di volo (partenza o arrivo)
     * e impostare correttamente i campi 'tipo_volo' e 'gate_numero' nella query SQL.
     *
     * @param volo l'oggetto Volo da persistere.
     * @return {@code true} se l'operazione di inserimento ha successo, {@code false} altrimenti.
     */
    @Override
    public boolean creaNuovoVolo(Volo volo) {
        String sql = "INSERT INTO Voli (codice_volo, compagnia_aerea, tipo_volo, origine, destinazione, data_volo, orario_previsto, stato, gate_numero) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String tipoVolo = (volo instanceof Volo_partenza) ? "Partenza" : "Arrivo";
            Integer gate = (volo instanceof Volo_partenza) ? ((Volo_partenza) volo).gate.getGate() : null;

            pstmt.setString(1, volo.getCodice());
            pstmt.setString(2, volo.getCompagniaAerea());
            pstmt.setString(3, tipoVolo);
            pstmt.setString(4, volo.getOrigine());
            pstmt.setString(5, volo.getDestinazione());
            pstmt.setDate(6, Date.valueOf(volo.getData()));
            pstmt.setTime(7, Time.valueOf(volo.getOrarioPrevisto()));
            pstmt.setString(8, volo.getStato().name());
            if (gate != null) pstmt.setInt(9, gate);
            else pstmt.setNull(9, Types.INTEGER);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Metodo di utilità privato per popolare un oggetto Volo con i dati da un ResultSet.
     * Evita la duplicazione del codice nei metodi di recupero dei voli.
     *
     * @param volo L'oggetto Volo da popolare.
     * @param rs Il ResultSet da cui leggere i dati.
     * @throws SQLException se si verifica un errore durante la lettura dal ResultSet.
     */
    private void populateVoloFromResultSet(Volo volo, ResultSet rs) throws SQLException {
        volo.setCodice(rs.getString("codice_volo"));
        volo.setCompagniaAerea(rs.getString("compagnia_aerea"));
        volo.setOrigine(rs.getString("origine"));
        volo.setDestinazione(rs.getString("destinazione"));
        volo.setData(rs.getDate("data_volo").toLocalDate());
        volo.setOrarioPrevisto(rs.getTime("orario_previsto").toLocalTime());
        volo.setStato(Stato_del_volo.valueOf(rs.getString("stato")));
    }
}

