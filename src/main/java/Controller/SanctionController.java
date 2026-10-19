package Controller;


import dao.ClientDao;
import dao.ContratDAO;
import dao.SanctionDAO;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import model.Client;
import model.Sanction;
import view.SanctionManagementView;

import java.util.List;
import java.util.Optional;

public class SanctionController {

    private SanctionManagementView view;
    private SanctionDAO sanctionDAO;
    private ClientDao clientDAO;
    private ContratDAO contratDAO;

    public SanctionController(SanctionManagementView view) {
        this.view = view;
        this.sanctionDAO = new SanctionDAO();
        this.clientDAO = new ClientDao();
        this.contratDAO = new ContratDAO();

        // Configurer les callbacks
        view.setCallback(new SanctionManagementView.SanctionCallback() {
            @Override
            public void onSave(Sanction sanction) {
                saveSanction(sanction);
            }

            @Override
            public void onUpdate(Sanction sanction) {
                updateSanction(sanction);
            }

            @Override
            public void onDelete(Sanction sanction) {
                deleteSanction(sanction);
            }

            @Override
            public void onLever(Sanction sanction) {
                leverSanction(sanction);
            }

            @Override
            public void onReduire(Sanction sanction) {
                reduireSanction(sanction);
            }
        });

        initEventHandlers();
        loadSanctions();
    }

    private void initEventHandlers() {
        // Recherche
        view.getSearchButton().setOnAction(e -> searchSanctions());
        view.getSearchField().setOnAction(e -> searchSanctions());

        // Rafraîchir
        view.getRefreshButton().setOnAction(e -> loadSanctions());

        // Filtre par statut
        view.getStatutFilterComboBox().setOnAction(e -> filterByStatut());

        // Enregistrer (Ajout/Modification)
        view.getSaveButton().setOnAction(e -> saveOrUpdateSanction());

        // Annuler
        view.getCancelButton().setOnAction(e -> view.hideForm());

        // Supprimer
        view.getDeleteButton().setOnAction(e -> {
            Sanction selected = view.getSelectedSanction();
            if (selected != null) deleteSanction(selected);
        });
    }

    private void loadSanctions() {
        List<Sanction> sanctions = sanctionDAO.getAll();
        view.refreshTable(sanctions);
        updateStats(sanctions);
    }

    private void searchSanctions() {
        String keyword = view.getSearchField().getText().trim();
        if (keyword.isEmpty()) {
            loadSanctions();
        } else {
            List<Sanction> sanctions = sanctionDAO.search(keyword);
            view.refreshTable(sanctions);
            updateStats(sanctions);
        }
    }

    private void filterByStatut() {
        String statut = view.getStatutFilterComboBox().getValue();
        List<Sanction> sanctions;

        if (statut == null || "Tous".equals(statut)) {
            sanctions = sanctionDAO.getAll();
        } else {
            sanctions = sanctionDAO.getByStatut(statut);
        }

        view.refreshTable(sanctions);
        updateStats(sanctions);
    }

    private void saveOrUpdateSanction() {
        Sanction selected = view.getSelectedSanction();

        if (selected == null) {
            // Ajout d'une nouvelle sanction
            Sanction newSanction = getSanctionFromForm();
            if (newSanction != null) {
                saveSanction(newSanction);
            }
        } else {
            // Modification d'une sanction existante
            Sanction updatedSanction = getSanctionFromForm();
            if (updatedSanction != null) {
                updatedSanction.setIdSanction(selected.getIdSanction());
                updateSanction(updatedSanction);
            }
        }
    }

    private Sanction getSanctionFromForm() {
        String clientStr = view.getClientSearchField().getText().trim();
        String type = view.getTypeComboBox().getValue();
        String montantStr = view.getMontantField().getText().trim();
        String description = view.getDescriptionArea().getText().trim();
        String statut = view.getStatutComboBox().getValue();

        // Validation
        if (clientStr.isEmpty() || type == null || montantStr.isEmpty() || description.isEmpty() || statut == null) {
            view.showError("⚠️ Tous les champs sont obligatoires !");
            return null;
        }

        int idClient;
        try {
            idClient = Integer.parseInt(clientStr);
            Client client = clientDAO.getById(idClient);
            if (client == null) {
                view.showError("⚠️ Client ID " + idClient + " non trouvé !");
                return null;
            }
        } catch (NumberFormatException e) {
            view.showError("⚠️ L'ID du client doit être un nombre valide !");
            return null;
        }

        double montant;
        try {
            montant = Double.parseDouble(montantStr);
            if (montant < 0) {
                view.showError("⚠️ Le montant ne peut pas être négatif !");
                return null;
            }
        } catch (NumberFormatException e) {
            view.showError("⚠️ Le montant doit être un nombre valide !");
            return null;
        }

        view.clearError();

        return new Sanction(idClient, null, type, montant, description, statut);
    }

    private void saveSanction(Sanction sanction) {
        if (sanction == null) return;

        if (sanctionDAO.create(sanction)) {
            showSuccess("✅ Sanction ajoutée avec succès !");
            view.hideForm();
            loadSanctions();
        } else {
            view.showError("❌ Erreur lors de l'ajout de la sanction.");
        }
    }

    private void updateSanction(Sanction sanction) {
        if (sanction == null) return;

        // Vérifier si le client existe
        Client client = clientDAO.getById(sanction.getIdClient());
        if (client == null) {
            view.showError("⚠️ Client non trouvé !");
            return;
        }

        if (sanctionDAO.update(sanction)) {
            showSuccess("✅ Sanction modifiée avec succès !");
            view.hideForm();
            loadSanctions();
        } else {
            view.showError("❌ Erreur lors de la modification de la sanction.");
        }
    }

    private void deleteSanction(Sanction sanction) {
        if (sanction == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la sanction #" + sanction.getIdSanction() + " ?");
        alert.setContentText("Cette action est définitive et irréversible !");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (sanctionDAO.delete(sanction.getIdSanction())) {
                showSuccess("🗑️ Sanction supprimée !");
                view.hideForm();
                loadSanctions();
            } else {
                view.showError("❌ Erreur lors de la suppression.");
            }
        }
    }

    private void leverSanction(Sanction sanction) {
        if (sanction == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Lever la sanction #" + sanction.getIdSanction() + " ?");
        alert.setContentText("Cette action marquera la sanction comme levée.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (sanctionDAO.updateStatut(sanction.getIdSanction(), "LEVEE")) {
                showSuccess("✅ Sanction levée avec succès !");
                loadSanctions();
            } else {
                view.showError("❌ Erreur lors du levage de la sanction.");
            }
        }
    }

    private void reduireSanction(Sanction sanction) {
        if (sanction == null) return;

        // Demander le nouveau montant
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Réduire la sanction #" + sanction.getIdSanction() + " ?");
        alert.setContentText("Le montant de la sanction sera réduit. Voulez-vous continuer ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (sanctionDAO.updateStatut(sanction.getIdSanction(), "REDUITE")) {
                showSuccess("🟡 Sanction réduite avec succès !");
                loadSanctions();
            } else {
                view.showError("❌ Erreur lors de la réduction de la sanction.");
            }
        }
    }

    private void updateStats(List<Sanction> sanctions) {
        int total = sanctions.size();
        int actives = 0, levees = 0, reduites = 0;

        for (Sanction s : sanctions) {
            switch (s.getStatut()) {
                case "ACTIVE" -> actives++;
                case "LEVEE" -> levees++;
                case "REDUITE" -> reduites++;
            }
        }

        view.updateStats(total, actives, levees, reduites);
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
