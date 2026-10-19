package view;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import model.Client;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClientManagementView {

    private VBox root;

    // Composants de recherche
    private TextField searchField;
    private Button searchButton;
    private Button refreshButton;

    // Tableau des clients
    private TableView<Client> clientTable;
    private TableColumn<Client, String> colId;
    private TableColumn<Client, String> colNom;
    private TableColumn<Client, String> colPrenom;
    private TableColumn<Client, String> colEmail;
    private TableColumn<Client, String> colTelephone;
    private TableColumn<Client, String> colDateInscription;
    private TableColumn<Client, String> colStatut;
    private TableColumn<Client, Void> colActions;

    // Composants du formulaire
    private TextField nomField;
    private TextField prenomField;
    private TextField emailField;
    private TextField telephoneField;
    private TextArea adresseArea;
    private CheckBox actifCheckBox;
    private Button saveButton;
    private Button cancelButton;
    private Button deleteButton;

    private Label formTitleLabel;
    private Label errorLabel;
    private VBox formContainer;

    // Formatteur de date
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Client sélectionné pour édition
    private Client selectedClient;

    public ClientManagementView() {
        initUI();
    }

    private void initUI() {
        root = new VBox();
        root.setSpacing(15);
        root.setPadding(new Insets(10));

        // Titre
        Label titleLabel = new Label("👥 Gestion des Clients");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Barre de recherche
        HBox searchBar = createSearchBar();

        // Tableau des clients
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
                clientTable,
                separator,
                formContainer
        );

        VBox.setVgrow(clientTable, Priority.ALWAYS);
    }

    private HBox createSearchBar() {
        HBox searchBar = new HBox();
        searchBar.setSpacing(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setPadding(new Insets(5, 0, 5, 0));

        Label searchLabel = new Label("🔍 Rechercher :");
        searchLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");

        searchField = new TextField();
        searchField.setPromptText("Nom, prénom, email ou téléphone...");
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

        Button addButton = new Button("➕ Ajouter un client");
        addButton.getStyleClass().add("action-button");
        addButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-pref-height: 32; -fx-padding: 0 20; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-weight: bold;");
        addButton.setOnAction(e -> showAddForm());

        searchBar.getChildren().addAll(searchLabel, searchField, searchButton, refreshButton, spacer, addButton);
        return searchBar;
    }

    private void createTable() {
        clientTable = new TableView<>();
        clientTable.setPrefHeight(300);
        clientTable.getStyleClass().add("table-view");

        // Colonne ID
        colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getIdClient())));
        colId.setPrefWidth(50);

        // Colonne Nom
        colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNom()));
        colNom.setPrefWidth(120);

        // Colonne Prénom
        colPrenom = new TableColumn<>("Prénom");
        colPrenom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrenom()));
        colPrenom.setPrefWidth(120);

        // Colonne Email
        colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colEmail.setPrefWidth(180);

        // Colonne Téléphone
        colTelephone = new TableColumn<>("Téléphone");
        colTelephone.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelephone()));
        colTelephone.setPrefWidth(120);

        // Colonne Date inscription
        colDateInscription = new TableColumn<>("Inscription");
        colDateInscription.setCellValueFactory(data -> {
            if (data.getValue().getDateInscription() != null) {
                return new SimpleStringProperty(data.getValue().getDateInscription().format(dateFormatter));
            }
            return new SimpleStringProperty("");
        });
        colDateInscription.setPrefWidth(150);

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
        colActions.setCellFactory(param -> new TableCell<Client, Void>() {
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
                    Client client = getTableView().getItems().get(getIndex());

                    // Bouton Modifier
                    editBtn.setOnAction(e -> {
                        if (client != null) {
                            showEditForm(client);
                        }
                    });

                    // Bouton Supprimer - maintenant directement fonctionnel
                    deleteBtn.setOnAction(e -> {
                        if (client != null) {
                            selectedClient = client;
                            // Appeler la méthode de suppression via le contrôleur
                            // Le contrôleur est accessible via un callback
                            if (deleteCallback != null) {
                                deleteCallback.onDelete(client);
                            }
                        }
                    });

                    setGraphic(actionBox);
                }
            }
        });

        clientTable.getColumns().addAll(colId, colNom, colPrenom, colEmail, colTelephone, colDateInscription, colStatut, colActions);
    }

    // Interface de callback pour la suppression
    public interface DeleteCallback {
        void onDelete(Client client);
    }

    private DeleteCallback deleteCallback;

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
        formTitleLabel = new Label("Nouveau client");
        formTitleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Grille du formulaire
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10, 0, 10, 0));

        // Champs
        int row = 0;

        Label nomLabel = new Label("Nom *");
        nomLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        nomField = new TextField();
        nomField.setPromptText("Dupont");
        nomField.setPrefWidth(200);
        nomField.getStyleClass().add("input-field");

        Label prenomLabel = new Label("Prénom *");
        prenomLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        prenomField = new TextField();
        prenomField.setPromptText("Jean");
        prenomField.setPrefWidth(200);
        prenomField.getStyleClass().add("input-field");

        formGrid.add(nomLabel, 0, row);
        formGrid.add(nomField, 1, row);
        formGrid.add(prenomLabel, 2, row);
        formGrid.add(prenomField, 3, row);

        row++;
        Label emailLabel = new Label("Email *");
        emailLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        emailField = new TextField();
        emailField.setPromptText("jean.dupont@email.com");
        emailField.setPrefWidth(300);
        emailField.getStyleClass().add("input-field");

        Label telephoneLabel = new Label("Téléphone *");
        telephoneLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        telephoneField = new TextField();
        telephoneField.setPromptText("0612345678 ou +33612345678");
        telephoneField.setPrefWidth(200);
        telephoneField.getStyleClass().add("input-field");

        // Tooltip d'aide pour le téléphone
        Tooltip phoneTooltip = new Tooltip("Formats acceptés :\n" +
                "• 0612345678 (10 chiffres)\n" +
                "• +33612345678 (international)\n" +
                "• 06 12 34 56 78 (avec espaces)\n" +
                "• 06.12.34.56.78 (avec points)\n" +
                "• 06-12-34-56-78 (avec tirets)");
        phoneTooltip.setStyle("-fx-font-size: 11;");
        telephoneField.setTooltip(phoneTooltip);

        formGrid.add(emailLabel, 0, row);
        formGrid.add(emailField, 1, row, 1, 1);
        formGrid.add(telephoneLabel, 2, row);
        formGrid.add(telephoneField, 3, row);

        row++;
        Label adresseLabel = new Label("Adresse");
        adresseLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        adresseArea = new TextArea();
        adresseArea.setPromptText("Adresse complète...");
        adresseArea.setPrefWidth(400);
        adresseArea.setPrefHeight(60);
        adresseArea.setWrapText(true);
        adresseArea.getStyleClass().add("input-field");

        formGrid.add(adresseLabel, 0, row);
        formGrid.add(adresseArea, 1, row, 3, 1);

        row++;
        actifCheckBox = new CheckBox("Client actif");
        actifCheckBox.setSelected(true);
        actifCheckBox.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");
        formGrid.add(actifCheckBox, 1, row);

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
        formTitleLabel.setText("➕ Nouveau client");
        selectedClient = null;
        deleteButton.setVisible(false);
        showForm();
    }

    public void showEditForm(Client client) {
        if (client == null) return;

        selectedClient = client;
        formTitleLabel.setText("✏️ Modifier le client #" + client.getIdClient());

        nomField.setText(client.getNom());
        prenomField.setText(client.getPrenom());
        emailField.setText(client.getEmail());
        telephoneField.setText(client.getTelephone());
        adresseArea.setText(client.getAdresse());
        actifCheckBox.setSelected(client.isActif());

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
        prenomField.clear();
        emailField.clear();
        telephoneField.clear();
        adresseArea.clear();
        actifCheckBox.setSelected(true);
        errorLabel.setVisible(false);
        errorLabel.setText("");
        selectedClient = null;
    }

    public void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    public void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

    public void refreshTable(List<Client> clients) {
        clientTable.getItems().clear();
        if (clients != null) {
            clientTable.getItems().addAll(clients);
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

    public TableView<Client> getClientTable() {
        return clientTable;
    }

    public TextField getNomField() {
        return nomField;
    }

    public TextField getPrenomField() {
        return prenomField;
    }

    public TextField getEmailField() {
        return emailField;
    }

    public TextField getTelephoneField() {
        return telephoneField;
    }

    public TextArea getAdresseArea() {
        return adresseArea;
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

    public Client getSelectedClient() {
        return selectedClient;
    }

    public TableColumn<Client, Void> getColActions() {
        return colActions;
    }
}