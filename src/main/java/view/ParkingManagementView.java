package view;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Parking;

import java.util.List;

public class ParkingManagementView {

    private VBox root;

    // Composants de recherche
    private TextField searchField;
    private Button searchButton;
    private Button refreshButton;

    // Tableau des parkings
    private TableView<Parking> parkingTable;
    private TableColumn<Parking, String> colId;
    private TableColumn<Parking, String> colNom;
    private TableColumn<Parking, String> colAdresse;
    private TableColumn<Parking, String> colCapacite;
    private TableColumn<Parking, String> colPlacesDisponibles;
    private TableColumn<Parking, String> colStatut;
    private TableColumn<Parking, Void> colActions;

    // Composants du formulaire
    private TextField nomField;
    private TextField adresseField;
    private TextField capaciteField;
    private CheckBox actifCheckBox;

    private Button saveButton;
    private Button cancelButton;
    private Button deleteButton;

    private Label formTitleLabel;
    private Label errorLabel;
    private VBox formContainer;

    // Parking sélectionné pour édition
    private Parking selectedParking;

    // Interface de callback pour la suppression
    public interface DeleteCallback {
        void onDelete(Parking parking);
    }

    private DeleteCallback deleteCallback;

    public ParkingManagementView() {
        initUI();
    }

    private void initUI() {
        root = new VBox();
        root.setSpacing(15);
        root.setPadding(new Insets(10));

        // Titre
        Label titleLabel = new Label("🅿️ Gestion des Parkings");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Barre de recherche
        HBox searchBar = createSearchBar();

        // Tableau des parkings
        createTable();

        // Formulaire
        createForm();

        // Séparateur
        Separator separator = new Separator();
        separator.setStyle("-fx-padding: 10 0 10 0;");

        // Assemblage
        root.getChildren().addAll(
                titleLabel,
                searchBar,
                parkingTable,
                separator,
                formContainer
        );

        VBox.setVgrow(parkingTable, Priority.ALWAYS);
    }

    private HBox createSearchBar() {
        HBox searchBar = new HBox();
        searchBar.setSpacing(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setPadding(new Insets(5, 0, 5, 0));

        Label searchLabel = new Label("🔍 Rechercher :");
        searchLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");

        searchField = new TextField();
        searchField.setPromptText("Nom ou adresse du parking...");
        searchField.setPrefWidth(300);
        searchField.getStyleClass().add("input-field");

        searchButton = new Button("Rechercher");
        searchButton.getStyleClass().add("action-button");
        searchButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-pref-height: 32; -fx-padding: 0 15; -fx-background-radius: 4; -fx-cursor: hand;");

        refreshButton = new Button("🔄 Rafraîchir");
        refreshButton.getStyleClass().add("action-button");
        refreshButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-pref-height: 32; -fx-padding: 0 15; -fx-background-radius: 4; -fx-cursor: hand;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addButton = new Button("➕ Ajouter un parking");
        addButton.getStyleClass().add("action-button");
        addButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-pref-height: 32; -fx-padding: 0 20; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-weight: bold;");
        addButton.setOnAction(e -> showAddForm());

        searchBar.getChildren().addAll(searchLabel, searchField, searchButton, refreshButton, spacer, addButton);
        return searchBar;
    }

    private void createTable() {
        parkingTable = new TableView<>();
        parkingTable.setPrefHeight(300);
        parkingTable.getStyleClass().add("table-view");

        // Colonne ID
        colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getIdParking())));
        colId.setPrefWidth(50);

        // Colonne Nom
        colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNom()));
        colNom.setPrefWidth(150);

        // Colonne Adresse
        colAdresse = new TableColumn<>("Adresse");
        colAdresse.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAdresse()));
        colAdresse.setPrefWidth(250);

        // Colonne Capacité
        colCapacite = new TableColumn<>("Capacité");
        colCapacite.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getCapacite())));
        colCapacite.setPrefWidth(80);

        // Colonne Places Disponibles
        colPlacesDisponibles = new TableColumn<>("Places dispo.");
        colPlacesDisponibles.setCellValueFactory(data -> {
            // TODO: Calculer les places disponibles
            return new SimpleStringProperty("N/A");
        });
        colPlacesDisponibles.setPrefWidth(100);

        // Colonne Statut
        colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(data -> {
            String statut = data.getValue().isActif() ? "✅ Actif" : "❌ Inactif";
            return new SimpleStringProperty(statut);
        });
        colStatut.setPrefWidth(100);

        // Colonne Actions
        colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(220);
        colActions.setCellFactory(param -> new TableCell<Parking, Void>() {
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
                    Parking parking = getTableView().getItems().get(getIndex());

                    editBtn.setOnAction(e -> {
                        if (parking != null) {
                            showEditForm(parking);
                        }
                    });

                    deleteBtn.setOnAction(e -> {
                        if (parking != null) {
                            selectedParking = parking;
                            if (deleteCallback != null) {
                                deleteCallback.onDelete(parking);
                            }
                        }
                    });

                    setGraphic(actionBox);
                }
            }
        });

        parkingTable.getColumns().addAll(colId, colNom, colAdresse, colCapacite, colPlacesDisponibles, colStatut, colActions);
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

        // Titre du formulaire
        formTitleLabel = new Label("Nouveau parking");
        formTitleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Grille du formulaire
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10, 0, 10, 0));

        // Champs
        int row = 0;

        // Nom
        Label nomLabel = new Label("Nom *");
        nomLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        nomField = new TextField();
        nomField.setPromptText("Parking Central");
        nomField.setPrefWidth(300);
        nomField.getStyleClass().add("input-field");

        formGrid.add(nomLabel, 0, row);
        formGrid.add(nomField, 1, row);

        // Adresse
        row++;
        Label adresseLabel = new Label("Adresse *");
        adresseLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        adresseField = new TextField();
        adresseField.setPromptText("123 Rue de la Gare, Dakar");
        adresseField.setPrefWidth(400);
        adresseField.getStyleClass().add("input-field");

        formGrid.add(adresseLabel, 0, row);
        formGrid.add(adresseField, 1, row);

        // Capacité
        row++;
        Label capaciteLabel = new Label("Capacité *");
        capaciteLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        capaciteField = new TextField();
        capaciteField.setPromptText("50");
        capaciteField.setPrefWidth(150);
        capaciteField.getStyleClass().add("input-field");

        // Actif
        actifCheckBox = new CheckBox("Parking actif");
        actifCheckBox.setSelected(true);
        actifCheckBox.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");

        formGrid.add(capaciteLabel, 0, row);
        formGrid.add(capaciteField, 1, row);
        formGrid.add(actifCheckBox, 2, row);

        // Message d'erreur
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12; -fx-padding: 5 0 0 0;");
        errorLabel.setVisible(false);

        // Boutons
        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        saveButton = new Button("💾 Enregistrer");
        saveButton.getStyleClass().add("action-button");
        saveButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-pref-height: 36; -fx-padding: 0 25; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-weight: bold;");

        cancelButton = new Button("❌ Annuler");
        cancelButton.getStyleClass().add("action-button");
        cancelButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-pref-height: 36; -fx-padding: 0 25; -fx-background-radius: 4; -fx-cursor: hand;");
        cancelButton.setOnAction(e -> hideForm());

        deleteButton = new Button("🗑️ Supprimer définitivement");
        deleteButton.getStyleClass().add("action-button");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-pref-height: 36; -fx-padding: 0 20; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-weight: bold;");
        deleteButton.setVisible(false);

        buttonBox.getChildren().addAll(deleteButton, cancelButton, saveButton);

        formContainer.getChildren().addAll(formTitleLabel, formGrid, errorLabel, buttonBox);
    }

    // ===== MÉTHODES D'AFFICHAGE =====

    public void showAddForm() {
        clearForm();
        formTitleLabel.setText("➕ Nouveau parking");
        selectedParking = null;
        deleteButton.setVisible(false);
        showForm();
    }

    public void showEditForm(Parking parking) {
        if (parking == null) return;

        selectedParking = parking;
        formTitleLabel.setText("✏️ Modifier le parking #" + parking.getIdParking());

        nomField.setText(parking.getNom());
        adresseField.setText(parking.getAdresse());
        capaciteField.setText(String.valueOf(parking.getCapacite()));
        actifCheckBox.setSelected(parking.isActif());

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
        nomField.clear();
        adresseField.clear();
        capaciteField.clear();
        actifCheckBox.setSelected(true);
        errorLabel.setVisible(false);
        errorLabel.setText("");
        selectedParking = null;
    }

    public void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    public void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

    public void refreshTable(List<Parking> parkings) {
        parkingTable.getItems().clear();
        if (parkings != null) {
            parkingTable.getItems().addAll(parkings);
        }
    }

    // ===== GETTERS =====

    public VBox getRoot() {
        return root;
    }

    public TextField getSearchField() {
        return searchField;
    }

    public Button getSearchButton() {
        return searchButton;
    }

    public Button getRefreshButton() {
        return refreshButton;
    }

    public TableView<Parking> getParkingTable() {
        return parkingTable;
    }

    public TextField getNomField() {
        return nomField;
    }

    public TextField getAdresseField() {
        return adresseField;
    }

    public TextField getCapaciteField() {
        return capaciteField;
    }

    public CheckBox getActifCheckBox() {
        return actifCheckBox;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Button getDeleteButton() {
        return deleteButton;
    }

    public Parking getSelectedParking() {
        return selectedParking;
    }

    public TableColumn<Parking, Void> getColActions() {
        return colActions;
    }
}