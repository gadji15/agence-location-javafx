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
import model.*;
import utils.SessionManager;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClientDashboardView {

    private Stage stage;
    private Scene scene;
    private BorderPane root;

    // Composants de la sidebar
    private VBox sidebar;
    private VBox menuContainer;
    private Button logoutButton;

    // Composants du contenu principal
    private Label pageTitleLabel;
    private VBox contentArea;
    private ScrollPane mainScrollPane;

    // Éléments du menu
    private HBox menuAccueil;
    private HBox menuReservations;
    private HBox menuVehicules;
    private HBox menuContrats;
    private HBox menuFactures;
    private HBox menuSanctions;
    private HBox menuProfil;

    // ===== VUES MISES EN CACHE =====
    private VBox cachedHomeContent;
    private VBox cachedReservationContent;
    private VBox cachedVehiculeContent;
    private VBox cachedContratContent;
    private VBox cachedFactureContent;
    private VBox cachedSanctionContent;
    private VBox cachedProfilContent;

    // ===== CONTROLEURS MIS EN CACHE =====
    private ClientReservationView clientReservationView;
    private ClientReservationController clientReservationController;
    private ClientVehiculeView clientVehiculeView;
    private ClientVehiculeController clientVehiculeController;
    private ClientContratView clientContratView;
    private ClientContratController clientContratController;
    private ClientFactureView clientFactureView;
    private ClientFactureController clientFactureController;
    private ClientSanctionView clientSanctionView;
    private ClientSanctionController clientSanctionController;
    private ClientProfilView clientProfilView;
    private ClientProfilController clientProfilController;

    // DAO pour les statistiques
    private ReservationDAO reservationDAO;
    private ContratDAO contratDAO;
    private FactureDAO factureDAO;
    private SanctionDAO sanctionDAO;
    private VehiculeDAO vehiculeDAO;
    private ClientDao clientDAO;
    private int clientId;

    // Labels des statistiques
    private Label totalReservationsLabel;
    private Label enAttenteLabel;
    private Label accepteesLabel;
    private Label totalContratsLabel;
    private Label totalFacturesLabel;
    private Label totalSanctionsLabel;

    // Tableaux
    private TableView<Reservation> reservationTable;
    private TableView<Contrat> contratTable;
    private TableView<Facture> factureTable;

    // Formatteur de date
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private DateTimeFormatter dateFormatterShort = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ClientDashboardView(Stage stage) {
        this.stage = stage;

        this.reservationDAO = new ReservationDAO();
        this.contratDAO = new ContratDAO();
        this.factureDAO = new FactureDAO();
        this.sanctionDAO = new SanctionDAO();
        this.vehiculeDAO = new VehiculeDAO();
        this.clientDAO = new ClientDao();
        this.clientId = SessionManager.getInstance().getCurrentClientId();

        initUI();
    }

    private void initUI() {
        root = new BorderPane();
        root.getStyleClass().add("root");

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
            String cssPath = getClass().getResource("/css/client-dashboard.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("⚠️ CSS client-dashboard.css non trouvé");
        }

        stage.setTitle("Agence de Location - Espace Client");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setMaxWidth(1920);
        stage.setMaxHeight(1080);

        // Précharger les vues
        preloadViews();
    }

    private void preloadViews() {
        System.out.println("📦 Préchargement des vues client...");

        // Créer toutes les vues au démarrage
        getHomeContent();
        getReservationContent();
        getVehiculeContent();
        getContratContent();
        getFactureContent();
        getSanctionContent();
        getProfilContent();

        System.out.println("✅ Toutes les vues sont préchargées !");
    }

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
        menuReservations = createMenuItem("📅", "Mes Réservations", false);
        menuVehicules = createMenuItem("🚗", "Véhicules", false);
        menuContrats = createMenuItem("📄", "Mes Contrats", false);
        menuFactures = createMenuItem("💰", "Mes Factures", false);
        menuSanctions = createMenuItem("⚖️", "Mes Sanctions", false);
        menuProfil = createMenuItem("👤", "Mon Profil", false);

        menuContainer.getChildren().addAll(
                menuAccueil,
                menuReservations,
                menuVehicules,
                menuContrats,
                menuFactures,
                menuSanctions,
                menuProfil
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

        ImageView userIcon = new ImageView();
        try {
            java.io.InputStream inputStream = getClass().getResourceAsStream("/images/user-icon.png");
            if (inputStream != null) {
                Image image = new Image(inputStream);
                userIcon.setImage(image);
                userIcon.setFitWidth(45);
                userIcon.setFitHeight(45);
                userIcon.setPreserveRatio(true);
            } else {
                userIcon.setFitWidth(45);
                userIcon.setFitHeight(45);
                userIcon.setStyle("-fx-background-color: #2ecc71; -fx-background-radius: 22.5;");
            }
        } catch (Exception e) {
            userIcon.setFitWidth(45);
            userIcon.setFitHeight(45);
            userIcon.setStyle("-fx-background-color: #2ecc71; -fx-background-radius: 22.5;");
        }
        userIcon.getStyleClass().add("sidebar-logo");

        Label title = new Label("Agence Auto");
        title.getStyleClass().add("sidebar-title");

        Label subtitle = new Label("Espace Client");
        subtitle.getStyleClass().add("sidebar-subtitle");

        header.getChildren().addAll(userIcon, title, subtitle);
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

    private VBox createMainContent() {
        VBox mainContent = new VBox();
        mainContent.getStyleClass().add("main-content");
        mainContent.setSpacing(15);
        mainContent.setPadding(new Insets(10, 15, 15, 15));

        HBox topBar = createTopBar();

        contentArea = new VBox();
        contentArea.getStyleClass().add("content-area");
        contentArea.setSpacing(15);

        // Charger l'accueil
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

        VBox clientInfo = new VBox();
        clientInfo.getStyleClass().add("client-info");
        clientInfo.setAlignment(Pos.CENTER_RIGHT);
        clientInfo.setSpacing(2);

        SessionManager session = SessionManager.getInstance();

        String clientName = session.getCurrentUserFullName();
        if (clientName == null || clientName.trim().isEmpty()) {
            clientName = "Client";
        }

        String clientEmail = session.getCurrentUserEmail();
        if (clientEmail == null) {
            clientEmail = "client@email.com";
        }

        Label clientNameLabel = new Label(clientName);
        clientNameLabel.getStyleClass().add("client-name");

        Label clientEmailLabel = new Label("📧 " + clientEmail);
        clientEmailLabel.getStyleClass().add("client-email");

        clientInfo.getChildren().addAll(clientNameLabel, clientEmailLabel);

        topBar.getChildren().addAll(pageTitleLabel, spacer, clientInfo);
        return topBar;
    }

    // ===== CONTENU ACCUEIL (AVEC MISE EN CACHE) =====

    private VBox getHomeContent() {
        if (cachedHomeContent == null) {
            System.out.println("🔄 Création de la vue Accueil");
            cachedHomeContent = createHomeContent();
        }
        return cachedHomeContent;
    }

    private VBox createHomeContent() {
        VBox homeContent = new VBox();
        homeContent.setSpacing(20);

        Label welcomeLabel = new Label("👋 Bienvenue sur votre espace client !");
        welcomeLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label infoLabel = new Label("📌 Voici un résumé de vos activités : réservations, contrats et factures.");
        infoLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #7f8c8d; -fx-wrap-text: true;");

        HBox statsGrid = createStatsGrid();
        VBox recentReservations = createRecentReservationsTable();
        VBox recentContrats = createRecentContratsTable();
        VBox recentFactures = createRecentFacturesTable();

        homeContent.getChildren().addAll(welcomeLabel, infoLabel, statsGrid, recentReservations, recentContrats, recentFactures);
        return homeContent;
    }

    private HBox createStatsGrid() {
        HBox grid = new HBox();
        grid.setSpacing(15);
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(5, 0, 10, 0));

        List<Reservation> reservations = reservationDAO.getByClient(clientId);
        List<Contrat> contrats = getClientContrats();
        List<Facture> factures = getClientFactures();
        List<Sanction> sanctions = sanctionDAO.getByClient(clientId);

        int totalRes = reservations.size();
        int enAttente = 0, acceptees = 0;
        for (Reservation r : reservations) {
            if ("EN_ATTENTE".equals(r.getStatut())) enAttente++;
            if ("ACCEPTEE".equals(r.getStatut())) acceptees++;
        }

        totalReservationsLabel = createStatCard(grid, "📅", String.valueOf(totalRes), "Mes réservations", "blue");
        enAttenteLabel = createStatCard(grid, "⏳", String.valueOf(enAttente), "En attente", "orange");
        accepteesLabel = createStatCard(grid, "✅", String.valueOf(acceptees), "Acceptées", "green");
        totalContratsLabel = createStatCard(grid, "📄", String.valueOf(contrats.size()), "Mes contrats", "purple");
        totalFacturesLabel = createStatCard(grid, "💰", String.valueOf(factures.size()), "Mes factures", "red");
        totalSanctionsLabel = createStatCard(grid, "⚖️", String.valueOf(sanctions.size()), "Sanctions", "gray");

        return grid;
    }

    private Label createStatCard(HBox grid, String icon, String number, String label, String colorClass) {
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.setSpacing(2);
        card.setPadding(new Insets(12, 15, 12, 15));
        card.setMinWidth(120);
        card.setPrefHeight(75);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2); -fx-border-color: " + getColorForClass(colorClass) + "; -fx-border-width: 0 0 3 0; -fx-border-style: solid;");

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 18;");

        Label numberLabel = new Label(number);
        numberLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label labelLabel = new Label(label);
        labelLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #7f8c8d;");

        card.getChildren().addAll(iconLabel, numberLabel, labelLabel);
        grid.getChildren().add(card);

        return numberLabel;
    }

    private String getColorForClass(String colorClass) {
        return switch (colorClass) {
            case "blue" -> "#3498db";
            case "green" -> "#2ecc71";
            case "orange" -> "#f39c12";
            case "purple" -> "#9b59b6";
            case "red" -> "#e74c3c";
            case "gray" -> "#95a5a6";
            default -> "#3498db";
        };
    }

    // ===== TABLEAU DES RÉSERVATIONS RÉCENTES =====

    private VBox createRecentReservationsTable() {
        VBox container = new VBox();
        container.setSpacing(8);

        Label title = new Label("📋 Mes réservations récentes");
        title.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        reservationTable = new TableView<>();
        reservationTable.setPrefHeight(180);
        reservationTable.getStyleClass().add("table-view");

        TableColumn<Reservation, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getIdReservation())));
        colId.setPrefWidth(50);

        TableColumn<Reservation, String> colVehicule = new TableColumn<>("Véhicule");
        colVehicule.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(getVehiculeName(data.getValue().getIdVehicule())));
        colVehicule.setPrefWidth(150);

        TableColumn<Reservation, String> colDate = new TableColumn<>("Date début");
        colDate.setCellValueFactory(data -> {
            if (data.getValue().getDateDebut() != null) {
                return new javafx.beans.property.SimpleStringProperty(data.getValue().getDateDebut().format(dateFormatter));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        colDate.setPrefWidth(150);

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

        reservationTable.getColumns().addAll(colId, colVehicule, colDate, colStatut);

        loadRecentReservations();

        container.getChildren().addAll(title, reservationTable);
        return container;
    }

    private void loadRecentReservations() {
        List<Reservation> reservations = reservationDAO.getByClient(clientId);
        if (reservations.size() > 5) {
            reservations = reservations.subList(0, 5);
        }
        reservationTable.getItems().clear();
        reservationTable.getItems().addAll(reservations);
    }

    // ===== TABLEAU DES CONTRATS RÉCENTS =====

    private VBox createRecentContratsTable() {
        VBox container = new VBox();
        container.setSpacing(8);

        Label title = new Label("📄 Mes contrats récents");
        title.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        contratTable = new TableView<>();
        contratTable.setPrefHeight(180);
        contratTable.getStyleClass().add("table-view");

        TableColumn<Contrat, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getIdContrat())));
        colId.setPrefWidth(50);

        TableColumn<Contrat, String> colReservation = new TableColumn<>("Réservation");
        colReservation.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("#" + data.getValue().getIdReservation()));
        colReservation.setPrefWidth(100);

        TableColumn<Contrat, String> colDate = new TableColumn<>("Signature");
        colDate.setCellValueFactory(data -> {
            if (data.getValue().getDateSignature() != null) {
                return new javafx.beans.property.SimpleStringProperty(data.getValue().getDateSignature().format(dateFormatterShort));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        colDate.setPrefWidth(120);

        TableColumn<Contrat, String> colMontant = new TableColumn<>("Montant");
        colMontant.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.format("%.2f €", data.getValue().getMontantTotal())));
        colMontant.setPrefWidth(100);

        TableColumn<Contrat, String> colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(data -> {
            String statut = data.getValue().isActif() ? "✅ Actif" : "📦 Clôturé";
            return new javafx.beans.property.SimpleStringProperty(statut);
        });
        colStatut.setPrefWidth(100);

        contratTable.getColumns().addAll(colId, colReservation, colDate, colMontant, colStatut);

        loadRecentContrats();

        container.getChildren().addAll(title, contratTable);
        return container;
    }

    private void loadRecentContrats() {
        List<Contrat> contrats = getClientContrats();
        if (contrats.size() > 5) {
            contrats = contrats.subList(0, 5);
        }
        contratTable.getItems().clear();
        contratTable.getItems().addAll(contrats);
    }

    // ===== TABLEAU DES FACTURES RÉCENTES =====

    private VBox createRecentFacturesTable() {
        VBox container = new VBox();
        container.setSpacing(8);

        Label title = new Label("💰 Mes factures récentes");
        title.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        factureTable = new TableView<>();
        factureTable.setPrefHeight(180);
        factureTable.getStyleClass().add("table-view");

        TableColumn<Facture, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getIdFacture())));
        colId.setPrefWidth(50);

        TableColumn<Facture, String> colContrat = new TableColumn<>("Contrat");
        colContrat.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("#" + data.getValue().getIdContrat()));
        colContrat.setPrefWidth(100);

        TableColumn<Facture, String> colDate = new TableColumn<>("Émise le");
        colDate.setCellValueFactory(data -> {
            if (data.getValue().getDateEmission() != null) {
                return new javafx.beans.property.SimpleStringProperty(data.getValue().getDateEmission().format(dateFormatterShort));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        colDate.setPrefWidth(120);

        TableColumn<Facture, String> colMontant = new TableColumn<>("Montant");
        colMontant.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.format("%.2f €", data.getValue().getMontantTotal())));
        colMontant.setPrefWidth(100);

        TableColumn<Facture, String> colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(data -> {
            String statut = data.getValue().getStatut();
            String emoji = "PAYEE".equals(statut) ? "✅ PAYÉE" : "❌ IMPAYÉE";
            return new javafx.beans.property.SimpleStringProperty(emoji);
        });
        colStatut.setPrefWidth(100);

        factureTable.getColumns().addAll(colId, colContrat, colDate, colMontant, colStatut);

        loadRecentFactures();

        container.getChildren().addAll(title, factureTable);
        return container;
    }

    private void loadRecentFactures() {
        List<Facture> factures = getClientFactures();
        if (factures.size() > 5) {
            factures = factures.subList(0, 5);
        }
        factureTable.getItems().clear();
        factureTable.getItems().addAll(factures);
    }

    // ===== MÉTHODES UTILITAIRES =====

    private String getVehiculeName(int vehiculeId) {
        Vehicule vehicule = vehiculeDAO.getById(vehiculeId);
        return vehicule != null ? vehicule.getMarque() + " " + vehicule.getModele() : "Véhicule #" + vehiculeId;
    }

    private List<Contrat> getClientContrats() {
        List<Reservation> reservations = reservationDAO.getByClient(clientId);
        List<Contrat> contrats = new java.util.ArrayList<>();
        for (Reservation r : reservations) {
            Contrat contrat = contratDAO.getByReservation(r.getIdReservation());
            if (contrat != null) {
                contrats.add(contrat);
            }
        }
        return contrats;
    }

    private List<Facture> getClientFactures() {
        List<Reservation> reservations = reservationDAO.getByClient(clientId);
        List<Facture> factures = new java.util.ArrayList<>();
        for (Reservation r : reservations) {
            Contrat contrat = contratDAO.getByReservation(r.getIdReservation());
            if (contrat != null) {
                Facture facture = factureDAO.getByContrat(contrat.getIdContrat());
                if (facture != null) {
                    factures.add(facture);
                }
            }
        }
        return factures;
    }

    public void refreshHomeStats() {
        if (totalReservationsLabel != null) {
            List<Reservation> reservations = reservationDAO.getByClient(clientId);
            int enAttente = 0, acceptees = 0;
            for (Reservation r : reservations) {
                if ("EN_ATTENTE".equals(r.getStatut())) enAttente++;
                if ("ACCEPTEE".equals(r.getStatut())) acceptees++;
            }
            totalReservationsLabel.setText(String.valueOf(reservations.size()));
            enAttenteLabel.setText(String.valueOf(enAttente));
            accepteesLabel.setText(String.valueOf(acceptees));
            totalContratsLabel.setText(String.valueOf(getClientContrats().size()));
            totalFacturesLabel.setText(String.valueOf(getClientFactures().size()));
            totalSanctionsLabel.setText(String.valueOf(sanctionDAO.getByClient(clientId).size()));

            loadRecentReservations();
            loadRecentContrats();
            loadRecentFactures();
        }
    }

    // ===== CRÉATION DES CONTENUS DES PAGES (AVEC MISE EN CACHE) =====

    private VBox getReservationContent() {
        if (cachedReservationContent == null) {
            System.out.println("🔄 Création de la vue Réservations");
            clientReservationView = new ClientReservationView();
            clientReservationController = new ClientReservationController(clientReservationView);
            cachedReservationContent = clientReservationView.getRoot();
        }
        return cachedReservationContent;
    }

    private VBox getVehiculeContent() {
        if (cachedVehiculeContent == null) {
            System.out.println("🔄 Création de la vue Véhicules");
            clientVehiculeView = new ClientVehiculeView();
            clientVehiculeController = new ClientVehiculeController(clientVehiculeView);
            cachedVehiculeContent = clientVehiculeView.getRoot();
        }
        return cachedVehiculeContent;
    }

    private VBox getContratContent() {
        if (cachedContratContent == null) {
            System.out.println("🔄 Création de la vue Contrats");
            clientContratView = new ClientContratView();
            clientContratController = new ClientContratController(clientContratView);
            cachedContratContent = clientContratView.getRoot();
        }
        return cachedContratContent;
    }

    private VBox getFactureContent() {
        if (cachedFactureContent == null) {
            System.out.println("🔄 Création de la vue Factures");
            clientFactureView = new ClientFactureView();
            clientFactureController = new ClientFactureController(clientFactureView);
            cachedFactureContent = clientFactureView.getRoot();
        }
        return cachedFactureContent;
    }

    private VBox getSanctionContent() {
        if (cachedSanctionContent == null) {
            System.out.println("🔄 Création de la vue Sanctions");
            clientSanctionView = new ClientSanctionView();
            clientSanctionController = new ClientSanctionController(clientSanctionView);
            cachedSanctionContent = clientSanctionView.getRoot();
        }
        return cachedSanctionContent;
    }

    private VBox getProfilContent() {
        if (cachedProfilContent == null) {
            System.out.println("🔄 Création de la vue Profil");
            clientProfilView = new ClientProfilView();
            clientProfilController = new ClientProfilController(clientProfilView);
            cachedProfilContent = clientProfilView.getRoot();
        }
        return cachedProfilContent;
    }

    // ===== MÉTHODES DE NAVIGATION (AVEC MISE EN CACHE) =====

    public void showHome() {
        setActiveMenuItem(menuAccueil);
        pageTitleLabel.setText("Tableau de bord");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(getHomeContent());
        refreshHomeStats();
    }

    public void showReservations() {
        setActiveMenuItem(menuReservations);
        pageTitleLabel.setText("📅 Mes Réservations");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(getReservationContent());
    }

    public void showVehicules() {
        setActiveMenuItem(menuVehicules);
        pageTitleLabel.setText("🚗 Véhicules disponibles");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(getVehiculeContent());
    }

    public void showContrats() {
        setActiveMenuItem(menuContrats);
        pageTitleLabel.setText("📄 Mes Contrats");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(getContratContent());
    }

    public void showFactures() {
        setActiveMenuItem(menuFactures);
        pageTitleLabel.setText("💰 Mes Factures");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(getFactureContent());
    }

    public void showSanctions() {
        setActiveMenuItem(menuSanctions);
        pageTitleLabel.setText("⚖️ Mes Sanctions");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(getSanctionContent());
    }

    public void showProfil() {
        setActiveMenuItem(menuProfil);
        pageTitleLabel.setText("👤 Mon Profil");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(getProfilContent());
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
    public HBox getMenuReservations() { return menuReservations; }
    public HBox getMenuVehicules() { return menuVehicules; }
    public HBox getMenuContrats() { return menuContrats; }
    public HBox getMenuFactures() { return menuFactures; }
    public HBox getMenuSanctions() { return menuSanctions; }
    public HBox getMenuProfil() { return menuProfil; }
    public Button getLogoutButton() { return logoutButton; }
}