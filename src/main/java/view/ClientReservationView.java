package view;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Parking;
import model.Reservation;
import model.Vehicule;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClientReservationView {

    private VBox root;

    // ===== COMPOSANTS DE RECHERCHE =====
    private TextField searchField;
    private Button searchButton;
    private Button refreshButton;
    private ComboBox<String> statutFilterComboBox;

    // ===== TABLEAU DES RÉSERVATIONS =====
    private TableView<Reservation> reservationTable;
    private TableColumn<Reservation, String> colId;
    private TableColumn<Reservation, String> colVehicule;
    private TableColumn<Reservation, String> colDateDebut;
    private TableColumn<Reservation, String> colDateFin;
    private TableColumn<Reservation, String> colDateDemande;
    private TableColumn<Reservation, String> colStatut;
    private TableColumn<Reservation, String> colLieux;
    private TableColumn<Reservation, Void> colActions;

    // ===== COMPOSANTS POUR NOUVELLE RÉSERVATION =====
    private VBox reservationFormContainer;
    private ComboBox<Vehicule> vehiculeComboBox;
    private DatePicker dateDebutPicker;
    private DatePicker dateFinPicker;

    // ===== CHANGEMENT : ComboBox<Parking> au lieu de TextField =====
    private ComboBox<Parking> lieuPriseEnChargeComboBox;
    private ComboBox<Parking> lieuRetourComboBox;

    private Button submitReservationButton;
    private Button cancelReservationButton;
    private Label formErrorLabel;

    // ===== FORMATTEUR =====
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ===== INTERFACE DE CALLBACK =====
    public interface ReservationCallback {
        void onSearch(String keyword);
        void onFilter(String statut);
        void onRefresh();
        void onAnnuler(Reservation reservation);
        void onCreerReservation(int idVehicule, LocalDateTime dateDebut, LocalDateTime dateFin,
                                String lieuPriseEnCharge, String lieuRetour);
    }

    private ReservationCallback callback;

    public ClientReservationView() {
        initUI();
    }

    private void initUI() {
        root = new VBox();
        root.setSpacing(15);
        root.setPadding(new Insets(10));

        Label titleLabel = new Label("📅 Mes Réservations");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        HBox searchBar = createSearchBar();
        VBox formSection = createReservationForm();
        createTable();
        HBox statsBar = createStatsBar();

        root.getChildren().addAll(
                titleLabel,
                searchBar,
                formSection,
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
        searchField.setPromptText("Véhicule, lieu...");
        searchField.setPrefWidth(200);
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
        statutFilterComboBox.setPrefWidth(120);
        statutFilterComboBox.getStyleClass().add("input-field");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button nouvelleReservationBtn = new Button("➕ Nouvelle réservation");
        nouvelleReservationBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-pref-height: 32; -fx-padding: 0 15; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-weight: bold;");
        nouvelleReservationBtn.setOnAction(e -> toggleFormVisibility());

        searchBar.getChildren().addAll(
                searchLabel, searchField, searchButton, refreshButton,
                filterLabel, statutFilterComboBox, spacer, nouvelleReservationBtn
        );
        return searchBar;
    }

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

        // ===== VÉHICULE =====
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

    public void toggleFormVisibility() {
        boolean visible = reservationFormContainer.isVisible();
        reservationFormContainer.setVisible(!visible);
        reservationFormContainer.setManaged(!visible);
        if (!visible) {
            clearForm();
        }
    }

    private void clearForm() {
        vehiculeComboBox.setValue(null);
        dateDebutPicker.setValue(null);
        dateFinPicker.setValue(null);
        lieuPriseEnChargeComboBox.setValue(null);
        lieuRetourComboBox.setValue(null);
        formErrorLabel.setVisible(false);
        formErrorLabel.setText("");
    }

    private void createTable() {
        reservationTable = new TableView<>();
        reservationTable.setPrefHeight(300);
        reservationTable.getStyleClass().add("table-view");

        colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getIdReservation())));
        colId.setPrefWidth(50);

        colVehicule = new TableColumn<>("Véhicule");
        colVehicule.setCellValueFactory(data -> new SimpleStringProperty("Véhicule #" + data.getValue().getIdVehicule()));
        colVehicule.setPrefWidth(120);

        colDateDebut = new TableColumn<>("Date début");
        colDateDebut.setCellValueFactory(data -> {
            if (data.getValue().getDateDebut() != null) {
                return new SimpleStringProperty(data.getValue().getDateDebut().format(dateFormatter));
            }
            return new SimpleStringProperty("");
        });
        colDateDebut.setPrefWidth(150);

        colDateFin = new TableColumn<>("Date fin");
        colDateFin.setCellValueFactory(data -> {
            if (data.getValue().getDateFin() != null) {
                return new SimpleStringProperty(data.getValue().getDateFin().format(dateFormatter));
            }
            return new SimpleStringProperty("");
        });
        colDateFin.setPrefWidth(150);

        colDateDemande = new TableColumn<>("Demandée le");
        colDateDemande.setCellValueFactory(data -> {
            if (data.getValue().getDateDemande() != null) {
                return new SimpleStringProperty(data.getValue().getDateDemande().format(dateFormatter));
            }
            return new SimpleStringProperty("");
        });
        colDateDemande.setPrefWidth(150);

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
        colStatut.setPrefWidth(120);

        colLieux = new TableColumn<>("Lieux");
        colLieux.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getLieuPriseEnCharge() + " → " + data.getValue().getLieuRetour()
        ));
        colLieux.setPrefWidth(200);

        // ===== COLONNE ACTIONS =====
        colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(180);
        colActions.setCellFactory(param -> new TableCell<Reservation, Void>() {
            private final Button annulerBtn = new Button();
            private final HBox actionBox = new HBox();

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Reservation reservation = getTableView().getItems().get(getIndex());

                    String statut = reservation.getStatut();

                    boolean datePassee = reservation.getDateDebut() != null &&
                            reservation.getDateDebut().isBefore(LocalDateTime.now());

                    boolean peutAnnuler = ("EN_ATTENTE".equals(statut) || "ACCEPTEE".equals(statut))
                            && !datePassee;

                    if (peutAnnuler) {
                        annulerBtn.setText("🚫 Annuler");
                        annulerBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 15; -fx-background-radius: 4; -fx-font-size: 12; -fx-font-weight: bold;");
                        annulerBtn.setDisable(false);

                        annulerBtn.setOnAction(e -> {
                            if (callback != null) {
                                callback.onAnnuler(reservation);
                            }
                        });
                    } else {
                        if ("REFUSEE".equals(statut)) {
                            annulerBtn.setText("❌ Refusée");
                            annulerBtn.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: #7f8c8d; -fx-padding: 6 15; -fx-background-radius: 4; -fx-font-size: 12;");
                        } else if ("ANNULEE".equals(statut)) {
                            annulerBtn.setText("🚫 Annulée");
                            annulerBtn.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: #7f8c8d; -fx-padding: 6 15; -fx-background-radius: 4; -fx-font-size: 12;");
                        } else {
                            annulerBtn.setText("🔒 Trop tard");
                            annulerBtn.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: #7f8c8d; -fx-padding: 6 15; -fx-background-radius: 4; -fx-font-size: 12;");
                        }
                        annulerBtn.setDisable(true);
                    }

                    actionBox.setAlignment(Pos.CENTER);
                    actionBox.getChildren().setAll(annulerBtn);
                    setGraphic(actionBox);
                }
            }
        });

        reservationTable.getColumns().addAll(
                colId, colVehicule, colDateDebut, colDateFin,
                colDateDemande, colStatut, colLieux, colActions
        );
    }

    private HBox createStatsBar() {
        HBox statsBar = new HBox();
        statsBar.setSpacing(20);
        statsBar.setAlignment(Pos.CENTER_LEFT);
        statsBar.setPadding(new Insets(5, 0, 10, 0));

        Label statsLabel = new Label("📊 Mes statistiques :");
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

        statsBar.getChildren().addAll(statsLabel, totalLabel, enAttenteLabel, accepteeLabel, refuseeLabel, annuleeLabel);
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
        for (javafx.scene.Node node : ((HBox) root.getChildren().get(3)).getChildren()) {
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

    public void showError(String message) {
        formErrorLabel.setText(message);
        formErrorLabel.setManaged(true);
        formErrorLabel.setVisible(true);
    }

    public void clearError() {
        formErrorLabel.setText("");
        formErrorLabel.setManaged(false);
        formErrorLabel.setVisible(false);
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

    public void loadVehicules(List<Vehicule> vehicules) {
        vehiculeComboBox.getItems().clear();
        if (vehicules != null) {
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
        }
    }

    // ===== NOUVELLE MÉTHODE : CHARGER LES PARKINGS =====
    public void loadParkings(List<Parking> parkings) {
        if (parkings != null) {
            lieuPriseEnChargeComboBox.getItems().clear();
            lieuPriseEnChargeComboBox.getItems().addAll(parkings);

            lieuRetourComboBox.getItems().clear();
            lieuRetourComboBox.getItems().addAll(parkings);
        }
    }

    // ===== GETTERS =====
    public VBox getRoot() { return root; }
    public TextField getSearchField() { return searchField; }
    public Button getSearchButton() { return searchButton; }
    public Button getRefreshButton() { return refreshButton; }
    public ComboBox<String> getStatutFilterComboBox() { return statutFilterComboBox; }
    public TableView<Reservation> getReservationTable() { return reservationTable; }
    public ComboBox<Vehicule> getVehiculeComboBox() { return vehiculeComboBox; }
    public DatePicker getDateDebutPicker() { return dateDebutPicker; }
    public DatePicker getDateFinPicker() { return dateFinPicker; }
    public ComboBox<Parking> getLieuPriseEnChargeComboBox() { return lieuPriseEnChargeComboBox; }
    public ComboBox<Parking> getLieuRetourComboBox() { return lieuRetourComboBox; }
    public Button getSubmitReservationButton() { return submitReservationButton; }
    public Button getCancelReservationButton() { return cancelReservationButton; }
}