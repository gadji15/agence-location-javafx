package dao;

import model.Facture;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FactureDAO {

    // ===== CREATE =====
    public boolean create(Facture facture) {
        String sql = "INSERT INTO facture (idContrat, dateEmission, montantTotal, montantPaye, " +
                "resteAPayer, statut, pdfPath, datePaiement) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, facture.getIdContrat());
            stmt.setTimestamp(2, Timestamp.valueOf(facture.getDateEmission()));
            stmt.setDouble(3, facture.getMontantTotal());
            stmt.setDouble(4, facture.getMontantPaye());
            stmt.setDouble(5, facture.getResteAPayer());
            stmt.setString(6, facture.getStatut());
            stmt.setString(7, facture.getPdfPath());

            if (facture.getDatePaiement() != null) {
                stmt.setTimestamp(8, Timestamp.valueOf(facture.getDatePaiement()));
            } else {
                stmt.setNull(8, Types.TIMESTAMP);
            }

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        facture.setIdFacture(rs.getInt(1));
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
    public Facture getById(int id) {
        String sql = "SELECT * FROM facture WHERE idFacture = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapResultSetToFacture(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ✅ NOUVELLE MÉTHODE : récupérer la facture d'un contrat
    public Facture getByContrat(int contratId) {
        String sql = "SELECT * FROM facture WHERE idContrat = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, contratId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapResultSetToFacture(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Facture> getAll() {
        List<Facture> factures = new ArrayList<>();
        String sql = "SELECT * FROM facture ORDER BY dateEmission DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) factures.add(mapResultSetToFacture(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return factures;
    }

    public List<Facture> getByStatut(String statut) {
        List<Facture> factures = new ArrayList<>();
        String sql = "SELECT * FROM facture WHERE statut = ? ORDER BY dateEmission DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, statut);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) factures.add(mapResultSetToFacture(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return factures;
    }

    public List<Facture> search(String keyword) {
        List<Facture> factures = new ArrayList<>();
        String sql = "SELECT f.* FROM facture f " +
                "INNER JOIN contratlocation c ON f.idContrat = c.idContrat " +
                "INNER JOIN reservation r ON c.idReservation = r.idReservation " +
                "INNER JOIN client cl ON r.idClient = cl.idClient " +
                "WHERE cl.nom LIKE ? OR cl.prenom LIKE ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String s = "%" + keyword + "%";
            stmt.setString(1, s);
            stmt.setString(2, s);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) factures.add(mapResultSetToFacture(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return factures;
    }

    // ===== UPDATE =====
    public boolean update(Facture facture) {
        String sql = "UPDATE facture SET idContrat = ?, montantTotal = ?, montantPaye = ?, " +
                "resteAPayer = ?, statut = ?, pdfPath = ?, datePaiement = ? WHERE idFacture = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, facture.getIdContrat());
            stmt.setDouble(2, facture.getMontantTotal());
            stmt.setDouble(3, facture.getMontantPaye());
            stmt.setDouble(4, facture.getResteAPayer());
            stmt.setString(5, facture.getStatut());
            stmt.setString(6, facture.getPdfPath());

            if (facture.getDatePaiement() != null) {
                stmt.setTimestamp(7, Timestamp.valueOf(facture.getDatePaiement()));
            } else {
                stmt.setNull(7, Types.TIMESTAMP);
            }
            stmt.setInt(8, facture.getIdFacture());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Ajoute un paiement à la facture et met à jour le statut
     */
    public boolean ajouterPaiement(int factureId, double montantPaye) {
        Facture facture = getById(factureId);
        if (facture == null) return false;

        double nouveauMontantPaye = facture.getMontantPaye() + montantPaye;
        double nouveauReste = facture.getMontantTotal() - nouveauMontantPaye;

        String statut;
        if (nouveauReste <= 0) {
            statut = "PAYEE";
        } else if (nouveauMontantPaye > 0) {
            statut = "PARTIELLE";
        } else {
            statut = "IMPAYEE";
        }

        String sql = "UPDATE facture SET montantPaye = ?, resteAPayer = ?, statut = ?, " +
                "datePaiement = ? WHERE idFacture = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, nouveauMontantPaye);
            stmt.setDouble(2, Math.max(0, nouveauReste));
            stmt.setString(3, statut);

            if ("PAYEE".equals(statut)) {
                stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            } else {
                stmt.setNull(4, Types.TIMESTAMP);
            }
            stmt.setInt(5, factureId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean marquerImpayee(int factureId) {
        String sql = "UPDATE facture SET montantPaye = 0, resteAPayer = montantTotal, " +
                "statut = 'IMPAYEE', datePaiement = NULL WHERE idFacture = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, factureId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean contratHasFacture(int contratId) {
        String sql = "SELECT COUNT(*) FROM facture WHERE idContrat = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, contratId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM facture WHERE idFacture = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ===== MAPPING =====
    private Facture mapResultSetToFacture(ResultSet rs) throws SQLException {
        Facture facture = new Facture();
        facture.setIdFacture(rs.getInt("idFacture"));
        facture.setIdContrat(rs.getInt("idContrat"));

        Timestamp dateEmission = rs.getTimestamp("dateEmission");
        if (dateEmission != null) facture.setDateEmission(dateEmission.toLocalDateTime());

        facture.setMontantTotal(rs.getDouble("montantTotal"));
        facture.setMontantPaye(rs.getDouble("montantPaye"));
        facture.setResteAPayer(rs.getDouble("resteAPayer"));
        facture.setStatut(rs.getString("statut"));
        facture.setPdfPath(rs.getString("pdfPath"));

        Timestamp datePaiement = rs.getTimestamp("datePaiement");
        if (datePaiement != null) facture.setDatePaiement(datePaiement.toLocalDateTime());

        return facture;
    }
}