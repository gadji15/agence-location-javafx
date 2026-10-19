package Controller;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import utils.SessionManager;
import view.AdminDashboardView;
import view.AdminLoginView;

public class AdminLoginController {

    private AdminLoginView view;
    private SessionManager sessionManager;
    private MainController mainController;

    public AdminLoginController(AdminLoginView view, MainController mainController) {
        this.view = view;
        this.mainController = mainController;
        this.sessionManager = SessionManager.getInstance();
        System.out.println("✅ AdminLoginController créé");
        initEventHandlers();
    }

    public AdminLoginController(AdminLoginView view) {
        this.view = view;
        this.sessionManager = SessionManager.getInstance();
        initEventHandlers();
    }

    private void initEventHandlers() {
        view.getLoginButton().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleLogin();
            }
        });

        view.getEmailField().setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    handleLogin();
                }
            }
        });

        view.getPasswordField().setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    handleLogin();
                }
            }
        });

        view.getClientLink().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("👤 Retour vers l'espace client");
                view.clearError();
                if (mainController != null) {
                    mainController.showClientLogin();
                }
            }
        });
    }

    private void handleLogin() {
        String email = view.getEmailField().getText().trim();
        String password = view.getPasswordField().getText().trim();

        System.out.println("========================================");
        System.out.println("🔐 TENTATIVE DE CONNEXION ADMIN");
        System.out.println("   Email : " + email);
        System.out.println("========================================");

        // Validation des champs
        if (email.isEmpty() || password.isEmpty()) {
            view.showError("⚠️ Veuillez remplir tous les champs");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            view.showError("⚠️ Format d'email invalide");
            return;
        }

        view.clearError();

        // Authentification
        boolean authenticated = sessionManager.authenticate(email, password);

        if (authenticated) {
            if (sessionManager.isAdmin()) {
                System.out.println("✅ Connexion ADMIN réussie !");

                // ===== MESSAGE DE SUCCÈS EN VERT =====
                view.showSuccess("✅ Connexion réussie ! Redirection...");

                view.getLoginButton().setDisable(true);

                PauseTransition delay = new PauseTransition(Duration.seconds(1));
                delay.setOnFinished(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        redirectToDashboard();
                    }
                });
                delay.play();
            } else {
                sessionManager.logout();
                // ===== MESSAGE D'ERREUR EN ROUGE =====
                view.showError("❌ Accès administrateur uniquement.");
                view.getPasswordField().clear();
                view.getPasswordField().requestFocus();
            }
        } else {
            System.out.println("❌ ÉCHEC DE CONNEXION ADMIN");
            // ===== MESSAGE D'ERREUR EN ROUGE =====
            view.showError("❌ Email ou mot de passe incorrect");
            view.getPasswordField().clear();
            view.getPasswordField().requestFocus();
        }
    }

    private void redirectToDashboard() {
        System.out.println("📊 Redirection vers le dashboard admin");
        Stage currentStage = view.getStage();
        AdminDashboardView dashboardView = new AdminDashboardView(currentStage);
        AdminDashboardController dashboardController = new AdminDashboardController(dashboardView, mainController);
        currentStage.setScene(dashboardView.getScene());
        currentStage.setTitle("Agence de Location - Tableau de Bord Admin");
        currentStage.show();
    }

    public static void returnToLogin(Stage currentStage, MainController mainController) {
        if (mainController != null) {
            mainController.showClientLogin();
        }
    }
}