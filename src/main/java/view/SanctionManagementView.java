package view;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Sanction;
import utils.FormatUtil;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class SanctionManagementView {

    private VBox root;

    // Composants de recherche
    private TextField searchField;
    private Button searchButton;
    private Button refreshButton;
    private ComboBox<String> statutFilterComboBox;

    // Tableau des sanctions
    private TableView<Sanction> sanctionTable;
    private TableColumn<Sanction, String> colId;
    private TableColumn<Sanction, String> colClient;
    private TableColumn<Sanction, String> colContrat;
    private TableColumn<Sanction, String> colType;
    private TableColumn<Sanction, String> colMontant;
    private TableColumn<Sanction, String> colDescription;
    private TableColumn<Sanction, String> colDate;
    private TableColumn<Sanction, String> colStatut;
    private TableColumn<Sanction, Void> colActions;

    // Composants du formulaire
    private TextField clientSearchField;
    private ComboBox<String> typeComboBox;
    private TextField montantField;
    private TextArea descriptionArea;
    private ComboBox<String> statutComboBox;

    private Button saveButton;
    private Button cancelButton;
    private Button deleteButton;

    private Label formTitleLabel;
    private Label errorLabel;
    private VBox formContainer;
    private GridPane formGrid;

    // Formatteur de date
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Sanction sélectionnée
    private Sanction selectedSanction;

    // Interface de callback pour les actions
    public interface SanctionCallback {
        void onSave(Sanction sanction);
        void onUpdate(Sanction sanction);
        void onDelete(Sanction sanction);
        void onLever(Sanction sanction);
        void onReduire(Sanction sanction);
    }

    private SanctionCallback callback;

    public SanctionManagementView() {
        initUI();
    }

    private void initUI() {
        root = new VBox();
        root.setSpacing(15);
        root.setPadding(new Insets(10));

        // Titre
        Label titleLabel = new Label("⚖️ Gestion des Sanctions");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Barre de recherche
        HBox searchBar = createSearchBar();

        // Tableau des sanctions
        createTable();

        // Formulaire
        createForm();

        // Statistiques
        HBox statsBar = createStatsBar();

        // Assemblage
        root.getChildren().addAll(
                titleLabel,
                searchBar,
                statsBar,
                sanctionTable,
                formContainer
        );

        VBox.setVgrow(sanctionTable, Priority.ALWAYS);
    }

    private HBox createSearchBar() {
        HBox searchBar = new HBox();
        searchBar.setSpacing(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setPadding(new Insets(5, 0, 10, 0));

        Label searchLabel = new Label("🔍 Rechercher :");
        searchLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");

        searchField = new TextField();
        searchField.setPromptText("Client, type ou description...");
        searchField.setPrefWidth(250);
        searchField.getStyleClass().add("input-field");

        searchButton = new Button("Rechercher");
        searchButton.getStyleClass().add("action-button");
        searchButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-pref-height: 32; -fx-padding: 0 15; -fx-background-radius: 4; -fx-cursor: hand;");

        refreshButton = new Button("🔄 Rafraîchir");
        refreshButton.getStyleClass().add("action-button");
        refreshButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                "-fx-pref-height: 32; -fx-padding: 0 15; -fx-background-radius: 4; -fx-cursor: hand;");

        // Filtre par statut
        Label filterLabel = new Label("Filtrer :");
        filterLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e; -fx-padding: 0 0 0 15;");

        statutFilterComboBox = new ComboBox<>();
        statutFilterComboBox.getItems().addAll("Tous", "ACTIVE", "LEVEE", "REDUITE");
        statutFilterComboBox.setValue("Tous");
        statutFilterComboBox.setPrefWidth(120);
        statutFilterComboBox.getStyleClass().add("input-field");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addButton = new Button("➕ Ajouter une sanction");
        addButton.getStyleClass().add("action-button");
        addButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; " +
                "-fx-pref-height: 32; -fx-padding: 0 20; -fx-background-radius: 4; " +
                "-fx-cursor: hand; -fx-font-weight: bold;");
        addButton.setOnAction(e -> showAddForm());

        searchBar.getChildren().addAll(searchLabel, searchField, searchButton, refreshButton,
                filterLabel, statutFilterComboBox, spacer, addButton);
        return searchBar;
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

        Label activeLabel = new Label("🔴 Actives: 0");
        activeLabel.setStyle("-fx-text-fill: #e74c3c;");
        activeLabel.setId("activeLabel");

        Label leveeLabel = new Label("📦 Clôturées: 0");
        leveeLabel.setStyle("-fx-text-fill: #2ecc71;");
        leveeLabel.setId("leveeLabel");

        Label reduiteLabel = new Label("🟡 Réduites: 0");
        reduiteLabel.setStyle("-fx-text-fill: #f39c12;");
        reduiteLabel.setId("reduiteLabel");

        statsBar.getChildren().addAll(statsLabel, totalLabel, activeLabel, leveeLabel, reduiteLabel);
        return statsBar;
    }

    private void createTable() {
        sanctionTable = new TableView<>();
        sanctionTable.setPrefHeight(250);
        sanctionTable.getStyleClass().add("table-view");

        // Colonne ID
        colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new SimpleStringProperty(
                String.valueOf(data.getValue().getIdSanction())));
        colId.setPrefWidth(50);

        // Colonne Client
        colClient = new TableColumn<>("Client");
        colClient.setCellValueFactory(data -> new SimpleStringProperty(
                String.valueOf(data.getValue().getIdClient())));
        colClient.setPrefWidth(80);

        // Colonne Contrat
        colContrat = new TableColumn<>("Contrat");
        colContrat.setCellValueFactory(data -> {
            Integer idContrat = data.getValue().getIdContrat();
            return new SimpleStringProperty(idContrat != null ? "#" + idContrat : "N/A");
        });
        colContrat.setPrefWidth(80);

        // Colonne Type
        colType = new TableColumn<>("Type");
        colType.setCellValueFactory(data -> {
            String type = data.getValue().getType();
            String emoji = switch (type) {
                case "RETARD" -> "⏰";
                case "DEGRADATION" -> "💥";
                default -> "📌";
            };
            return new SimpleStringProperty(emoji + " " + type);
        });
        colType.setPrefWidth(140);

        // Colonne Montant (en FCFA)
        colMontant = new TableColumn<>("Montant");
        colMontant.setCellValueFactory(data -> new SimpleStringProperty(
                FormatUtil.formatFCFA(data.getValue().getMontant())
        ));
        colMontant.setPrefWidth(110);

        // Colonne Description
        colDescription = new TableColumn<>("Description");
        colDescription.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDescription()));
        colDescription.setPrefWidth(180);

        // Colonne Date
        colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(data -> {
            if (data.getValue().getDateEnregistrement() != null) {
                return new SimpleStringProperty(
                        data.getValue().getDateEnregistrement().format(dateFormatter));
            }
            return new SimpleStringProperty("");
        });
        colDate.setPrefWidth(140);

        // Colonne Statut : affiche "CLÔTURÉE" quand LEVEE
        colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(data -> {
            String statut = data.getValue().getStatut();
            String emoji = switch (statut) {
                case "ACTIVE" -> "🔴";
                case "LEVEE" -> "📦";      // CLÔTURÉE
                case "REDUITE" -> "🟡";
                default -> "⚪";
            };
            String label = "LEVEE".equals(statut) ? "CLÔTURÉE" : statut;
            return new SimpleStringProperty(emoji + " " + label);
        });
        colStatut.setPrefWidth(130);

        // Colonne Actions
        // ✅ RÈGLES MÉTIER :
        //   - ACTIVE  : Modifier + Lever + Réduire + Supprimer
        //   - REDUITE : Modifier + Lever + Supprimer
        //   - LEVEE   : Modifier + Supprimer  ← Supprimer reste visible
        colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(320);
        colActions.setCellFactory(param -> new TableCell<Sanction, Void>() {
            private final Button editBtn = new Button("✏️ Modifier");
            private final Button leverBtn = new Button("✅ Lever");
            private final Button reduireBtn = new Button("🟡 Réduire");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox actionBox = new HBox(5, editBtn, leverBtn, reduireBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; " +
                        "-fx-cursor: hand; -fx-padding: 4 8; -fx-background-radius: 3; -fx-font-size: 10;");
                leverBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                        "-fx-cursor: hand; -fx-padding: 4 8; -fx-background-radius: 3; -fx-font-size: 10;");
                reduireBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; " +
                        "-fx-cursor: hand; -fx-padding: 4 8; -fx-background-radius: 3; -fx-font-size: 10;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                        "-fx-cursor: hand; -fx-padding: 4 8; -fx-background-radius: 3; -fx-font-size: 10;");
                actionBox.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Sanction sanction = getTableView().getItems().get(getIndex());
                    String statut = sanction.getStatut();

                    editBtn.setVisible(true);
                    leverBtn.setVisible("ACTIVE".equals(statut) || "REDUITE".equals(statut));
                    reduireBtn.setVisible("ACTIVE".equals(statut));
                    deleteBtn.setVisible(true);   // ✅ Toujours visible

                    editBtn.setOnAction(e -> {
                        try {
                            if (sanction != null) showEditForm(sanction);
                        } catch (Exception ex) {
                            System.err.println("❌ Erreur bouton Modifier :");
                            ex.printStackTrace();
                        }
                    });
                    leverBtn.setOnAction(e -> {
                        try {
                            if (callback != null && sanction != null) callback.onLever(sanction);
                        } catch (Exception ex) {
                            System.err.println("❌ Erreur bouton Lever :");
                            ex.printStackTrace();
                        }
                    });
                    reduireBtn.setOnAction(e -> {
                        try {
                            if (callback != null && sanction != null) callback.onReduire(sanction);
                        } catch (Exception ex) {
                            System.err.println("❌ Erreur bouton Réduire :");
                            ex.printStackTrace();
                        }
                    });
                    deleteBtn.setOnAction(e -> {
                        try {
                            if (callback != null && sanction != null) callback.onDelete(sanction);
                        } catch (Exception ex) {
                            System.err.println("❌ Erreur bouton Supprimer :");
                            ex.printStackTrace();
                        }
                    });

                    setGraphic(actionBox);
                }
            }
        });

        sanctionTable.getColumns().addAll(colId, colClient, colContrat, colType, colMontant,
                colDescription, colDate, colStatut, colActions);
    }

    private void createForm() {
        formContainer = new VBox();
        formContainer.setSpacing(12);
        formContainer.setPadding(new Insets(15));
        formContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; " +
                "-fx-border-color: #e0e5ec; -fx-border-radius: 8; -fx-border-width: 1;");
        formContainer.setVisible(false);
        formContainer.setManaged(false);

        formTitleLabel = new Label("Nouvelle sanction");
        formTitleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10, 0, 10, 0));

        int row = 0;

        // Client
        Label clientLabel = new Label("Client *");
        clientLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        clientSearchField = new TextField();
        clientSearchField.setPromptText("ID du client");
        clientSearchField.setPrefWidth(150);
        clientSearchField.getStyleClass().add("input-field");

        formGrid.add(clientLabel, 0, row);
        formGrid.add(clientSearchField, 1, row);

        // Type
        row++;
        Label typeLabel = new Label("Type *");
        typeLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("RETARD", "DEGRADATION", "AUTRE");
        typeComboBox.setPromptText("Sélectionner...");
        typeComboBox.setPrefWidth(150);
        typeComboBox.getStyleClass().add("input-field");

        formGrid.add(typeLabel, 0, row);
        formGrid.add(typeComboBox, 1, row);

        // Montant
        row++;
        Label montantLabel = new Label("Montant (FCFA) *");
        montantLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        montantField = new TextField();
        montantField.setPromptText("Ex: 5000");
        montantField.setPrefWidth(150);
        montantField.getStyleClass().add("input-field");

        formGrid.add(montantLabel, 0, row);
        formGrid.add(montantField, 1, row);

        // Description
        row++;
        Label descriptionLabel = new Label("Description *");
        descriptionLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description de la sanction...");
        descriptionArea.setPrefWidth(400);
        descriptionArea.setPrefHeight(60);
        descriptionArea.setWrapText(true);
        descriptionArea.getStyleClass().add("input-field");

        formGrid.add(descriptionLabel, 0, row);
        formGrid.add(descriptionArea, 1, row);

        // Statut
        row++;
        Label statutLabel = new Label("Statut *");
        statutLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        statutComboBox = new ComboBox<>();
        statutComboBox.getItems().addAll("ACTIVE", "LEVEE", "REDUITE");
        statutComboBox.setValue("ACTIVE");
        statutComboBox.setPrefWidth(150);
        statutComboBox.getStyleClass().add("input-field");

        formGrid.add(statutLabel, 0, row);
        formGrid.add(statutComboBox, 1, row);

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
        saveButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                "-fx-pref-height: 36; -fx-padding: 0 25; -fx-background-radius: 4; " +
                "-fx-cursor: hand; -fx-font-weight: bold;");

        cancelButton = new Button("❌ Annuler");
        cancelButton.getStyleClass().add("action-button");
        cancelButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                "-fx-pref-height: 36; -fx-padding: 0 25; -fx-background-radius: 4; -fx-cursor: hand;");
        cancelButton.setOnAction(e -> hideForm());

        deleteButton = new Button("🗑️ Supprimer");
        deleteButton.getStyleClass().add("action-button");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-pref-height: 36; -fx-padding: 0 20; -fx-background-radius: 4; " +
                "-fx-cursor: hand; -fx-font-weight: bold;");
        deleteButton.setVisible(false);

        buttonBox.getChildren().addAll(deleteButton, cancelButton, saveButton);

        formContainer.getChildren().addAll(formTitleLabel, formGrid, errorLabel, buttonBox);
    }

    // ===== MÉTHODES D'AFFICHAGE =====

    public void showAddForm() {
        clearForm();
        formTitleLabel.setText("➕ Nouvelle sanction");
        selectedSanction = null;
        deleteButton.setVisible(false);
        showForm();
    }

    public void showEditForm(Sanction sanction) {
        if (sanction == null) return;

        selectedSanction = sanction;
        formTitleLabel.setText("✏️ Modifier la sanction #" + sanction.getIdSanction());

        clientSearchField.setText(String.valueOf(sanction.getIdClient()));
        typeComboBox.setValue(sanction.getType());
        montantField.setText(String.valueOf((long) sanction.getMontant()));
        descriptionArea.setText(sanction.getDescription());
        statutComboBox.setValue(sanction.getStatut());

        // ✅ Supprimer reste visible même si clôturée
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
        clientSearchField.clear();
        typeComboBox.setValue(null);
        montantField.clear();
        descriptionArea.clear();
        statutComboBox.setValue("ACTIVE");
        errorLabel.setVisible(false);
        errorLabel.setText("");
        selectedSanction = null;
    }

    public void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    public void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

    public void refreshTable(List<Sanction> sanctions) {
        sanctionTable.getItems().clear();
        if (sanctions != null) {
            sanctionTable.getItems().addAll(sanctions);
        }
    }

    public void updateStats(int total, int actives, int levees, int reduites) {
        for (javafx.scene.Node node : ((HBox) root.getChildren().get(2)).getChildren()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                String id = label.getId();
                if (id != null) {
                    switch (id) {
                        case "totalLabel" -> label.setText("Total: " + total);
                        case "activeLabel" -> label.setText("🔴 Actives: " + actives);
                        case "leveeLabel" -> label.setText("📦 Clôturées: " + levees);
                        case "reduiteLabel" -> label.setText("🟡 Réduites: " + reduites);
                    }
                }
            }
        }
    }

    public void setCallback(SanctionCallback callback) {
        this.callback = callback;
    }

    // ===== GETTERS =====
    public VBox getRoot() { return root; }
    public TextField getSearchField() { return searchField; }
    public Button getSearchButton() { return searchButton; }
    public Button getRefreshButton() { return refreshButton; }
    public ComboBox<String> getStatutFilterComboBox() { return statutFilterComboBox; }
    public TableView<Sanction> getSanctionTable() { return sanctionTable; }
    public Sanction getSelectedSanction() { return selectedSanction; }
    public TextField getClientSearchField() { return clientSearchField; }
    public ComboBox<String> getTypeComboBox() { return typeComboBox; }
    public TextField getMontantField() { return montantField; }
    public TextArea getDescriptionArea() { return descriptionArea; }
    public ComboBox<String> getStatutComboBox() { return statutComboBox; }
    public Button getSaveButton() { return saveButton; }
    public Button getCancelButton() { return cancelButton; }
    public Button getDeleteButton() { return deleteButton; }
}