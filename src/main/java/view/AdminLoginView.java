package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class AdminLoginView {

    private Stage stage;
    private Scene scene;
    private VBox mainContainer;

    // Composants de l'interface
    private TextField emailField;
    private PasswordField passwordField;
    private Button loginButton;
    private Label errorLabel;
    private Hyperlink clientLink;

    public AdminLoginView(Stage stage) {
        this.stage = stage;
        initUI();
    }

    private void initUI() {
        mainContainer = new VBox();
        mainContainer.getStyleClass().add("login-container");
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(30));

        VBox loginPanel = createLoginPanel();
        mainContainer.getChildren().add(loginPanel);

        scene = new Scene(mainContainer, 750, 550);

        try {
            String cssPath = getClass().getResource("/css/admin-login.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("⚠️ CSS admin-login.css non trouvé");
        }

        stage.setTitle("Agence de Location - Connexion Admin");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(650);
        stage.setMinHeight(500);
        stage.setMaxWidth(1920);
        stage.setMaxHeight(1080);
    }

    private VBox createLoginPanel() {
        VBox panel = new VBox();
        panel.getStyleClass().add("login-panel");
        panel.setAlignment(Pos.CENTER);
        panel.setSpacing(8);
        panel.setPadding(new Insets(25, 30, 25, 30));
        panel.setMaxWidth(400);
        panel.setMinWidth(320);

        // Logo
        VBox logoContainer = new VBox();
        logoContainer.getStyleClass().add("logo-container");
        logoContainer.setAlignment(Pos.CENTER);
        ImageView logo = new ImageView();
        try {
            Image image = new Image(getClass().getResourceAsStream("/images/logo.jpg"));
            logo.setImage(image);
            logo.setFitWidth(60);
            logo.setFitHeight(60);
            logo.setPreserveRatio(true);
        } catch (Exception e) {
            logo.setFitWidth(60);
            logo.setFitHeight(60);
            logo.setStyle("-fx-background-color: #3498db; -fx-background-radius: 30;");
        }
        logoContainer.getChildren().add(logo);

        // Icône admin
        Label adminIcon = new Label("🔐");
        adminIcon.setStyle("-fx-font-size: 28; -fx-alignment: center; -fx-padding: 0 0 2 0;");

        // Titre
        Label titleLabel = new Label("Agence Auto");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-alignment: center;");

        Label subtitleLabel = new Label("Espace Administrateur");
        subtitleLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d; -fx-padding: 0 0 10 0; -fx-alignment: center;");

        // Séparateur
        Separator separator = new Separator();
        separator.setStyle("-fx-padding: 3 0 8 0;");

        // Champ Email
        VBox emailBox = createEmailField();

        // Champ Mot de passe
        VBox passwordBox = createPasswordField();

        // Bouton de connexion
        loginButton = new Button("Se connecter");
        loginButton.getStyleClass().add("login-button");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setPrefHeight(35);
        loginButton.setDefaultButton(true);

        // Message d'erreur / succès
        errorLabel = new Label();
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);

        // Lien vers l'espace client
        clientLink = new Hyperlink("🔑 Accéder à l'espace client");
        clientLink.getStyleClass().add("client-link");
        clientLink.setAlignment(Pos.CENTER);
        clientLink.setStyle("-fx-font-size: 11.5; -fx-text-fill: #3498db; -fx-padding: 8 0 0 0;");

        // Pied de page
        Label footerLabel = new Label("© 2026 Agence Auto");
        footerLabel.setStyle("-fx-font-size: 9.5; -fx-text-fill: #95a5a6; -fx-padding: 12 0 0 0; -fx-alignment: center;");

        // Assemblage
        panel.getChildren().addAll(
                logoContainer,
                adminIcon,
                titleLabel,
                subtitleLabel,
                separator,
                emailBox,
                passwordBox,
                loginButton,
                errorLabel,
                clientLink,
                footerLabel
        );

        return panel;
    }

    private VBox createEmailField() {
        VBox box = new VBox();
        box.setSpacing(3);

        Label label = new Label("📧 Adresse Email");
        label.setStyle("-fx-font-size: 11.5; -fx-font-weight: 600; -fx-text-fill: #34495e;");

        emailField = new TextField();
        emailField.getStyleClass().add("input-field");
        emailField.setPromptText("admin@agence.com");
        emailField.setPrefHeight(33);

        box.getChildren().addAll(label, emailField);
        return box;
    }

    private VBox createPasswordField() {
        VBox box = new VBox();
        box.setSpacing(3);

        Label label = new Label("🔒 Mot de passe");
        label.setStyle("-fx-font-size: 11.5; -fx-font-weight: 600; -fx-text-fill: #34495e;");

        passwordField = new PasswordField();
        passwordField.getStyleClass().add("input-field");
        passwordField.setPromptText("••••••••");
        passwordField.setPrefHeight(33);

        box.getChildren().addAll(label, passwordField);
        return box;
    }

    // ===== MÉTHODES DE MESSAGE =====

    /**
     * Affiche un message d'erreur en ROUGE
     */
    public void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12; -fx-padding: 5 0 0 0; -fx-alignment: center; -fx-font-weight: 600;");
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }

    /**
     * Affiche un message de succès en VERT
     */
    public void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 12; -fx-padding: 5 0 0 0; -fx-alignment: center; -fx-font-weight: 600;");
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }

    public void clearError() {
        errorLabel.setText("");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
    }

    public void clearFields() {
        emailField.clear();
        passwordField.clear();
        errorLabel.setText("");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
        loginButton.setDisable(false);
    }

    // ===== GETTERS =====

    public TextField getEmailField() { return emailField; }
    public PasswordField getPasswordField() { return passwordField; }
    public Button getLoginButton() { return loginButton; }
    public Label getErrorLabel() { return errorLabel; }
    public Hyperlink getClientLink() { return clientLink; }
    public Scene getScene() { return scene; }
    public Stage getStage() { return stage; }

    public void show() { stage.show(); }
    public void close() { stage.close(); }
}