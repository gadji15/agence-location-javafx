package dao;


import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatistiqueDAO {

    // ===== STATISTIQUES GÉNÉRALES =====

    public Map<String, Object> getGeneralStats() {
        Map<String, Object> stats = new HashMap<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {

            // Total clients
            stats.put("totalClients", getCount(conn, "client"));

            // Total véhicules
            stats.put("totalVehicules", getCount(conn, "vehicule"));

            // Total réservations
            stats.put("totalReservations", getCount(conn, "reservation"));

            // Chiffre d'affaires total
            stats.put("caTotal", getSum(conn, "facture", "montantTotal", "statut = 'PAYEE'"));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stats;
    }

    public List<Map<String, Object>> getMonthlyEvolution(int annee) {
        List<Map<String, Object>> evolution = new ArrayList<>();

        String sql = "SELECT MONTH(dateDemande) as mois, COUNT(*) as total " +
                "FROM reservation WHERE YEAR(dateDemande) = ? " +
                "GROUP BY MONTH(dateDemande) ORDER BY mois";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, annee);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> month = new HashMap<>();
                month.put("mois", rs.getInt("mois"));
                month.put("total", rs.getInt("total"));
                month.put("acceptees", 0);
                month.put("en_attente", 0);
                month.put("refusees", 0);
                evolution.add(month);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return evolution;
    }

    public List<Map<String, Object>> getTopVehicules() {
        List<Map<String, Object>> topVehicules = new ArrayList<>();

        String sql = "SELECT v.marque, v.modele, v.immatriculation, COUNT(r.idVehicule) as nb_locations " +
                "FROM vehicule v " +
                "INNER JOIN reservation r ON v.idVehicule = r.idVehicule " +
                "GROUP BY v.idVehicule " +
                "ORDER BY nb_locations DESC LIMIT 5";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> vehicule = new HashMap<>();
                vehicule.put("marque", rs.getString("marque"));
                vehicule.put("modele", rs.getString("modele"));
                vehicule.put("immatriculation", rs.getString("immatriculation"));
                vehicule.put("nb_locations", rs.getInt("nb_locations"));
                topVehicules.add(vehicule);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return topVehicules;
    }

    public List<Map<String, Object>> getTopClients() {
        List<Map<String, Object>> topClients = new ArrayList<>();

        String sql = "SELECT c.nom, c.prenom, c.email, COUNT(r.idReservation) as nb_reservations " +
                "FROM client c " +
                "INNER JOIN reservation r ON c.idClient = r.idClient " +
                "GROUP BY c.idClient " +
                "ORDER BY nb_reservations DESC LIMIT 5";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> client = new HashMap<>();
                client.put("nom", rs.getString("nom"));
                client.put("prenom", rs.getString("prenom"));
                client.put("email", rs.getString("email"));
                client.put("nb_reservations", rs.getInt("nb_reservations"));
                topClients.add(client);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return topClients;
    }

    public Map<String, Integer> getReservationStatusDistribution() {
        Map<String, Integer> distribution = new HashMap<>();

        String sql = "SELECT statut, COUNT(*) as count FROM reservation GROUP BY statut";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                distribution.put(rs.getString("statut"), rs.getInt("count"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return distribution;
    }

    // ===== MÉTHODES UTILITAIRES =====

    private int getCount(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private double getSum(Connection conn, String tableName, String column, String condition) throws SQLException {
        String sql = "SELECT SUM(" + column + ") FROM " + tableName;
        if (condition != null && !condition.isEmpty()) {
            sql += " WHERE " + condition;
        }
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }
}