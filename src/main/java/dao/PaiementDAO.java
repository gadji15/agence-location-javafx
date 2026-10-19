package dao;

import model.Paiement;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaiementDAO {

    // ===== CREATE =====
    /**
     * Enregistre un nouveau paiement dans la base de données
     */
    public boolean create(Paiement paiement) {
        String sql = "INSERT INTO paiement (idFacture, mode, montantPaye, devise, " +
                "datePaiement, referenceTransaction, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, paiement.getIdFacture());
            stmt.setString(2, paiement.getMode());
            stmt.setDouble(3, paiement.getMontantPaye());
            stmt.setString(4, paiement.getDevise() != null ? paiement.getDevise() : "XOF");
            stmt.setTimestamp(5, Timestamp.valueOf(paiement.getDatePaiement()));
            stmt.setString(6, paiement.getReferenceTransaction());
            stmt.setString(7, paiement.getNotes());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        paiement.setIdPaiement(rs.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ===== READ =====
    /**
     * Récupère un paiement par son ID
     */
    public Paiement getById(int id) {
        String sql = "SELECT * FROM paiement WHERE idPaiement = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToPaiement(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupère les paiements d'une facture
     */
    public List<Paiement> getByFacture(int factureId) {
        List<Paiement> paiements = new ArrayList<>();
        String sql = "SELECT * FROM paiement WHERE idFacture = ? ORDER BY datePaiement DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, factureId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                paiements.add(mapResultSetToPaiement(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return paiements;
    }

    /**
     * Récupère tous les paiements
     */
    public List<Paiement> getAll() {
        List<Paiement> paiements = new ArrayList<>();
        String sql = "SELECT * FROM paiement ORDER BY datePaiement DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                paiements.add(mapResultSetToPaiement(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return paiements;
    }

    // ===== UTILITAIRES =====
    /**
     * Calcule le montant total payé pour une facture
     * Utile pour vérifier si la facture est totalement payée
     */
    public double getTotalPayeByFacture(int factureId) {
        String sql = "SELECT SUM(montantPaye) FROM paiement WHERE idFacture = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, factureId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Vérifie si une facture a déjà un paiement
     */
    public boolean factureHasPaiement(int factureId) {
        String sql = "SELECT COUNT(*) FROM paiement WHERE idFacture = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, factureId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Compte le nombre total de paiements
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM paiement";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Compte les paiements par mode
     */
    public int countByMode(String mode) {
        String sql = "SELECT COUNT(*) FROM paiement WHERE mode = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, mode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ===== DELETE =====
    /**
     * Supprime un paiement par son ID
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM paiement WHERE idPaiement = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Supprime tous les paiements d'une facture
     * Utile si on annule un paiement
     */
    public boolean deleteByFacture(int factureId) {
        String sql = "DELETE FROM paiement WHERE idFacture = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, factureId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ===== MAPPING =====
    /**
     * Convertit un ResultSet en objet Paiement
     */
    private Paiement mapResultSetToPaiement(ResultSet rs) throws SQLException {
        Paiement paiement = new Paiement();
        paiement.setIdPaiement(rs.getInt("idPaiement"));
        paiement.setIdFacture(rs.getInt("idFacture"));
        paiement.setMode(rs.getString("mode"));
        paiement.setMontantPaye(rs.getDouble("montantPaye"));
        paiement.setDevise(rs.getString("devise"));

        Timestamp timestamp = rs.getTimestamp("datePaiement");
        if (timestamp != null) {
            paiement.setDatePaiement(timestamp.toLocalDateTime());
        }

        paiement.setReferenceTransaction(rs.getString("referenceTransaction"));
        paiement.setNotes(rs.getString("notes"));
        return paiement;
    }
}