package dao;

import model.Contrat;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ContratDAO {

    // ===== CRUD =====

    public boolean create(Contrat contrat) {
        String sql = "INSERT INTO contratlocation (idReservation, dateSignature, dateDebutEffective, " +
                "dateFinEffective, etatRetour, kilometrageFinal, montantTotal, montantProvisoire, actif, " +
                "nombreJours, prixJournalier, sousTotal, fraisRetourDiffere, fraisCarburant, " +
                "fraisRetard, fraisDegradation, remise) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, contrat.getIdReservation());
            stmt.setTimestamp(2, Timestamp.valueOf(contrat.getDateSignature()));
            stmt.setTimestamp(3, Timestamp.valueOf(contrat.getDateDebutEffective()));
            stmt.setTimestamp(4, Timestamp.valueOf(contrat.getDateFinEffective()));
            stmt.setString(5, contrat.getEtatRetour());
            stmt.setInt(6, contrat.getKilometrageFinal());
            stmt.setDouble(7, contrat.getMontantTotal());
            stmt.setDouble(8, contrat.getMontantProvisoire());  // ← NOUVEAU
            stmt.setBoolean(9, contrat.isActif());
            stmt.setInt(10, contrat.getNombreJours());
            stmt.setDouble(11, contrat.getPrixJournalier());
            stmt.setDouble(12, contrat.getSousTotal());
            stmt.setDouble(13, contrat.getFraisRetourDiffere());
            stmt.setDouble(14, contrat.getFraisCarburant());
            stmt.setDouble(15, contrat.getFraisRetard());
            stmt.setDouble(16, contrat.getFraisDegradation());
            stmt.setDouble(17, contrat.getRemise());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        contrat.setIdContrat(rs.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Contrat getById(int id) {
        String sql = "SELECT * FROM contratlocation WHERE idContrat = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToContrat(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Contrat getByReservation(int reservationId) {
        String sql = "SELECT * FROM contratlocation WHERE idReservation = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reservationId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToContrat(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Contrat> getAll() {
        List<Contrat> contrats = new ArrayList<>();
        String sql = "SELECT * FROM contratlocation ORDER BY dateSignature DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                contrats.add(mapResultSetToContrat(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contrats;
    }

    public List<Contrat> getActiveContrats() {
        List<Contrat> contrats = new ArrayList<>();
        String sql = "SELECT * FROM contratlocation WHERE actif = TRUE ORDER BY dateSignature DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                contrats.add(mapResultSetToContrat(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contrats;
    }

    public List<Contrat> search(String keyword) {
        List<Contrat> contrats = new ArrayList<>();
        String sql = "SELECT c.* FROM contratlocation c " +
                "INNER JOIN reservation r ON c.idReservation = r.idReservation " +
                "INNER JOIN client cl ON r.idClient = cl.idClient " +
                "INNER JOIN vehicule v ON r.idVehicule = v.idVehicule " +
                "WHERE cl.nom LIKE ? OR cl.prenom LIKE ? OR v.marque LIKE ? " +
                "OR v.modele LIKE ? OR v.immatriculation LIKE ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchTerm = "%" + keyword + "%";
            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);
            stmt.setString(3, searchTerm);
            stmt.setString(4, searchTerm);
            stmt.setString(5, searchTerm);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                contrats.add(mapResultSetToContrat(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contrats;
    }

    public boolean update(Contrat contrat) {
        String sql = "UPDATE contratlocation SET idReservation = ?, dateSignature = ?, " +
                "dateDebutEffective = ?, dateFinEffective = ?, etatRetour = ?, " +
                "kilometrageFinal = ?, montantTotal = ?, montantProvisoire = ?, actif = ?, " +
                "nombreJours = ?, prixJournalier = ?, sousTotal = ?, " +
                "fraisRetourDiffere = ?, fraisCarburant = ?, fraisRetard = ?, " +
                "fraisDegradation = ?, remise = ? WHERE idContrat = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, contrat.getIdReservation());
            stmt.setTimestamp(2, Timestamp.valueOf(contrat.getDateSignature()));
            stmt.setTimestamp(3, Timestamp.valueOf(contrat.getDateDebutEffective()));
            stmt.setTimestamp(4, Timestamp.valueOf(contrat.getDateFinEffective()));
            stmt.setString(5, contrat.getEtatRetour());
            stmt.setInt(6, contrat.getKilometrageFinal());
            stmt.setDouble(7, contrat.getMontantTotal());
            stmt.setDouble(8, contrat.getMontantProvisoire());  // ← NOUVEAU
            stmt.setBoolean(9, contrat.isActif());
            stmt.setInt(10, contrat.getNombreJours());
            stmt.setDouble(11, contrat.getPrixJournalier());
            stmt.setDouble(12, contrat.getSousTotal());
            stmt.setDouble(13, contrat.getFraisRetourDiffere());
            stmt.setDouble(14, contrat.getFraisCarburant());
            stmt.setDouble(15, contrat.getFraisRetard());
            stmt.setDouble(16, contrat.getFraisDegradation());
            stmt.setDouble(17, contrat.getRemise());
            stmt.setInt(18, contrat.getIdContrat());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean cloturer(int contratId, String etatRetour, int kilometrageFinal) {
        String sql = "UPDATE contratlocation SET etatRetour = ?, kilometrageFinal = ?, actif = FALSE " +
                "WHERE idContrat = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, etatRetour);
            stmt.setInt(2, kilometrageFinal);
            stmt.setInt(3, contratId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ===== MISE À JOUR DU MONTANT AVEC CALCUL =====
    public boolean updateMontant(int contratId, int nombreJours, double prixJournalier,
                                 double fraisRetourDiffere, double fraisCarburant,
                                 double fraisRetard, double fraisDegradation, double remise) {

        double sousTotal = prixJournalier * nombreJours;
        double montantTotal = sousTotal + fraisRetourDiffere + fraisCarburant +
                fraisRetard + fraisDegradation - remise;

        String sql = "UPDATE contratlocation SET nombreJours = ?, prixJournalier = ?, " +
                "sousTotal = ?, fraisRetourDiffere = ?, fraisCarburant = ?, " +
                "fraisRetard = ?, fraisDegradation = ?, remise = ?, montantTotal = ? " +
                "WHERE idContrat = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, nombreJours);
            stmt.setDouble(2, prixJournalier);
            stmt.setDouble(3, sousTotal);
            stmt.setDouble(4, fraisRetourDiffere);
            stmt.setDouble(5, fraisCarburant);
            stmt.setDouble(6, fraisRetard);
            stmt.setDouble(7, fraisDegradation);
            stmt.setDouble(8, remise);
            stmt.setDouble(9, montantTotal);
            stmt.setInt(10, contratId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM contratlocation WHERE idContrat = ?";

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

    // ===== MÉTHODES UTILITAIRES =====

    public int count() {
        String sql = "SELECT COUNT(*) FROM contratlocation";

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

    public int countActive() {
        String sql = "SELECT COUNT(*) FROM contratlocation WHERE actif = TRUE";

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

    public boolean reservationHasContrat(int reservationId) {
        String sql = "SELECT COUNT(*) FROM contratlocation WHERE idReservation = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reservationId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ===== MÉTHODE DE MAPPAGE =====
    private Contrat mapResultSetToContrat(ResultSet rs) throws SQLException {
        Contrat contrat = new Contrat();
        contrat.setIdContrat(rs.getInt("idContrat"));
        contrat.setIdReservation(rs.getInt("idReservation"));

        Timestamp timestampSignature = rs.getTimestamp("dateSignature");
        if (timestampSignature != null) {
            contrat.setDateSignature(timestampSignature.toLocalDateTime());
        }

        Timestamp timestampDebut = rs.getTimestamp("dateDebutEffective");
        if (timestampDebut != null) {
            contrat.setDateDebutEffective(timestampDebut.toLocalDateTime());
        }

        Timestamp timestampFin = rs.getTimestamp("dateFinEffective");
        if (timestampFin != null) {
            contrat.setDateFinEffective(timestampFin.toLocalDateTime());
        }

        contrat.setEtatRetour(rs.getString("etatRetour"));
        contrat.setKilometrageFinal(rs.getInt("kilometrageFinal"));
        contrat.setMontantTotal(rs.getDouble("montantTotal"));
        contrat.setMontantProvisoire(rs.getDouble("montantProvisoire"));  // ← NOUVEAU
        contrat.setActif(rs.getBoolean("actif"));

        contrat.setNombreJours(rs.getInt("nombreJours"));
        contrat.setPrixJournalier(rs.getDouble("prixJournalier"));
        contrat.setSousTotal(rs.getDouble("sousTotal"));
        contrat.setFraisRetourDiffere(rs.getDouble("fraisRetourDiffere"));
        contrat.setFraisCarburant(rs.getDouble("fraisCarburant"));
        contrat.setFraisRetard(rs.getDouble("fraisRetard"));
        contrat.setFraisDegradation(rs.getDouble("fraisDegradation"));
        contrat.setRemise(rs.getDouble("remise"));

        return contrat;
    }
}