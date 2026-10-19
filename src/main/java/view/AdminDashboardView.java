package view;

import Controller.*;
import dao.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Client;
import model.Reservation;
import model.Vehicule;
import utils.SessionManager;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminDashboardView {

    private Stage stage;
    private Scene scene;
    private BorderPane root;

    // ===== SIDEBAR =====
    private VBox sidebar;
    private VBox menuContainer;
    private Button logoutButton;

    // ===== CONTENU PRINCIPAL =====
    private Label pageTitleLabel;
    private VBox contentArea;
    private ScrollPane mainScrollPane;

    // ===== ÉLÉMENTS DU MENU =====
    private HBox menuAccueil;
    private HBox menuClients;
    private HBox menuVehicules;
    private HBox menuParkings;
    private HBox menuReservations;
    private HBox menuContrats;
    private HBox menuFactures;
    private HBox menuPaiements;
    private HBox menuSanctions;
    private HBox menuStatistiques;

    // ===== VUES MISES EN CACHE =====
    private VBox cachedHomeContent;
    private VBox cachedClientContent;
    private VBox cachedVehiculeContent;
    private VBox cachedParkingContent;
    private VBox cachedReservationContent;
    private VBox cachedContratContent;
    private VBox cachedFactureContent;
    private VBox cachedPaiementContent;
    private VBox cachedSanctionContent;
    private VBox cachedStatistiqueContent;

    // ===== CONTRÔLEURS =====
    private ClientManagementView clientManagementView;
    private ClientController clientController;
    private VehiculeManagementView vehiculeManagementView;
    private VehiculeController vehiculeController;
    private ParkingManagementView parkingManagementView;
    private ParkingController parkingController;
    private ReservationManagementView reservationManagementView;
    private ReservationController reservationController;
    private ContratManagementView contratManagementView;
    private ContratController contratController;
    private FactureManagementView factureManagementView;
    private FactureController factureController;
    private PaiementManagementView paiementManagementView;
    private PaiementController paiementController;
    private SanctionManagementView sanctionManagementView;
    private SanctionController sanctionController;
    private StatistiqueView statistiqueView;
    private StatistiqueController statistiqueController;

    // ===== DAO =====
    private ClientDao clientDAO;
    private ReservationDAO reservationDAO;
    private VehiculeDAO vehiculeDAO;
    private SanctionDAO sanctionDAO;
    private FactureDAO factureDAO;

    // ===== LABELS STATS =====
    private Label demandesTotalesLabel;
    private Label demandesTraiteesLabel;
    private Label demandesEnAttenteLabel;
    private Label demandesRejeteesLabel;
    private Label vehiculesDisponiblesLabel;

    // ===== TABLEAU =====
    private TableView<Reservation> reservationTable;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public AdminDashboardView(Stage stage) {
        this.stage = stage;

        this.clientDAO = new ClientDao();
        this.reservationDAO = new ReservationDAO();
        this.vehiculeDAO = new VehiculeDAO();
        this.sanctionDAO = new SanctionDAO();
        this.factureDAO = new FactureDAO();

        initUI();
    }

    private void initUI() {
        root = new BorderPane();
        root.getStyleClass().add("root");

        // Background
        try {
            java.io.InputStream inputStream = getClass().getResourceAsStream("/images/background.jpg");
            if (inputStream != null) {
                Image backgroundImage = new Image(inputStream);
                BackgroundImage bgImage = new BackgroundImage(
                        backgroundImage,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
                );
                root.setBackground(new Background(bgImage));
            } else {
                root.setStyle("-fx-background-color: #f0f2f5;");
            }
        } catch (Exception e) {
            root.setStyle("-fx-background-color: #f0f2f5;");
        }

        createSidebar();

        mainScrollPane = new ScrollPane();
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setFitToHeight(true);
        mainScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox mainContent = createMainContent();
        mainScrollPane.setContent(mainContent);

        root.setLeft(sidebar);
        root.setCenter(mainScrollPane);

        scene = new Scene(root, 1100, 700);

        try {
            String cssPath = getClass().getResource("/css/admin-dashboard.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("⚠️ CSS admin-dashboard.css non trouvé");
        }

        stage.setTitle("Agence de Location - Tableau de Bord");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setMaxWidth(1920);
        stage.setMaxHeight(1080);

        preloadViews();
    }

    private void preloadViews() {
        System.out.println("📦 Préchargement des vues admin...");
        getHomeContent();
        getClientContent();
        getVehiculeContent();
        getParkingContent();
        getReservationContent();
        getContratContent();
        getFactureContent();
        getPaiementContent();
        getSanctionContent();
        getStatistiqueContent();
        System.out.println("✅ Toutes les vues admin sont préchargées !");
    }

    // ===== SIDEBAR =====
    private void createSidebar() {
        sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(200);
        sidebar.setMinWidth(200);
        sidebar.setMaxWidth(200);

        VBox header = createSidebarHeader();

        menuContainer = new VBox();
        menuContainer.getStyleClass().add("sidebar-menu");
        menuContainer.setSpacing(1);

        menuAccueil = createMenuItem("🏠", "Accueil", true);
        menuClients = createMenuItem("👥", "Clients", false);
        menuVehicules = createMenuItem("🚗", "Véhicules", false);
        menuParkings = createMenuItem("🅿️", "Parkings", false);
        menuReservations = createMenuItem("📅", "Réservations", false);
        menuContrats = createMenuItem("📄", "Contrats", false);
        menuFactures = createMenuItem("💰", "Factures", false);
        menuPaiements = createMenuItem("💳", "Paiements", false);
        menuSanctions = createMenuItem("⚖️", "Sanctions", false);
        menuStatistiques = createMenuItem("📊", "Statistiques", false);

        menuContainer.getChildren().addAll(
                menuAccueil,
                menuClients,
                menuVehicules,
                menuParkings,
                menuReservations,
                menuContrats,
                menuFactures,
                menuPaiements,
                menuSanctions,
                menuStatistiques
        );

        VBox footer = createSidebarFooter();

        sidebar.getChildren().addAll(header, menuContainer, footer);
        VBox.setVgrow(menuContainer, Priority.ALWAYS);
    }

    private VBox createSidebarHeader() {
        VBox header = new VBox();
        header.getStyleClass().add("sidebar-header");
        header.setAlignment(Pos.CENTER);
        header.setSpacing(5);

        ImageView logo = new ImageView();
        try {
            java.io.InputStream inputStream = getClass().getResourceAsStream("/images/user-icon.png");
            if (inputStream != null) {
                Image image = new Image(inputStream);
                logo.setImage(image);
                logo.setFitWidth(45);
                logo.setFitHeight(45);
                logo.setPreserveRatio(true);
            } else {
                logo.setFitWidth(45);
                logo.setFitHeight(45);
                logo.setStyle("-fx-background-color: #3498db; -fx-background-radius: 22.5;");
            }
        } catch (Exception e) {
            logo.setFitWidth(45);
            logo.setFitHeight(45);
            logo.setStyle("-fx-background-color: #3498db; -fx-background-radius: 22.5;");
        }
        logo.getStyleClass().add("sidebar-logo");

        Label title = new Label("Agence Auto");
        title.getStyleClass().add("sidebar-title");

        Label subtitle = new Label("Administration");
        subtitle.getStyleClass().add("sidebar-subtitle");

        header.getChildren().addAll(logo, title, subtitle);
        return header;
    }

    private HBox createMenuItem(String icon, String text, boolean active) {
        HBox item = new HBox();
        item.getStyleClass().add("sidebar-menu-item");
        if (active) {
            item.getStyleClass().add("active");
        }
        item.setAlignment(Pos.CENTER_LEFT);
        item.setSpacing(10);

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("icon-label");

        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("text-label");

        item.getChildren().addAll(iconLabel, textLabel);
        return item;
    }

    private VBox createSidebarFooter() {
        VBox footer = new VBox();
        footer.getStyleClass().add("sidebar-footer");
        footer.setAlignment(Pos.CENTER);

        logoutButton = new Button("🚪 Déconnexion");
        logoutButton.getStyleClass().add("logout-button");
        logoutButton.setMaxWidth(Double.MAX_VALUE);

        footer.getChildren().add(logoutButton);
        return footer;
    }

    // ===== CONTENU PRINCIPAL =====
    private VBox createMainContent() {
        VBox mainContent = new VBox();
        mainContent.getStyleClass().add("main-content");
        mainContent.setSpacing(15);
        mainContent.setPadding(new Insets(10, 15, 15, 15));

        HBox topBar = createTopBar();

        contentArea = new VBox();
        contentArea.getStyleClass().add("content-area");
        contentArea.setSpacing(15);
        contentArea.getChildren().add(getHomeContent());

        mainContent.getChildren().addAll(topBar, contentArea);
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        return mainContent;
    }

    private HBox createTopBar() {
        HBox topBar = new HBox();
        topBar.getStyleClass().add("top-bar");

        pageTitleLabel = new Label("Tableau de bord");
        pageTitleLabel.getStyleClass().add("page-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox adminInfo = new VBox();
        adminInfo.getStyleClass().add("admin-info");
        adminInfo.setAlignment(Pos.CENTER_RIGHT);
        adminInfo.setSpacing(2);

        SessionManager session = SessionManager.getInstance();

        String adminName = session.getCurrentUserFullName();
        if (adminName == null || adminName.trim().isEmpty()) {
            adminName = "Administrateur";
        }

        String adminEmail = session.getCurrentUserEmail();
        if (adminEmail == null) {
            adminEmail = "admin@agence.com";
        }

        Label adminNameLabel = new Label(adminName);
        adminNameLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label adminEmailLabel = new Label("📧 " + adminEmail);
        adminEmailLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #7f8c8d;");

        Label adminRole = new Label(session.isAdmin() ? "🔑 Admin" : "👤 Utilisateur");
        adminRole.setStyle("-fx-font-size: 10; -fx-text-fill: #3498db; -fx-font-weight: 600;");

        adminInfo.getChildren().addAll(adminNameLabel, adminEmailLabel, adminRole);

        topBar.getChildren().addAll(pageTitleLabel, spacer, adminInfo);
        return topBar;
    }

    // ===== CONTENU ACCUEIL =====
    private VBox getHomeContent() {
        if (cachedHomeContent == null) {
            cachedHomeContent = createHomeContent();
        }
        return cachedHomeContent;
    }

    private VBox createHomeContent() {
        VBox homeContent = new VBox();
        homeContent.setSpacing(15);

        Label title = new Label("📊 Vue d'ensemble");
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        HBox statsGrid = createStatsGrid();
        VBox recentTable = createRecentTable();

        homeContent.getChildren().addAll(title, statsGrid, recentTable);
        return homeContent;
    }

    private HBox createStatsGrid() {
        HBox grid = new HBox();
        grid.getStyleClass().add("stats-grid");
        grid.setSpacing(15);
        grid.setAlignment(Pos.CENTER);

        int totalReservations = reservationDAO.count();
        int enAttente = reservationDAO.countByStatut("EN_ATTENTE");
        int acceptees = reservationDAO.countByStatut("ACCEPTEE");
        int refusees = reservationDAO.countByStatut("REFUSEE");
        int vehiculesDispo = vehiculeDAO.countAvailable();

        demandesTotalesLabel = createStatCard(grid, "📋", String.valueOf(totalReservations), "Demandes totales", "blue");
        demandesTraiteesLabel = createStatCard(grid, "✅", String.valueOf(acceptees), "Demandes traitées", "green");
        demandesEnAttenteLabel = createStatCard(grid, "⏳", String.valueOf(enAttente), "En attente", "orange");
        demandesRejeteesLabel = createStatCard(grid, "❌", String.valueOf(refusees), "Rejetées", "red");
        vehiculesDisponiblesLabel = createStatCard(grid, "🚗", String.valueOf(vehiculesDispo), "Véhicules disponibles", "purple");

        return grid;
    }

    private Label createStatCard(HBox grid, String icon, String number, String label, String colorClass) {
        VBox card = new VBox();
        card.getStyleClass().addAll("stat-card", colorClass);
        card.setAlignment(Pos.CENTER);
        card.setSpacing(2);

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("stat-icon");

        Label numberLabel = new Label(number);
        numberLabel.getStyleClass().add("stat-number");

        Label labelLabel = new Label(label);
        labelLabel.getStyleClass().add("stat-label");

        card.getChildren().addAll(iconLabel, numberLabel, labelLabel);
        grid.getChildren().add(card);

        return numberLabel;
    }

    private VBox createRecentTable() {
        VBox container = new VBox();
        container.setSpacing(8);

        Label title = new Label("📋 Dernières réservations");
        title.getStyleClass().add("table-title");

        reservationTable = new TableView<>();
        reservationTable.setPrefHeight(250);
        reservationTable.getStyleClass().add("table-view");

        TableColumn<Reservation, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getIdReservation())));
        colId.setPrefWidth(50);

        TableColumn<Reservation, String> colClient = new TableColumn<>("Client");
        colClient.setCellValueFactory(data -> {
            Client client = clientDAO.getById(data.getValue().getIdClient());
            return new javafx.beans.property.SimpleStringProperty(client != null ? client.getPrenom() + " " + client.getNom() : "N/A");
        });
        colClient.setPrefWidth(150);

        TableColumn<Reservation, String> colVehicule = new TableColumn<>("Véhicule");
        colVehicule.setCellValueFactory(data -> {
            Vehicule vehicule = vehiculeDAO.getById(data.getValue().getIdVehicule());
            return new javafx.beans.property.SimpleStringProperty(vehicule != null ? vehicule.getMarque() + " " + vehicule.getModele() : "N/A");
        });
        colVehicule.setPrefWidth(150);

        TableColumn<Reservation, String> colDateDebut = new TableColumn<>("Date début");
        colDateDebut.setCellValueFactory(data -> {
            if (data.getValue().getDateDebut() != null) {
                return new javafx.beans.property.SimpleStringProperty(data.getValue().getDateDebut().format(dateFormatter));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        colDateDebut.setPrefWidth(140);

        TableColumn<Reservation, String> colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(data -> {
            String statut = data.getValue().getStatut();
            String emoji = switch (statut) {
                case "EN_ATTENTE" -> "⏳";
                case "ACCEPTEE" -> "✅";
                case "REFUSEE" -> "❌";
                case "ANNULEE" -> "🚫";
                default -> "⚪";
            };
            return new javafx.beans.property.SimpleStringProperty(emoji + " " + statut);
        });
        colStatut.setPrefWidth(120);

        reservationTable.getColumns().addAll(colId, colClient, colVehicule, colDateDebut, colStatut);

        loadRecentReservations();

        container.getChildren().addAll(title, reservationTable);
        return container;
    }

    private void loadRecentReservations() {
        List<Reservation> reservations = reservationDAO.getAll();
        if (reservations.size() > 10) {
            reservations = reservations.subList(0, 10);
        }
        reservationTable.getItems().clear();
        reservationTable.getItems().addAll(reservations);
    }

    public void refreshStats() {
        if (demandesTotalesLabel != null) {
            demandesTotalesLabel.setText(String.valueOf(reservationDAO.count()));
        }
        if (demandesTraiteesLabel != null) {
            demandesTraiteesLabel.setText(String.valueOf(reservationDAO.countByStatut("ACCEPTEE")));
        }
        if (demandesEnAttenteLabel != null) {
            demandesEnAttenteLabel.setText(String.valueOf(reservationDAO.countByStatut("EN_ATTENTE")));
        }
        if (demandesRejeteesLabel != null) {
            demandesRejeteesLabel.setText(String.valueOf(reservationDAO.countByStatut("REFUSEE")));
        }
        if (vehiculesDisponiblesLabel != null) {
            vehiculesDisponiblesLabel.setText(String.valueOf(vehiculeDAO.countAvailable()));
        }
        if (reservationTable != null) {
            loadRecentReservations();
        }
    }

    // ===== CRÉATION DES CONTENUS =====
    private VBox getClientContent() {
        if (cachedClientContent == null) {
            clientManagementView = new ClientManagementView();
            clientController = new ClientController(clientManagementView);
            cachedClientContent = clientManagementView.getRoot();
        }
        return cachedClientContent;
    }

    private VBox getVehiculeContent() {
        if (cachedVehiculeContent == null) {
            vehiculeManagementView = new VehiculeManagementView();
            vehiculeController = new VehiculeController(vehiculeManagementView);
            cachedVehiculeContent = vehiculeManagementView.getRoot();
        }
        return cachedVehiculeContent;
    }

    private VBox getParkingContent() {
        if (cachedParkingContent == null) {
            parkingManagementView = new ParkingManagementView();
            parkingController = new ParkingController(parkingManagementView);
            cachedParkingContent = parkingManagementView.getRoot();
        }
        return cachedParkingContent;
    }

    private VBox getReservationContent() {
        if (cachedReservationContent == null) {
            reservationManagementView = new ReservationManagementView();
            reservationController = new ReservationController(reservationManagementView);
            cachedReservationContent = reservationManagementView.getRoot();
        }
        return cachedReservationContent;
    }

    private VBox getContratContent() {
        if (cachedContratContent == null) {
            contratManagementView = new ContratManagementView();
            contratController = new ContratController(contratManagementView);
            cachedContratContent = contratManagementView.getRoot();
        }
        return cachedContratContent;
    }

    private VBox getFactureContent() {
        if (cachedFactureContent == null) {
            factureManagementView = new FactureManagementView();
            factureController = new FactureController(factureManagementView);
            cachedFactureContent = factureManagementView.getRoot();
        }
        return cachedFactureContent;
    }

    private VBox getPaiementContent() {
        if (cachedPaiementContent == null) {
            paiementManagementView = new PaiementManagementView();
            paiementController = new PaiementController(paiementManagementView);
            cachedPaiementContent = paiementManagementView.getRoot();
        }
        return cachedPaiementContent;
    }

    private VBox getSanctionContent() {
        if (cachedSanctionContent == null) {
            sanctionManagementView = new SanctionManagementView();
            sanctionController = new SanctionController(sanctionManagementView);
            cachedSanctionContent = sanctionManagementView.getRoot();
        }
        return cachedSanctionContent;
    }

    private VBox getStatistiqueContent() {
        if (cachedStatistiqueContent == null) {
            statistiqueView = new StatistiqueView();
            statistiqueController = new StatistiqueController(statistiqueView);
            cachedStatistiqueContent = statistiqueView.getRoot();
        }
        return cachedStatistiqueContent;
    }

    // ===== MÉTHODES DE NAVIGATION =====
    public void showHome() {
        setActiveMenuItem(menuAccueil);
        pageTitleLabel.setText("Tableau de bord");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(getHomeContent());
        refreshStats();
    }

    public void showClients() {
        setActiveMenuItem(menuClients);
        pageTitleLabel.setText("👥 Gestion des Clients");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(getClientContent());
    }

    public void showVehicules() {
        setActiveMenuItem(menuVehicules);
        pageTitleLabel.setText("🚗 Gestion des Véhicules");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(getVehiculeContent());
    }

    public void showParkings() {
        setActiveMenuItem(menuParkings);
        pageTitleLabel.setText("🅿️ Gestion des Parkings");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(getParkingContent());
    }

    public void showReservations() {
        setActiveMenuItem(menuReservations);
        pageTitleLabel.setText("📅 Gestion des Réservations");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(getReservationContent());
    }

    public void showContrats() {
        setActiveMenuItem(menuContrats);
        pageTitleLabel.setText("📄 Gestion des Contrats");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(getContratContent());
    }

    public void showFactures() {
        setActiveMenuItem(menuFactures);
        pageTitleLabel.setText("💰 Gestion des Factures");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(getFactureContent());
    }

    public void showPaiements() {
        setActiveMenuItem(menuPaiements);
        pageTitleLabel.setText("💳 Historique des Paiements");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(getPaiementContent());
    }

    public void showSanctions() {
        setActiveMenuItem(menuSanctions);
        pageTitleLabel.setText("⚖️ Gestion des Sanctions");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(getSanctionContent());
    }

    public void showStatistiques() {
        setActiveMenuItem(menuStatistiques);
        pageTitleLabel.setText("📊 Statistiques");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(getStatistiqueContent());
    }

    private void setActiveMenuItem(HBox activeItem) {
        for (javafx.scene.Node node : menuContainer.getChildren()) {
            if (node instanceof HBox) {
                ((HBox) node).getStyleClass().remove("active");
            }
        }
        activeItem.getStyleClass().add("active");
    }

    // ===== GETTERS =====
    public Scene getScene() { return scene; }
    public Stage getStage() { return stage; }
    public void show() { stage.show(); }
    public void close() { stage.close(); }

    public HBox getMenuAccueil() { return menuAccueil; }
    public HBox getMenuClients() { return menuClients; }
    public HBox getMenuVehicules() { return menuVehicules; }
    public HBox getMenuParkings() { return menuParkings; }
    public HBox getMenuReservations() { return menuReservations; }
    public HBox getMenuContrats() { return menuContrats; }
    public HBox getMenuFactures() { return menuFactures; }
    public HBox getMenuPaiements() { return menuPaiements; }
    public HBox getMenuSanctions() { return menuSanctions; }
    public HBox getMenuStatistiques() { return menuStatistiques; }
    public Button getLogoutButton() { return logoutButton; }
}