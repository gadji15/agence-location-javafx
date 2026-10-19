package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import model.Client;

import java.time.format.DateTimeFormatter;

public class ClientProfilView {

    private VBox root;
    private ScrollPane scrollPane;  // Ajout du ScrollPane
    private VBox contentContainer;   // Conteneur pour le contenu

    // ===== COMPOSANTS DU FORMULAIRE =====
    private TextField nomField;
    private TextField prenomField;
    private TextField emailField;
    private TextField telephoneField;
    private TextField adresseField;
    private Label dateInscriptionLabel;
    private Label statutLabel;

    private Button modifierButton;
    private Button saveButton;
    private Button cancelButton;
    private Button changePasswordButton;

    private Label errorLabel;
    private Label successLabel;
    private VBox formContainer;

    // ===== COMPOSANTS POUR CHANGEMENT DE MOT DE PASSE =====
    private VBox passwordContainer;
    private PasswordField currentPasswordField;
    private PasswordField newPasswordField;
    private PasswordField confirmPasswordField;
    private Button savePasswordButton;
    private Button cancelPasswordButton;
    private Label passwordErrorLabel;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Interface de callback
    public interface ProfilCallback {
        void onModifier();
        void onSave(Client client);
        void onCancel();
        void onChangePassword(String currentPassword, String newPassword);
    }

    private ProfilCallback callback;
    private boolean isEditing = false;

    public ClientProfilView() {
        initUI();
    }

    private void initUI() {
        root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(10));
        root.setFillWidth(true);

        // ===== CRÉATION DU SCROLLPANE =====
        scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Conteneur du contenu
        contentContainer = new VBox();
        contentContainer.setSpacing(20);
        contentContainer.setPadding(new Insets(20));
        contentContainer.setFillWidth(true);

        // Titre
        Label titleLabel = new Label("👤 Mon Profil");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Sous-titre
        Label subtitleLabel = new Label("Gérez vos informations personnelles");
        subtitleLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #7f8c8d; -fx-padding: 0 0 15 0;");

        // Formulaire du profil
        VBox formSection = createProfilForm();

        // Section changement de mot de passe
        VBox passwordSection = createPasswordSection();

        // Message d'erreur global
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13; -fx-font-weight: 500; -fx-padding: 10 0 0 0;");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);

        successLabel = new Label();
        successLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 13; -fx-font-weight: 500; -fx-padding: 10 0 0 0;");
        successLabel.setManaged(false);
        successLabel.setVisible(false);

        // Assemblage du contenu
        contentContainer.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                formSection,
                passwordSection,
                errorLabel,
                successLabel
        );

        // Ajout du conteneur dans le ScrollPane
        scrollPane.setContent(contentContainer);

        // Ajout du ScrollPane dans la racine
        root.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private VBox createProfilForm() {
        formContainer = new VBox();
        formContainer.setSpacing(15);
        formContainer.setPadding(new Insets(20));
        formContainer.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");
        formContainer.setFillWidth(true);

        Label formTitle = new Label("📋 Informations personnelles");
        formTitle.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Grille du formulaire
        GridPane formGrid = new GridPane();
        formGrid.setHgap(20);
        formGrid.setVgap(12);
        formGrid.setPadding(new Insets(10, 0, 15, 0));
        formGrid.setMaxWidth(Double.MAX_VALUE);

        int row = 0;

        // Nom
        Label nomLabel = new Label("Nom *");
        nomLabel.setStyle("-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #34495e;");
        nomLabel.setMinWidth(80);
        nomField = new TextField();
        nomField.setPromptText("Votre nom");
        nomField.setPrefWidth(250);
        nomField.setStyle("-fx-pref-height: 35; -fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-border-color: #dde1e6; -fx-border-radius: 5; -fx-border-width: 1; -fx-padding: 0 12 0 12; -fx-font-size: 13;");
        nomField.setEditable(false);

        // Prénom
        Label prenomLabel = new Label("Prénom *");
        prenomLabel.setStyle("-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #34495e;");
        prenomLabel.setMinWidth(80);
        prenomField = new TextField();
        prenomField.setPromptText("Votre prénom");
        prenomField.setPrefWidth(250);
        prenomField.setStyle("-fx-pref-height: 35; -fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-border-color: #dde1e6; -fx-border-radius: 5; -fx-border-width: 1; -fx-padding: 0 12 0 12; -fx-font-size: 13;");
        prenomField.setEditable(false);

        formGrid.add(nomLabel, 0, row);
        formGrid.add(nomField, 1, row);
        formGrid.add(prenomLabel, 2, row);
        formGrid.add(prenomField, 3, row);

        // Email
        row++;
        Label emailLabel = new Label("Email *");
        emailLabel.setStyle("-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #34495e;");
        emailLabel.setMinWidth(80);
        emailField = new TextField();
        emailField.setPromptText("Votre email");
        emailField.setPrefWidth(350);
        emailField.setStyle("-fx-pref-height: 35; -fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-border-color: #dde1e6; -fx-border-radius: 5; -fx-border-width: 1; -fx-padding: 0 12 0 12; -fx-font-size: 13;");
        emailField.setEditable(false);

        // Téléphone
        Label telephoneLabel = new Label("Téléphone *");
        telephoneLabel.setStyle("-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #34495e;");
        telephoneLabel.setMinWidth(80);
        telephoneField = new TextField();
        telephoneField.setPromptText("Votre téléphone");
        telephoneField.setPrefWidth(200);
        telephoneField.setStyle("-fx-pref-height: 35; -fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-border-color: #dde1e6; -fx-border-radius: 5; -fx-border-width: 1; -fx-padding: 0 12 0 12; -fx-font-size: 13;");
        telephoneField.setEditable(false);

        formGrid.add(emailLabel, 0, row);
        formGrid.add(emailField, 1, row, 1, 1);
        formGrid.add(telephoneLabel, 2, row);
        formGrid.add(telephoneField, 3, row);

        // Adresse
        row++;
        Label adresseLabel = new Label("Adresse");
        adresseLabel.setStyle("-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #34495e;");
        adresseLabel.setMinWidth(80);
        adresseField = new TextField();
        adresseField.setPromptText("Votre adresse");
        adresseField.setPrefWidth(450);
        adresseField.setStyle("-fx-pref-height: 35; -fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-border-color: #dde1e6; -fx-border-radius: 5; -fx-border-width: 1; -fx-padding: 0 12 0 12; -fx-font-size: 13;");
        adresseField.setEditable(false);

        formGrid.add(adresseLabel, 0, row);
        formGrid.add(adresseField, 1, row, 3, 1);

        // Date d'inscription
        row++;
        Label dateLabel = new Label("Inscrit le");
        dateLabel.setStyle("-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #34495e;");
        dateLabel.setMinWidth(80);
        dateInscriptionLabel = new Label("---");
        dateInscriptionLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #7f8c8d;");

        // Statut
        Label statutTitreLabel = new Label("Statut");
        statutTitreLabel.setStyle("-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #34495e;");
        statutTitreLabel.setMinWidth(80);
        statutLabel = new Label("✅ Actif");
        statutLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #2ecc71; -fx-font-weight: 600;");

        formGrid.add(dateLabel, 0, row);
        formGrid.add(dateInscriptionLabel, 1, row);
        formGrid.add(statutTitreLabel, 2, row);
        formGrid.add(statutLabel, 3, row);

        // Boutons
        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        modifierButton = new Button("✏️ Modifier");
        modifierButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-pref-height: 36; -fx-padding: 0 25; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-weight: bold;");
        modifierButton.setOnAction(e -> {
            if (callback != null) callback.onModifier();
        });

        saveButton = new Button("💾 Enregistrer");
        saveButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-pref-height: 36; -fx-padding: 0 25; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-weight: bold;");
        saveButton.setVisible(false);
        saveButton.setOnAction(e -> {
            if (callback != null) {
                Client client = getClientFromForm();
                if (client != null) {
                    callback.onSave(client);
                }
            }
        });

        cancelButton = new Button("❌ Annuler");
        cancelButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-pref-height: 36; -fx-padding: 0 25; -fx-background-radius: 4; -fx-cursor: hand;");
        cancelButton.setVisible(false);
        cancelButton.setOnAction(e -> {
            if (callback != null) callback.onCancel();
        });

        buttonBox.getChildren().addAll(modifierButton, saveButton, cancelButton);

        formContainer.getChildren().addAll(formTitle, formGrid, buttonBox);
        return formContainer;
    }

    private VBox createPasswordSection() {
        passwordContainer = new VBox();
        passwordContainer.setSpacing(12);
        passwordContainer.setPadding(new Insets(20));
        passwordContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-border-color: #e0e5ec; -fx-border-radius: 8; -fx-border-width: 1;");
        passwordContainer.setFillWidth(true);

        Label passwordTitle = new Label("🔒 Changer le mot de passe");
        passwordTitle.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        GridPane passwordGrid = new GridPane();
        passwordGrid.setHgap(15);
        passwordGrid.setVgap(10);
        passwordGrid.setPadding(new Insets(10, 0, 10, 0));
        passwordGrid.setMaxWidth(Double.MAX_VALUE);

        int row = 0;

        // Mot de passe actuel
        Label currentLabel = new Label("Mot de passe actuel *");
        currentLabel.setStyle("-fx-font-size: 12; -fx-font-weight: 600; -fx-text-fill: #34495e;");
        currentLabel.setMinWidth(140);
        currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("••••••••");
        currentPasswordField.setPrefWidth(250);
        currentPasswordField.setStyle("-fx-pref-height: 35; -fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-border-color: #dde1e6; -fx-border-radius: 5; -fx-border-width: 1; -fx-padding: 0 12 0 12; -fx-font-size: 13;");

        passwordGrid.add(currentLabel, 0, row);
        passwordGrid.add(currentPasswordField, 1, row);

        // Nouveau mot de passe
        row++;
        Label newLabel = new Label("Nouveau mot de passe *");
        newLabel.setStyle("-fx-font-size: 12; -fx-font-weight: 600; -fx-text-fill: #34495e;");
        newLabel.setMinWidth(140);
        newPasswordField = new PasswordField();
        newPasswordField.setPromptText("•••••••• (min 6 caractères)");
        newPasswordField.setPrefWidth(250);
        newPasswordField.setStyle("-fx-pref-height: 35; -fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-border-color: #dde1e6; -fx-border-radius: 5; -fx-border-width: 1; -fx-padding: 0 12 0 12; -fx-font-size: 13;");

        passwordGrid.add(newLabel, 0, row);
        passwordGrid.add(newPasswordField, 1, row);

        // Confirmation nouveau mot de passe
        row++;
        Label confirmLabel = new Label("Confirmer *");
        confirmLabel.setStyle("-fx-font-size: 12; -fx-font-weight: 600; -fx-text-fill: #34495e;");
        confirmLabel.setMinWidth(140);
        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("••••••••");
        confirmPasswordField.setPrefWidth(250);
        confirmPasswordField.setStyle("-fx-pref-height: 35; -fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-border-color: #dde1e6; -fx-border-radius: 5; -fx-border-width: 1; -fx-padding: 0 12 0 12; -fx-font-size: 13;");

        passwordGrid.add(confirmLabel, 0, row);
        passwordGrid.add(confirmPasswordField, 1, row);

        // Message d'erreur mot de passe
        passwordErrorLabel = new Label();
        passwordErrorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12; -fx-font-weight: 500;");
        passwordErrorLabel.setManaged(false);
        passwordErrorLabel.setVisible(false);

        // Boutons mot de passe
        HBox passwordButtonBox = new HBox();
        passwordButtonBox.setSpacing(10);
        passwordButtonBox.setAlignment(Pos.CENTER_RIGHT);

        savePasswordButton = new Button("✅ Changer le mot de passe");
        savePasswordButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-pref-height: 36; -fx-padding: 0 20; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-weight: bold;");
        savePasswordButton.setOnAction(e -> {
            if (callback != null) {
                String current = currentPasswordField.getText().trim();
                String newPwd = newPasswordField.getText().trim();
                String confirm = confirmPasswordField.getText().trim();

                if (current.isEmpty() || newPwd.isEmpty() || confirm.isEmpty()) {
                    showPasswordError("⚠️ Tous les champs sont obligatoires !");
                    return;
                }

                if (newPwd.length() < 6) {
                    showPasswordError("⚠️ Le nouveau mot de passe doit contenir au moins 6 caractères !");
                    return;
                }

                if (!newPwd.equals(confirm)) {
                    showPasswordError("⚠️ Les mots de passe ne correspondent pas !");
                    return;
                }

                callback.onChangePassword(current, newPwd);
            }
        });

        cancelPasswordButton = new Button("❌ Annuler");
        cancelPasswordButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-pref-height: 36; -fx-padding: 0 20; -fx-background-radius: 4; -fx-cursor: hand;");
        cancelPasswordButton.setOnAction(e -> clearPasswordFields());

        passwordButtonBox.getChildren().addAll(cancelPasswordButton, savePasswordButton);

        passwordContainer.getChildren().addAll(passwordTitle, passwordGrid, passwordErrorLabel, passwordButtonBox);
        return passwordContainer;
    }

    // ===== MÉTHODES D'AFFICHAGE =====

    public void displayClient(Client client) {
        if (client == null) {
            System.out.println("❌ Client null dans displayClient()");
            return;
        }

        System.out.println("📋 Affichage du client: " + client.getPrenom() + " " + client.getNom());

        nomField.setText(client.getNom() != null ? client.getNom() : "");
        prenomField.setText(client.getPrenom() != null ? client.getPrenom() : "");
        emailField.setText(client.getEmail() != null ? client.getEmail() : "");
        telephoneField.setText(client.getTelephone() != null ? client.getTelephone() : "");
        adresseField.setText(client.getAdresse() != null ? client.getAdresse() : "");

        if (client.getDateInscription() != null) {
            dateInscriptionLabel.setText(client.getDateInscription().format(dateFormatter));
        } else {
            dateInscriptionLabel.setText("---");
        }

        String statut = client.isActif() ? "✅ Actif" : "❌ Inactif";
        String color = client.isActif() ? "#2ecc71" : "#e74c3c";
        statutLabel.setText(statut);
        statutLabel.setStyle("-fx-font-size: 13; -fx-text-fill: " + color + "; -fx-font-weight: 600;");
    }

    public Client getClientFromForm() {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String telephone = telephoneField.getText().trim();
        String adresse = adresseField.getText().trim();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || telephone.isEmpty()) {
            showError("⚠️ Les champs Nom, Prénom, Email et Téléphone sont obligatoires !");
            return null;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("⚠️ Format d'email invalide !");
            return null;
        }

        Client client = new Client();
        client.setNom(nom);
        client.setPrenom(prenom);
        client.setEmail(email);
        client.setTelephone(telephone);
        client.setAdresse(adresse);

        return client;
    }

    public void setEditingMode(boolean editing) {
        this.isEditing = editing;
        nomField.setEditable(editing);
        prenomField.setEditable(editing);
        emailField.setEditable(editing);
        telephoneField.setEditable(editing);
        adresseField.setEditable(editing);

        modifierButton.setVisible(!editing);
        saveButton.setVisible(editing);
        cancelButton.setVisible(editing);
    }

    public void clearPasswordFields() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        passwordErrorLabel.setVisible(false);
        passwordErrorLabel.setManaged(false);
    }

    public void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
        successLabel.setManaged(false);
        successLabel.setVisible(false);
    }

    public void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setManaged(true);
        successLabel.setVisible(true);
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
    }

    public void showPasswordError(String message) {
        passwordErrorLabel.setText(message);
        passwordErrorLabel.setManaged(true);
        passwordErrorLabel.setVisible(true);
    }

    public void clearError() {
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
        successLabel.setManaged(false);
        successLabel.setVisible(false);
        passwordErrorLabel.setManaged(false);
        passwordErrorLabel.setVisible(false);
    }

    public void setCallback(ProfilCallback callback) {
        this.callback = callback;
    }

    // ===== GETTERS =====

    public VBox getRoot() { return root; }
    public ScrollPane getScrollPane() { return scrollPane; }
    public TextField getNomField() { return nomField; }
    public TextField getPrenomField() { return prenomField; }
    public TextField getEmailField() { return emailField; }
    public TextField getTelephoneField() { return telephoneField; }
    public TextField getAdresseField() { return adresseField; }
    public Button getModifierButton() { return modifierButton; }
    public Button getSaveButton() { return saveButton; }
    public Button getCancelButton() { return cancelButton; }
    public Button getChangePasswordButton() { return changePasswordButton; }
    public PasswordField getCurrentPasswordField() { return currentPasswordField; }
    public PasswordField getNewPasswordField() { return newPasswordField; }
    public PasswordField getConfirmPasswordField() { return confirmPasswordField; }
    public Button getSavePasswordButton() { return savePasswordButton; }
    public Button getCancelPasswordButton() { return cancelPasswordButton; }
}