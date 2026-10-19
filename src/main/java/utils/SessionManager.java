package utils;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class SessionManager {

    private static SessionManager instance;
    private Map<String, Object> sessionData;
    private boolean isLoggedIn;
    private String currentUserEmail;
    private String currentUserRole;
    private Integer currentUserId;
    private Integer currentClientId;
    private LocalDateTime loginTime;

    private SessionManager() {
        sessionData = new HashMap<>();
        isLoggedIn = false;
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // ===== MÉTHODES D'AUTHENTIFICATION =====

    public boolean authenticate(String email, String password) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "SELECT u.idUtilisateur, u.email, u.role, u.idClient, u.motDePasseHash, " +
                    "c.idClient, c.nom, c.prenom " +
                    "FROM utilisateur u " +
                    "LEFT JOIN client c ON u.idClient = c.idClient " +
                    "WHERE u.email = ? AND u.actif = TRUE";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("motDePasseHash");

                System.out.println("========================================");
                System.out.println("🔐 AUTHENTIFICATION");
                System.out.println("   Email: " + email);
                System.out.println("   Mot de passe saisi: " + password);
                System.out.println("   Mot de passe stocké: " + storedPassword);

                // Comparaison en clair
                boolean matches = storedPassword != null && storedPassword.equals(password);

                if (matches) {
                    currentUserId = rs.getInt("idUtilisateur");
                    currentUserEmail = rs.getString("email");
                    currentUserRole = rs.getString("role");
                    currentClientId = rs.getInt("idClient");
                    if (rs.wasNull()) {
                        currentClientId = null;
                    }
                    loginTime = LocalDateTime.now();
                    isLoggedIn = true;

                    updateLastAccess(currentUserId);

                    System.out.println("✅ Authentification réussie !");
                    System.out.println("   ID: " + currentUserId);
                    System.out.println("   Rôle: " + currentUserRole);
                    System.out.println("========================================");
                    return true;
                } else {
                    System.out.println("❌ Mot de passe incorrect");
                    System.out.println("========================================");
                }
            } else {
                System.out.println("❌ Utilisateur non trouvé : " + email);
                System.out.println("========================================");
            }

            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateLastAccess(int userId) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "UPDATE utilisateur SET dernierAcces = NOW() WHERE idUtilisateur = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ===== MÉTHODES DE GESTION DE SESSION =====

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public String getCurrentUserEmail() {
        return currentUserEmail;
    }

    public String getCurrentUserRole() {
        return currentUserRole;
    }

    public Integer getCurrentUserId() {
        return currentUserId;
    }

    public Integer getCurrentClientId() {
        return currentClientId;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public boolean isAdmin() {
        return isLoggedIn && "ADMIN".equals(currentUserRole);
    }

    public boolean isClient() {
        return isLoggedIn && "CLIENT".equals(currentUserRole);
    }

    public void setAttribute(String key, Object value) {
        sessionData.put(key, value);
    }

    public Object getAttribute(String key) {
        return sessionData.get(key);
    }

    public void removeAttribute(String key) {
        sessionData.remove(key);
    }

    public void clearSession() {
        sessionData.clear();
        isLoggedIn = false;
        currentUserEmail = null;
        currentUserRole = null;
        currentUserId = null;
        currentClientId = null;
        loginTime = null;
        System.out.println("🧹 Session vidée");
    }

    public void logout() {
        clearSession();
        System.out.println("🚪 Déconnexion réussie");
    }

    // ===== MÉTHODE POUR L'ADMIN PAR DÉFAUT =====

    public void createDefaultAdmin() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            // Vérifier si un admin existe déjà
            String checkSql = "SELECT COUNT(*) FROM utilisateur WHERE role = 'ADMIN'";
            stmt = conn.prepareStatement(checkSql);
            rs = stmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("👤 Un administrateur existe déjà.");
                return;
            }

            // Créer un admin par défaut
            String insertClient = "INSERT INTO client (nom, prenom, email, telephone, adresse, actif) VALUES (?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(insertClient);
            stmt.setString(1, "Admin");
            stmt.setString(2, "System");
            stmt.setString(3, "admin@agence.com");
            stmt.setString(4, "0102030405");
            stmt.setString(5, "Agence principale");
            stmt.setBoolean(6, true);
            stmt.executeUpdate();

            // Récupérer l'ID du client créé
            String getClientId = "SELECT LAST_INSERT_ID()";
            stmt = conn.prepareStatement(getClientId);
            rs = stmt.executeQuery();

            int clientId = 0;
            if (rs.next()) {
                clientId = rs.getInt(1);
            }

            // Mot de passe en clair
            String password = "admin123";

            // Créer l'utilisateur admin
            String insertUser = "INSERT INTO utilisateur (email, motDePasseHash, role, idClient, actif) VALUES (?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(insertUser);
            stmt.setString(1, "admin@agence.com");
            stmt.setString(2, password);
            stmt.setString(3, "ADMIN");
            stmt.setInt(4, clientId);
            stmt.setBoolean(5, true);
            stmt.executeUpdate();

            System.out.println("✅ Compte admin par défaut créé !");
            System.out.println("   Email: admin@agence.com");
            System.out.println("   Mot de passe: " + password);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ===== MÉTHODES POUR LE PROFIL CLIENT =====

    public void setCurrentUserFullName(String fullName) {
        sessionData.put("userFullName", fullName);
        System.out.println("👤 Nom du client mis à jour dans la session: " + fullName);
    }

    public String getCurrentUserFullName() {
        if (sessionData.containsKey("userFullName")) {
            return (String) sessionData.get("userFullName");
        }

        if (currentUserId == null) {
            return "Utilisateur";
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT c.nom, c.prenom FROM client c " +
                    "INNER JOIN utilisateur u ON u.idClient = c.idClient " +
                    "WHERE u.idUtilisateur = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, currentUserId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                String fullName = rs.getString("prenom") + " " + rs.getString("nom");
                sessionData.put("userFullName", fullName);
                return fullName;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return "Utilisateur";
    }

    public String getCurrentClientFullName() {
        if (currentClientId == null) {
            return "Client";
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT nom, prenom FROM client WHERE idClient = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, currentClientId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("prenom") + " " + rs.getString("nom");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return "Client";
    }
}