package Controller;

import dao.ParkingDAO;
import dao.ReservationDAO;
import dao.VehiculeDAO;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import model.Parking;
import model.Reservation;
import model.Vehicule;
import utils.EmailSender;
import utils.NotificationManager;
import utils.SessionManager;
import view.ClientReservationView;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class ClientReservationController {

    private ClientReservationView view;
    private ReservationDAO reservationDAO;
    private VehiculeDAO vehiculeDAO;
    private ParkingDAO parkingDAO;  // ← AJOUTÉ
    private int clientId;

    public ClientReservationController(ClientReservationView view) {
        this.view = view;
        this.reservationDAO = new ReservationDAO();
        this.vehiculeDAO = new VehiculeDAO();
        this.parkingDAO = new ParkingDAO();  // ← AJOUTÉ
        this.clientId = SessionManager.getInstance().getCurrentClientId();

        System.out.println("✅ ClientReservationController créé pour client ID: " + clientId);

        initEventHandlers();
        loadReservations();
        loadVehicules();
        loadParkings();  // ← AJOUTÉ
    }

    private void initEventHandlers() {
        view.getSearchButton().setOnAction(e -> searchReservations());
        view.getSearchField().setOnAction(e -> searchReservations());
        view.getRefreshButton().setOnAction(e -> loadReservations());
        view.getStatutFilterComboBox().setOnAction(e -> filterByStatut());
        view.getSubmitReservationButton().setOnAction(e -> creerReservation());

        view.setCallback(new ClientReservationView.ReservationCallback() {
            @Override
            public void onSearch(String keyword) {
                searchReservations();
            }

            @Override
            public void onFilter(String statut) {
                filterByStatut();
            }

            @Override
            public void onRefresh() {
                loadReservations();
            }

            @Override
            public void onAnnuler(Reservation reservation) {
                annulerReservation(reservation);
            }

            @Override
            public void onCreerReservation(int idVehicule, LocalDateTime dateDebut, LocalDateTime dateFin,
                                           String lieuPriseEnCharge, String lieuRetour) {
                // Non utilisé
            }
        });
    }

    private void loadReservations() {
        List<Reservation> reservations = reservationDAO.getByClient(clientId);
        view.refreshTable(reservations);
        updateStats(reservations);
        System.out.println("📋 " + reservations.size() + " réservations chargées pour le client " + clientId);
    }

    private void searchReservations() {
        String keyword = view.getSearchField().getText().trim();
        if (keyword.isEmpty()) {
            loadReservations();
        } else {
            List<Reservation> reservations = reservationDAO.search(keyword);
            reservations.removeIf(r -> r.getIdClient() != clientId);
            view.refreshTable(reservations);
            updateStats(reservations);
        }
    }

    private void filterByStatut() {
        String statut = view.getStatutFilterComboBox().getValue();
        List<Reservation> reservations;

        if (statut == null || "Tous".equals(statut)) {
            reservations = reservationDAO.getByClient(clientId);
        } else {
            reservations = reservationDAO.getByStatut(statut);
            reservations.removeIf(r -> r.getIdClient() != clientId);
        }

        view.refreshTable(reservations);
        updateStats(reservations);
    }

    private void loadVehicules() {
        List<Vehicule> vehicules = vehiculeDAO.getAvailableVehicules();
        view.loadVehicules(vehicules);
        System.out.println("🚗 " + vehicules.size() + " véhicules disponibles chargés");
    }

    // ===== NOUVELLE MÉTHODE : CHARGER LES PARKINGS =====
    private void loadParkings() {
        List<Parking> parkings = parkingDAO.getAll();
        view.loadParkings(parkings);
        System.out.println("🅿️ " + parkings.size() + " parkings chargés");
    }

    // ===== CRÉATION AVEC COMBOBOX PARKINGS =====
    private void creerReservation() {
        Vehicule selectedVehicule = view.getVehiculeComboBox().getValue();
        LocalDateTime dateDebut = view.getDateDebutPicker().getValue() != null
                ? view.getDateDebutPicker().getValue().atTime(LocalTime.of(10, 0))
                : null;
        LocalDateTime dateFin = view.getDateFinPicker().getValue() != null
                ? view.getDateFinPicker().getValue().atTime(LocalTime.of(10, 0))
                : null;

        // ===== RÉCUPÉRATION DES PARKINGS =====
        Parking parkingPrise = view.getLieuPriseEnChargeComboBox().getValue();
        Parking parkingRetour = view.getLieuRetourComboBox().getValue();

        if (selectedVehicule == null) {
            view.showFormError("⚠️ Veuillez sélectionner un véhicule !");
            return;
        }

        if (dateDebut == null || dateFin == null) {
            view.showFormError("⚠️ Veuillez sélectionner les dates !");
            return;
        }

        if (dateFin.isBefore(dateDebut) || dateFin.isEqual(dateDebut)) {
            view.showFormError("⚠️ La date de fin doit être postérieure à la date de début !");
            return;
        }

        // ===== VÉRIFICATION DES PARKINGS =====
        if (parkingPrise == null) {
            view.showFormError("⚠️ Veuillez sélectionner un lieu de prise en charge !");
            return;
        }

        if (parkingRetour == null) {
            view.showFormError("⚠️ Veuillez sélectionner un lieu de retour !");
            return;
        }

        view.clearFormError();

        if (!reservationDAO.isVehiculeAvailable(selectedVehicule.getIdVehicule(), dateDebut, dateFin)) {
            view.showFormError("⚠️ Ce véhicule n'est pas disponible pour cette période !");
            return;
        }

        // ===== UTILISATION DES NOMS DES PARKINGS =====
        String lieuPriseEnCharge = parkingPrise.getNom();
        String lieuRetour = parkingRetour.getNom();

        Reservation reservation = new Reservation(
                clientId,
                selectedVehicule.getIdVehicule(),
                dateDebut,
                dateFin,
                "EN_ATTENTE",
                lieuPriseEnCharge,
                lieuRetour
        );

        if (reservationDAO.create(reservation)) {
            NotificationManager.notifyReservationCreated(reservation.getIdReservation());

            String clientName = SessionManager.getInstance().getCurrentUserFullName();
            String clientEmail = SessionManager.getInstance().getCurrentUserEmail();
            if (clientEmail != null && !clientEmail.isEmpty()) {
                EmailSender.sendReservationConfirmation(clientEmail, clientName, reservation.getIdReservation());
            }

            view.toggleFormVisibility();
            loadReservations();
            loadVehicules();
        } else {
            view.showFormError("❌ Erreur lors de la création de la réservation.");
        }
    }

    // ===== VÉRIFIER SI LA RÉSERVATION PEUT ÊTRE ANNULÉE =====
    private boolean peutEtreAnnulee(Reservation reservation) {
        if (reservation == null) return false;

        String statut = reservation.getStatut();

        if (!"EN_ATTENTE".equals(statut) && !"ACCEPTEE".equals(statut)) {
            return false;
        }

        if (reservation.getDateDebut() != null &&
                reservation.getDateDebut().isBefore(LocalDateTime.now())) {
            return false;
        }

        return true;
    }

    // ===== ANNULATION DE RÉSERVATION =====
    private void annulerReservation(Reservation reservation) {
        if (reservation == null) return;

        if (!peutEtreAnnulee(reservation)) {
            NotificationManager.showError("❌ Impossible",
                    "Cette réservation ne peut plus être annulée.\n" +
                            "Statut : " + reservation.getStatut());
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation d'annulation");
        alert.setHeaderText("Annuler la réservation #" + reservation.getIdReservation() + " ?");
        alert.setContentText("Êtes-vous sûr de vouloir annuler cette réservation ?\n" +
                "Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (reservationDAO.updateStatut(reservation.getIdReservation(), "ANNULEE")) {
                NotificationManager.notifyReservationCancelled(reservation.getIdReservation());
                loadReservations();
                loadVehicules();
            } else {
                view.showError("❌ Erreur lors de l'annulation de la réservation.");
            }
        }
    }

    private void updateStats(List<Reservation> reservations) {
        int total = reservations.size();
        int enAttente = 0, acceptees = 0, refusees = 0, annulees = 0;

        for (Reservation r : reservations) {
            switch (r.getStatut()) {
                case "EN_ATTENTE" -> enAttente++;
                case "ACCEPTEE" -> acceptees++;
                case "REFUSEE" -> refusees++;
                case "ANNULEE" -> annulees++;
            }
        }

        view.updateStats(total, enAttente, acceptees, refusees, annulees);
    }

    public void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void handleAnnuler(Reservation reservation) {
        annulerReservation(reservation);
    }
}