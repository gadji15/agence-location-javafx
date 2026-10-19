package Controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import utils.SessionManager;
import view.ClientDashboardView;

public class ClientDashboardController {

    private ClientDashboardView view;
    private MainController mainController;

    public ClientDashboardController(ClientDashboardView view, MainController mainController) {
        this.view = view;
        this.mainController = mainController;
        System.out.println("✅ ClientDashboardController créé avec MainController");
        initEventHandlers();
    }

    public ClientDashboardController(ClientDashboardView view) {
        this.view = view;
        initEventHandlers();
    }

    private void initEventHandlers() {
        // Navigation - Accueil
        view.getMenuAccueil().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("🏠 Clic sur Accueil");
                view.showHome();
            }
        });

        // Navigation - Réservations
        view.getMenuReservations().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("📅 Clic sur Réservations");
                view.showReservations();
            }
        });

        // Navigation - Véhicules
        view.getMenuVehicules().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("🚗 Clic sur Véhicules");
                view.showVehicules();
            }
        });

        // Navigation - Contrats
        view.getMenuContrats().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("📄 Clic sur Contrats");
                view.showContrats();
            }
        });

        // Navigation - Factures
        view.getMenuFactures().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("💰 Clic sur Factures");
                view.showFactures();
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

        // Navigation - Profil
        view.getMenuProfil().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("👤 Clic sur Profil");
                view.showProfil();
            }
        });

        // Déconnexion
        view.getLogoutButton().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("🚪 Déconnexion client");
                SessionManager.getInstance().logout();
                if (mainController != null) {
                    mainController.showClientLogin();
                }
            }
        });
    }
}