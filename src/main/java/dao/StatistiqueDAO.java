package dao;

import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatistiqueDAO {

    public Map<String, Object> getGeneralStats(int annee, Integer mois) throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        String reservationPeriod = periodPredicate("dateDemande", mois);
        String paymentPeriod = periodPredicate("datePaiement", mois);

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            stats.put("totalClients", getCount(conn, "client"));
            stats.put("totalVehicules", getCount(conn, "vehicule"));
            stats.put("totalReservations", getPeriodCount(conn,
                    "SELECT COUNT(*) FROM reservation WHERE " + reservationPeriod, annee, mois));
            stats.put("caTotal", getPeriodSum(conn,
                    "SELECT COALESCE(SUM(montantPaye), 0) FROM paiement WHERE devise = 'XOF' AND " +
                            paymentPeriod, annee, mois));
        }
        return stats;
    }

    public List<Map<String, Object>> getMonthlyEvolution(int annee, Integer mois) throws SQLException {
        List<Map<String, Object>> evolution = new ArrayList<>();
        String sql = "SELECT MONTH(dateDemande) AS mois, COUNT(*) AS total " +
                "FROM reservation WHERE YEAR(dateDemande) = ?" +
                (mois == null ? "" : " AND MONTH(dateDemande) = ?") +
                " GROUP BY MONTH(dateDemande) ORDER BY mois";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindPeriod(stmt, 1, annee, mois);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> month = new HashMap<>();
                    month.put("mois", rs.getInt("mois"));
                    month.put("total", rs.getInt("total"));
                    evolution.add(month);
                }
            }
        }
        return evolution;
    }

    public List<Map<String, Object>> getTopVehicules(int annee, Integer mois) throws SQLException {
        List<Map<String, Object>> topVehicules = new ArrayList<>();
        String sql = "SELECT v.marque, v.modele, v.immatriculation, COUNT(c.idContrat) AS nb_locations " +
                "FROM vehicule v INNER JOIN reservation r ON v.idVehicule = r.idVehicule " +
                "INNER JOIN contratlocation c ON c.idReservation = r.idReservation " +
                "WHERE " + periodPredicate("r.dateDemande", mois) +
                " GROUP BY v.idVehicule, v.marque, v.modele, v.immatriculation " +
                "ORDER BY nb_locations DESC LIMIT 5";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindPeriod(stmt, 1, annee, mois);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> vehicule = new HashMap<>();
                    vehicule.put("marque", rs.getString("marque"));
                    vehicule.put("modele", rs.getString("modele"));
                    vehicule.put("immatriculation", rs.getString("immatriculation"));
                    vehicule.put("nb_locations", rs.getInt("nb_locations"));
                    topVehicules.add(vehicule);
                }
            }
        }
        return topVehicules;
    }

    public List<Map<String, Object>> getTopClients(int annee, Integer mois) throws SQLException {
        List<Map<String, Object>> topClients = new ArrayList<>();
        String sql = "SELECT c.nom, c.prenom, c.email, COUNT(r.idReservation) AS nb_reservations " +
                "FROM client c INNER JOIN reservation r ON c.idClient = r.idClient " +
                "WHERE " + periodPredicate("r.dateDemande", mois) +
                " GROUP BY c.idClient, c.nom, c.prenom, c.email " +
                "ORDER BY nb_reservations DESC LIMIT 5";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindPeriod(stmt, 1, annee, mois);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> client = new HashMap<>();
                    client.put("nom", rs.getString("nom"));
                    client.put("prenom", rs.getString("prenom"));
                    client.put("email", rs.getString("email"));
                    client.put("nb_reservations", rs.getInt("nb_reservations"));
                    topClients.add(client);
                }
            }
        }
        return topClients;
    }

    public Map<String, Integer> getReservationStatusDistribution(int annee, Integer mois) throws SQLException {
        Map<String, Integer> distribution = new HashMap<>();
        String sql = "SELECT statut, COUNT(*) AS total FROM reservation WHERE " +
                periodPredicate("dateDemande", mois) + " GROUP BY statut";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindPeriod(stmt, 1, annee, mois);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    distribution.put(rs.getString("statut"), rs.getInt("total"));
                }
            }
        }
        return distribution;
    }

    private int getCount(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private int getPeriodCount(Connection conn, String sql, int annee, Integer mois) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindPeriod(stmt, 1, annee, mois);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private double getPeriodSum(Connection conn, String sql, int annee, Integer mois) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindPeriod(stmt, 1, annee, mois);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getDouble(1);
            }
        }
    }

    private String periodPredicate(String dateColumn, Integer mois) {
        return "YEAR(" + dateColumn + ") = ?" + (mois == null ? "" : " AND MONTH(" + dateColumn + ") = ?");
    }

    private void bindPeriod(PreparedStatement stmt, int firstParameter, int annee, Integer mois)
            throws SQLException {
        stmt.setInt(firstParameter, annee);
        if (mois != null) {
            stmt.setInt(firstParameter + 1, mois);
        }
    }
}
