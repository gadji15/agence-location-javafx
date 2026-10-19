package Controller;

import dao.ClientDao;
import dao.UtilisateurDAO;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import model.Client;
import utils.SessionManager;
import view.ClientProfilView;

import java.util.Optional;

public class ClientProfilController {

    private ClientProfilView view;
    private ClientDao clientDAO;
    private UtilisateurDAO utilisateurDAO;
    private int clientId;

    public ClientProfilController(ClientProfilView view) {
        this.view = view;
        this.clientDAO = new ClientDao();
        this.utilisateurDAO = new UtilisateurDAO();
        this.clientId = SessionManager.getInstance().getCurrentClientId();

        System.out.println("✅ ClientProfilController créé pour client ID: " + clientId);

        initEventHandlers();
        loadProfil();
    }

    private void initEventHandlers() {
        view.setCallback(new ClientProfilView.ProfilCallback() {
            @Override
            public void onModifier() {
                startEditing();
            }

            @Override
            public void onSave(Client client) {
                saveProfil(client);
            }

            @Override
            public void onCancel() {
                cancelEditing();
            }

            @Override
            public void onChangePassword(String currentPassword, String newPassword) {
                changePassword(currentPassword, newPassword);
            }
        });
    }

    private void loadProfil() {
        System.out.println("📋 Chargement du profil pour client ID: " + clientId);

        if (clientId <= 0) {
            view.showError("❌ Erreur: Client non connecté !");
            return;
        }

        Client client = clientDAO.getById(clientId);
        if (client != null) {
            view.displayClient(client);
            view.setEditingMode(false);
            view.clearError();
            System.out.println("👤 Profil chargé pour: " + client.getPrenom() + " " + client.getNom());
        } else {
            view.showError("❌ Erreur lors du chargement du profil.");
            System.out.println("❌ Client non trouvé pour l'ID: " + clientId);
        }
    }

    private void startEditing() {
        view.setEditingMode(true);
        view.clearError();
    }

    private void saveProfil(Client client) {
        if (client == null) return;

        if (clientDAO.emailExistsExcept(client.getEmail(), clientId)) {
            view.showError("⚠️ Cet email est déjà utilisé par un autre client !");
            return;
        }

        int userId = SessionManager.getInstance().getCurrentUserId();
        if (utilisateurDAO.emailExistsExcept(client.getEmail(), userId)) {
            view.showError("⚠️ Cet email est déjà utilisé par un autre compte !");
            return;
        }

        Client existingClient = clientDAO.getById(clientId);
        if (existingClient == null) {
            view.showError("❌ Client non trouvé !");
            return;
        }

        // L'email du client sert aussi d'identifiant de connexion (table utilisateur)
        boolean emailModifie = !existingClient.getEmail().equals(client.getEmail());

        existingClient.setNom(client.getNom());
        existingClient.setPrenom(client.getPrenom());
        existingClient.setEmail(client.getEmail());
        existingClient.setTelephone(client.getTelephone());
        existingClient.setAdresse(client.getAdresse());

        if (clientDAO.update(existingClient)) {
            if (emailModifie) {
                if (!utilisateurDAO.updateEmail(userId, existingClient.getEmail())) {
                    view.showError("❌ Profil mis à jour, mais erreur lors de la mise à jour de l'email de connexion.");
                    return;
                }
                SessionManager.getInstance().setCurrentUserEmail(existingClient.getEmail());
            }

            view.showSuccess("✅ Profil mis à jour avec succès !");
            view.setEditingMode(false);
            loadProfil();
            SessionManager.getInstance().setCurrentUserFullName(existingClient.getPrenom() + " " + existingClient.getNom());
        } else {
            view.showError("❌ Erreur lors de la mise à jour du profil.");
        }
    }

    private void cancelEditing() {
        loadProfil();
        view.setEditingMode(false);
        view.clearError();
    }

    private void changePassword(String currentPassword, String newPassword) {
        if (!utilisateurDAO.verifyPassword(SessionManager.getInstance().getCurrentUserId(), currentPassword)) {
            view.showPasswordError("⚠️ Le mot de passe actuel est incorrect !");
            return;
        }

        if (utilisateurDAO.updatePassword(SessionManager.getInstance().getCurrentUserId(), newPassword)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("✅ Mot de passe modifié avec succès !");
            alert.showAndWait();

            view.clearPasswordFields();
            view.clearError();
            view.showSuccess("✅ Mot de passe modifié avec succès !");
        } else {
            view.showPasswordError("❌ Erreur lors du changement de mot de passe.");
        }
    }
}