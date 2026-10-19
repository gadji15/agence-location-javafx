package Controller;


import dao.VehiculeDAO;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import model.Vehicule;
import view.VehiculeManagementView;

import java.util.List;
import java.util.Optional;

public class VehiculeController {

    private VehiculeManagementView view;
    private VehiculeDAO vehiculeDAO;

    public VehiculeController(VehiculeManagementView view) {
        this.view = view;
        this.vehiculeDAO = new VehiculeDAO();

        // Configurer le callback pour la suppression depuis le tableau
        view.setDeleteCallback(new VehiculeManagementView.DeleteCallback() {
            @Override
            public void onDelete(Vehicule vehicule) {
                view.getVehiculeTable().getSelectionModel().select(vehicule);
                view.showEditForm(vehicule);
                deleteVehicule();
            }
        });

        initEventHandlers();
        loadVehicules();
    }

    private void initEventHandlers() {
        // Recherche
        view.getSearchButton().setOnAction(e -> searchVehicules());
        view.getSearchField().setOnAction(e -> searchVehicules());

        // Rafraîchir
        view.getRefreshButton().setOnAction(e -> loadVehicules());

        // Enregistrer (Ajout/Modification)
        view.getSaveButton().setOnAction(e -> saveVehicule());

        // Annuler
        view.getCancelButton().setOnAction(e -> view.hideForm());

        // Supprimer (bouton du formulaire)
        view.getDeleteButton().setOnAction(e -> deleteVehicule());
    }

    private void loadVehicules() {
        List<Vehicule> vehicules = vehiculeDAO.getAll();
        view.refreshTable(vehicules);
    }

    private void searchVehicules() {
        String keyword = view.getSearchField().getText().trim();
        if (keyword.isEmpty()) {
            loadVehicules();
        } else {
            List<Vehicule> vehicules = vehiculeDAO.search(keyword);
            view.refreshTable(vehicules);
        }
    }

    private void saveVehicule() {
        // Récupérer les données du formulaire
        String marque = view.getMarqueField().getText().trim();
        String modele = view.getModeleField().getText().trim();
        String anneeStr = view.getAnneeField().getText().trim();
        String immatriculation = view.getImmatriculationField().getText().trim().toUpperCase();
        String carburant = view.getCarburantComboBox().getValue();
        String kilometrageStr = view.getKilometrageField().getText().trim();
        String prixStr = view.getPrixField().getText().trim();
        String statut = view.getStatutComboBox().getValue();
        // String parking = view.getParkingComboBox().getValue();
        boolean actif = view.getActifCheckBox().isSelected();

        // Validation des champs obligatoires
        if (marque.isEmpty() || modele.isEmpty() || anneeStr.isEmpty() ||
                immatriculation.isEmpty() || carburant == null || kilometrageStr.isEmpty() ||
                prixStr.isEmpty() || statut == null) {
            view.showError("⚠️ Tous les champs avec * sont obligatoires !");
            return;
        }

        // Validation de l'année
        int annee;
        try {
            annee = Integer.parseInt(anneeStr);
            if (annee < 1900 || annee > 2026) {
                view.showError("⚠️ L'année doit être comprise entre 1900 et 2026 !");
                return;
            }
        } catch (NumberFormatException e) {
            view.showError("⚠️ L'année doit être un nombre valide !");
            return;
        }

        // Validation du kilométrage
        int kilometrage;
        try {
            kilometrage = Integer.parseInt(kilometrageStr);
            if (kilometrage < 0) {
                view.showError("⚠️ Le kilométrage ne peut pas être négatif !");
                return;
            }
        } catch (NumberFormatException e) {
            view.showError("⚠️ Le kilométrage doit être un nombre valide !");
            return;
        }

        // Validation du prix
        double prix;
        try {
            prix = Double.parseDouble(prixStr);
            if (prix <= 0) {
                view.showError("⚠️ Le prix journalier doit être supérieur à 0 !");
                return;
            }
        } catch (NumberFormatException e) {
            view.showError("⚠️ Le prix doit être un nombre valide !");
            return;
        }

        // Validation de l'immatriculation (format: AA-123-AA ou 123-ABC-45)
        if (!immatriculation.matches("^[A-Z0-9\\-]{5,10}$")) {
            view.showError("⚠️ Format d'immatriculation invalide !\n" +
                    "Exemples : AB-123-CD, 123-ABC-45");
            return;
        }

        view.clearError();

        Vehicule selected = view.getSelectedVehicule();

        if (selected == null) {
            // Ajout d'un nouveau véhicule
            if (vehiculeDAO.immatriculationExists(immatriculation)) {
                view.showError("⚠️ Cette immatriculation est déjà utilisée !");
                return;
            }

            Vehicule newVehicule = new Vehicule(
                    marque, modele, annee, immatriculation,
                    carburant, kilometrage, prix, statut,
                    null, actif
            );

            if (vehiculeDAO.create(newVehicule)) {
                showSuccess("✅ Véhicule ajouté avec succès !");
                view.hideForm();
                loadVehicules();
            } else {
                view.showError("❌ Erreur lors de l'ajout du véhicule.");
            }
        } else {
            // Modification d'un véhicule existant
            if (vehiculeDAO.immatriculationExistsExcept(immatriculation, selected.getIdVehicule())) {
                view.showError("⚠️ Cette immatriculation est déjà utilisée par un autre véhicule !");
                return;
            }

            selected.setMarque(marque);
            selected.setModele(modele);
            selected.setAnnee(annee);
            selected.setImmatriculation(immatriculation);
            selected.setCarburant(carburant);
            selected.setKilometrage(kilometrage);
            selected.setPrixJournalier(prix);
            selected.setStatut(statut);
            // selected.setIdParking(null);
            selected.setActif(actif);

            if (vehiculeDAO.update(selected)) {
                showSuccess("✅ Véhicule modifié avec succès !");
                view.hideForm();
                loadVehicules();
            } else {
                view.showError("❌ Erreur lors de la modification du véhicule.");
            }
        }
    }

    private void deleteVehicule() {
        Vehicule vehicule = view.getSelectedVehicule();
        if (vehicule == null) {
            Vehicule selected = view.getVehiculeTable().getSelectionModel().getSelectedItem();
            if (selected != null) {
                vehicule = selected;
                view.showEditForm(vehicule);
            } else {
                view.showError("⚠️ Veuillez sélectionner un véhicule dans le tableau !");
                return;
            }
        }

        // Vérifier si le véhicule est loué
        if ("LOUE".equals(vehicule.getStatut())) {
            view.showError("⚠️ Ce véhicule est actuellement loué !\n" +
                    "Impossible de supprimer un véhicule en cours de location.");
            return;
        }

        String message = vehicule.isActif()
                ? "Cette action marquera le véhicule comme inactif.\nVoulez-vous continuer ?"
                : "Ce véhicule est déjà inactif.\nVoulez-vous le supprimer définitivement ?\n\n⚠️ Cette action est irréversible !";

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le véhicule " + vehicule.getFullName() + " ?");
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success;
            if (vehicule.isActif()) {
                success = vehiculeDAO.delete(vehicule.getIdVehicule());
                if (success) {
                    showSuccess("✅ Véhicule désactivé avec succès !");
                }
            } else {
                success = vehiculeDAO.deletePermanently(vehicule.getIdVehicule());
                if (success) {
                    showSuccess("✅ Véhicule supprimé définitivement !");
                }
            }

            if (success) {
                view.hideForm();
                loadVehicules();
            } else {
                view.showError("❌ Erreur lors de la suppression.");
            }
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}