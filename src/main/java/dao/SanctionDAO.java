package dao;

import model.Sanction;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SanctionDAO {

    // ===== CREATE =====
    public boolean create(Sanction sanction) {
        String sql = "INSERT INTO sanction (idClient, idContrat, idFacture, type, montant, " +
                "description, dateEnregistrement, statut) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, sanction.getIdClient());

            if (sanction.getIdContrat() != null) stmt.setInt(2, sanction.getIdContrat());
            else stmt.setNull(2, Types.INTEGER);

            if (sanction.getIdFacture() != null) stmt.setInt(3, sanction.getIdFacture());
            else stmt.setNull(3, Types.INTEGER);

            stmt.setString(4, sanction.getType());
            stmt.setDouble(5, sanction.getMontant());
            stmt.setString(6, sanction.getDescription());
            stmt.setTimestamp(7, Timestamp.valueOf(sanction.getDateEnregistrement()));
            stmt.setString(8, sanction.getStatut());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) sanction.setIdSanction(rs.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ===== READ =====
    public Sanction getById(int id) {
        String sql = "SELECT * FROM sanction WHERE idSanction = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapResultSetToSanction(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Sanction> getByClient(int clientId) {
        List<Sanction> sanctions = new ArrayList<>();
        String sql = "SELECT * FROM sanction WHERE idClient = ? ORDER BY dateEnregistrement DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) sanctions.add(mapResultSetToSanction(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return sanctions;
    }

    public List<Sanction> getAll() {
        List<Sanction> sanctions = new ArrayList<>();
        String sql = "SELECT * FROM sanction ORDER BY dateEnregistrement DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) sanctions.add(mapResultSetToSanction(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return sanctions;
    }

    public List<Sanction> getActiveSanctions() {
        List<Sanction> sanctions = new ArrayList<>();
        String sql = "SELECT * FROM sanction WHERE statut = 'ACTIVE' ORDER BY dateEnregistrement DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) sanctions.add(mapResultSetToSanction(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return sanctions;
    }

    public List<Sanction> getByStatut(String statut) {
        List<Sanction> sanctions = new ArrayList<>();
        String sql = "SELECT * FROM sanction WHERE statut = ? ORDER BY dateEnregistrement DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, statut);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) sanctions.add(mapResultSetToSanction(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return sanctions;
    }

    public List<Sanction> search(String keyword) {
        List<Sanction> sanctions = new ArrayList<>();
        String sql = "SELECT s.* FROM sanction s " +
                "INNER JOIN client c ON s.idClient = c.idClient " +
                "WHERE c.nom LIKE ? OR c.prenom LIKE ? OR s.type LIKE ? OR s.description LIKE ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String searchTerm = "%" + keyword + "%";
            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);
            stmt.setString(3, searchTerm);
            stmt.setString(4, searchTerm);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) sanctions.add(mapResultSetToSanction(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return sanctions;
    }

    // ===== UPDATE =====
    public boolean update(Sanction sanction) {
        String sql = "UPDATE sanction SET idClient = ?, idContrat = ?, idFacture = ?, " +
                "type = ?, montant = ?, description = ?, statut = ? WHERE idSanction = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sanction.getIdClient());

            if (sanction.getIdContrat() != null) stmt.setInt(2, sanction.getIdContrat());
            else stmt.setNull(2, Types.INTEGER);

            if (sanction.getIdFacture() != null) stmt.setInt(3, sanction.getIdFacture());
            else stmt.setNull(3, Types.INTEGER);

            stmt.setString(4, sanction.getType());
            stmt.setDouble(5, sanction.getMontant());
            stmt.setString(6, sanction.getDescription());
            stmt.setString(7, sanction.getStatut());
            stmt.setInt(8, sanction.getIdSanction());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateStatut(int sanctionId, String statut) {
        String sql = "UPDATE sanction SET statut = ? WHERE idSanction = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, statut);
            stmt.setInt(2, sanctionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * ✅ NOUVELLE MÉTHODE : Marque toutes les sanctions liées à une facture comme LEVEE.
     * Appelée automatiquement après un paiement complet de la facture.
     */
    public boolean marquerLeveeByFacture(int idFacture) {
        String sql = "UPDATE sanction SET statut = 'LEVEE' " +
                "WHERE idFacture = ? AND statut != 'LEVEE'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idFacture);
            int rows = stmt.executeUpdate();
            System.out.println("✅ " + rows + " sanction(s) levée(s) pour la facture #" + idFacture);
            return rows >= 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM sanction WHERE idSanction = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ===== UTILITAIRES =====
    public int count() {
        String sql = "SELECT COUNT(*) FROM sanction";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int countByStatut(String statut) {
        String sql = "SELECT COUNT(*) FROM sanction WHERE statut = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, statut);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public boolean contratHasSanction(int contratId) {
        String sql = "SELECT COUNT(*) FROM sanction WHERE idContrat = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, contratId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ===== MAPPING =====
    private Sanction mapResultSetToSanction(ResultSet rs) throws SQLException {
        Sanction sanction = new Sanction();
        sanction.setIdSanction(rs.getInt("idSanction"));
        sanction.setIdClient(rs.getInt("idClient"));

        int idContrat = rs.getInt("idContrat");
        if (!rs.wasNull()) sanction.setIdContrat(idContrat);

        int idFacture = rs.getInt("idFacture");           // ⬅️ NOUVEAU
        if (!rs.wasNull()) sanction.setIdFacture(idFacture);  // ⬅️ NOUVEAU

        sanction.setType(rs.getString("type"));
        sanction.setMontant(rs.getDouble("montant"));
        sanction.setDescription(rs.getString("description"));

        Timestamp timestamp = rs.getTimestamp("dateEnregistrement");
        if (timestamp != null) sanction.setDateEnregistrement(timestamp.toLocalDateTime());

        sanction.setStatut(rs.getString("statut"));
        return sanction;
    }
}