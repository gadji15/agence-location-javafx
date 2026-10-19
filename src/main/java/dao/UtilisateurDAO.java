package dao;

import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UtilisateurDAO {

    /**
     * Vérifie si le mot de passe correspond à l'utilisateur
     */
    public boolean verifyPassword(int userId, String password) {
        String sql = "SELECT motDePasseHash FROM utilisateur WHERE idUtilisateur = ? AND actif = TRUE";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("motDePasseHash");
                // Comparaison en clair
                return storedPassword != null && storedPassword.equals(password);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Met à jour le mot de passe d'un utilisateur
     */
    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE utilisateur SET motDePasseHash = ? WHERE idUtilisateur = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPassword);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Vérifie si l'email est déjà utilisé par un autre compte utilisateur
     */
    public boolean emailExistsExcept(String email, int userId) {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE email = ? AND idUtilisateur != ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setInt(2, userId);
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
     * Met à jour l'email (identifiant de connexion) d'un utilisateur
     */
    public boolean updateEmail(int userId, String newEmail) {
        String sql = "UPDATE utilisateur SET email = ? WHERE idUtilisateur = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newEmail);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}