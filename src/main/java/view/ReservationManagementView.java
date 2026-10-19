package view;

import dao.ClientDao;
import dao.ParkingDAO;
import dao.VehiculeDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Client;
import model.Parking;
import model.Reservation;
import model.Vehicule;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReservationManagementView {

    private VBox root;

    // ===== COMPOSANTS DE RECHERCHE =====
    private TextField searchField;
    private Button searchButton;
    private Button refreshButton;
    private ComboBox<String> statutFilterComboBox;

    // ===== TABLEAU DES RÉSERVATIONS =====
    private TableView<Reservation> reservationTable;
    private TableColumn<Reservation, String> colId;
    private TableColumn<Reservation, String> colClient;
    private TableColumn<Reservation, String> colVehicule;
    private TableColumn<Reservation, String> colImmatriculation;
    private TableColumn<Reservation, String> colDateDebut;
    private TableColumn<Reservation, String> colDateFin;
    private TableColumn<Reservation, String> colStatut;
    private TableColumn<Reservation, String> colLieux;
    private TableColumn<Reservation, Void> colActions;

    // ===== FORMULAIRE DE RÉSERVATION =====
    private VBox reservationFormContainer;
    private ComboBox<Client> clientComboBox;
    private Button nouveauClientBtn;
    private ComboBox<Vehicule> vehiculeComboBox;
    private DatePicker dateDebutPicker;
    private DatePicker dateFinPicker;
    private ComboBox<Parking> lieuPriseEnChargeComboBox;  // ← CHANGÉ EN ComboBox
    private ComboBox<Parking> lieuRetourComboBox;          // ← CHANGÉ EN ComboBox
    private Button submitReservationButton;
    private Button cancelReservationButton;
    private Label formErrorLabel;

    // ===== FORMULAIRE NOUVEAU CLIENT (WALK-IN) =====
    private VBox newClientFormContainer;
    private TextField newNomField;
    private TextField newPrenomField;
    private TextField newEmailField;
    private TextField newTelephoneField;
    private TextField newAdresseField;
    private Button saveNewClientButton;
    private Button cancelNewClientButton;
    private Label newClientErrorLabel;

    // ===== DAO =====
    private ClientDao clientDAO;
    private VehiculeDAO vehiculeDAO;
    private ParkingDAO parkingDAO;

    // ===== FORMATTEUR DE DATE =====
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ===== RÉSERVATION SÉLECTIONNÉE =====
    private Reservation selectedReservation;

    // ===== INTERFACE DE CALLBACK =====
    public interface ReservationCallback {
        void onAccept(Reservation reservation);
        void onRefuse(Reservation reservation);
        void onCancel(Reservation reservation);
        void onDelete(Reservation reservation);
        void onCreateReservation(int clientId, int vehiculeId, java.time.LocalDateTime dateDebut,
                                 java.time.LocalDateTime dateFin, String lieuPriseEnCharge, String lieuRetour);
        void onCreateClient(String nom, String prenom, String email, String telephone, String adresse);
    }

    private ReservationCallback callback;

    public ReservationManagementView() {
        this.clientDAO = new ClientDao();
        this.vehiculeDAO = new VehiculeDAO();
        this.parkingDAO = new ParkingDAO();
        initUI();
    }

    private void initUI() {
        root = new VBox();
        root.setSpacing(15);
        root.setPadding(new Insets(10));

        Label titleLabel = new Label("📅 Gestion des Réservations");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        HBox searchBar = createSearchBar();
        VBox formSection = createReservationForm();
        VBox newClientForm = createNewClientForm();
        HBox statsBar = createStatsBar();
        createTable();

        root.getChildren().addAll(
                titleLabel,
                searchBar,
                formSection,
                newClientForm,
                statsBar,
                reservationTable
        );

        VBox.setVgrow(reservationTable, Priority.ALWAYS);
    }

    private HBox createSearchBar() {
        HBox searchBar = new HBox();
        searchBar.setSpacing(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setPadding(new Insets(5, 0, 10, 0));

        Label searchLabel = new Label("🔍 Rechercher :");
        searchLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");

        searchField = new TextField();
        searchField.setPromptText("Client, véhicule ou immatriculation...");
        searchField.setPrefWidth(250);
        searchField.getStyleClass().add("input-field");

        searchButton = new Button("Rechercher");
        searchButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-pref-height: 32; -fx-padding: 0 15; -fx-background-radius: 4; -fx-cursor: hand;");

        refreshButton = new Button("🔄 Rafraîchir");
        refreshButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-pref-height: 32; -fx-padding: 0 15; -fx-background-radius: 4; -fx-cursor: hand;");

        Label filterLabel = new Label("Filtrer :");
        filterLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e; -fx-padding: 0 0 0 15;");

        statutFilterComboBox = new ComboBox<>();
        statutFilterComboBox.getItems().addAll("Tous", "EN_ATTENTE", "ACCEPTEE", "REFUSEE", "ANNULEE");
        statutFilterComboBox.setValue("Tous");
        statutFilterComboBox.setPrefWidth(130);
        statutFilterComboBox.getStyleClass().add("input-field");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button nouvelleReservationBtn = new Button("➕ Nouvelle réservation");
        nouvelleReservationBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-pref-height: 32; -fx-padding: 0 20; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-weight: bold;");
        nouvelleReservationBtn.setOnAction(e -> toggleFormVisibility());

        searchBar.getChildren().addAll(
                searchLabel, searchField, searchButton, refreshButton,
                filterLabel, statutFilterComboBox, spacer, nouvelleReservationBtn
        );
        return searchBar;
    }

    // ===== FORMULAIRE DE RÉSERVATION AVEC COMBOBOX PARKINGS =====
    private VBox createReservationForm() {
        reservationFormContainer = new VBox();
        reservationFormContainer.setSpacing(12);
        reservationFormContainer.setPadding(new Insets(15));
        reservationFormContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-border-color: #e0e5ec; -fx-border-radius: 8; -fx-border-width: 1;");
        reservationFormContainer.setVisible(false);
        reservationFormContainer.setManaged(false);

        Label formTitle = new Label("📝 Nouvelle réservation");
        formTitle.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10, 0, 10, 0));

        int row = 0;

        // ===== CLIENT =====
        Label clientLabel = new Label("Client *");
        clientLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        clientComboBox = new ComboBox<>();
        clientComboBox.setPromptText("Sélectionner un client existant...");
        clientComboBox.setPrefWidth(250);
        clientComboBox.getStyleClass().add("input-field");

        nouveauClientBtn = new Button("➕ Nouveau client (walk-in)");
        nouveauClientBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-pref-height: 32; -fx-padding: 0 15; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-weight: bold;");
        nouveauClientBtn.setOnAction(e -> showNewClientForm());

        HBox clientRow = new HBox(10);
        clientRow.setAlignment(Pos.CENTER_LEFT);
        clientRow.getChildren().addAll(clientComboBox, nouveauClientBtn);

        formGrid.add(clientLabel, 0, row);
        formGrid.add(clientRow, 1, row, 3, 1);

        // ===== VÉHICULE =====
        row++;
        Label vehiculeLabel = new Label("Véhicule *");
        vehiculeLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        vehiculeComboBox = new ComboBox<>();
        vehiculeComboBox.setPromptText("Sélectionner un véhicule...");
        vehiculeComboBox.setPrefWidth(250);
        vehiculeComboBox.getStyleClass().add("input-field");

        formGrid.add(vehiculeLabel, 0, row);
        formGrid.add(vehiculeComboBox, 1, row);

        // ===== DATES =====
        row++;
        Label dateDebutLabel = new Label("Date de début *");
        dateDebutLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        dateDebutPicker = new DatePicker();
        dateDebutPicker.setPromptText("jj/mm/aaaa");
        dateDebutPicker.setPrefWidth(200);
        dateDebutPicker.getStyleClass().add("input-field");

        Label dateFinLabel = new Label("Date de fin *");
        dateFinLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        dateFinPicker = new DatePicker();
        dateFinPicker.setPromptText("jj/mm/aaaa");
        dateFinPicker.setPrefWidth(200);
        dateFinPicker.getStyleClass().add("input-field");

        formGrid.add(dateDebutLabel, 0, row);
        formGrid.add(dateDebutPicker, 1, row);
        formGrid.add(dateFinLabel, 2, row);
        formGrid.add(dateFinPicker, 3, row);

        // ===== LIEUX : COMBOBOX PARKINGS =====
        row++;
        Label lieuPriseLabel = new Label("Lieu de prise en charge *");
        lieuPriseLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        lieuPriseEnChargeComboBox = new ComboBox<>();
        lieuPriseEnChargeComboBox.setPromptText("Sélectionner un parking...");
        lieuPriseEnChargeComboBox.setPrefWidth(200);
        lieuPriseEnChargeComboBox.getStyleClass().add("input-field");

        Label lieuRetourLabel = new Label("Lieu de retour *");
        lieuRetourLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        lieuRetourComboBox = new ComboBox<>();
        lieuRetourComboBox.setPromptText("Sélectionner un parking...");
        lieuRetourComboBox.setPrefWidth(200);
        lieuRetourComboBox.getStyleClass().add("input-field");

        formGrid.add(lieuPriseLabel, 0, row);
        formGrid.add(lieuPriseEnChargeComboBox, 1, row);
        formGrid.add(lieuRetourLabel, 2, row);
        formGrid.add(lieuRetourComboBox, 3, row);

        // ===== CONVERTISSEUR POUR AFFICHER LE NOM DU PARKING =====
        javafx.util.StringConverter<Parking> parkingConverter = new javafx.util.StringConverter<Parking>() {
            @Override
            public String toString(Parking p) {
                return p != null ? p.getNom() + " (" + p.getAdresse() + ")" : "";
            }
            @Override
            public Parking fromString(String string) {
                return null;
            }
        };
        lieuPriseEnChargeComboBox.setConverter(parkingConverter);
        lieuRetourComboBox.setConverter(parkingConverter);

        // ===== MESSAGE D'ERREUR =====
        formErrorLabel = new Label();
        formErrorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12; -fx-padding: 5 0 0 0; -fx-font-weight: 600;");
        formErrorLabel.setManaged(false);
        formErrorLabel.setVisible(false);

        // ===== BOUTONS =====
        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        submitReservationButton = new Button("📝 Confirmer la réservation");
        submitReservationButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-pref-height: 36; -fx-padding: 0 20; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-weight: bold;");

        cancelReservationButton = new Button("❌ Annuler");
        cancelReservationButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-pref-height: 36; -fx-padding: 0 20; -fx-background-radius: 4; -fx-cursor: hand;");
        cancelReservationButton.setOnAction(e -> toggleFormVisibility());

        buttonBox.getChildren().addAll(cancelReservationButton, submitReservationButton);

        reservationFormContainer.getChildren().addAll(formTitle, formGrid, formErrorLabel, buttonBox);
        return reservationFormContainer;
    }

    // ===== FORMULAIRE NOUVEAU CLIENT =====
    private VBox createNewClientForm() {
        newClientFormContainer = new VBox();
        newClientFormContainer.setSpacing(12);
        newClientFormContainer.setPadding(new Insets(15));
        newClientFormContainer.setStyle("-fx-background-color: #fef9e7; -fx-background-radius: 8; -fx-border-color: #f39c12; -fx-border-radius: 8; -fx-border-width: 1;");
        newClientFormContainer.setVisible(false);
        newClientFormContainer.setManaged(false);

        Label formTitle = new Label("👤 Nouveau client (sans compte)");
        formTitle.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label infoLabel = new Label("ℹ️ Ce client n'aura pas de compte. Il pourra être associé à une réservation.");
        infoLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10, 0, 10, 0));

        int row = 0;

        Label nomLabel = new Label("Nom *");
        nomLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        newNomField = new TextField();
        newNomField.setPromptText("Dupont");
        newNomField.setPrefWidth(200);
        newNomField.getStyleClass().add("input-field");

        Label prenomLabel = new Label("Prénom *");
        prenomLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        newPrenomField = new TextField();
        newPrenomField.setPromptText("Jean");
        newPrenomField.setPrefWidth(200);
        newPrenomField.getStyleClass().add("input-field");

        formGrid.add(nomLabel, 0, row);
        formGrid.add(newNomField, 1, row);
        formGrid.add(prenomLabel, 2, row);
        formGrid.add(newPrenomField, 3, row);

        row++;
        Label emailLabel = new Label("Email *");
        emailLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        newEmailField = new TextField();
        newEmailField.setPromptText("jean.dupont@email.com");
        newEmailField.setPrefWidth(250);
        newEmailField.getStyleClass().add("input-field");

        Label telephoneLabel = new Label("Téléphone *");
        telephoneLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        newTelephoneField = new TextField();
        newTelephoneField.setPromptText("771234567");
        newTelephoneField.setPrefWidth(200);
        newTelephoneField.getStyleClass().add("input-field");

        formGrid.add(emailLabel, 0, row);
        formGrid.add(newEmailField, 1, row);
        formGrid.add(telephoneLabel, 2, row);
        formGrid.add(newTelephoneField, 3, row);

        row++;
        Label adresseLabel = new Label("Adresse");
        adresseLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        newAdresseField = new TextField();
        newAdresseField.setPromptText("12 Rue de Paris, Dakar");
        newAdresseField.setPrefWidth(400);
        newAdresseField.getStyleClass().add("input-field");

        formGrid.add(adresseLabel, 0, row);
        formGrid.add(newAdresseField, 1, row, 3, 1);

        newClientErrorLabel = new Label();
        newClientErrorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12; -fx-padding: 5 0 0 0; -fx-font-weight: 600;");
        newClientErrorLabel.setManaged(false);
        newClientErrorLabel.setVisible(false);

        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        saveNewClientButton = new Button("💾 Créer le client");
        saveNewClientButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-pref-height: 36; -fx-padding: 0 20; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-weight: bold;");

        cancelNewClientButton = new Button("❌ Annuler");
        cancelNewClientButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-pref-height: 36; -fx-padding: 0 20; -fx-background-radius: 4; -fx-cursor: hand;");
        cancelNewClientButton.setOnAction(e -> hideNewClientForm());

        buttonBox.getChildren().addAll(cancelNewClientButton, saveNewClientButton);

        newClientFormContainer.getChildren().addAll(formTitle, infoLabel, formGrid, newClientErrorLabel, buttonBox);
        return newClientFormContainer;
    }

    // ===== AFFICHAGE DU FORMULAIRE NOUVEAU CLIENT =====
    public void showNewClientForm() {
        newClientFormContainer.setVisible(true);
        newClientFormContainer.setManaged(true);
        clearNewClientForm();
    }

    public void hideNewClientForm() {
        newClientFormContainer.setVisible(false);
        newClientFormContainer.setManaged(false);
        clearNewClientForm();
    }

    private void clearNewClientForm() {
        newNomField.clear();
        newPrenomField.clear();
        newEmailField.clear();
        newTelephoneField.clear();
        newAdresseField.clear();
        newClientErrorLabel.setVisible(false);
        newClientErrorLabel.setText("");
    }

    public void showNewClientError(String message) {
        newClientErrorLabel.setText(message);
        newClientErrorLabel.setManaged(true);
        newClientErrorLabel.setVisible(true);
    }

    public void clearNewClientError() {
        newClientErrorLabel.setText("");
        newClientErrorLabel.setManaged(false);
        newClientErrorLabel.setVisible(false);
    }

    // ===== GESTION DU FORMULAIRE DE RÉSERVATION =====
    public void toggleFormVisibility() {
        boolean visible = reservationFormContainer.isVisible();
        reservationFormContainer.setVisible(!visible);
        reservationFormContainer.setManaged(!visible);
        if (!visible) {
            clearForm();
            loadClientsVehiculesParkings();
        }
    }

    private void clearForm() {
        clientComboBox.setValue(null);
        vehiculeComboBox.setValue(null);
        dateDebutPicker.setValue(null);
        dateFinPicker.setValue(null);
        lieuPriseEnChargeComboBox.setValue(null);
        lieuRetourComboBox.setValue(null);
        formErrorLabel.setVisible(false);
        formErrorLabel.setText("");
    }

    // ===== CHARGER CLIENTS + VÉHICULES + PARKINGS =====
    private void loadClientsVehiculesParkings() {
        // Clients
        List<Client> clients = clientDAO.getAll();
        clientComboBox.getItems().clear();
        clientComboBox.getItems().addAll(clients);
        clientComboBox.setConverter(new javafx.util.StringConverter<Client>() {
            @Override
            public String toString(Client c) {
                return c != null ? c.getPrenom() + " " + c.getNom() + " (" + c.getEmail() + ")" : "";
            }
            @Override
            public Client fromString(String string) {
                return null;
            }
        });

        // Véhicules actifs
        List<Vehicule> vehicules = vehiculeDAO.getVehiculesActifs();
        vehiculeComboBox.getItems().clear();
        vehiculeComboBox.getItems().addAll(vehicules);
        vehiculeComboBox.setConverter(new javafx.util.StringConverter<Vehicule>() {
            @Override
            public String toString(Vehicule v) {
                return v != null ? v.getMarque() + " " + v.getModele() + " - " + v.getImmatriculation() : "";
            }
            @Override
            public Vehicule fromString(String string) {
                return null;
            }
        });

        // ===== PARKINGS =====
        List<Parking> parkings = parkingDAO.getAll();
        lieuPriseEnChargeComboBox.getItems().clear();
        lieuPriseEnChargeComboBox.getItems().addAll(parkings);
        lieuRetourComboBox.getItems().clear();
        lieuRetourComboBox.getItems().addAll(parkings);

        System.out.println("📋 " + clients.size() + " clients chargés");
        System.out.println("🚗 " + vehicules.size() + " véhicules actifs chargés");
        System.out.println("🅿️ " + parkings.size() + " parkings chargés");
    }

    public void showFormError(String message) {
        formErrorLabel.setText(message);
        formErrorLabel.setManaged(true);
        formErrorLabel.setVisible(true);
    }

    public void clearFormError() {
        formErrorLabel.setText("");
        formErrorLabel.setManaged(false);
        formErrorLabel.setVisible(false);
    }

    // ===== TABLEAU =====
    private void createTable() {
        reservationTable = new TableView<>();
        reservationTable.setPrefHeight(300);
        reservationTable.getStyleClass().add("table-view");

        colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getIdReservation())));
        colId.setPrefWidth(50);

        colClient = new TableColumn<>("Client");
        colClient.setCellValueFactory(data -> {
            Client client = clientDAO.getById(data.getValue().getIdClient());
            return new SimpleStringProperty(client != null ? client.getPrenom() + " " + client.getNom() : "N/A");
        });
        colClient.setPrefWidth(150);

        colVehicule = new TableColumn<>("Véhicule");
        colVehicule.setCellValueFactory(data -> {
            Vehicule vehicule = vehiculeDAO.getById(data.getValue().getIdVehicule());
            return new SimpleStringProperty(vehicule != null ? vehicule.getMarque() + " " + vehicule.getModele() : "N/A");
        });
        colVehicule.setPrefWidth(150);

        colImmatriculation = new TableColumn<>("Immatriculation");
        colImmatriculation.setCellValueFactory(data -> {
            Vehicule vehicule = vehiculeDAO.getById(data.getValue().getIdVehicule());
            return new SimpleStringProperty(vehicule != null ? vehicule.getImmatriculation() : "N/A");
        });
        colImmatriculation.setPrefWidth(120);

        colDateDebut = new TableColumn<>("Date début");
        colDateDebut.setCellValueFactory(data -> {
            if (data.getValue().getDateDebut() != null) {
                return new SimpleStringProperty(data.getValue().getDateDebut().format(dateFormatter));
            }
            return new SimpleStringProperty("");
        });
        colDateDebut.setPrefWidth(140);

        colDateFin = new TableColumn<>("Date fin");
        colDateFin.setCellValueFactory(data -> {
            if (data.getValue().getDateFin() != null) {
                return new SimpleStringProperty(data.getValue().getDateFin().format(dateFormatter));
            }
            return new SimpleStringProperty("");
        });
        colDateFin.setPrefWidth(140);

        colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(data -> {
            String statut = data.getValue().getStatut();
            String emoji = switch (statut) {
                case "EN_ATTENTE" -> "⏳";
                case "ACCEPTEE" -> "✅";
                case "REFUSEE" -> "❌";
                case "ANNULEE" -> "🚫";
                default -> "⚪";
            };
            return new SimpleStringProperty(emoji + " " + statut);
        });
        colStatut.setPrefWidth(110);

        colLieux = new TableColumn<>("Lieux");
        colLieux.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getLieuPriseEnCharge() + " → " + data.getValue().getLieuRetour()
        ));
        colLieux.setPrefWidth(220);

        colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(220);
        colActions.setCellFactory(param -> new TableCell<Reservation, Void>() {
            private final Button acceptBtn = new Button("✅");
            private final Button refuseBtn = new Button("❌");
            private final Button cancelBtn = new Button("🚫");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox actionBox = new HBox(5, acceptBtn, refuseBtn, cancelBtn, deleteBtn);

            {
                acceptBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 4 8; -fx-background-radius: 3; -fx-font-size: 11;");
                refuseBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 4 8; -fx-background-radius: 3; -fx-font-size: 11;");
                cancelBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 4 8; -fx-background-radius: 3; -fx-font-size: 11;");
                deleteBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 4 8; -fx-background-radius: 3; -fx-font-size: 11;");
                actionBox.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Reservation reservation = getTableView().getItems().get(getIndex());

                    String statut = reservation.getStatut();
                    acceptBtn.setVisible("EN_ATTENTE".equals(statut));
                    refuseBtn.setVisible("EN_ATTENTE".equals(statut));
                    cancelBtn.setVisible("EN_ATTENTE".equals(statut) || "ACCEPTEE".equals(statut));
                    deleteBtn.setVisible(true);

                    acceptBtn.setOnAction(e -> {
                        if (callback != null) callback.onAccept(reservation);
                    });
                    refuseBtn.setOnAction(e -> {
                        if (callback != null) callback.onRefuse(reservation);
                    });
                    cancelBtn.setOnAction(e -> {
                        if (callback != null) callback.onCancel(reservation);
                    });
                    deleteBtn.setOnAction(e -> {
                        if (callback != null) callback.onDelete(reservation);
                    });

                    setGraphic(actionBox);
                }
            }
        });

        reservationTable.getColumns().addAll(
                colId, colClient, colVehicule, colImmatriculation,
                colDateDebut, colDateFin, colStatut, colLieux, colActions
        );
    }

    private HBox createStatsBar() {
        HBox statsBar = new HBox();
        statsBar.setSpacing(20);
        statsBar.setAlignment(Pos.CENTER_LEFT);
        statsBar.setPadding(new Insets(5, 0, 10, 0));

        Label statsLabel = new Label("📊 Statistiques :");
        statsLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");

        Label totalLabel = new Label("Total: 0");
        totalLabel.setStyle("-fx-text-fill: #2c3e50;");
        totalLabel.setId("totalLabel");

        Label enAttenteLabel = new Label("⏳ En attente: 0");
        enAttenteLabel.setStyle("-fx-text-fill: #f39c12;");
        enAttenteLabel.setId("enAttenteLabel");

        Label accepteeLabel = new Label("✅ Acceptées: 0");
        accepteeLabel.setStyle("-fx-text-fill: #2ecc71;");
        accepteeLabel.setId("accepteeLabel");

        Label refuseeLabel = new Label("❌ Refusées: 0");
        refuseeLabel.setStyle("-fx-text-fill: #e74c3c;");
        refuseeLabel.setId("refuseeLabel");

        Label annuleeLabel = new Label("🚫 Annulées: 0");
        annuleeLabel.setStyle("-fx-text-fill: #95a5a6;");
        annuleeLabel.setId("annuleeLabel");

        statsBar.getChildren().addAll(
                statsLabel, totalLabel, enAttenteLabel, accepteeLabel, refuseeLabel, annuleeLabel
        );
        return statsBar;
    }

    public void setCallback(ReservationCallback callback) {
        this.callback = callback;
    }

    public void refreshTable(List<Reservation> reservations) {
        reservationTable.getItems().clear();
        if (reservations != null) {
            reservationTable.getItems().addAll(reservations);
        }
    }

    public void updateStats(int total, int enAttente, int acceptees, int refusees, int annulees) {
        for (javafx.scene.Node node : ((HBox) root.getChildren().get(4)).getChildren()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                String id = label.getId();
                if (id != null) {
                    switch (id) {
                        case "totalLabel" -> label.setText("Total: " + total);
                        case "enAttenteLabel" -> label.setText("⏳ En attente: " + enAttente);
                        case "accepteeLabel" -> label.setText("✅ Acceptées: " + acceptees);
                        case "refuseeLabel" -> label.setText("❌ Refusées: " + refusees);
                        case "annuleeLabel" -> label.setText("🚫 Annulées: " + annulees);
                    }
                }
            }
        }
    }

    // ===== GETTERS =====
    public VBox getRoot() { return root; }
    public TextField getSearchField() { return searchField; }
    public Button getSearchButton() { return searchButton; }
    public Button getRefreshButton() { return refreshButton; }
    public ComboBox<String> getStatutFilterComboBox() { return statutFilterComboBox; }
    public TableView<Reservation> getReservationTable() { return reservationTable; }
    public Reservation getSelectedReservation() { return reservationTable.getSelectionModel().getSelectedItem(); }
    public ComboBox<Client> getClientComboBox() { return clientComboBox; }
    public Button getNouveauClientBtn() { return nouveauClientBtn; }
    public ComboBox<Vehicule> getVehiculeComboBox() { return vehiculeComboBox; }
    public DatePicker getDateDebutPicker() { return dateDebutPicker; }
    public DatePicker getDateFinPicker() { return dateFinPicker; }
    public ComboBox<Parking> getLieuPriseEnChargeComboBox() { return lieuPriseEnChargeComboBox; }
    public ComboBox<Parking> getLieuRetourComboBox() { return lieuRetourComboBox; }
    public Button getSubmitReservationButton() { return submitReservationButton; }
    public Button getCancelReservationButton() { return cancelReservationButton; }
    public VBox getNewClientFormContainer() { return newClientFormContainer; }
    public TextField getNewNomField() { return newNomField; }
    public TextField getNewPrenomField() { return newPrenomField; }
    public TextField getNewEmailField() { return newEmailField; }
    public TextField getNewTelephoneField() { return newTelephoneField; }
    public TextField getNewAdresseField() { return newAdresseField; }
    public Button getSaveNewClientButton() { return saveNewClientButton; }
    public Button getCancelNewClientButton() { return cancelNewClientButton; }
}