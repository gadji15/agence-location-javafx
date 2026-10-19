package dao;


import model.Reservation;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    // ===== CRUD =====

    public boolean create(Reservation reservation) {
        String sql = "INSERT INTO reservation (idClient, idVehicule, dateDebut, dateFin, " +
                "statut, lieuPriseEnCharge, lieuRetour) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, reservation.getIdClient());
            stmt.setInt(2, reservation.getIdVehicule());
            stmt.setTimestamp(3, Timestamp.valueOf(reservation.getDateDebut()));
            stmt.setTimestamp(4, Timestamp.valueOf(reservation.getDateFin()));
            stmt.setString(5, reservation.getStatut());
            stmt.setString(6, reservation.getLieuPriseEnCharge());
            stmt.setString(7, reservation.getLieuRetour());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        reservation.setIdReservation(rs.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Reservation getById(int id) {
        String sql = "SELECT * FROM reservation WHERE idReservation = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToReservation(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Reservation> getAll() {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservation ORDER BY dateDemande DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;
    }

    public List<Reservation> getByClient(int clientId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservation WHERE idClient = ? ORDER BY dateDemande DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;
    }

    public List<Reservation> getByVehicule(int vehiculeId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservation WHERE idVehicule = ? ORDER BY dateDemande DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, vehiculeId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;
    }

    public List<Reservation> getByStatut(String statut) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservation WHERE statut = ? ORDER BY dateDemande DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, statut);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;
    }

    public List<Reservation> search(String keyword) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.* FROM reservation r " +
                "INNER JOIN client c ON r.idClient = c.idClient " +
                "INNER JOIN vehicule v ON r.idVehicule = v.idVehicule " +
                "WHERE c.nom LIKE ? OR c.prenom LIKE ? OR v.marque LIKE ? " +
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
                reservations.add(mapResultSetToReservation(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;
    }

    public boolean update(Reservation reservation) {
        String sql = "UPDATE reservation SET idClient = ?, idVehicule = ?, dateDebut = ?, dateFin = ?, " +
                "statut = ?, lieuPriseEnCharge = ?, lieuRetour = ? WHERE idReservation = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reservation.getIdClient());
            stmt.setInt(2, reservation.getIdVehicule());
            stmt.setTimestamp(3, Timestamp.valueOf(reservation.getDateDebut()));
            stmt.setTimestamp(4, Timestamp.valueOf(reservation.getDateFin()));
            stmt.setString(5, reservation.getStatut());
            stmt.setString(6, reservation.getLieuPriseEnCharge());
            stmt.setString(7, reservation.getLieuRetour());
            stmt.setInt(8, reservation.getIdReservation());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateStatut(int reservationId, String statut) {
        String sql = "UPDATE reservation SET statut = ? WHERE idReservation = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, statut);
            stmt.setInt(2, reservationId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM reservation WHERE idReservation = ?";

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

    public boolean isVehiculeAvailable(int vehiculeId, LocalDateTime dateDebut, LocalDateTime dateFin) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE idVehicule = ? AND statut NOT IN ('REFUSEE', 'ANNULEE') " +
                "AND ((dateDebut <= ? AND dateFin >= ?) OR (dateDebut <= ? AND dateFin >= ?) " +
                "OR (dateDebut >= ? AND dateFin <= ?))";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, vehiculeId);
            stmt.setTimestamp(2, Timestamp.valueOf(dateFin));
            stmt.setTimestamp(3, Timestamp.valueOf(dateDebut));
            stmt.setTimestamp(4, Timestamp.valueOf(dateDebut));
            stmt.setTimestamp(5, Timestamp.valueOf(dateDebut));
            stmt.setTimestamp(6, Timestamp.valueOf(dateDebut));
            stmt.setTimestamp(7, Timestamp.valueOf(dateFin));

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) == 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isVehiculeAvailableExcept(int vehiculeId, LocalDateTime dateDebut, LocalDateTime dateFin, int excludeReservationId) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE idVehicule = ? AND idReservation != ? " +
                "AND statut NOT IN ('REFUSEE', 'ANNULEE') " +
                "AND ((dateDebut <= ? AND dateFin >= ?) OR (dateDebut <= ? AND dateFin >= ?) " +
                "OR (dateDebut >= ? AND dateFin <= ?))";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, vehiculeId);
            stmt.setInt(2, excludeReservationId);
            stmt.setTimestamp(3, Timestamp.valueOf(dateFin));
            stmt.setTimestamp(4, Timestamp.valueOf(dateDebut));
            stmt.setTimestamp(5, Timestamp.valueOf(dateDebut));
            stmt.setTimestamp(6, Timestamp.valueOf(dateDebut));
            stmt.setTimestamp(7, Timestamp.valueOf(dateDebut));
            stmt.setTimestamp(8, Timestamp.valueOf(dateFin));

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) == 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Indique si le véhicule a encore une réservation active : EN_ATTENTE, ou ACCEPTEE
     * dont le contrat n'est pas clôturé (une location terminée ne bloque plus le véhicule).
     */
    public boolean hasReservationActive(int vehiculeId) {
        String sql = "SELECT COUNT(*) FROM reservation r " +
                "LEFT JOIN contratlocation c ON c.idReservation = r.idReservation " +
                "WHERE r.idVehicule = ? AND r.statut IN ('EN_ATTENTE', 'ACCEPTEE') " +
                "AND (c.idContrat IS NULL OR c.actif = TRUE)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, vehiculeId);
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
        String sql = "SELECT COUNT(*) FROM reservation";

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

    public int countByStatut(String statut) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE statut = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, statut);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation();
        reservation.setIdReservation(rs.getInt("idReservation"));
        reservation.setIdClient(rs.getInt("idClient"));
        reservation.setIdVehicule(rs.getInt("idVehicule"));

        Timestamp timestampDebut = rs.getTimestamp("dateDebut");
        if (timestampDebut != null) {
            reservation.setDateDebut(timestampDebut.toLocalDateTime());
        }

        Timestamp timestampFin = rs.getTimestamp("dateFin");
        if (timestampFin != null) {
            reservation.setDateFin(timestampFin.toLocalDateTime());
        }

        Timestamp timestampDemande = rs.getTimestamp("dateDemande");
        if (timestampDemande != null) {
            reservation.setDateDemande(timestampDemande.toLocalDateTime());
        }

        reservation.setStatut(rs.getString("statut"));
        reservation.setLieuPriseEnCharge(rs.getString("lieuPriseEnCharge"));
        reservation.setLieuRetour(rs.getString("lieuRetour"));
        return reservation;
    }
}