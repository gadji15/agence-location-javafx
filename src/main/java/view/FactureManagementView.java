package view;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Facture;
import utils.FormatUtil;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class FactureManagementView {

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
    private TableColumn<Facture, String> colMontantPaye;
    private TableColumn<Facture, String> colReste;
    private TableColumn<Facture, String> colStatut;
    private TableColumn<Facture, String> colDatePaiement;
    private TableColumn<Facture, Void> colActions;

    // ===== FORMATTEUR =====
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ===== INTERFACE DE CALLBACK =====
    public interface FactureCallback {
        void onMarquerPayee(Facture facture);
        void onMarquerImpayee(Facture facture);
        void onTelechargerPDF(Facture facture);
        void onDelete(Facture facture);
    }

    private FactureCallback callback;

    public FactureManagementView() {
        initUI();
    }

    private void initUI() {
        root = new VBox();
        root.setSpacing(15);
        root.setPadding(new Insets(10));

        // Titre
        Label titleLabel = new Label("💰 Gestion des Factures");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Barre de recherche
        HBox searchBar = createSearchBar();

        // Statistiques
        HBox statsBar = createStatsBar();

        // Tableau des factures
        createTable();

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
        searchField.setPromptText("Client, véhicule ou immatriculation...");
        searchField.setPrefWidth(250);
        searchField.getStyleClass().add("input-field");

        searchButton = new Button("Rechercher");
        searchButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-pref-height: 32; -fx-padding: 0 15; -fx-background-radius: 4; " +
                "-fx-cursor: hand;");

        refreshButton = new Button("🔄 Rafraîchir");
        refreshButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                "-fx-pref-height: 32; -fx-padding: 0 15; -fx-background-radius: 4; " +
                "-fx-cursor: hand;");

        // Filtre par statut
        Label filterLabel = new Label("Filtrer :");
        filterLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e; -fx-padding: 0 0 0 15;");

        statutFilterComboBox = new ComboBox<>();
        statutFilterComboBox.getItems().addAll("Tous", "PAYEE", "PARTIELLE", "IMPAYEE");
        statutFilterComboBox.setValue("Tous");
        statutFilterComboBox.setPrefWidth(130);
        statutFilterComboBox.getStyleClass().add("input-field");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchBar.getChildren().addAll(
                searchLabel, searchField, searchButton, refreshButton,
                filterLabel, statutFilterComboBox, spacer
        );
        return searchBar;
    }

    private HBox createStatsBar() {
        HBox statsBar = new HBox();
        statsBar.setSpacing(15);
        statsBar.setAlignment(Pos.CENTER_LEFT);
        statsBar.setPadding(new Insets(5, 0, 10, 0));

        Label statsLabel = new Label("📊 Statistiques :");
        statsLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");

        Label totalLabel = new Label("Total: 0");
        totalLabel.setStyle("-fx-text-fill: #2c3e50;");
        totalLabel.setId("totalLabel");

        Label payeeLabel = new Label("✅ Payées: 0");
        payeeLabel.setStyle("-fx-text-fill: #2ecc71;");
        payeeLabel.setId("payeeLabel");

        Label partielleLabel = new Label("🟡 Partielles: 0");
        partielleLabel.setStyle("-fx-text-fill: #f39c12;");
        partielleLabel.setId("partielleLabel");

        Label impayeeLabel = new Label("❌ Impayées: 0");
        impayeeLabel.setStyle("-fx-text-fill: #e74c3c;");
        impayeeLabel.setId("impayeeLabel");

        Separator separator = new Separator(Orientation.VERTICAL);
        separator.setPadding(new Insets(0, 5, 0, 5));

        Label montantLabel = new Label("💰 Total: 0 FCFA");
        montantLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        montantLabel.setId("montantLabel");

        Label payeLabel = new Label("💵 Payé: 0 FCFA");
        payeLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        payeLabel.setId("payeLabel");

        Label resteLabel = new Label("⏳ Reste: 0 FCFA");
        resteLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        resteLabel.setId("resteLabel");

        statsBar.getChildren().addAll(
                statsLabel, totalLabel, payeeLabel, partielleLabel, impayeeLabel,
                separator,
                montantLabel, payeLabel, resteLabel
        );
        return statsBar;
    }

    private void createTable() {
        factureTable = new TableView<>();
        factureTable.setPrefHeight(350);
        factureTable.getStyleClass().add("table-view");

        // Colonne ID
        colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new SimpleStringProperty(
                String.valueOf(data.getValue().getIdFacture())));
        colId.setPrefWidth(50);

        // Colonne Contrat
        colContrat = new TableColumn<>("Contrat");
        colContrat.setCellValueFactory(data -> new SimpleStringProperty(
                "#" + data.getValue().getIdContrat()));
        colContrat.setPrefWidth(80);

        // Colonne Date émission
        colDateEmission = new TableColumn<>("Émise le");
        colDateEmission.setCellValueFactory(data -> {
            if (data.getValue().getDateEmission() != null) {
                return new SimpleStringProperty(data.getValue().getDateEmission().format(dateFormatter));
            }
            return new SimpleStringProperty("");
        });
        colDateEmission.setPrefWidth(140);

        // Colonne Montant total
        colMontant = new TableColumn<>("Montant");
        colMontant.setCellValueFactory(data -> new SimpleStringProperty(
                FormatUtil.formatFCFA(data.getValue().getMontantTotal())
        ));
        colMontant.setPrefWidth(130);

        // Colonne Montant payé
        colMontantPaye = new TableColumn<>("Payé");
        colMontantPaye.setCellValueFactory(data -> new SimpleStringProperty(
                FormatUtil.formatFCFA(data.getValue().getMontantPaye())
        ));
        colMontantPaye.setPrefWidth(130);

        // Colonne Reste à payer
        colReste = new TableColumn<>("Reste");
        colReste.setCellValueFactory(data -> new SimpleStringProperty(
                FormatUtil.formatFCFA(data.getValue().getResteAPayer())
        ));
        colReste.setPrefWidth(130);

        // Colonne Statut
        colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(data -> {
            String statut = data.getValue().getStatut();
            String emoji = switch (statut) {
                case "PAYEE" -> "✅ PAYÉE";
                case "PARTIELLE" -> "🟡 PARTIELLE";
                default -> "❌ IMPAYÉE";
            };
            return new SimpleStringProperty(emoji);
        });
        colStatut.setPrefWidth(120);

        // Colonne Date paiement
        colDatePaiement = new TableColumn<>("Payée le");
        colDatePaiement.setCellValueFactory(data -> {
            if (data.getValue().getDatePaiement() != null) {
                return new SimpleStringProperty(data.getValue().getDatePaiement().format(dateFormatter));
            }
            return new SimpleStringProperty("N/A");
        });
        colDatePaiement.setPrefWidth(140);

        // Colonne Actions
        colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(280);
        colActions.setCellFactory(param -> new TableCell<Facture, Void>() {
            private final Button payerBtn = new Button("✅ Payer");
            private final Button impayerBtn = new Button("❌ Impayer");
            private final Button pdfBtn = new Button("📄 PDF");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox actionBox = new HBox(5, payerBtn, impayerBtn, pdfBtn, deleteBtn);

            {
                payerBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                        "-fx-cursor: hand; -fx-padding: 4 10; -fx-background-radius: 3; -fx-font-size: 11;");
                impayerBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                        "-fx-cursor: hand; -fx-padding: 4 10; -fx-background-radius: 3; -fx-font-size: 11;");
                pdfBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                        "-fx-cursor: hand; -fx-padding: 4 10; -fx-background-radius: 3; -fx-font-size: 11;");
                deleteBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                        "-fx-cursor: hand; -fx-padding: 4 10; -fx-background-radius: 3; -fx-font-size: 11;");
                actionBox.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Facture facture = getTableView().getItems().get(getIndex());

                    String statut = facture.getStatut();
                    payerBtn.setVisible(!"PAYEE".equals(statut));
                    impayerBtn.setVisible("PAYEE".equals(statut) || "PARTIELLE".equals(statut));
                    pdfBtn.setVisible(true);
                    deleteBtn.setVisible(true);

                    // ✅ TRY/CATCH pour révéler les exceptions silencieuses
                    payerBtn.setOnAction(e -> {
                        try {
                            if (callback != null) callback.onMarquerPayee(facture);
                        } catch (Exception ex) {
                            System.err.println("❌ Erreur bouton Payer :");
                            ex.printStackTrace();
                        }
                    });

                    impayerBtn.setOnAction(e -> {
                        try {
                            if (callback != null) callback.onMarquerImpayee(facture);
                        } catch (Exception ex) {
                            System.err.println("❌ Erreur bouton Impayer :");
                            ex.printStackTrace();
                        }
                    });

                    pdfBtn.setOnAction(e -> {
                        try {
                            if (callback != null) callback.onTelechargerPDF(facture);
                        } catch (Exception ex) {
                            System.err.println("❌ Erreur bouton PDF :");
                            ex.printStackTrace();
                        }
                    });

                    deleteBtn.setOnAction(e -> {
                        try {
                            if (callback != null) callback.onDelete(facture);
                        } catch (Exception ex) {
                            System.err.println("❌ Erreur bouton Supprimer :");
                            ex.printStackTrace();
                        }
                    });

                    setGraphic(actionBox);
                }
            }
        });

        factureTable.getColumns().addAll(
                colId, colContrat, colDateEmission,
                colMontant, colMontantPaye, colReste,
                colStatut, colDatePaiement, colActions
        );
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

    // ===== MISE À JOUR DES STATISTIQUES =====
    public void updateStats(int total, int payees, int partielles, int impayees,
                            double montantTotal, double montantPaye, double resteAPayer) {
        for (javafx.scene.Node node : ((HBox) root.getChildren().get(2)).getChildren()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                String id = label.getId();
                if (id != null) {
                    switch (id) {
                        case "totalLabel" -> label.setText("Total: " + total);
                        case "payeeLabel" -> label.setText("✅ Payées: " + payees);
                        case "partielleLabel" -> label.setText("🟡 Partielles: " + partielles);
                        case "impayeeLabel" -> label.setText("❌ Impayées: " + impayees);
                        case "montantLabel" -> label.setText("💰 Total: " + FormatUtil.formatFCFA(montantTotal));
                        case "payeLabel" -> label.setText("💵 Payé: " + FormatUtil.formatFCFA(montantPaye));
                        case "resteLabel" -> label.setText("⏳ Reste: " + FormatUtil.formatFCFA(resteAPayer));
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