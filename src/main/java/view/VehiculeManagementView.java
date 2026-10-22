package view;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Parking;
import model.Vehicule;
import utils.FormatUtil;

import java.util.List;

public class VehiculeManagementView {

    private VBox root;

    // ===== COMPOSANTS DE RECHERCHE =====
    private TextField searchField;
    private Button searchButton;
    private Button refreshButton;

    // ===== TABLEAU DES VÉHICULES =====
    private TableView<Vehicule> vehiculeTable;
    private TableColumn<Vehicule, String> colId;
    private TableColumn<Vehicule, String> colMarque;
    private TableColumn<Vehicule, String> colModele;
    private TableColumn<Vehicule, String> colAnnee;
    private TableColumn<Vehicule, String> colImmatriculation;
    private TableColumn<Vehicule, String> colCarburant;
    private TableColumn<Vehicule, String> colKilometrage;
    private TableColumn<Vehicule, String> colPrix;
    private TableColumn<Vehicule, String> colStatut;
    private TableColumn<Vehicule, Void> colActions;

    // ===== COMPOSANTS DU FORMULAIRE =====
    private TextField marqueField;
    private TextField modeleField;
    private TextField anneeField;
    private TextField immatriculationField;
    private ComboBox<String> carburantComboBox;
    private TextField kilometrageField;
    private TextField prixField;
    private ComboBox<String> statutComboBox;
    private ComboBox<Parking> parkingComboBox;
    private CheckBox actifCheckBox;

    private Button saveButton;
    private Button cancelButton;
    private Button deleteButton;

    private Label formTitleLabel;
    private Label errorLabel;
    private VBox formContainer;

    private Vehicule selectedVehicule;

    public interface DeleteCallback {
        void onDelete(Vehicule vehicule);
    }

    private DeleteCallback deleteCallback;

    public VehiculeManagementView() {
        initUI();
    }

    private void initUI() {
        root = new VBox();
        root.setSpacing(15);
        root.setPadding(new Insets(10));

        Label titleLabel = new Label("🚗 Gestion des Véhicules");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        HBox searchBar = createSearchBar();
        createTable();
        createForm();

        Separator separator = new Separator();
        separator.setStyle("-fx-padding: 10 0 10 0;");

        root.getChildren().addAll(titleLabel, searchBar, vehiculeTable, separator, formContainer);
        VBox.setVgrow(vehiculeTable, Priority.ALWAYS);
    }

    private HBox createSearchBar() {
        HBox searchBar = new HBox();
        searchBar.setSpacing(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setPadding(new Insets(5, 0, 5, 0));

        Label searchLabel = new Label("🔍 Rechercher :");
        searchLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");

        searchField = new TextField();
        searchField.setPromptText("Marque, modèle ou immatriculation...");
        searchField.setPrefWidth(300);
        searchField.getStyleClass().add("input-field");

        searchButton = new Button("Rechercher");
        searchButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-pref-height: 32; -fx-padding: 0 15; -fx-background-radius: 4; -fx-cursor: hand;");

        refreshButton = new Button("🔄 Rafraîchir");
        refreshButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-pref-height: 32; -fx-padding: 0 15; -fx-background-radius: 4; -fx-cursor: hand;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addButton = new Button("➕ Ajouter un véhicule");
        addButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-pref-height: 32; -fx-padding: 0 20; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-weight: bold;");
        addButton.setOnAction(e -> showAddForm());

        searchBar.getChildren().addAll(searchLabel, searchField, searchButton, refreshButton, spacer, addButton);
        return searchBar;
    }

    private void createTable() {
        vehiculeTable = new TableView<>();
        vehiculeTable.setPrefHeight(300);
        vehiculeTable.getStyleClass().add("table-view");

        colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getIdVehicule())));
        colId.setPrefWidth(50);

        colMarque = new TableColumn<>("Marque");
        colMarque.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMarque()));
        colMarque.setPrefWidth(100);

        colModele = new TableColumn<>("Modèle");
        colModele.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getModele()));
        colModele.setPrefWidth(100);

        colAnnee = new TableColumn<>("Année");
        colAnnee.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getAnnee())));
        colAnnee.setPrefWidth(70);

        colImmatriculation = new TableColumn<>("Immatriculation");
        colImmatriculation.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getImmatriculation()));
        colImmatriculation.setPrefWidth(120);

        colCarburant = new TableColumn<>("Carburant");
        colCarburant.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCarburant()));
        colCarburant.setPrefWidth(90);

        colKilometrage = new TableColumn<>("Km");
        colKilometrage.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getKilometrage())));
        colKilometrage.setPrefWidth(80);

        // ===== COLONNE PRIX EN FCFA =====
        colPrix = new TableColumn<>("Prix/jour");
        colPrix.setCellValueFactory(data -> new SimpleStringProperty(
                FormatUtil.formatFCFA(data.getValue().getPrixJournalier())
        ));
        colPrix.setPrefWidth(130);

        colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(data -> {
            String statut = data.getValue().getStatut();
            String emoji = switch (statut) {
                case "DISPONIBLE" -> "✅";
                case "LOUE" -> "🔴";
                case "MAINTENANCE" -> "🟡";
                case "NETTOYAGE" -> "🔵";
                case "RESERVE" -> "🟣";
                default -> "⚪";
            };
            return new SimpleStringProperty(emoji + " " + statut);
        });
        colStatut.setPrefWidth(130);

        colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(220);
        colActions.setCellFactory(param -> new TableCell<Vehicule, Void>() {
            private final Button editBtn = new Button("✏️ Modifier");
            private final Button deleteBtn = new Button("🗑️ Supprimer");
            private final HBox actionBox = new HBox(8, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 4 12; -fx-background-radius: 3; -fx-font-size: 11;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 4 12; -fx-background-radius: 3; -fx-font-size: 11;");
                actionBox.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Vehicule vehicule = getTableView().getItems().get(getIndex());

                    editBtn.setOnAction(e -> {
                        if (vehicule != null) {
                            showEditForm(vehicule);
                        }
                    });

                    deleteBtn.setOnAction(e -> {
                        if (vehicule != null) {
                            selectedVehicule = vehicule;
                            if (deleteCallback != null) {
                                deleteCallback.onDelete(vehicule);
                            }
                        }
                    });

                    setGraphic(actionBox);
                }
            }
        });

        vehiculeTable.getColumns().addAll(colId, colMarque, colModele, colAnnee, colImmatriculation,
                colCarburant, colKilometrage, colPrix, colStatut, colActions);
    }

    public void setDeleteCallback(DeleteCallback callback) {
        this.deleteCallback = callback;
    }

    private void createForm() {
        formContainer = new VBox();
        formContainer.setSpacing(12);
        formContainer.setPadding(new Insets(15));
        formContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-border-color: #e0e5ec; -fx-border-radius: 8; -fx-border-width: 1;");
        formContainer.setVisible(false);
        formContainer.setManaged(false);

        formTitleLabel = new Label("Nouveau véhicule");
        formTitleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10, 0, 10, 0));

        int row = 0;

        Label marqueLabel = new Label("Marque *");
        marqueLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        marqueField = new TextField();
        marqueField.setPromptText("Toyota");
        marqueField.setPrefWidth(200);
        marqueField.getStyleClass().add("input-field");

        Label modeleLabel = new Label("Modèle *");
        modeleLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        modeleField = new TextField();
        modeleField.setPromptText("Yaris");
        modeleField.setPrefWidth(200);
        modeleField.getStyleClass().add("input-field");

        formGrid.add(marqueLabel, 0, row);
        formGrid.add(marqueField, 1, row);
        formGrid.add(modeleLabel, 2, row);
        formGrid.add(modeleField, 3, row);

        row++;
        Label anneeLabel = new Label("Année *");
        anneeLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        anneeField = new TextField();
        anneeField.setPromptText("2020");
        anneeField.setPrefWidth(100);
        anneeField.getStyleClass().add("input-field");

        Label immatriculationLabel = new Label("Immatriculation *");
        immatriculationLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        immatriculationField = new TextField();
        immatriculationField.setPromptText("AB-123-CD");
        immatriculationField.setPrefWidth(150);
        immatriculationField.getStyleClass().add("input-field");

        formGrid.add(anneeLabel, 0, row);
        formGrid.add(anneeField, 1, row);
        formGrid.add(immatriculationLabel, 2, row);
        formGrid.add(immatriculationField, 3, row);

        row++;
        Label carburantLabel = new Label("Carburant *");
        carburantLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        carburantComboBox = new ComboBox<>();
        carburantComboBox.getItems().addAll("ESSENCE", "DIESEL", "ELECTRIQUE", "HYBRIDE", "GPL");
        carburantComboBox.setPromptText("Sélectionner...");
        carburantComboBox.setPrefWidth(150);
        carburantComboBox.getStyleClass().add("input-field");

        Label kilometrageLabel = new Label("Kilométrage *");
        kilometrageLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        kilometrageField = new TextField();
        kilometrageField.setPromptText("0");
        kilometrageField.setPrefWidth(100);
        kilometrageField.getStyleClass().add("input-field");

        formGrid.add(carburantLabel, 0, row);
        formGrid.add(carburantComboBox, 1, row);
        formGrid.add(kilometrageLabel, 2, row);
        formGrid.add(kilometrageField, 3, row);

        row++;
        Label prixLabel = new Label("Prix journalier (FCFA) *");
        prixLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        prixField = new TextField();
        prixField.setPromptText("45000");
        prixField.setPrefWidth(150);
        prixField.getStyleClass().add("input-field");

        Label statutLabel = new Label("Statut *");
        statutLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        statutComboBox = new ComboBox<>();
        statutComboBox.getItems().addAll("DISPONIBLE", "LOUE", "MAINTENANCE", "NETTOYAGE", "RESERVE");
        statutComboBox.setPromptText("Sélectionner...");
        statutComboBox.setPrefWidth(150);
        statutComboBox.getStyleClass().add("input-field");

        formGrid.add(prixLabel, 0, row);
        formGrid.add(prixField, 1, row);
        formGrid.add(statutLabel, 2, row);
        formGrid.add(statutComboBox, 3, row);

        row++;
        Label parkingLabel = new Label("Parking");
        parkingLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        parkingComboBox = new ComboBox<>();
        parkingComboBox.setPromptText("Sélectionner un parking...");
        parkingComboBox.setPrefWidth(200);
        parkingComboBox.getStyleClass().add("input-field");

        actifCheckBox = new CheckBox("Véhicule actif");
        actifCheckBox.setSelected(true);
        actifCheckBox.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");

        formGrid.add(parkingLabel, 0, row);
        formGrid.add(parkingComboBox, 1, row);
        formGrid.add(actifCheckBox, 2, row);

        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12; -fx-padding: 5 0 0 0;");
        errorLabel.setVisible(false);

        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        saveButton = new Button("💾 Enregistrer");
        saveButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-pref-height: 36; -fx-padding: 0 25; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-weight: bold;");

        cancelButton = new Button("❌ Annuler");
        cancelButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-pref-height: 36; -fx-padding: 0 25; -fx-background-radius: 4; -fx-cursor: hand;");
        cancelButton.setOnAction(e -> hideForm());

        deleteButton = new Button("🗑️ Supprimer définitivement");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-pref-height: 36; -fx-padding: 0 20; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-weight: bold;");
        deleteButton.setVisible(false);

        buttonBox.getChildren().addAll(deleteButton, cancelButton, saveButton);

        formContainer.getChildren().addAll(formTitleLabel, formGrid, errorLabel, buttonBox);
    }

    public void showAddForm() {
        clearForm();
        formTitleLabel.setText("➕ Nouveau véhicule");
        selectedVehicule = null;
        deleteButton.setVisible(false);
        showForm();
    }

    public void showEditForm(Vehicule vehicule) {
        if (vehicule == null) return;

        selectedVehicule = vehicule;
        formTitleLabel.setText("✏️ Modifier le véhicule #" + vehicule.getIdVehicule());

        marqueField.setText(vehicule.getMarque());
        modeleField.setText(vehicule.getModele());
        anneeField.setText(String.valueOf(vehicule.getAnnee()));
        immatriculationField.setText(vehicule.getImmatriculation());
        carburantComboBox.setValue(vehicule.getCarburant());
        kilometrageField.setText(String.valueOf(vehicule.getKilometrage()));
        prixField.setText(String.valueOf((int) vehicule.getPrixJournalier()));
        statutComboBox.setValue(vehicule.getStatut());
        parkingComboBox.setValue(vehicule.getIdParking() == null
                ? null
                : parkingComboBox.getItems().stream()
                        .filter(parking -> parking.getIdParking() == vehicule.getIdParking())
                        .findFirst()
                        .orElse(null));
        actifCheckBox.setSelected(vehicule.isActif());

        deleteButton.setVisible(true);
        showForm();
    }

    private void showForm() {
        formContainer.setVisible(true);
        formContainer.setManaged(true);
    }

    public void hideForm() {
        formContainer.setVisible(false);
        formContainer.setManaged(false);
        clearForm();
    }

    private void clearForm() {
        marqueField.clear();
        modeleField.clear();
        anneeField.clear();
        immatriculationField.clear();
        carburantComboBox.setValue(null);
        kilometrageField.clear();
        prixField.clear();
        statutComboBox.setValue(null);
        parkingComboBox.setValue(null);
        actifCheckBox.setSelected(true);
        errorLabel.setVisible(false);
        errorLabel.setText("");
        selectedVehicule = null;
    }

    public void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    public void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

    public void refreshTable(List<Vehicule> vehicules) {
        vehiculeTable.getItems().clear();
        if (vehicules != null) {
            vehiculeTable.getItems().addAll(vehicules);
        }
    }

    // ===== GETTERS =====
    public VBox getRoot() { return root; }
    public TextField getSearchField() { return searchField; }
    public Button getSearchButton() { return searchButton; }
    public Button getRefreshButton() { return refreshButton; }
    public TableView<Vehicule> getVehiculeTable() { return vehiculeTable; }
    public TextField getMarqueField() { return marqueField; }
    public TextField getModeleField() { return modeleField; }
    public TextField getAnneeField() { return anneeField; }
    public TextField getImmatriculationField() { return immatriculationField; }
    public ComboBox<String> getCarburantComboBox() { return carburantComboBox; }
    public TextField getKilometrageField() { return kilometrageField; }
    public TextField getPrixField() { return prixField; }
    public ComboBox<String> getStatutComboBox() { return statutComboBox; }
    public ComboBox<Parking> getParkingComboBox() { return parkingComboBox; }
    public CheckBox getActifCheckBox() { return actifCheckBox; }
    public Button getSaveButton() { return saveButton; }
    public Button getCancelButton() { return cancelButton; }
    public Button getDeleteButton() { return deleteButton; }
    public Vehicule getSelectedVehicule() { return selectedVehicule; }
    public TableColumn<Vehicule, Void> getColActions() { return colActions; }
}