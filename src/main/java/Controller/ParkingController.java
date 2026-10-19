package Controller;

import dao.ParkingDAO;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import model.Parking;
import view.ParkingManagementView;

import java.util.List;
import java.util.Optional;

public class ParkingController {

    private ParkingManagementView view;
    private ParkingDAO parkingDAO;

    public ParkingController(ParkingManagementView view) {
        this.view = view;
        this.parkingDAO = new ParkingDAO();

        view.setDeleteCallback(new ParkingManagementView.DeleteCallback() {
            @Override
            public void onDelete(Parking parking) {
                view.getParkingTable().getSelectionModel().select(parking);
                view.showEditForm(parking);
                deleteParking();
            }
        });

        initEventHandlers();
        loadParkings();
    }

    private void initEventHandlers() {
        view.getSearchButton().setOnAction(e -> searchParkings());
        view.getSearchField().setOnAction(e -> searchParkings());
        view.getRefreshButton().setOnAction(e -> loadParkings());
        view.getSaveButton().setOnAction(e -> saveParking());
        view.getCancelButton().setOnAction(e -> view.hideForm());
        view.getDeleteButton().setOnAction(e -> deleteParking());
    }

    private void loadParkings() {
        List<Parking> parkings = parkingDAO.getAll();
        view.refreshTable(parkings);
    }

    private void searchParkings() {
        String keyword = view.getSearchField().getText().trim();
        if (keyword.isEmpty()) {
            loadParkings();
        } else {
            List<Parking> parkings = parkingDAO.search(keyword);
            view.refreshTable(parkings);
        }
    }

    private void saveParking() {
        String nom = view.getNomField().getText().trim();
        String adresse = view.getAdresseField().getText().trim();
        String capaciteStr = view.getCapaciteField().getText().trim();
        boolean actif = view.getActifCheckBox().isSelected();

        // Validation
        if (nom.isEmpty() || adresse.isEmpty() || capaciteStr.isEmpty()) {
            view.showError("⚠️ Tous les champs sont obligatoires !");
            return;
        }

        int capacite;
        try {
            capacite = Integer.parseInt(capaciteStr);
            if (capacite <= 0) {
                view.showError("⚠️ La capacité doit être supérieure à 0 !");
                return;
            }
        } catch (NumberFormatException e) {
            view.showError("⚠️ La capacité doit être un nombre valide !");
            return;
        }

        view.clearError();

        Parking selected = view.getSelectedParking();

        if (selected == null) {
            // Ajout
            if (parkingDAO.nomExists(nom)) {
                view.showError("⚠️ Un parking avec ce nom existe déjà !");
                return;
            }

            Parking newParking = new Parking(nom, adresse, capacite, actif);

            if (parkingDAO.create(newParking)) {
                showSuccess("✅ Parking ajouté avec succès !");
                view.hideForm();
                loadParkings();
            } else {
                view.showError("❌ Erreur lors de l'ajout du parking.");
            }
        } else {
            // Modification
            if (parkingDAO.nomExistsExcept(nom, selected.getIdParking())) {
                view.showError("⚠️ Un parking avec ce nom existe déjà !");
                return;
            }

            selected.setNom(nom);
            selected.setAdresse(adresse);
            selected.setCapacite(capacite);
            selected.setActif(actif);

            if (parkingDAO.update(selected)) {
                showSuccess("✅ Parking modifié avec succès !");
                view.hideForm();
                loadParkings();
            } else {
                view.showError("❌ Erreur lors de la modification du parking.");
            }
        }
    }

    private void deleteParking() {
        Parking parking = view.getSelectedParking();
        if (parking == null) {
            Parking selected = view.getParkingTable().getSelectionModel().getSelectedItem();
            if (selected != null) {
                parking = selected;
                view.showEditForm(parking);
            } else {
                view.showError("⚠️ Veuillez sélectionner un parking dans le tableau !");
                return;
            }
        }

        // Vérifier si des véhicules sont stationnés dans ce parking
        // TODO: Ajouter cette vérification

        String message = parking.isActif()
                ? "Cette action marquera le parking comme inactif.\nVoulez-vous continuer ?"
                : "Ce parking est déjà inactif.\nVoulez-vous le supprimer définitivement ?\n\n⚠️ Cette action est irréversible !";

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le parking " + parking.getNom() + " ?");
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success;
            if (parking.isActif()) {
                success = parkingDAO.delete(parking.getIdParking());
                if (success) {
                    showSuccess("✅ Parking désactivé avec succès !");
                }
            } else {
                success = parkingDAO.deletePermanently(parking.getIdParking());
                if (success) {
                    showSuccess("✅ Parking supprimé définitivement !");
                }
            }

            if (success) {
                view.hideForm();
                loadParkings();
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
