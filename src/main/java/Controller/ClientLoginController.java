package Controller;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import utils.SessionManager;
import view.ClientDashboardView;
import view.ClientLoginView;

public class ClientLoginController {

    private ClientLoginView view;
    private SessionManager sessionManager;
    private MainController mainController;

    public ClientLoginController(ClientLoginView view, MainController mainController) {
        this.view = view;
        this.mainController = mainController;
        this.sessionManager = SessionManager.getInstance();
        System.out.println("✅ ClientLoginController créé");
        initEventHandlers();
    }

    public ClientLoginController(ClientLoginView view) {
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

        view.getRegisterLink().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("📝 Redirection vers l'inscription");
                view.clearError();
                if (mainController != null) {
                    mainController.showClientRegister();
                }
            }
        });

        view.getAdminLink().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("🔑 Redirection vers l'espace admin");
                view.clearError();
                if (mainController != null) {
                    mainController.showAdminLogin();
                }
            }
        });
    }

    private void handleLogin() {
        String email = view.getEmailField().getText().trim();
        String password = view.getPasswordField().getText().trim();

        System.out.println("========================================");
        System.out.println("🔐 TENTATIVE DE CONNEXION CLIENT");
        System.out.println("   Email : " + email);
        System.out.println("========================================");

        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            view.showError("⚠️ Veuillez remplir tous les champs");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            view.showError("⚠️ Format d'email invalide");
            return;
        }

        view.clearError();

        boolean authenticated = sessionManager.authenticate(email, password);

        if (authenticated) {
            if (sessionManager.isClient()) {
                System.out.println("✅ Connexion CLIENT réussie !");

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
                view.showError("❌ Accès client uniquement. Veuillez utiliser l'espace admin.");
                view.getPasswordField().clear();
                view.getPasswordField().requestFocus();
            }
        } else {
            System.out.println("❌ ÉCHEC DE CONNEXION CLIENT");
            // ===== MESSAGE D'ERREUR EN ROUGE =====
            view.showError("❌ Email ou mot de passe incorrect");
            view.getPasswordField().clear();
            view.getPasswordField().requestFocus();
        }
    }

    private void redirectToDashboard() {
        System.out.println("📊 Redirection vers le dashboard client");
        Stage currentStage = view.getStage();
        ClientDashboardView dashboardView = new ClientDashboardView(currentStage);
        ClientDashboardController dashboardController = new ClientDashboardController(dashboardView, mainController);
        currentStage.setScene(dashboardView.getScene());
        currentStage.setTitle("Agence de Location - Tableau de Bord Client");
        currentStage.show();
    }

    public static void returnToLogin(Stage currentStage, MainController mainController) {
        if (mainController != null) {
            mainController.showClientLogin();
        }
    }
}