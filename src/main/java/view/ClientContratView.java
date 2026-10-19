package view;

import dao.ClientDao;
import dao.ReservationDAO;
import dao.VehiculeDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Client;
import model.Contrat;
import model.Reservation;
import model.Vehicule;
import utils.FormatUtil;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClientContratView {

    private VBox root;

    // ===== COMPOSANTS DE RECHERCHE =====
    private TextField searchField;
    private Button searchButton;
    private Button refreshButton;
    private ComboBox<String> statutFilterComboBox;

    // ===== TABLEAU DES CONTRATS =====
    private TableView<Contrat> contratTable;
    private TableColumn<Contrat, String> colId;
    private TableColumn<Contrat, String> colReservation;
    private TableColumn<Contrat, String> colVehicule;
    private TableColumn<Contrat, String> colDateSignature;
    private TableColumn<Contrat, String> colDateDebut;
    private TableColumn<Contrat, String> colDateFin;
    private TableColumn<Contrat, String> colMontantProvisoire;   // ← NOUVEAU
    private TableColumn<Contrat, String> colMontantFinal;        // ← NOUVEAU
    private TableColumn<Contrat, String> colEtatRetour;
    private TableColumn<Contrat, String> colKilometrage;
    private TableColumn<Contrat, String> colStatut;

    // ===== DAO =====
    private ReservationDAO reservationDAO;
    private VehiculeDAO vehiculeDAO;
    private ClientDao clientDAO;

    // ===== FORMATTEUR DE DATE =====
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Interface de callback
    public interface ContratCallback {
        void onSearch(String keyword);
        void onFilter(String statut);
        void onRefresh();
    }

    private ContratCallback callback;

    public ClientContratView() {
        this.reservationDAO = new ReservationDAO();
        this.vehiculeDAO = new VehiculeDAO();
        this.clientDAO = new ClientDao();
        initUI();
    }

    private void initUI() {
        root = new VBox();
        root.setSpacing(15);
        root.setPadding(new Insets(10));

        // Titre
        Label titleLabel = new Label("📄 Mes Contrats");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Barre de recherche
        HBox searchBar = createSearchBar();

        // Tableau des contrats
        createTable();

        // Statistiques
        HBox statsBar = createStatsBar();

        // Assemblage
        root.getChildren().addAll(
                titleLabel,
                searchBar,
                statsBar,
                contratTable
        );

        VBox.setVgrow(contratTable, Priority.ALWAYS);
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

        // Filtre par statut
        Label filterLabel = new Label("Filtrer :");
        filterLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e; -fx-padding: 0 0 0 15;");

        statutFilterComboBox = new ComboBox<>();
        statutFilterComboBox.getItems().addAll("Tous", "Actifs", "Clôturés");
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
        contratTable = new TableView<>();
        contratTable.setPrefHeight(350);
        contratTable.getStyleClass().add("table-view");

        // Colonne ID
        colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getIdContrat())));
        colId.setPrefWidth(50);

        // Colonne Réservation
        colReservation = new TableColumn<>("Réservation");
        colReservation.setCellValueFactory(data -> new SimpleStringProperty("#" + data.getValue().getIdReservation()));
        colReservation.setPrefWidth(90);

        // ===== COLONNE VÉHICULE (avec marque + modèle) =====
        colVehicule = new TableColumn<>("Véhicule");
        colVehicule.setCellValueFactory(data -> {
            Reservation r = reservationDAO.getById(data.getValue().getIdReservation());
            if (r == null) return new SimpleStringProperty("N/A");
            Vehicule v = vehiculeDAO.getById(r.getIdVehicule());
            return new SimpleStringProperty(v != null ? v.getMarque() + " " + v.getModele() : "N/A");
        });
        colVehicule.setPrefWidth(150);

        // Colonne Date signature
        colDateSignature = new TableColumn<>("Signature");
        colDateSignature.setCellValueFactory(data -> {
            if (data.getValue().getDateSignature() != null) {
                return new SimpleStringProperty(data.getValue().getDateSignature().format(dateFormatter));
            }
            return new SimpleStringProperty("");
        });
        colDateSignature.setPrefWidth(150);

        // Colonne Date début
        colDateDebut = new TableColumn<>("Début");
        colDateDebut.setCellValueFactory(data -> {
            if (data.getValue().getDateDebutEffective() != null) {
                return new SimpleStringProperty(data.getValue().getDateDebutEffective().format(dateFormatter));
            }
            return new SimpleStringProperty("");
        });
        colDateDebut.setPrefWidth(150);

        // Colonne Date fin
        colDateFin = new TableColumn<>("Fin");
        colDateFin.setCellValueFactory(data -> {
            if (data.getValue().getDateFinEffective() != null) {
                return new SimpleStringProperty(data.getValue().getDateFinEffective().format(dateFormatter));
            }
            return new SimpleStringProperty("");
        });
        colDateFin.setPrefWidth(150);

        // ===== NOUVELLE COLONNE : MONTANT PROVISOIRE =====
        colMontantProvisoire = new TableColumn<>("Montant initial");
        colMontantProvisoire.setCellValueFactory(data -> new SimpleStringProperty(
                FormatUtil.formatFCFA(data.getValue().getMontantProvisoire())
        ));
        colMontantProvisoire.setPrefWidth(130);

        // ===== NOUVELLE COLONNE : MONTANT FINAL =====
        colMontantFinal = new TableColumn<>("Montant final");
        colMontantFinal.setCellValueFactory(data -> {
            Contrat c = data.getValue();
            if (c.isActif()) {
                return new SimpleStringProperty("⏳ En attente");
            } else {
                return new SimpleStringProperty(FormatUtil.formatFCFA(c.getMontantTotal()));
            }
        });
        colMontantFinal.setPrefWidth(130);

        // Colonne État retour
        colEtatRetour = new TableColumn<>("Retour");
        colEtatRetour.setCellValueFactory(data -> {
            String etat = data.getValue().getEtatRetour();
            if (etat == null || etat.isEmpty()) return new SimpleStringProperty("Non retourné");

            String emoji = switch (etat) {
                case "BON" -> "✅";
                case "MOYEN" -> "🟡";
                case "MAUVAIS" -> "🔴";
                default -> "⚪";
            };
            return new SimpleStringProperty(emoji + " " + etat);
        });
        colEtatRetour.setPrefWidth(100);

        // Colonne Kilométrage
        colKilometrage = new TableColumn<>("Km final");
        colKilometrage.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getKilometrageFinal() > 0 ? String.valueOf(data.getValue().getKilometrageFinal()) : "N/A"
        ));
        colKilometrage.setPrefWidth(90);

        // Colonne Statut
        colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(data -> {
            String statut = data.getValue().isActif() ? "✅ Actif" : "📦 Clôturé";
            return new SimpleStringProperty(statut);
        });
        colStatut.setPrefWidth(100);

        contratTable.getColumns().addAll(
                colId, colReservation, colVehicule,
                colDateSignature, colDateDebut, colDateFin,
                colMontantProvisoire, colMontantFinal,
                colEtatRetour, colKilometrage, colStatut
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

        Label actifLabel = new Label("✅ Actifs: 0");
        actifLabel.setStyle("-fx-text-fill: #2ecc71;");
        actifLabel.setId("actifLabel");

        Label clotureLabel = new Label("📦 Clôturés: 0");
        clotureLabel.setStyle("-fx-text-fill: #95a5a6;");
        clotureLabel.setId("clotureLabel");

        statsBar.getChildren().addAll(statsLabel, totalLabel, actifLabel, clotureLabel);
        return statsBar;
    }

    public void setCallback(ContratCallback callback) {
        this.callback = callback;
    }

    public void refreshTable(List<Contrat> contrats) {
        contratTable.getItems().clear();
        if (contrats != null) {
            contratTable.getItems().addAll(contrats);
        }
    }

    public void updateStats(int total, int actifs, int clotures) {
        for (javafx.scene.Node node : ((HBox) root.getChildren().get(2)).getChildren()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                String id = label.getId();
                if (id != null) {
                    switch (id) {
                        case "totalLabel" -> label.setText("Total: " + total);
                        case "actifLabel" -> label.setText("✅ Actifs: " + actifs);
                        case "clotureLabel" -> label.setText("📦 Clôturés: " + clotures);
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
    public TableView<Contrat> getContratTable() { return contratTable; }
}