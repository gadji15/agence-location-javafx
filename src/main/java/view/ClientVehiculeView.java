package view;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Vehicule;
import utils.FormatUtil;

import java.util.List;

public class ClientVehiculeView {

    private VBox root;

    // ===== COMPOSANTS DE RECHERCHE =====
    private TextField searchField;
    private Button searchButton;
    private Button refreshButton;
    private ComboBox<String> statutFilterComboBox;

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

    // Interface de callback
    public interface VehiculeCallback {
        void onSearch(String keyword);
        void onFilter(String statut);
        void onRefresh();
        void onReserver(Vehicule vehicule);
    }

    private VehiculeCallback callback;

    public ClientVehiculeView() {
        initUI();
    }

    private void initUI() {
        root = new VBox();
        root.setSpacing(15);
        root.setPadding(new Insets(10));

        // Titre
        Label titleLabel = new Label("🚗 Véhicules disponibles");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Barre de recherche
        HBox searchBar = createSearchBar();

        // Tableau des véhicules
        createTable();

        // Statistiques
        HBox statsBar = createStatsBar();

        // Assemblage
        root.getChildren().addAll(
                titleLabel,
                searchBar,
                statsBar,
                vehiculeTable
        );

        VBox.setVgrow(vehiculeTable, Priority.ALWAYS);
    }

    private HBox createSearchBar() {
        HBox searchBar = new HBox();
        searchBar.setSpacing(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setPadding(new Insets(5, 0, 10, 0));

        Label searchLabel = new Label("🔍 Rechercher :");
        searchLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");

        searchField = new TextField();
        searchField.setPromptText("Marque, modèle ou immatriculation...");
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
        statutFilterComboBox.getItems().addAll("Tous", "DISPONIBLE", "RESERVE", "LOUE", "MAINTENANCE", "NETTOYAGE");
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
        vehiculeTable = new TableView<>();
        vehiculeTable.setPrefHeight(350);
        vehiculeTable.getStyleClass().add("table-view");

        // Colonne ID
        colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new SimpleStringProperty(
                String.valueOf(data.getValue().getIdVehicule())));
        colId.setPrefWidth(50);

        // Colonne Marque
        colMarque = new TableColumn<>("Marque");
        colMarque.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMarque()));
        colMarque.setPrefWidth(100);

        // Colonne Modèle
        colModele = new TableColumn<>("Modèle");
        colModele.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getModele()));
        colModele.setPrefWidth(100);

        // Colonne Année
        colAnnee = new TableColumn<>("Année");
        colAnnee.setCellValueFactory(data -> new SimpleStringProperty(
                String.valueOf(data.getValue().getAnnee())));
        colAnnee.setPrefWidth(70);

        // Colonne Immatriculation
        colImmatriculation = new TableColumn<>("Immatriculation");
        colImmatriculation.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getImmatriculation()));
        colImmatriculation.setPrefWidth(120);

        // Colonne Carburant
        colCarburant = new TableColumn<>("Carburant");
        colCarburant.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCarburant()));
        colCarburant.setPrefWidth(90);

        // Colonne Kilométrage
        colKilometrage = new TableColumn<>("Km");
        colKilometrage.setCellValueFactory(data -> new SimpleStringProperty(
                String.valueOf(data.getValue().getKilometrage())));
        colKilometrage.setPrefWidth(80);

        // ===== ✅ COLONNE PRIX CORRIGÉE EN FCFA =====
        colPrix = new TableColumn<>("Prix/jour");
        colPrix.setCellValueFactory(data -> new SimpleStringProperty(
                FormatUtil.formatFCFA(data.getValue().getPrixJournalier())
        ));
        colPrix.setPrefWidth(120);

        // Colonne Statut
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
        colStatut.setPrefWidth(120);

        // Colonne Actions
        colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(150);
        colActions.setCellFactory(param -> new TableCell<Vehicule, Void>() {
            private final Button reserverBtn = new Button("📅 Réserver");
            private final HBox actionBox = new HBox(5, reserverBtn);

            {
                reserverBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                        "-fx-cursor: hand; -fx-padding: 4 12; -fx-background-radius: 3; -fx-font-size: 11;");
                actionBox.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Vehicule vehicule = getTableView().getItems().get(getIndex());

                    // Seulement afficher le bouton pour les véhicules disponibles
                    reserverBtn.setVisible("DISPONIBLE".equals(vehicule.getStatut()));

                    reserverBtn.setOnAction(e -> {
                        try {
                            if (callback != null && vehicule != null) {
                                callback.onReserver(vehicule);
                            }
                        } catch (Exception ex) {
                            System.err.println("❌ Erreur bouton Réserver :");
                            ex.printStackTrace();
                        }
                    });

                    setGraphic(actionBox);
                }
            }
        });

        vehiculeTable.getColumns().addAll(colId, colMarque, colModele, colAnnee, colImmatriculation,
                colCarburant, colKilometrage, colPrix, colStatut, colActions);
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

        Label disponibleLabel = new Label("✅ Disponibles: 0");
        disponibleLabel.setStyle("-fx-text-fill: #2ecc71;");
        disponibleLabel.setId("disponibleLabel");

        Label loueLabel = new Label("🔴 Loués: 0");
        loueLabel.setStyle("-fx-text-fill: #e74c3c;");
        loueLabel.setId("loueLabel");

        Label maintenanceLabel = new Label("🟡 Maintenance: 0");
        maintenanceLabel.setStyle("-fx-text-fill: #f39c12;");
        maintenanceLabel.setId("maintenanceLabel");

        statsBar.getChildren().addAll(statsLabel, totalLabel, disponibleLabel, loueLabel, maintenanceLabel);
        return statsBar;
    }

    public void setCallback(VehiculeCallback callback) {
        this.callback = callback;
    }

    public void refreshTable(List<Vehicule> vehicules) {
        vehiculeTable.getItems().clear();
        if (vehicules != null) {
            vehiculeTable.getItems().addAll(vehicules);
        }
    }

    public void updateStats(int total, int disponibles, int loues, int maintenance) {
        for (javafx.scene.Node node : ((HBox) root.getChildren().get(2)).getChildren()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                String id = label.getId();
                if (id != null) {
                    switch (id) {
                        case "totalLabel" -> label.setText("Total: " + total);
                        case "disponibleLabel" -> label.setText("✅ Disponibles: " + disponibles);
                        case "loueLabel" -> label.setText("🔴 Loués: " + loues);
                        case "maintenanceLabel" -> label.setText("🟡 Maintenance: " + maintenance);
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
    public TableView<Vehicule> getVehiculeTable() { return vehiculeTable; }
}