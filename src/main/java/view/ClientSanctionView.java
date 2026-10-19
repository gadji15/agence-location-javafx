package view;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Sanction;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClientSanctionView {

    private VBox root;

    // ===== COMPOSANTS DE RECHERCHE =====
    private TextField searchField;
    private Button searchButton;
    private Button refreshButton;
    private ComboBox<String> statutFilterComboBox;

    // ===== TABLEAU DES SANCTIONS =====
    private TableView<Sanction> sanctionTable;
    private TableColumn<Sanction, String> colId;
    private TableColumn<Sanction, String> colType;
    private TableColumn<Sanction, String> colMontant;
    private TableColumn<Sanction, String> colDescription;
    private TableColumn<Sanction, String> colDate;
    private TableColumn<Sanction, String> colStatut;

    // ===== FORMATTEUR DE DATE =====
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Interface de callback
    public interface SanctionCallback {
        void onSearch(String keyword);
        void onFilter(String statut);
        void onRefresh();
    }

    private SanctionCallback callback;

    public ClientSanctionView() {
        initUI();
    }

    private void initUI() {
        root = new VBox();
        root.setSpacing(15);
        root.setPadding(new Insets(10));

        // Titre
        Label titleLabel = new Label("⚖️ Mes Sanctions");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Barre de recherche
        HBox searchBar = createSearchBar();

        // Tableau des sanctions
        createTable();

        // Statistiques
        HBox statsBar = createStatsBar();

        // Assemblage
        root.getChildren().addAll(
                titleLabel,
                searchBar,
                statsBar,
                sanctionTable
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
        searchField.setPromptText("Type, description...");
        searchField.setPrefWidth(200);
        searchField.getStyleClass().add("input-field");

        searchButton = new Button("Rechercher");
        searchButton.getStyleClass().add("action-button");
        searchButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-pref-height: 32; -fx-padding: 0 15; -fx-background-radius: 4; -fx-cursor: hand;");

        refreshButton = new Button("🔄 Rafraîchir");
        refreshButton.getStyleClass().add("action-button");
        refreshButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-pref-height: 32; -fx-padding: 0 15; -fx-background-radius: 4; -fx-cursor: hand;");

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

        searchBar.getChildren().addAll(searchLabel, searchField, searchButton, refreshButton,
                filterLabel, statutFilterComboBox, spacer);
        return searchBar;
    }

    private void createTable() {
        sanctionTable = new TableView<>();
        sanctionTable.setPrefHeight(350);
        sanctionTable.getStyleClass().add("table-view");

        // Colonne ID
        colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getIdSanction())));
        colId.setPrefWidth(50);

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
        colType.setPrefWidth(120);

        // Colonne Montant
        colMontant = new TableColumn<>("Montant");
        colMontant.setCellValueFactory(data -> new SimpleStringProperty(String.format("%.2f €", data.getValue().getMontant())));
        colMontant.setPrefWidth(100);

        // Colonne Description
        colDescription = new TableColumn<>("Description");
        colDescription.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
        colDescription.setPrefWidth(250);

        // Colonne Date
        colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(data -> {
            if (data.getValue().getDateEnregistrement() != null) {
                return new SimpleStringProperty(data.getValue().getDateEnregistrement().format(dateFormatter));
            }
            return new SimpleStringProperty("");
        });
        colDate.setPrefWidth(150);

        // Colonne Statut
        colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(data -> {
            String statut = data.getValue().getStatut();
            String emoji = switch (statut) {
                case "ACTIVE" -> "🔴";
                case "LEVEE" -> "✅";
                case "REDUITE" -> "🟡";
                default -> "⚪";
            };
            return new SimpleStringProperty(emoji + " " + statut);
        });
        colStatut.setPrefWidth(110);

        sanctionTable.getColumns().addAll(colId, colType, colMontant, colDescription, colDate, colStatut);
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

        Label activeLabel = new Label("🔴 Actives: 0");
        activeLabel.setStyle("-fx-text-fill: #e74c3c;");
        activeLabel.setId("activeLabel");

        Label leveeLabel = new Label("✅ Levées: 0");
        leveeLabel.setStyle("-fx-text-fill: #2ecc71;");
        leveeLabel.setId("leveeLabel");

        Label reduiteLabel = new Label("🟡 Réduites: 0");
        reduiteLabel.setStyle("-fx-text-fill: #f39c12;");
        reduiteLabel.setId("reduiteLabel");

        statsBar.getChildren().addAll(statsLabel, totalLabel, activeLabel, leveeLabel, reduiteLabel);
        return statsBar;
    }

    public void setCallback(SanctionCallback callback) {
        this.callback = callback;
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
                        case "leveeLabel" -> label.setText("✅ Levées: " + levees);
                        case "reduiteLabel" -> label.setText("🟡 Réduites: " + reduites);
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
    public TableView<Sanction> getSanctionTable() { return sanctionTable; }
}
