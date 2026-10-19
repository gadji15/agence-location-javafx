package utils;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    // Configuration de la base de données
    // Valeurs par défaut adaptées à XAMPP ; surchargeables via les variables
    // d'environnement DB_URL, DB_USER et DB_PASSWORD (ex. pour Docker).
    private static final String URL = config("DB_URL", "jdbc:mysql://localhost:3306/agence_location");
    private static final String USER = config("DB_USER", "root");
    private static final String PASSWORD = config("DB_PASSWORD", ""); // Sous XAMPP, le mot de passe est vide par défaut

    private static String config(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }

    // Constructeur privé (Singleton)
    private DatabaseConnection() {
        try {
            // Charger le driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Établir la connexion
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connexion à la base de données réussie !");

        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL non trouvé !");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion à la base de données !");
            e.printStackTrace();
        }
    }

    // Méthode pour obtenir l'instance unique (Singleton)
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    // Méthode pour obtenir la connexion
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    // Méthode pour fermer la connexion
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔒 Connexion fermée.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour tester la connexion
    public boolean testConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}