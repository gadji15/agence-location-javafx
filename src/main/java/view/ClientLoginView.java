package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class ClientLoginView {

    private Stage stage;
    private Scene scene;
    private VBox mainContainer;

    // Composants de l'interface
    private TextField emailField;
    private PasswordField passwordField;
    private Button loginButton;
    private Label errorLabel;
    private Hyperlink adminLink;
    private Hyperlink registerLink;

    public ClientLoginView(Stage stage) {
        this.stage = stage;
        initUI();
    }

    private void initUI() {
        mainContainer = new VBox();
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(30));

        // ===== BACKGROUND APPLIQUÉ DIRECTEMENT DANS LE CODE JAVA =====
        try {
            java.io.InputStream inputStream = getClass().getResourceAsStream("/images/background.jpg");
            if (inputStream != null) {
                Image backgroundImage = new Image(inputStream);
                mainContainer.setBackground(new Background(new BackgroundImage(
                        backgroundImage,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
                )));
                System.out.println("✅ Background client login chargé");
            } else {
                System.err.println("⚠️ Image background.jpg non trouvée");
                mainContainer.setStyle("-fx-background-color: #1a2332;");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur chargement background: " + e.getMessage());
            mainContainer.setStyle("-fx-background-color: #1a2332;");
        }

        VBox loginPanel = createLoginPanel();
        mainContainer.getChildren().add(loginPanel);

        scene = new Scene(mainContainer, 750, 550);

        // ===== CHARGEMENT DU CSS (avec getClassLoader - sans slash) =====
        try {
            java.io.InputStream cssStream = getClass().getClassLoader().getResourceAsStream("css/client-login.css");
            if (cssStream != null) {
                scene.getStylesheets().add(getClass().getClassLoader().getResource("css/client-login.css").toExternalForm());
                System.out.println("✅ CSS client-login.css chargé");
                cssStream.close();
            } else {
                System.out.println("ℹ️ CSS client-login.css non trouvé - styles inline utilisés");
            }
        } catch (Exception e) {
            System.out.println("ℹ️ CSS client-login.css non trouvé - styles inline utilisés");
        }

        stage.setTitle("Agence de Location - Espace Client");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(650);
        stage.setMinHeight(500);
        stage.setMaxWidth(1920);
        stage.setMaxHeight(1080);
    }

    private VBox createLoginPanel() {
        VBox panel = new VBox();
        panel.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 12, 0.5, 0, 6); -fx-padding: 25 30 25 30; -fx-max-width: 400; -fx-min-width: 320;");
        panel.setAlignment(Pos.CENTER);
        panel.setSpacing(8);

        // Logo
        VBox logoContainer = new VBox();
        logoContainer.setAlignment(Pos.CENTER);
        logoContainer.setPadding(new Insets(0, 0, 5, 0));
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
            logo.setStyle("-fx-background-color: #2ecc71; -fx-background-radius: 30;");
        }
        logoContainer.getChildren().add(logo);

        // Icône client
        Label clientIcon = new Label("👤");
        clientIcon.setStyle("-fx-font-size: 28; -fx-alignment: center; -fx-padding: 0 0 2 0;");

        // Titre
        Label titleLabel = new Label("Agence Auto");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-alignment: center;");

        Label subtitleLabel = new Label("Espace Client");
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
        loginButton.setStyle("-fx-pref-height: 35; -fx-background-color: #2ecc71; -fx-background-radius: 6; -fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0 20 0 20;");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setPrefHeight(35);
        loginButton.setDefaultButton(true);

        // Message d'erreur / succès
        errorLabel = new Label();
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);

        // Lien vers l'inscription
        registerLink = new Hyperlink("📝 Créer un compte");
        registerLink.setStyle("-fx-font-size: 11.5; -fx-text-fill: #2ecc71; -fx-padding: 5 0 0 0; -fx-alignment: center;");

        // Lien vers l'espace admin
        adminLink = new Hyperlink("🔑 Accéder à l'espace administrateur");
        adminLink.setStyle("-fx-font-size: 11.5; -fx-text-fill: #3498db; -fx-padding: 5 0 0 0; -fx-alignment: center;");

        // Pied de page
        Label footerLabel = new Label("© 2026 Agence Auto");
        footerLabel.setStyle("-fx-font-size: 9.5; -fx-text-fill: #95a5a6; -fx-padding: 12 0 0 0; -fx-alignment: center;");

        // Assemblage
        panel.getChildren().addAll(
                logoContainer,
                clientIcon,
                titleLabel,
                subtitleLabel,
                separator,
                emailBox,
                passwordBox,
                loginButton,
                errorLabel,
                registerLink,
                adminLink,
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
        emailField.setStyle("-fx-pref-height: 33; -fx-background-color: #f8f9fa; -fx-background-radius: 6; -fx-border-color: #dde1e6; -fx-border-radius: 6; -fx-border-width: 1; -fx-padding: 0 12 0 12; -fx-font-size: 13;");
        emailField.setPromptText("client@email.com");
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
        passwordField.setStyle("-fx-pref-height: 33; -fx-background-color: #f8f9fa; -fx-background-radius: 6; -fx-border-color: #dde1e6; -fx-border-radius: 6; -fx-border-width: 1; -fx-padding: 0 12 0 12; -fx-font-size: 13;");
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
    public Hyperlink getAdminLink() { return adminLink; }
    public Hyperlink getRegisterLink() { return registerLink; }
    public Scene getScene() { return scene; }
    public Stage getStage() { return stage; }

    public void show() { stage.show(); }
    public void close() { stage.close(); }
}