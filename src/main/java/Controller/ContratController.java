package Controller;

import dao.ClientDao;
import dao.ContratDAO;
import dao.FactureDAO;
import dao.ReservationDAO;
import dao.SanctionDAO;
import dao.VehiculeDAO;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import model.Client;
import model.Contrat;
import model.Facture;
import model.Reservation;
import model.Sanction;
import model.Vehicule;
import utils.FormatUtil;
import utils.NotificationManager;
import utils.PDFGenerator;
import view.ContratManagementView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ContratController {

    private ContratManagementView view;
    private ContratDAO contratDAO;
    private ReservationDAO reservationDAO;
    private ClientDao clientDAO;
    private VehiculeDAO vehiculeDAO;
    private FactureDAO factureDAO;
    private SanctionDAO sanctionDAO;

    public ContratController(ContratManagementView view) {
        this.view = view;
        this.contratDAO = new ContratDAO();
        this.reservationDAO = new ReservationDAO();
        this.clientDAO = new ClientDao();
        this.vehiculeDAO = new VehiculeDAO();
        this.factureDAO = new FactureDAO();
        this.sanctionDAO = new SanctionDAO();

        view.setCallback(new ContratManagementView.ContratCallback() {
            @Override
            public void onCloturer(Contrat contrat) {
                showCloturerDialog(contrat);
            }

            @Override
            public void onGenererFacture(Contrat contrat) {
                genererFacture(contrat);
            }

            @Override
            public void onDelete(Contrat contrat) {
                deleteContrat(contrat);
            }
        });

        initEventHandlers();
        loadContrats();
    }

    private void initEventHandlers() {
        view.getSearchButton().setOnAction(e -> searchContrats());
        view.getSearchField().setOnAction(e -> searchContrats());

        view.getRefreshButton().setOnAction(e -> {
            System.out.println("🔄 Rafraîchissement des contrats...");
            loadContrats();
        });

        view.getFilterComboBox().setOnAction(e -> filterContrats());
    }

    private void loadContrats() {
        List<Contrat> contrats = contratDAO.getAll();
        view.refreshTable(contrats);
        System.out.println("📄 " + contrats.size() + " contrats chargés");
    }

    private void searchContrats() {
        String keyword = view.getSearchField().getText().trim();
        if (keyword.isEmpty()) {
            loadContrats();
        } else {
            List<Contrat> contrats = contratDAO.search(keyword);
            view.refreshTable(contrats);
        }
    }

    private void filterContrats() {
        String filter = view.getFilterComboBox().getValue();
        List<Contrat> contrats;

        if (filter == null || "Tous".equals(filter)) {
            contrats = contratDAO.getAll();
        } else if ("Actifs".equals(filter)) {
            contrats = contratDAO.getActiveContrats();
        } else {
            contrats = contratDAO.getAll();
        }

        view.refreshTable(contrats);
    }

    // ===== CLÔTURE DU CONTRAT AVEC GÉNÉRATION AUTO FACTURE + SANCTION =====
    private void showCloturerDialog(Contrat contrat) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Clôture du contrat");
        dialog.setHeaderText("Clôturer le contrat #" + contrat.getIdContrat());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        int row = 0;

        ComboBox<String> etatRetourCombo = new ComboBox<>();
        etatRetourCombo.getItems().addAll("BON", "MOYEN", "MAUVAIS");
        etatRetourCombo.setValue("BON");
        grid.add(new Label("État du retour * :"), 0, row);
        grid.add(etatRetourCombo, 1, row);

        row++;
        TextField kilometrageField = new TextField();
        kilometrageField.setPromptText("Kilométrage final");
        grid.add(new Label("Kilométrage final * :"), 0, row);
        grid.add(kilometrageField, 1, row);

        row++;
        int nombreJours = (int) java.time.Duration.between(
                contrat.getDateDebutEffective(),
                contrat.getDateFinEffective()
        ).toDays() + 1;
        Label nombreJoursLabel = new Label(nombreJours + " jour(s)");
        nombreJoursLabel.setStyle("-fx-font-weight: bold;");
        grid.add(new Label("Nombre de jours :"), 0, row);
        grid.add(nombreJoursLabel, 1, row);

        row++;
        TextField prixJournalierField = new TextField(String.valueOf((int) contrat.getPrixJournalier()));
        grid.add(new Label("Prix journalier :"), 0, row);
        grid.add(prixJournalierField, 1, row);

        row++;
        grid.add(new Separator(), 0, row, 2, 1);

        row++;
        Label montantProvisoireLabel = new Label("💰 MONTANT PROVISOIRE (à la réservation)");
        montantProvisoireLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #3498db; -fx-font-size: 13;");
        grid.add(montantProvisoireLabel, 0, row, 2, 1);

        row++;
        Label montantProvisoireValue = new Label(FormatUtil.formatFCFA(contrat.getMontantProvisoire()));
        montantProvisoireValue.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14;");
        grid.add(new Label("Montant initial :"), 0, row);
        grid.add(montantProvisoireValue, 1, row);

        row++;
        grid.add(new Separator(), 0, row, 2, 1);

        row++;
        Label fraisLabel = new Label("➕ FRAIS SUPPLÉMENTAIRES (FCFA)");
        fraisLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c; -fx-font-size: 13;");
        grid.add(fraisLabel, 0, row, 2, 1);

        row++;
        TextField fraisRetourField = new TextField("0");
        grid.add(new Label("Frais retour différé :"), 0, row);
        grid.add(fraisRetourField, 1, row);

        row++;
        TextField fraisCarburantField = new TextField("0");
        grid.add(new Label("Frais carburant :"), 0, row);
        grid.add(fraisCarburantField, 1, row);

        row++;
        TextField fraisRetardField = new TextField("0");
        grid.add(new Label("Frais retard :"), 0, row);
        grid.add(fraisRetardField, 1, row);

        row++;
        TextField fraisDegradationField = new TextField("0");
        grid.add(new Label("Frais dégradation :"), 0, row);
        grid.add(fraisDegradationField, 1, row);

        row++;
        TextField remiseField = new TextField("0");
        grid.add(new Label("Remise :"), 0, row);
        grid.add(remiseField, 1, row);

        row++;
        grid.add(new Separator(), 0, row, 2, 1);

        row++;
        Label recapLabel = new Label("📊 RÉCAPITULATIF");
        recapLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14;");
        grid.add(recapLabel, 0, row, 2, 1);

        row++;
        Label montantTotalLabel = new Label("0 FCFA");
        montantTotalLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2ecc71; -fx-font-size: 18;");
        grid.add(new Label("MONTANT FINAL :"), 0, row);
        grid.add(montantTotalLabel, 1, row);

        row++;
        Label variationLabel = new Label("");
        variationLabel.setStyle("-fx-font-size: 12; -fx-font-style: italic;");
        grid.add(variationLabel, 0, row, 2, 1);

        double montantProvisoire = contrat.getMontantProvisoire();

        Runnable calculerMontant = () -> {
            try {
                double prix = parseDouble(prixJournalierField.getText());
                double fraisRetour = parseDouble(fraisRetourField.getText());
                double fraisCarburant = parseDouble(fraisCarburantField.getText());
                double fraisRetard = parseDouble(fraisRetardField.getText());
                double fraisDegradation = parseDouble(fraisDegradationField.getText());
                double remise = parseDouble(remiseField.getText());

                double sousTotal = prix * nombreJours;
                double montantTotal = sousTotal + fraisRetour + fraisCarburant +
                        fraisRetard + fraisDegradation - remise;

                montantTotalLabel.setText(FormatUtil.formatFCFA(montantTotal));

                double variation = montantTotal - montantProvisoire;
                if (variation > 0) {
                    variationLabel.setText("⬆️ +" + FormatUtil.formatFCFA(variation) + " (frais supplémentaires)");
                    variationLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12; -fx-font-style: italic;");
                } else if (variation < 0) {
                    variationLabel.setText("⬇️ " + FormatUtil.formatFCFA(variation) + " (remise appliquée)");
                    variationLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 12; -fx-font-style: italic;");
                } else {
                    variationLabel.setText("➡️ Aucun changement");
                    variationLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12; -fx-font-style: italic;");
                }
            } catch (Exception e) {
                // Ignorer
            }
        };

        prixJournalierField.textProperty().addListener((obs, old, val) -> calculerMontant.run());
        fraisRetourField.textProperty().addListener((obs, old, val) -> calculerMontant.run());
        fraisCarburantField.textProperty().addListener((obs, old, val) -> calculerMontant.run());
        fraisRetardField.textProperty().addListener((obs, old, val) -> calculerMontant.run());
        fraisDegradationField.textProperty().addListener((obs, old, val) -> calculerMontant.run());
        remiseField.textProperty().addListener((obs, old, val) -> calculerMontant.run());

        calculerMontant.run();

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String etatRetour = etatRetourCombo.getValue();
            String kmStr = kilometrageField.getText().trim();

            if (etatRetour == null || kmStr.isEmpty()) {
                NotificationManager.showError("⚠️ Erreur", "Tous les champs sont obligatoires !");
                return;
            }

            int kilometrageFinal;
            try {
                kilometrageFinal = Integer.parseInt(kmStr);
            } catch (NumberFormatException e) {
                NotificationManager.showError("⚠️ Erreur", "Le kilométrage doit être un nombre valide !");
                return;
            }

            double prix = parseDouble(prixJournalierField.getText());
            double fraisRetour = parseDouble(fraisRetourField.getText());
            double fraisCarburant = parseDouble(fraisCarburantField.getText());
            double fraisRetard = parseDouble(fraisRetardField.getText());
            double fraisDegradation = parseDouble(fraisDegradationField.getText());
            double remise = parseDouble(remiseField.getText());

            if (contratDAO.cloturer(contrat.getIdContrat(), etatRetour, kilometrageFinal)) {
                contratDAO.updateMontant(contrat.getIdContrat(), nombreJours, prix,
                        fraisRetour, fraisCarburant, fraisRetard, fraisDegradation, remise);

                // ===== LIBÉRATION AUTOMATIQUE DU VÉHICULE =====
                libererVehiculeApresCloture(contrat.getIdReservation());

                // ===== GÉNÉRATION AUTOMATIQUE DE LA FACTURE =====
                genererFactureAutomatique(contrat.getIdContrat());

                // ===== CRÉATION AUTOMATIQUE DE SANCTION (si dégradation) =====
                if (fraisDegradation > 0) {
                    creerSanctionAutomatique(contrat, fraisDegradation, etatRetour);
                }

                double sousTotal = prix * nombreJours;
                double montantTotal = sousTotal + fraisRetour + fraisCarburant +
                        fraisRetard + fraisDegradation - remise;

                NotificationManager.showSuccess("✅ Clôturé",
                        "Contrat #" + contrat.getIdContrat() + " clôturé !\n" +
                                "Montant total : " + FormatUtil.formatFCFA(montantTotal) + "\n" +
                                "📄 Facture générée automatiquement" +
                                (fraisDegradation > 0 ? "\n⚖️ Sanction créée pour dégradation" : ""));
                loadContrats();
            } else {
                NotificationManager.showError("❌ Erreur", "Erreur lors de la clôture du contrat.");
            }
        }
    }

    // ===== GÉNÉRATION AUTOMATIQUE DE LA FACTURE =====
    private void genererFactureAutomatique(int contratId) {
        if (factureDAO.contratHasFacture(contratId)) {
            System.out.println("ℹ️ Une facture existe déjà pour le contrat #" + contratId);
            return;
        }

        Contrat contrat = contratDAO.getById(contratId);
        if (contrat == null) return;

        Facture facture = new Facture();
        facture.setIdContrat(contratId);
        facture.setDateEmission(LocalDateTime.now());
        facture.setMontantTotal(contrat.getMontantTotal());
        facture.setStatut("IMPAYEE");
        facture.setMontantPaye(0);
        facture.setResteAPayer(contrat.getMontantTotal());
        facture.setPdfPath(null);
        facture.setDatePaiement(null);

        if (factureDAO.create(facture)) {
            System.out.println("✅ Facture #" + facture.getIdFacture() +
                    " créée automatiquement pour le contrat #" + contratId);
            NotificationManager.showInfo("📄 Facture générée",
                    "Facture #" + facture.getIdFacture() + " créée.\n" +
                            "Montant : " + FormatUtil.formatFCFA(contrat.getMontantTotal()));
        } else {
            NotificationManager.showError("❌ Erreur", "Erreur lors de la création de la facture.");
        }
    }

    // ===== CRÉATION AUTOMATIQUE DE SANCTION =====
    private void creerSanctionAutomatique(Contrat contrat, double fraisDegradation, String etatRetour) {
        if (sanctionDAO.contratHasSanction(contrat.getIdContrat())) {
            System.out.println("ℹ️ Une sanction existe déjà pour le contrat #" + contrat.getIdContrat());
            return;
        }

        Reservation reservation = reservationDAO.getById(contrat.getIdReservation());
        if (reservation == null) {
            System.out.println("❌ Réservation non trouvée pour le contrat #" + contrat.getIdContrat());
            return;
        }

        // ✅ NOUVEAU : récupérer la facture liée au contrat pour renseigner idFacture
        Facture facture = factureDAO.getByContrat(contrat.getIdContrat());

        Sanction sanction = new Sanction();
        sanction.setIdClient(reservation.getIdClient());
        sanction.setIdContrat(contrat.getIdContrat());
        sanction.setIdFacture(facture != null ? facture.getIdFacture() : null);   // ✅
        sanction.setType("DEGRADATION");
        sanction.setMontant(fraisDegradation);
        sanction.setDescription("Frais de dégradation constatés au retour du véhicule. " +
                "État du retour : " + etatRetour);
        sanction.setDateEnregistrement(LocalDateTime.now());
        sanction.setStatut("ACTIVE");

        if (sanctionDAO.create(sanction)) {
            System.out.println("✅ Sanction #" + sanction.getIdSanction() +
                    " créée automatiquement (Montant : " + fraisDegradation + " FCFA)" +
                    " - liée à la facture #" + (facture != null ? facture.getIdFacture() : "N/A"));
            NotificationManager.showWarning("⚖️ Sanction créée",
                    "Sanction #" + sanction.getIdSanction() + " créée pour le client.\n" +
                            "Montant : " + FormatUtil.formatFCFA(fraisDegradation));
        } else {
            NotificationManager.showError("❌ Erreur", "Erreur lors de la création de la sanction.");
        }
    }

    // ===== LIBÉRATION DU VÉHICULE APRÈS CLÔTURE =====
    private void libererVehiculeApresCloture(int reservationId) {
        Reservation reservation = reservationDAO.getById(reservationId);
        if (reservation == null) return;

        Vehicule vehicule = vehiculeDAO.getById(reservation.getIdVehicule());
        if (vehicule != null) {
            System.out.println("🔍 Libération véhicule #" + vehicule.getIdVehicule());
            System.out.println("   Statut actuel : " + vehicule.getStatut());

            if ("RESERVE".equals(vehicule.getStatut()) || "LOUE".equals(vehicule.getStatut())) {
                boolean success = vehiculeDAO.updateStatut(vehicule.getIdVehicule(), "DISPONIBLE");
                if (success) {
                    System.out.println("   ✅ Véhicule #" + vehicule.getIdVehicule() + " → DISPONIBLE");
                }
            }
        }
    }

    // ===== GÉNÉRATION MANUELLE DE FACTURE =====
    private void genererFacture(Contrat contrat) {
        Reservation reservation = reservationDAO.getById(contrat.getIdReservation());
        if (reservation == null) {
            NotificationManager.showError("❌ Erreur", "Réservation non trouvée !");
            return;
        }

        Client client = clientDAO.getById(reservation.getIdClient());
        Vehicule vehicule = vehiculeDAO.getById(reservation.getIdVehicule());

        if (client == null || vehicule == null) {
            NotificationManager.showError("❌ Erreur", "Client ou véhicule non trouvé !");
            return;
        }

        try {
            String pdfPath = PDFGenerator.generateContratPDF(contrat, client, vehicule);
            NotificationManager.showSuccess("📄 Contrat généré",
                    "Le contrat #" + contrat.getIdContrat() + " a été généré en PDF.\n" +
                            "Montant : " + FormatUtil.formatFCFA(contrat.getMontantTotal()) +
                            "\nFichier : " + pdfPath);
        } catch (java.io.IOException | RuntimeException e) {
            NotificationManager.showError("❌ Erreur", "Impossible de générer le PDF : " + e.getMessage());
        }
    }

    private void deleteContrat(Contrat contrat) {
        if (contrat == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le contrat #" + contrat.getIdContrat() + " ?");
        alert.setContentText("Cette action est définitive et irréversible !");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            libererVehiculeApresCloture(contrat.getIdReservation());

            if (contratDAO.delete(contrat.getIdContrat())) {
                NotificationManager.showSuccess("🗑️ Supprimé",
                        "Contrat #" + contrat.getIdContrat() + " supprimé !");
                loadContrats();
            } else {
                NotificationManager.showError("❌ Erreur", "Erreur lors de la suppression.");
            }
        }
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}