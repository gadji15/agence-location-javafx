package dao;


import model.Client;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ClientDao {

    // ===== CRUD =====

    // CREATE - Ajouter un client
    public boolean create(Client client) {
        String sql = "INSERT INTO client (nom, prenom, email, telephone, adresse, actif) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, client.getNom());
            stmt.setString(2, client.getPrenom());
            stmt.setString(3, client.getEmail());
            stmt.setString(4, client.getTelephone());
            stmt.setString(5, client.getAdresse());
            stmt.setBoolean(6, client.isActif());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        client.setIdClient(rs.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // READ - Récupérer un client par ID
    public Client getById(int id) {
        String sql = "SELECT * FROM client WHERE idClient = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToClient(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // READ - Récupérer tous les clients
    public List<Client> getAll() {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM client ORDER BY dateInscription DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                clients.add(mapResultSetToClient(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clients;
    }

    // READ - Récupérer les clients actifs uniquement
    public List<Client> getActiveClients() {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM client WHERE actif = TRUE ORDER BY dateInscription DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                clients.add(mapResultSetToClient(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clients;
    }

    // READ - Rechercher des clients par nom ou email
    public List<Client> search(String keyword) {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM client WHERE nom LIKE ? OR prenom LIKE ? OR email LIKE ? OR telephone LIKE ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchTerm = "%" + keyword + "%";
            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);
            stmt.setString(3, searchTerm);
            stmt.setString(4, searchTerm);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                clients.add(mapResultSetToClient(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clients;
    }

    // UPDATE - Modifier un client
    public boolean update(Client client) {
        String sql = "UPDATE client SET nom = ?, prenom = ?, email = ?, telephone = ?, adresse = ?, actif = ? WHERE idClient = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, client.getNom());
            stmt.setString(2, client.getPrenom());
            stmt.setString(3, client.getEmail());
            stmt.setString(4, client.getTelephone());
            stmt.setString(5, client.getAdresse());
            stmt.setBoolean(6, client.isActif());
            stmt.setInt(7, client.getIdClient());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // DELETE - Supprimer un client (soft delete)
    public boolean delete(int id) {
        String sql = "UPDATE client SET actif = FALSE WHERE idClient = ?";

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

    // DELETE - Supprimer définitivement un client (hard delete)
    public boolean deletePermanently(int id) {
        String sql = "DELETE FROM client WHERE idClient = ?";

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

    // Vérifier si un email existe déjà
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM client WHERE email = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Vérifier si un email existe déjà (sauf pour un ID donné)
    public boolean emailExistsExcept(String email, int id) {
        String sql = "SELECT COUNT(*) FROM client WHERE email = ? AND idClient != ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
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

    // Compter le nombre total de clients
    public int count() {
        String sql = "SELECT COUNT(*) FROM client";

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

    // ===== MÉTHODE DE MAPPAGE =====

    private Client mapResultSetToClient(ResultSet rs) throws SQLException {
        Client client = new Client();
        client.setIdClient(rs.getInt("idClient"));
        client.setNom(rs.getString("nom"));
        client.setPrenom(rs.getString("prenom"));
        client.setEmail(rs.getString("email"));
        client.setTelephone(rs.getString("telephone"));
        client.setAdresse(rs.getString("adresse"));

        Timestamp timestamp = rs.getTimestamp("dateInscription");
        if (timestamp != null) {
            client.setDateInscription(timestamp.toLocalDateTime());
        }

        client.setActif(rs.getBoolean("actif"));
        return client;
    }
}
