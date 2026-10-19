package view;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Facture;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClientFactureView {

    private VBox root;

    // ===== COMPOSANTS DE RECHERCHE =====
    private TextField searchField;
    private Button searchButton;
    private Button refreshButton;
    private ComboBox<String> statutFilterComboBox;

    // ===== TABLEAU DES FACTURES =====
    private TableView<Facture> factureTable;
    private TableColumn<Facture, String> colId;
    private TableColumn<Facture, String> colContrat;
    private TableColumn<Facture, String> colDateEmission;
    private TableColumn<Facture, String> colMontant;
    private TableColumn<Facture, String> colStatut;
    private TableColumn<Facture, String> colDatePaiement;
    private TableColumn<Facture, Void> colActions;

    // ===== FORMATTEUR DE DATE =====
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Interface de callback
    public interface FactureCallback {
        void onSearch(String keyword);
        void onFilter(String statut);
        void onRefresh();
        void onTelechargerPDF(Facture facture);
    }

    private FactureCallback callback;

    public ClientFactureView() {
        initUI();
    }

    private void initUI() {
        root = new VBox();
        root.setSpacing(15);
        root.setPadding(new Insets(10));

        // Titre
        Label titleLabel = new Label("💰 Mes Factures");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Barre de recherche
        HBox searchBar = createSearchBar();

        // Tableau des factures
        createTable();

        // Statistiques
        HBox statsBar = createStatsBar();

        // Assemblage
        root.getChildren().addAll(
                titleLabel,
                searchBar,
                statsBar,
                factureTable
        );

        VBox.setVgrow(factureTable, Priority.ALWAYS);
    }

    private HBox createSearchBar() {
        HBox searchBar = new HBox();
        searchBar.setSpacing(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setPadding(new Insets(5, 0, 10, 0));

        Label searchLabel = new Label("🔍 Rechercher :");
        searchLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");

        searchField = new TextField();
        searchField.setPromptText("Contrat, montant...");
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
        statutFilterComboBox.getItems().addAll("Tous", "PAYEE", "IMPAYEE");
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
        factureTable = new TableView<>();
        factureTable.setPrefHeight(350);
        factureTable.getStyleClass().add("table-view");

        // Colonne ID
        colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getIdFacture())));
        colId.setPrefWidth(50);

        // Colonne Contrat
        colContrat = new TableColumn<>("Contrat");
        colContrat.setCellValueFactory(data -> new SimpleStringProperty("#" + data.getValue().getIdContrat()));
        colContrat.setPrefWidth(100);

        // Colonne Date émission
        colDateEmission = new TableColumn<>("Émise le");
        colDateEmission.setCellValueFactory(data -> {
            if (data.getValue().getDateEmission() != null) {
                return new SimpleStringProperty(data.getValue().getDateEmission().format(dateFormatter));
            }
            return new SimpleStringProperty("");
        });
        colDateEmission.setPrefWidth(150);

        // Colonne Montant
        colMontant = new TableColumn<>("Montant");
        colMontant.setCellValueFactory(data -> new SimpleStringProperty(String.format("%.2f €", data.getValue().getMontantTotal())));
        colMontant.setPrefWidth(100);

        // Colonne Statut
        colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(data -> {
            String statut = data.getValue().getStatut();
            String emoji = "PAYEE".equals(statut) ? "✅ PAYÉE" : "❌ IMPAYÉE";
            String color = "PAYEE".equals(statut) ? "#2ecc71" : "#e74c3c";
            return new SimpleStringProperty(emoji);
        });
        colStatut.setPrefWidth(110);

        // Colonne Date paiement
        colDatePaiement = new TableColumn<>("Payée le");
        colDatePaiement.setCellValueFactory(data -> {
            if (data.getValue().getDatePaiement() != null) {
                return new SimpleStringProperty(data.getValue().getDatePaiement().format(dateFormatter));
            }
            return new SimpleStringProperty("N/A");
        });
        colDatePaiement.setPrefWidth(150);

        // Colonne Actions
        colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(150);
        colActions.setCellFactory(param -> new TableCell<Facture, Void>() {
            private final Button pdfBtn = new Button("📄 PDF");
            private final HBox actionBox = new HBox(5, pdfBtn);

            {
                pdfBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 4 12; -fx-background-radius: 3; -fx-font-size: 11;");
                actionBox.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Facture facture = getTableView().getItems().get(getIndex());

                    pdfBtn.setOnAction(e -> {
                        if (callback != null && facture != null) {
                            callback.onTelechargerPDF(facture);
                        }
                    });

                    setGraphic(actionBox);
                }
            }
        });

        factureTable.getColumns().addAll(colId, colContrat, colDateEmission, colMontant,
                colStatut, colDatePaiement, colActions);
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

        Label payeeLabel = new Label("✅ Payées: 0");
        payeeLabel.setStyle("-fx-text-fill: #2ecc71;");
        payeeLabel.setId("payeeLabel");

        Label impayeeLabel = new Label("❌ Impayées: 0");
        impayeeLabel.setStyle("-fx-text-fill: #e74c3c;");
        impayeeLabel.setId("impayeeLabel");

        statsBar.getChildren().addAll(statsLabel, totalLabel, payeeLabel, impayeeLabel);
        return statsBar;
    }

    public void setCallback(FactureCallback callback) {
        this.callback = callback;
    }

    public void refreshTable(List<Facture> factures) {
        factureTable.getItems().clear();
        if (factures != null) {
            factureTable.getItems().addAll(factures);
        }
    }

    public void updateStats(int total, int payees, int impayees) {
        for (javafx.scene.Node node : ((HBox) root.getChildren().get(2)).getChildren()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                String id = label.getId();
                if (id != null) {
                    switch (id) {
                        case "totalLabel" -> label.setText("Total: " + total);
                        case "payeeLabel" -> label.setText("✅ Payées: " + payees);
                        case "impayeeLabel" -> label.setText("❌ Impayées: " + impayees);
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
    public TableView<Facture> getFactureTable() { return factureTable; }
}
