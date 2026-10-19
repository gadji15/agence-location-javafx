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

public class ContratManagementView {

    private VBox root;

    // Composants de recherche
    private TextField searchField;
    private Button searchButton;
    private Button refreshButton;
    private ComboBox<String> filterComboBox;

    // Tableau des contrats
    private TableView<Contrat> contratTable;
    private TableColumn<Contrat, String> colId;
    private TableColumn<Contrat, String> colReservation;
    private TableColumn<Contrat, String> colClient;
    private TableColumn<Contrat, String> colVehicule;
    private TableColumn<Contrat, String> colDateSignature;
    private TableColumn<Contrat, String> colDateDebut;
    private TableColumn<Contrat, String> colDateFin;
    private TableColumn<Contrat, String> colMontantProvisoire;  // ← NOUVEAU
    private TableColumn<Contrat, String> colMontantFinal;       // ← NOUVEAU
    private TableColumn<Contrat, String> colEtatRetour;
    private TableColumn<Contrat, String> colStatut;
    private TableColumn<Contrat, Void> colActions;

    // DAO
    private ReservationDAO reservationDAO;
    private ClientDao clientDAO;
    private VehiculeDAO vehiculeDAO;

    // Formatteur
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private DateTimeFormatter dateFormatterShort = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Callback
    public interface ContratCallback {
        void onCloturer(Contrat contrat);
        void onGenererFacture(Contrat contrat);
        void onDelete(Contrat contrat);
    }

    private ContratCallback callback;

    public ContratManagementView() {
        this.reservationDAO = new ReservationDAO();
        this.clientDAO = new ClientDao();
        this.vehiculeDAO = new VehiculeDAO();
        initUI();
    }

    private void initUI() {
        root = new VBox();
        root.setSpacing(15);
        root.setPadding(new Insets(10));

        Label titleLabel = new Label("📄 Gestion des Contrats");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        HBox searchBar = createSearchBar();
        createTable();

        root.getChildren().addAll(titleLabel, searchBar, contratTable);
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
        searchField.setPromptText("Client, véhicule ou immatriculation...");
        searchField.setPrefWidth(250);
        searchField.getStyleClass().add("input-field");

        searchButton = new Button("Rechercher");
        searchButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-pref-height: 32; -fx-padding: 0 15; -fx-background-radius: 4; -fx-cursor: hand;");

        refreshButton = new Button("🔄 Rafraîchir");
        refreshButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-pref-height: 32; -fx-padding: 0 15; -fx-background-radius: 4; -fx-cursor: hand;");

        Label filterLabel = new Label("Filtrer :");
        filterLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e; -fx-padding: 0 0 0 15;");

        filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll("Tous", "Actifs", "Clôturés");
        filterComboBox.setValue("Tous");
        filterComboBox.setPrefWidth(120);
        filterComboBox.getStyleClass().add("input-field");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchBar.getChildren().addAll(searchLabel, searchField, searchButton, refreshButton,
                filterLabel, filterComboBox, spacer);
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

        // Colonne Client
        colClient = new TableColumn<>("Client");
        colClient.setCellValueFactory(data -> {
            Reservation r = reservationDAO.getById(data.getValue().getIdReservation());
            if (r == null) return new SimpleStringProperty("N/A");
            Client c = clientDAO.getById(r.getIdClient());
            return new SimpleStringProperty(c != null ? c.getPrenom() + " " + c.getNom() : "N/A");
        });
        colClient.setPrefWidth(140);

        // Colonne Véhicule
        colVehicule = new TableColumn<>("Véhicule");
        colVehicule.setCellValueFactory(data -> {
            Reservation r = reservationDAO.getById(data.getValue().getIdReservation());
            if (r == null) return new SimpleStringProperty("N/A");
            Vehicule v = vehiculeDAO.getById(r.getIdVehicule());
            return new SimpleStringProperty(v != null ? v.getMarque() + " " + v.getModele() : "N/A");
        });
        colVehicule.setPrefWidth(140);

        // Colonne Date signature
        colDateSignature = new TableColumn<>("Signature");
        colDateSignature.setCellValueFactory(data -> {
            if (data.getValue().getDateSignature() != null) {
                return new SimpleStringProperty(data.getValue().getDateSignature().format(dateFormatterShort));
            }
            return new SimpleStringProperty("");
        });
        colDateSignature.setPrefWidth(100);

        // Colonne Date début
        colDateDebut = new TableColumn<>("Début");
        colDateDebut.setCellValueFactory(data -> {
            if (data.getValue().getDateDebutEffective() != null) {
                return new SimpleStringProperty(data.getValue().getDateDebutEffective().format(dateFormatterShort));
            }
            return new SimpleStringProperty("");
        });
        colDateDebut.setPrefWidth(100);

        // Colonne Date fin
        colDateFin = new TableColumn<>("Fin");
        colDateFin.setCellValueFactory(data -> {
            if (data.getValue().getDateFinEffective() != null) {
                return new SimpleStringProperty(data.getValue().getDateFinEffective().format(dateFormatterShort));
            }
            return new SimpleStringProperty("");
        });
        colDateFin.setPrefWidth(100);

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

        // Colonne Statut
        colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(data -> {
            String statut = data.getValue().isActif() ? "✅ Actif" : "📦 Clôturé";
            return new SimpleStringProperty(statut);
        });
        colStatut.setPrefWidth(100);

        // Colonne Actions
        colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(230);
        colActions.setCellFactory(param -> new TableCell<Contrat, Void>() {
            private final Button cloturerBtn = new Button("📦 Clôturer");
            private final Button factureBtn = new Button("💰 Facture");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox actionBox = new HBox(5, cloturerBtn, factureBtn, deleteBtn);

            {
                cloturerBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 4 8; -fx-background-radius: 3; -fx-font-size: 10;");
                factureBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 4 8; -fx-background-radius: 3; -fx-font-size: 10;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 4 8; -fx-background-radius: 3; -fx-font-size: 10;");
                actionBox.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Contrat contrat = getTableView().getItems().get(getIndex());

                    cloturerBtn.setVisible(contrat.isActif());
                    factureBtn.setVisible(!contrat.isActif());
                    deleteBtn.setVisible(true);

                    cloturerBtn.setOnAction(e -> {
                        if (callback != null) callback.onCloturer(contrat);
                    });
                    factureBtn.setOnAction(e -> {
                        if (callback != null) callback.onGenererFacture(contrat);
                    });
                    deleteBtn.setOnAction(e -> {
                        if (callback != null) callback.onDelete(contrat);
                    });

                    setGraphic(actionBox);
                }
            }
        });

        contratTable.getColumns().addAll(colId, colReservation, colClient, colVehicule,
                colDateSignature, colDateDebut, colDateFin,
                colMontantProvisoire, colMontantFinal,
                colEtatRetour, colStatut, colActions);
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

    public VBox getRoot() { return root; }
    public TextField getSearchField() { return searchField; }
    public Button getSearchButton() { return searchButton; }
    public Button getRefreshButton() { return refreshButton; }
    public ComboBox<String> getFilterComboBox() { return filterComboBox; }
    public TableView<Contrat> getContratTable() { return contratTable; }
    public Contrat getSelectedContrat() { return contratTable.getSelectionModel().getSelectedItem(); }
}