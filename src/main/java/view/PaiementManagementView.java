package view;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Paiement;
import utils.FormatUtil;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class PaiementManagementView {

    private VBox root;

    // ===== COMPOSANTS DE RECHERCHE =====
    private TextField searchField;
    private Button searchButton;
    private Button refreshButton;
    private ComboBox<String> modeFilterComboBox;

    // ===== TABLEAU DES PAIEMENTS =====
    private TableView<Paiement> paiementTable;
    private TableColumn<Paiement, String> colId;
    private TableColumn<Paiement, String> colFacture;
    private TableColumn<Paiement, String> colMode;
    private TableColumn<Paiement, String> colMontant;
    private TableColumn<Paiement, String> colDevise;
    private TableColumn<Paiement, String> colDate;
    private TableColumn<Paiement, String> colReference;
    private TableColumn<Paiement, String> colNotes;

    // ===== FORMATTEUR =====
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public PaiementManagementView() {
        initUI();
    }

    private void initUI() {
        root = new VBox();
        root.setSpacing(15);
        root.setPadding(new Insets(10));

        Label titleLabel = new Label("💳 Historique des Paiements");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        HBox searchBar = createSearchBar();
        HBox statsBar = createStatsBar();
        createTable();

        root.getChildren().addAll(titleLabel, searchBar, statsBar, paiementTable);
        VBox.setVgrow(paiementTable, Priority.ALWAYS);
    }

    private HBox createSearchBar() {
        HBox searchBar = new HBox();
        searchBar.setSpacing(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setPadding(new Insets(5, 0, 10, 0));

        Label searchLabel = new Label("🔍 Rechercher :");
        searchLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");

        searchField = new TextField();
        searchField.setPromptText("Référence, facture...");
        searchField.setPrefWidth(250);
        searchField.getStyleClass().add("input-field");

        searchButton = new Button("Rechercher");
        searchButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-pref-height: 32; -fx-padding: 0 15; -fx-background-radius: 4; -fx-cursor: hand;");

        refreshButton = new Button("🔄 Rafraîchir");
        refreshButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-pref-height: 32; -fx-padding: 0 15; -fx-background-radius: 4; -fx-cursor: hand;");

        Label filterLabel = new Label("Mode :");
        filterLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e; -fx-padding: 0 0 0 15;");

        modeFilterComboBox = new ComboBox<>();
        modeFilterComboBox.getItems().addAll("Tous", "ESPECE", "CARTE", "VIREMENT", "CHEQUE", "WAVE", "ORANGE_MONEY", "FREE_MONEY");
        modeFilterComboBox.setValue("Tous");
        modeFilterComboBox.setPrefWidth(150);
        modeFilterComboBox.getStyleClass().add("input-field");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchBar.getChildren().addAll(searchLabel, searchField, searchButton, refreshButton, filterLabel, modeFilterComboBox, spacer);
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

        Label montantLabel = new Label("💰 Montant total: 0 FCFA");
        montantLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        montantLabel.setId("montantLabel");

        Label especeLabel = new Label("💵 Espèces: 0");
        especeLabel.setStyle("-fx-text-fill: #27ae60;");
        especeLabel.setId("especeLabel");

        Label mobileLabel = new Label("📱 Mobile: 0");
        mobileLabel.setStyle("-fx-text-fill: #3498db;");
        mobileLabel.setId("mobileLabel");

        statsBar.getChildren().addAll(statsLabel, totalLabel, montantLabel, especeLabel, mobileLabel);
        return statsBar;
    }

    private void createTable() {
        paiementTable = new TableView<>();
        paiementTable.setPrefHeight(350);
        paiementTable.getStyleClass().add("table-view");

        colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getIdPaiement())));
        colId.setPrefWidth(50);

        colFacture = new TableColumn<>("Facture");
        colFacture.setCellValueFactory(data -> new SimpleStringProperty("#" + data.getValue().getIdFacture()));
        colFacture.setPrefWidth(80);

        colMode = new TableColumn<>("Mode");
        colMode.setCellValueFactory(data -> {
            String mode = data.getValue().getMode();
            String emoji = switch (mode) {
                case "ESPECE" -> "💵";
                case "CARTE" -> "💳";
                case "VIREMENT" -> "🏦";
                case "CHEQUE" -> "📝";
                case "WAVE" -> "🌊";
                case "ORANGE_MONEY" -> "🟠";
                case "FREE_MONEY" -> "🔴";
                default -> "💰";
            };
            return new SimpleStringProperty(emoji + " " + mode);
        });
        colMode.setPrefWidth(140);

        colMontant = new TableColumn<>("Montant");
        colMontant.setCellValueFactory(data -> new SimpleStringProperty(
                FormatUtil.formatFCFA(data.getValue().getMontantPaye())
        ));
        colMontant.setPrefWidth(130);

        colDevise = new TableColumn<>("Devise");
        colDevise.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDevise()));
        colDevise.setPrefWidth(70);

        colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(data -> {
            if (data.getValue().getDatePaiement() != null) {
                return new SimpleStringProperty(data.getValue().getDatePaiement().format(dateFormatter));
            }
            return new SimpleStringProperty("");
        });
        colDate.setPrefWidth(140);

        colReference = new TableColumn<>("Référence");
        colReference.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getReferenceTransaction() != null ? data.getValue().getReferenceTransaction() : "N/A"
        ));
        colReference.setPrefWidth(150);

        colNotes = new TableColumn<>("Notes");
        colNotes.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getNotes() != null ? data.getValue().getNotes() : ""
        ));
        colNotes.setPrefWidth(200);

        paiementTable.getColumns().addAll(colId, colFacture, colMode, colMontant, colDevise, colDate, colReference, colNotes);
    }

    public void refreshTable(List<Paiement> paiements) {
        paiementTable.getItems().clear();
        if (paiements != null) {
            paiementTable.getItems().addAll(paiements);
        }
    }

    public void updateStats(int total, double montantTotal, int especes, int mobiles) {
        for (javafx.scene.Node node : ((HBox) root.getChildren().get(2)).getChildren()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                String id = label.getId();
                if (id != null) {
                    switch (id) {
                        case "totalLabel" -> label.setText("Total: " + total);
                        case "montantLabel" -> label.setText("💰 Montant total: " + FormatUtil.formatFCFA(montantTotal));
                        case "especeLabel" -> label.setText("💵 Espèces: " + especes);
                        case "mobileLabel" -> label.setText("📱 Mobile: " + mobiles);
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
    public ComboBox<String> getModeFilterComboBox() { return modeFilterComboBox; }
    public TableView<Paiement> getPaiementTable() { return paiementTable; }
}