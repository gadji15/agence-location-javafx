package Controller;

import dao.ClientDao;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import model.Client;
import utils.DatabaseConnection;
import view.ClientRegisterView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ClientRegisterController {

    private ClientRegisterView view;
    private ClientDao clientDAO;
    private MainController mainController;

    public ClientRegisterController(ClientRegisterView view, MainController mainController) {
        this.view = view;
        this.mainController = mainController;
        this.clientDAO = new ClientDao();
        System.out.println("✅ ClientRegisterController créé");
        initEventHandlers();
    }

    public ClientRegisterController(ClientRegisterView view) {
        this.view = view;
        this.clientDAO = new ClientDao();
        initEventHandlers();
    }

    private void initEventHandlers() {
        view.getRegisterButton().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleRegister();
            }
        });

        view.getNomField().setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) handleRegister(); });
        view.getPrenomField().setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) handleRegister(); });
        view.getEmailField().setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) handleRegister(); });
        view.getTelephoneField().setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) handleRegister(); });
        view.getPasswordField().setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) handleRegister(); });
        view.getConfirmPasswordField().setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) handleRegister(); });

        view.getLoginLink().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("🔑 Retour vers la connexion");
                view.clearError();
                if (mainController != null) {
                    mainController.showClientLogin();
                }
            }
        });
    }

    private void handleRegister() {
        String nom = view.getNomField().getText().trim();
        String prenom = view.getPrenomField().getText().trim();
        String email = view.getEmailField().getText().trim();
        String telephone = view.getTelephoneField().getText().trim();
        String adresse = view.getAdresseField().getText().trim();
        String password = view.getPasswordField().getText().trim();
        String confirmPassword = view.getConfirmPasswordField().getText().trim();

        System.out.println("========================================");
        System.out.println("📝 TENTATIVE D'INSCRIPTION");
        System.out.println("   Email : " + email);
        System.out.println("========================================");

        // Validation des champs obligatoires
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || telephone.isEmpty() || password.isEmpty()) {
            view.showError("⚠️ Tous les champs marqués * sont obligatoires !");
            return;
        }

        // Validation email
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            view.showError("⚠️ Format d'email invalide !");
            return;
        }

        // Validation téléphone
        if (!telephone.matches("^[0-9]{9,10}$")) {
            view.showError("⚠️ Format de téléphone invalide (9-10 chiffres) !");
            return;
        }

        // Validation mot de passe
        if (!password.equals(confirmPassword)) {
            view.showError("⚠️ Les mots de passe ne correspondent pas !");
            return;
        }

        if (password.length() < 6) {
            view.showError("⚠️ Le mot de passe doit contenir au moins 6 caractères !");
            return;
        }

        view.clearError();

        // Vérifier si l'email existe déjà
        if (clientDAO.emailExists(email)) {
            view.showError("⚠️ Cet email est déjà utilisé !");
            return;
        }

        // Créer le client
        Client newClient = new Client(nom, prenom, email, telephone, adresse, true);

        if (clientDAO.create(newClient)) {
            if (createUserAccount(newClient.getIdClient(), email, password)) {
                System.out.println("✅ Inscription réussie pour : " + email);

                // ===== MESSAGE DE SUCCÈS EN VERT =====
                view.showSuccess("✅ Inscription réussie ! Redirection vers la connexion...");

                view.getRegisterButton().setDisable(true);

                PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
                delay.setOnFinished(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        if (mainController != null) {
                            mainController.showClientLogin();
                        }
                    }
                });
                delay.play();
            } else {
                view.showError("❌ Erreur lors de la création du compte utilisateur.");
                view.getRegisterButton().setDisable(false);
            }
        } else {
            view.showError("❌ Erreur lors de l'inscription. Veuillez réessayer.");
            view.getRegisterButton().setDisable(false);
        }
    }

    private boolean createUserAccount(int clientId, String email, String password) {
        String sql = "INSERT INTO utilisateur (email, motDePasseHash, role, idClient, actif) VALUES (?, ?, 'CLIENT', ?, 1)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);
            stmt.setInt(3, clientId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}