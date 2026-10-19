package Controller;

import dao.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import model.*;
import utils.FormatUtil;
import utils.NotificationManager;
import view.ReservationManagementView;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class ReservationController {

    private ReservationManagementView view;
    private ReservationDAO reservationDAO;
    private VehiculeDAO vehiculeDAO;
    private ClientDao clientDAO;
    private ParkingDAO parkingDAO;
    private ContratDAO contratDAO;

    public ReservationController(ReservationManagementView view) {
        this.view = view;
        this.reservationDAO = new ReservationDAO();
        this.vehiculeDAO = new VehiculeDAO();
        this.clientDAO = new ClientDao();
        this.parkingDAO = new ParkingDAO();
        this.contratDAO = new ContratDAO();

        view.setCallback(new ReservationManagementView.ReservationCallback() {
            @Override
            public void onAccept(Reservation reservation) {
                acceptReservation(reservation);
            }

            @Override
            public void onRefuse(Reservation reservation) {
                refuseReservation(reservation);
            }

            @Override
            public void onCancel(Reservation reservation) {
                cancelReservation(reservation);
            }

            @Override
            public void onDelete(Reservation reservation) {
                deleteReservation(reservation);
            }

            @Override
            public void onCreateReservation(int clientId, int vehiculeId, LocalDateTime dateDebut,
                                            LocalDateTime dateFin, String lieuPriseEnCharge, String lieuRetour) {
                creerReservation(clientId, vehiculeId, dateDebut, dateFin, lieuPriseEnCharge, lieuRetour);
            }

            @Override
            public void onCreateClient(String nom, String prenom, String email, String telephone, String adresse) {
                creerNouveauClient(nom, prenom, email, telephone, adresse);
            }
        });

        initEventHandlers();
        loadReservations();
    }

    private void initEventHandlers() {
        view.getSearchButton().setOnAction(e -> searchReservations());
        view.getSearchField().setOnAction(e -> searchReservations());
        view.getRefreshButton().setOnAction(e -> loadReservations());
        view.getStatutFilterComboBox().setOnAction(e -> filterByStatut());
        view.getSubmitReservationButton().setOnAction(e -> handleCreateReservation());
        view.getSaveNewClientButton().setOnAction(e -> handleCreateNewClient());
        view.getCancelNewClientButton().setOnAction(e -> view.hideNewClientForm());
    }

    private void loadReservations() {
        List<Reservation> reservations = reservationDAO.getAll();
        view.refreshTable(reservations);
        updateStats(reservations);
    }

    private void searchReservations() {
        String keyword = view.getSearchField().getText().trim();
        if (keyword.isEmpty()) {
            loadReservations();
        } else {
            List<Reservation> reservations = reservationDAO.search(keyword);
            view.refreshTable(reservations);
            updateStats(reservations);
        }
    }

    private void filterByStatut() {
        String statut = view.getStatutFilterComboBox().getValue();
        List<Reservation> reservations;

        if (statut == null || "Tous".equals(statut)) {
            reservations = reservationDAO.getAll();
        } else {
            reservations = reservationDAO.getByStatut(statut);
        }

        view.refreshTable(reservations);
        updateStats(reservations);
    }

    // ===== CRÉATION D'UN NOUVEAU CLIENT (WALK-IN) =====
    private void handleCreateNewClient() {
        String nom = view.getNewNomField().getText().trim();
        String prenom = view.getNewPrenomField().getText().trim();
        String email = view.getNewEmailField().getText().trim();
        String telephone = view.getNewTelephoneField().getText().trim();
        String adresse = view.getNewAdresseField().getText().trim();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || telephone.isEmpty()) {
            view.showNewClientError("⚠️ Les champs Nom, Prénom, Email et Téléphone sont obligatoires !");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            view.showNewClientError("⚠️ Format d'email invalide !");
            return;
        }

        if (!telephone.matches("^[0-9]{9,10}$")) {
            view.showNewClientError("⚠️ Format de téléphone invalide (9-10 chiffres) !");
            return;
        }

        if (clientDAO.emailExists(email)) {
            view.showNewClientError("⚠️ Cet email est déjà utilisé !");
            return;
        }

        view.clearNewClientError();

        Client newClient = new Client(nom, prenom, email, telephone, adresse, true);

        if (clientDAO.create(newClient)) {
            NotificationManager.showSuccess("✅ Client créé",
                    "Le client " + prenom + " " + nom + " a été créé !");
            view.hideNewClientForm();
            loadClientsVehiculesParkings();

            for (Client c : view.getClientComboBox().getItems()) {
                if (c.getIdClient() == newClient.getIdClient()) {
                    view.getClientComboBox().setValue(c);
                    break;
                }
            }
        } else {
            view.showNewClientError("❌ Erreur lors de la création du client.");
        }
    }

    // ===== CRÉATION DE RÉSERVATION (DIRECTEMENT ACCEPTÉE) =====
    private void handleCreateReservation() {
        Client client = view.getClientComboBox().getValue();
        Vehicule vehicule = view.getVehiculeComboBox().getValue();
        LocalDateTime dateDebut = view.getDateDebutPicker().getValue() != null
                ? view.getDateDebutPicker().getValue().atTime(LocalTime.of(10, 0))
                : null;
        LocalDateTime dateFin = view.getDateFinPicker().getValue() != null
                ? view.getDateFinPicker().getValue().atTime(LocalTime.of(10, 0))
                : null;

        Parking parkingPrise = view.getLieuPriseEnChargeComboBox().getValue();
        Parking parkingRetour = view.getLieuRetourComboBox().getValue();

        if (client == null) {
            view.showFormError("⚠️ Veuillez sélectionner un client !");
            return;
        }

        if (vehicule == null) {
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

        if (parkingPrise == null) {
            view.showFormError("⚠️ Veuillez sélectionner un lieu de prise en charge !");
            return;
        }

        if (parkingRetour == null) {
            view.showFormError("⚠️ Veuillez sélectionner un lieu de retour !");
            return;
        }

        view.clearFormError();

        if (!reservationDAO.isVehiculeAvailable(vehicule.getIdVehicule(), dateDebut, dateFin)) {
            view.showFormError("⚠️ Ce véhicule a déjà une réservation sur cette période !");
            return;
        }

        String lieuPriseEnCharge = parkingPrise.getNom();
        String lieuRetour = parkingRetour.getNom();

        // ===== CRÉATION AVEC STATUT ACCEPTEE DIRECTEMENT =====
        Reservation reservation = new Reservation(
                client.getIdClient(),
                vehicule.getIdVehicule(),
                dateDebut,
                dateFin,
                "ACCEPTEE",
                lieuPriseEnCharge,
                lieuRetour
        );

        if (reservationDAO.create(reservation)) {
            // ===== METTRE LE VÉHICULE EN RESERVE =====
            vehiculeDAO.updateStatut(vehicule.getIdVehicule(), "RESERVE");
            System.out.println("✅ Véhicule #" + vehicule.getIdVehicule() + " → RESERVE");

            // ===== CRÉATION AUTOMATIQUE DU CONTRAT =====
            creerContratAutomatique(reservation, vehicule);

            NotificationManager.showSuccess("✅ Réservation validée",
                    "La réservation N°" + reservation.getIdReservation() + " a été créée et validée pour " +
                            client.getPrenom() + " " + client.getNom() + "\n" +
                            "📄 Un contrat a été généré automatiquement.");

            view.toggleFormVisibility();
            loadReservations();
        } else {
            view.showFormError("❌ Erreur lors de la création de la réservation.");
        }
    }

    private void creerReservation(int clientId, int vehiculeId, LocalDateTime dateDebut,
                                  LocalDateTime dateFin, String lieuPriseEnCharge, String lieuRetour) {
        Reservation reservation = new Reservation(
                clientId, vehiculeId, dateDebut, dateFin, "ACCEPTEE", lieuPriseEnCharge, lieuRetour
        );

        if (reservationDAO.create(reservation)) {
            Vehicule vehicule = vehiculeDAO.getById(vehiculeId);
            if (vehicule != null) {
                vehiculeDAO.updateStatut(vehiculeId, "RESERVE");
                creerContratAutomatique(reservation, vehicule);
            }

            NotificationManager.showSuccess("✅ Réservation créée",
                    "La réservation N°" + reservation.getIdReservation() + " a été créée !");
            loadReservations();
        }
    }

    private void creerNouveauClient(String nom, String prenom, String email, String telephone, String adresse) {
        if (clientDAO.emailExists(email)) {
            view.showNewClientError("⚠️ Cet email est déjà utilisé !");
            return;
        }

        Client newClient = new Client(nom, prenom, email, telephone, adresse, true);

        if (clientDAO.create(newClient)) {
            NotificationManager.showSuccess("✅ Client créé",
                    "Le client " + prenom + " " + nom + " a été créé !");
            view.hideNewClientForm();
            loadClientsVehiculesParkings();
        } else {
            view.showNewClientError("❌ Erreur lors de la création du client.");
        }
    }

    private void loadClientsVehiculesParkings() {
        List<Client> clients = clientDAO.getAll();
        view.getClientComboBox().getItems().clear();
        view.getClientComboBox().getItems().addAll(clients);

        List<Vehicule> vehicules = vehiculeDAO.getVehiculesActifs();
        view.getVehiculeComboBox().getItems().clear();
        view.getVehiculeComboBox().getItems().addAll(vehicules);

        List<Parking> parkings = parkingDAO.getAll();
        view.getLieuPriseEnChargeComboBox().getItems().clear();
        view.getLieuPriseEnChargeComboBox().getItems().addAll(parkings);
        view.getLieuRetourComboBox().getItems().clear();
        view.getLieuRetourComboBox().getItems().addAll(parkings);
    }

    // ===== ACCEPTER UNE RÉSERVATION + CRÉATION AUTOMATIQUE DU CONTRAT =====
    private void acceptReservation(Reservation reservation) {
        if (reservation == null) return;

        Vehicule vehicule = vehiculeDAO.getById(reservation.getIdVehicule());
        if (vehicule == null) {
            NotificationManager.showError("❌ Erreur", "Véhicule non trouvé !");
            return;
        }

        if (!reservationDAO.isVehiculeAvailableExcept(
                reservation.getIdVehicule(),
                reservation.getDateDebut(),
                reservation.getDateFin(),
                reservation.getIdReservation())) {
            NotificationManager.showError("❌ Erreur",
                    "Ce véhicule a déjà une autre réservation sur cette période !");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Accepter la réservation #" + reservation.getIdReservation() + " ?");
        alert.setContentText(
                "Véhicule : " + vehicule.getMarque() + " " + vehicule.getModele() + "\n" +
                        "Période : " + reservation.getDateDebut().toLocalDate() +
                        " → " + reservation.getDateFin().toLocalDate() + "\n\n" +
                        "📄 Un contrat sera automatiquement créé."
        );

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (reservationDAO.updateStatut(reservation.getIdReservation(), "ACCEPTEE")) {
                vehiculeDAO.updateStatut(vehicule.getIdVehicule(), "RESERVE");
                creerContratAutomatique(reservation, vehicule);

                NotificationManager.showSuccess("✅ Acceptée",
                        "Réservation #" + reservation.getIdReservation() + " acceptée !\n" +
                                "Contrat créé automatiquement.");
                loadReservations();
            } else {
                NotificationManager.showError("❌ Erreur", "Erreur lors de l'acceptation.");
            }
        }
    }

    // ===== MÉTHODE DE CRÉATION AUTOMATIQUE DU CONTRAT =====
    private void creerContratAutomatique(Reservation reservation, Vehicule vehicule) {
        if (contratDAO.reservationHasContrat(reservation.getIdReservation())) {
            System.out.println("ℹ️ Un contrat existe déjà pour la réservation #" + reservation.getIdReservation());
            return;
        }

        int nombreJours = (int) java.time.Duration.between(
                reservation.getDateDebut(),
                reservation.getDateFin()
        ).toDays() + 1;

        double prixJournalier = vehicule.getPrixJournalier();
        double sousTotal = prixJournalier * nombreJours;
        double montantTotal = sousTotal;

        Contrat contrat = new Contrat();
        contrat.setIdReservation(reservation.getIdReservation());
        contrat.setDateSignature(LocalDateTime.now());
        contrat.setDateDebutEffective(reservation.getDateDebut());
        contrat.setDateFinEffective(reservation.getDateFin());
        contrat.setEtatRetour("BON");
        contrat.setKilometrageFinal(0);
        contrat.setMontantTotal(montantTotal);
        contrat.setMontantProvisoire(montantTotal);   // ← MONTANT PROVISOIRE
        contrat.setActif(true);

        contrat.setNombreJours(nombreJours);
        contrat.setPrixJournalier(prixJournalier);
        contrat.setSousTotal(sousTotal);
        contrat.setFraisRetourDiffere(0);
        contrat.setFraisCarburant(0);
        contrat.setFraisRetard(0);
        contrat.setFraisDegradation(0);
        contrat.setRemise(0);

        if (contratDAO.create(contrat)) {
            System.out.println("✅ Contrat #" + contrat.getIdContrat() +
                    " créé automatiquement pour la réservation #" + reservation.getIdReservation());
            NotificationManager.showInfo("📄 Contrat créé",
                    "Contrat #" + contrat.getIdContrat() + " créé automatiquement.\n" +
                            "Montant provisoire : " + FormatUtil.formatFCFA(montantTotal));
        } else {
            NotificationManager.showError("❌ Erreur", "Erreur lors de la création du contrat.");
        }
    }

    // ===== REFUSER UNE RÉSERVATION =====
    private void refuseReservation(Reservation reservation) {
        if (reservation == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Refuser la réservation #" + reservation.getIdReservation() + " ?");
        alert.setContentText("Voulez-vous refuser cette demande de réservation ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int vehiculeId = reservation.getIdVehicule();

            if (reservationDAO.updateStatut(reservation.getIdReservation(), "REFUSEE")) {
                libererVehiculeAutomatiquement(vehiculeId);

                NotificationManager.showSuccess("❌ Refusée",
                        "Réservation #" + reservation.getIdReservation() + " refusée.");
                loadReservations();
            } else {
                NotificationManager.showError("❌ Erreur", "Erreur lors du refus.");
            }
        }
    }

    // ===== ANNULER UNE RÉSERVATION =====
    private void cancelReservation(Reservation reservation) {
        if (reservation == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Annuler la réservation #" + reservation.getIdReservation() + " ?");
        alert.setContentText("Voulez-vous annuler cette réservation ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int vehiculeId = reservation.getIdVehicule();

            if (reservationDAO.updateStatut(reservation.getIdReservation(), "ANNULEE")) {
                libererVehiculeAutomatiquement(vehiculeId);

                NotificationManager.showSuccess("🚫 Annulée",
                        "Réservation #" + reservation.getIdReservation() + " annulée.");
                loadReservations();
            } else {
                NotificationManager.showError("❌ Erreur", "Erreur lors de l'annulation.");
            }
        }
    }

    // ===== SUPPRIMER UNE RÉSERVATION (LIBÉRATION APRÈS SUPPRESSION) =====
    private void deleteReservation(Reservation reservation) {
        if (reservation == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la réservation #" + reservation.getIdReservation() + " ?");
        alert.setContentText("Cette action est définitive et irréversible !");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int vehiculeId = reservation.getIdVehicule();

            // ===== SUPPRIMER D'ABORD LA RÉSERVATION =====
            if (reservationDAO.delete(reservation.getIdReservation())) {
                System.out.println("🗑️ Réservation #" + reservation.getIdReservation() + " supprimée");

                // ===== PUIS LIBÉRER LE VÉHICULE =====
                libererVehiculeAutomatiquement(vehiculeId);

                NotificationManager.showSuccess("🗑️ Supprimée",
                        "Réservation #" + reservation.getIdReservation() + " supprimée.\n" +
                                "Le véhicule est maintenant disponible.");
                loadReservations();
            } else {
                NotificationManager.showError("❌ Erreur", "Erreur lors de la suppression.");
            }
        }
    }

    // ===== LIBÉRATION AUTOMATIQUE DU VÉHICULE =====
    private void libererVehiculeAutomatiquement(int vehiculeId) {
        System.out.println("🔍 Vérification du véhicule #" + vehiculeId);

        List<Reservation> reservations = reservationDAO.getByVehicule(vehiculeId);

        System.out.println("   Réservations restantes : " + reservations.size());

        boolean hasActiveReservation = false;
        for (Reservation r : reservations) {
            System.out.println("   - Réservation #" + r.getIdReservation() + " : " + r.getStatut());
            if ("ACCEPTEE".equals(r.getStatut()) || "EN_ATTENTE".equals(r.getStatut())) {
                hasActiveReservation = true;
                break;
            }
        }

        if (!hasActiveReservation) {
            Vehicule vehicule = vehiculeDAO.getById(vehiculeId);
            if (vehicule != null) {
                System.out.println("   Statut actuel : " + vehicule.getStatut());

                if ("RESERVE".equals(vehicule.getStatut()) || "LOUE".equals(vehicule.getStatut())) {
                    boolean success = vehiculeDAO.updateStatut(vehiculeId, "DISPONIBLE");
                    if (success) {
                        System.out.println("   ✅ Véhicule #" + vehiculeId + " → DISPONIBLE");
                    } else {
                        System.out.println("   ❌ Échec de la mise à jour");
                    }
                } else {
                    System.out.println("   ℹ️ Véhicule déjà DISPONIBLE ou autre statut");
                }
            } else {
                System.out.println("   ❌ Véhicule #" + vehiculeId + " non trouvé !");
            }
        } else {
            System.out.println("   ℹ️ Véhicule #" + vehiculeId + " a encore des réservations actives");
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
}