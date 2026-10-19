package Controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import utils.SessionManager;
import view.AdminDashboardView;

public class AdminDashboardController {

    private AdminDashboardView view;
    private MainController mainController;

    public AdminDashboardController(AdminDashboardView view, MainController mainController) {
        this.view = view;
        this.mainController = mainController;
        System.out.println("✅ AdminDashboardController créé");
        initEventHandlers();
    }

    public AdminDashboardController(AdminDashboardView view) {
        this.view = view;
        initEventHandlers();
    }

    private void initEventHandlers() {
        // ===== NAVIGATION - ACCUEIL =====
        view.getMenuAccueil().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("🏠 Clic sur Accueil");
                view.showHome();
            }
        });

        // ===== NAVIGATION - CLIENTS =====
        view.getMenuClients().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("👥 Clic sur Clients");
                view.showClients();
            }
        });

        // ===== NAVIGATION - VÉHICULES =====
        view.getMenuVehicules().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("🚗 Clic sur Véhicules");
                view.showVehicules();
            }
        });

        // ===== NAVIGATION - PARKINGS =====
        view.getMenuParkings().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("🅿️ Clic sur Parkings");
                view.showParkings();
            }
        });

        // ===== NAVIGATION - RÉSERVATIONS =====
        view.getMenuReservations().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("📅 Clic sur Réservations");
                view.showReservations();
            }
        });

        // ===== NAVIGATION - CONTRATS =====
        view.getMenuContrats().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("📄 Clic sur Contrats");
                view.showContrats();
            }
        });

        // ===== NAVIGATION - FACTURES =====
        view.getMenuFactures().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("💰 Clic sur Factures");
                view.showFactures();
            }
        });

        // ===== NAVIGATION - PAIEMENTS =====
        view.getMenuPaiements().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("💳 Clic sur Paiements");
                view.showPaiements();
            }
        });

        // ===== NAVIGATION - SANCTIONS =====
        view.getMenuSanctions().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("⚖️ Clic sur Sanctions");
                view.showSanctions();
            }
        });

        // ===== NAVIGATION - STATISTIQUES =====
        view.getMenuStatistiques().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("📊 Clic sur Statistiques");
                view.showStatistiques();
            }
        });

        // ===== DÉCONNEXION =====
        view.getLogoutButton().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("🚪 Déconnexion admin");
                SessionManager.getInstance().logout();
                if (mainController != null) {
                    mainController.showClientLogin();
                }
            }
        });
    }
}