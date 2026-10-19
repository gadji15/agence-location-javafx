package Controller;

import javafx.stage.Stage;
import view.AdminLoginView;
import view.ClientLoginView;
import view.ClientRegisterView;

public class MainController {

    private Stage primaryStage;
    private ClientLoginView clientLoginView;
    private ClientLoginController clientLoginController;
    private ClientRegisterView clientRegisterView;
    private ClientRegisterController clientRegisterController;
    private AdminLoginView adminLoginView;
    private AdminLoginController adminLoginController;

    private double windowWidth;
    private double windowHeight;
    private double windowX;
    private double windowY;
    private boolean isMaximized;
    private boolean isFullScreen;

    public MainController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        System.out.println("🚀 MainController initialisé");

        setupWindowListeners();
        showClientLogin();
    }

    private void setupWindowListeners() {
        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
            windowWidth = newVal.doubleValue();
        });

        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> {
            windowHeight = newVal.doubleValue();
        });

        primaryStage.xProperty().addListener((obs, oldVal, newVal) -> {
            windowX = newVal.doubleValue();
        });

        primaryStage.yProperty().addListener((obs, oldVal, newVal) -> {
            windowY = newVal.doubleValue();
        });

        primaryStage.maximizedProperty().addListener((obs, oldVal, newVal) -> {
            isMaximized = newVal;
        });

        primaryStage.fullScreenProperty().addListener((obs, oldVal, newVal) -> {
            isFullScreen = newVal;
        });

        System.out.println("👂 Écouteurs de fenêtre installés");
    }

    private void saveWindowState() {
        windowWidth = primaryStage.getWidth();
        windowHeight = primaryStage.getHeight();
        windowX = primaryStage.getX();
        windowY = primaryStage.getY();
        isMaximized = primaryStage.isMaximized();
        isFullScreen = primaryStage.isFullScreen();
    }

    private void restoreWindowState() {
        if (isFullScreen) {
            primaryStage.setFullScreen(true);
        } else if (isMaximized) {
            primaryStage.setMaximized(true);
        } else {
            primaryStage.setWidth(windowWidth);
            primaryStage.setHeight(windowHeight);
            primaryStage.setX(windowX);
            primaryStage.setY(windowY);
        }
    }

    // ===== AFFICHER LA CONNEXION CLIENT AVEC NETTOYAGE =====
    public void showClientLogin() {
        System.out.println("📱 showClientLogin() appelée");
        saveWindowState();

        if (clientLoginView == null) {
            System.out.println("   Création de ClientLoginView");
            clientLoginView = new ClientLoginView(primaryStage);
            clientLoginController = new ClientLoginController(clientLoginView, this);
        }

        // ===== NETTOYER LES CHAMPS =====
        clientLoginView.clearFields();

        primaryStage.setScene(clientLoginView.getScene());
        primaryStage.setTitle("Agence de Location - Espace Client");
        restoreWindowState();
        primaryStage.show();
    }

    // ===== AFFICHER L'INSCRIPTION CLIENT AVEC NETTOYAGE =====
    public void showClientRegister() {
        System.out.println("📝 showClientRegister() appelée");
        saveWindowState();

        if (clientRegisterView == null) {
            System.out.println("   Création de ClientRegisterView");
            clientRegisterView = new ClientRegisterView(primaryStage);
            clientRegisterController = new ClientRegisterController(clientRegisterView, this);
        }

        // ===== NETTOYER LES CHAMPS =====
        clientRegisterView.clearFields();

        primaryStage.setScene(clientRegisterView.getScene());
        primaryStage.setTitle("Agence de Location - Inscription Client");
        restoreWindowState();
        primaryStage.show();
    }

    // ===== AFFICHER LA CONNEXION ADMIN AVEC NETTOYAGE =====
    public void showAdminLogin() {
        System.out.println("🔐 showAdminLogin() appelée");
        saveWindowState();

        if (adminLoginView == null) {
            System.out.println("   Création de AdminLoginView");
            adminLoginView = new AdminLoginView(primaryStage);
            adminLoginController = new AdminLoginController(adminLoginView, this);
        }

        // ===== NETTOYER LES CHAMPS =====
        adminLoginView.clearFields();

        primaryStage.setScene(adminLoginView.getScene());
        primaryStage.setTitle("Agence de Location - Connexion Admin");
        restoreWindowState();
        primaryStage.show();
    }
}