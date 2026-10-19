package dao;

import model.Parking;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParkingDAO {

    // ===== CRUD =====

    // CREATE - Ajouter un parking
    public boolean create(Parking parking) {
        String sql = "INSERT INTO parking (nom, adresse, capacite, actif) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, parking.getNom());
            stmt.setString(2, parking.getAdresse());
            stmt.setInt(3, parking.getCapacite());
            stmt.setBoolean(4, parking.isActif());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        parking.setIdParking(rs.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // READ - Récupérer un parking par ID
    public Parking getById(int id) {
        String sql = "SELECT * FROM parking WHERE idParking = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToParking(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // READ - Récupérer tous les parkings
    public List<Parking> getAll() {
        List<Parking> parkings = new ArrayList<>();
        String sql = "SELECT * FROM parking ORDER BY nom";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                parkings.add(mapResultSetToParking(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return parkings;
    }

    // READ - Récupérer les parkings actifs
    public List<Parking> getActiveParkings() {
        List<Parking> parkings = new ArrayList<>();
        String sql = "SELECT * FROM parking WHERE actif = TRUE ORDER BY nom";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                parkings.add(mapResultSetToParking(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return parkings;
    }

    // READ - Rechercher des parkings
    public List<Parking> search(String keyword) {
        List<Parking> parkings = new ArrayList<>();
        String sql = "SELECT * FROM parking WHERE nom LIKE ? OR adresse LIKE ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchTerm = "%" + keyword + "%";
            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                parkings.add(mapResultSetToParking(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return parkings;
    }

    // UPDATE - Modifier un parking
    public boolean update(Parking parking) {
        String sql = "UPDATE parking SET nom = ?, adresse = ?, capacite = ?, actif = ? WHERE idParking = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, parking.getNom());
            stmt.setString(2, parking.getAdresse());
            stmt.setInt(3, parking.getCapacite());
            stmt.setBoolean(4, parking.isActif());
            stmt.setInt(5, parking.getIdParking());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // DELETE - Soft delete
    public boolean delete(int id) {
        String sql = "UPDATE parking SET actif = FALSE WHERE idParking = ?";

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

    // DELETE - Hard delete
    public boolean deletePermanently(int id) {
        String sql = "DELETE FROM parking WHERE idParking = ?";

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

    // Vérifier si un nom de parking existe déjà
    public boolean nomExists(String nom) {
        String sql = "SELECT COUNT(*) FROM parking WHERE nom = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nom);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Vérifier si un nom de parking existe déjà (sauf pour un ID donné)
    public boolean nomExistsExcept(String nom, int id) {
        String sql = "SELECT COUNT(*) FROM parking WHERE nom = ? AND idParking != ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nom);
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

    // Compter le nombre total de parkings
    public int count() {
        String sql = "SELECT COUNT(*) FROM parking";

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

    // Compter le nombre de places disponibles dans un parking
    public int getAvailableSpaces(int parkingId) {
        String sql = "SELECT capacite - (SELECT COUNT(*) FROM vehicule WHERE idParking = ? AND actif = TRUE) AS places_disponibles FROM parking WHERE idParking = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, parkingId);
            stmt.setInt(2, parkingId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("places_disponibles");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ===== MÉTHODE DE MAPPAGE =====

    private Parking mapResultSetToParking(ResultSet rs) throws SQLException {
        Parking parking = new Parking();
        parking.setIdParking(rs.getInt("idParking"));
        parking.setNom(rs.getString("nom"));
        parking.setAdresse(rs.getString("adresse"));
        parking.setCapacite(rs.getInt("capacite"));
        parking.setActif(rs.getBoolean("actif"));
        return parking;
    }
}
