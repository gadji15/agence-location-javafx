package dao;

import model.Vehicule;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiculeDAO {

    // ===== CRUD =====

    public boolean create(Vehicule vehicule) {
        String sql = "INSERT INTO vehicule (marque, modele, annee, immatriculation, carburant, " +
                "kilometrage, prixJournalier, statut, idParking, actif) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, vehicule.getMarque());
            stmt.setString(2, vehicule.getModele());
            stmt.setInt(3, vehicule.getAnnee());
            stmt.setString(4, vehicule.getImmatriculation());
            stmt.setString(5, vehicule.getCarburant());
            stmt.setInt(6, vehicule.getKilometrage());
            stmt.setDouble(7, vehicule.getPrixJournalier());
            stmt.setString(8, vehicule.getStatut());

            if (vehicule.getIdParking() != null) {
                stmt.setInt(9, vehicule.getIdParking());
            } else {
                stmt.setNull(9, Types.INTEGER);
            }

            stmt.setBoolean(10, vehicule.isActif());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        vehicule.setIdVehicule(rs.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Vehicule getById(int id) {
        String sql = "SELECT * FROM vehicule WHERE idVehicule = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToVehicule(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Vehicule> getAll() {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM vehicule ORDER BY marque, modele";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                vehicules.add(mapResultSetToVehicule(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vehicules;
    }

    public List<Vehicule> getActiveVehicules() {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM vehicule WHERE actif = TRUE ORDER BY marque, modele";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                vehicules.add(mapResultSetToVehicule(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vehicules;
    }

    public List<Vehicule> getAvailableVehicules() {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM vehicule WHERE actif = TRUE AND statut = 'DISPONIBLE' ORDER BY marque, modele";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                vehicules.add(mapResultSetToVehicule(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vehicules;
    }

    public List<Vehicule> getVehiculesActifs() {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM vehicule WHERE actif = TRUE " +
                "AND statut IN ('DISPONIBLE', 'LOUE', 'RESERVE') " +
                "ORDER BY marque, modele";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                vehicules.add(mapResultSetToVehicule(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vehicules;
    }

    public List<Vehicule> search(String keyword) {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM vehicule WHERE marque LIKE ? OR modele LIKE ? OR immatriculation LIKE ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchTerm = "%" + keyword + "%";
            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);
            stmt.setString(3, searchTerm);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                vehicules.add(mapResultSetToVehicule(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vehicules;
    }

    public boolean update(Vehicule vehicule) {
        String sql = "UPDATE vehicule SET marque = ?, modele = ?, annee = ?, immatriculation = ?, " +
                "carburant = ?, kilometrage = ?, prixJournalier = ?, statut = ?, idParking = ?, actif = ? " +
                "WHERE idVehicule = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vehicule.getMarque());
            stmt.setString(2, vehicule.getModele());
            stmt.setInt(3, vehicule.getAnnee());
            stmt.setString(4, vehicule.getImmatriculation());
            stmt.setString(5, vehicule.getCarburant());
            stmt.setInt(6, vehicule.getKilometrage());
            stmt.setDouble(7, vehicule.getPrixJournalier());
            stmt.setString(8, vehicule.getStatut());

            if (vehicule.getIdParking() != null) {
                stmt.setInt(9, vehicule.getIdParking());
            } else {
                stmt.setNull(9, Types.INTEGER);
            }

            stmt.setBoolean(10, vehicule.isActif());
            stmt.setInt(11, vehicule.getIdVehicule());

            int affectedRows = stmt.executeUpdate();
            System.out.println("   UPDATE vehicule #" + vehicule.getIdVehicule() + " : " + affectedRows + " ligne(s)");
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur update vehicule : " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ===== MÉTHODE DIRECTE POUR LE STATUT =====
    public boolean updateStatut(int vehiculeId, String statut) {
        String sql = "UPDATE vehicule SET statut = ? WHERE idVehicule = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, statut);
            stmt.setInt(2, vehiculeId);

            int affectedRows = stmt.executeUpdate();
            System.out.println("✅ updateStatut(#" + vehiculeId + ", " + statut + ") : " + affectedRows + " ligne(s)");
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur updateStatut : " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "UPDATE vehicule SET actif = FALSE WHERE idVehicule = ?";

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

    public boolean deletePermanently(int id) {
        String sql = "DELETE FROM vehicule WHERE idVehicule = ?";

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

    public boolean immatriculationExists(String immatriculation) {
        String sql = "SELECT COUNT(*) FROM vehicule WHERE immatriculation = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, immatriculation);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean immatriculationExistsExcept(String immatriculation, int id) {
        String sql = "SELECT COUNT(*) FROM vehicule WHERE immatriculation = ? AND idVehicule != ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, immatriculation);
            stmt.setInt(2, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM vehicule";

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

    public int countAvailable() {
        String sql = "SELECT COUNT(*) FROM vehicule WHERE actif = TRUE AND statut = 'DISPONIBLE'";

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

    private Vehicule mapResultSetToVehicule(ResultSet rs) throws SQLException {
        Vehicule vehicule = new Vehicule();
        vehicule.setIdVehicule(rs.getInt("idVehicule"));
        vehicule.setMarque(rs.getString("marque"));
        vehicule.setModele(rs.getString("modele"));
        vehicule.setAnnee(rs.getInt("annee"));
        vehicule.setImmatriculation(rs.getString("immatriculation"));
        vehicule.setCarburant(rs.getString("carburant"));
        vehicule.setKilometrage(rs.getInt("kilometrage"));
        vehicule.setPrixJournalier(rs.getDouble("prixJournalier"));
        vehicule.setStatut(rs.getString("statut"));

        int idParking = rs.getInt("idParking");
        if (!rs.wasNull()) {
            vehicule.setIdParking(idParking);
        }

        vehicule.setActif(rs.getBoolean("actif"));
        return vehicule;
    }
}