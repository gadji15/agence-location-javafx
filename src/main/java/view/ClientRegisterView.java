package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class ClientRegisterView {

    private Stage stage;
    private Scene scene;
    private BorderPane mainContainer;
    private ScrollPane scrollPane;

    // Composants du formulaire
    private TextField nomField;
    private TextField prenomField;
    private TextField emailField;
    private TextField telephoneField;
    private TextField adresseField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private Button registerButton;
    private Label errorLabel;
    private Hyperlink loginLink;

    public ClientRegisterView(Stage stage) {
        this.stage = stage;
        initUI();
    }

    private void initUI() {
        mainContainer = new BorderPane();

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
            } else {
                mainContainer.setStyle("-fx-background-color: #1a2332;");
            }
        } catch (Exception e) {
            mainContainer.setStyle("-fx-background-color: #1a2332;");
        }

        scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox registerPanel = createRegisterPanel();
        scrollPane.setContent(registerPanel);
        mainContainer.setCenter(scrollPane);
        BorderPane.setAlignment(scrollPane, Pos.CENTER);

        scene = new Scene(mainContainer, 750, 650);

        try {
            String cssPath = getClass().getResource("/css/client-login.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            // CSS optionnel
        }

        stage.setTitle("Agence de Location - Inscription Client");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(650);
        stage.setMinHeight(550);
    }

    private VBox createRegisterPanel() {
        VBox panel = new VBox();
        panel.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 12, 0.5, 0, 6); -fx-padding: 30 40 35 40;");
        panel.setAlignment(Pos.CENTER);
        panel.setSpacing(12);
        panel.setMaxWidth(500);
        panel.setMinWidth(380);
        panel.setFillWidth(true);

        VBox wrapper = new VBox();
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setFillWidth(true);
        wrapper.getChildren().add(panel);
        wrapper.setPadding(new Insets(20));

        // Logo
        VBox logoContainer = new VBox();
        logoContainer.setAlignment(Pos.CENTER);
        logoContainer.setPadding(new Insets(0, 0, 5, 0));
        ImageView logo = new ImageView();
        try {
            java.io.InputStream inputStream = getClass().getResourceAsStream("/images/logo.jpg");
            if (inputStream != null) {
                Image image = new Image(inputStream);
                logo.setImage(image);
                logo.setFitWidth(55);
                logo.setFitHeight(55);
                logo.setPreserveRatio(true);
            } else {
                logo.setFitWidth(55);
                logo.setFitHeight(55);
                logo.setStyle("-fx-background-color: #2ecc71; -fx-background-radius: 27.5;");
            }
        } catch (Exception e) {
            logo.setFitWidth(55);
            logo.setFitHeight(55);
            logo.setStyle("-fx-background-color: #2ecc71; -fx-background-radius: 27.5;");
        }
        logoContainer.getChildren().add(logo);

        // Titre
        Label titleLabel = new Label("📝 Créer un compte");
        titleLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-alignment: center;");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);

        Label subtitleLabel = new Label("Inscription sur l'espace client");
        subtitleLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #7f8c8d; -fx-alignment: center; -fx-padding: 0 0 10 0;");
        subtitleLabel.setMaxWidth(Double.MAX_VALUE);
        subtitleLabel.setAlignment(Pos.CENTER);

        Separator separator = new Separator();
        separator.setStyle("-fx-padding: 5 0 10 0;");

        // Grille du formulaire
        GridPane formGrid = new GridPane();
        formGrid.setHgap(20);
        formGrid.setVgap(12);
        formGrid.setPadding(new Insets(10, 0, 15, 0));
        formGrid.setAlignment(Pos.CENTER);
        formGrid.setMaxWidth(Double.MAX_VALUE);

        int row = 0;

        // Nom
        Label nomLabel = new Label("Nom *");
        nomLabel.setStyle("-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #34495e;");
        nomLabel.setMinWidth(70);
        nomField = new TextField();
        nomField.setPromptText("Dupont");
        nomField.setPrefWidth(180);
        nomField.setMaxWidth(Double.MAX_VALUE);
        nomField.setStyle("-fx-pref-height: 36; -fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-border-color: #dde1e6; -fx-border-radius: 5; -fx-border-width: 1; -fx-padding: 0 12 0 12; -fx-font-size: 13;");

        Label prenomLabel = new Label("Prénom *");
        prenomLabel.setStyle("-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #34495e;");
        prenomLabel.setMinWidth(70);
        prenomField = new TextField();
        prenomField.setPromptText("Jean");
        prenomField.setPrefWidth(180);
        prenomField.setMaxWidth(Double.MAX_VALUE);
        prenomField.setStyle("-fx-pref-height: 36; -fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-border-color: #dde1e6; -fx-border-radius: 5; -fx-border-width: 1; -fx-padding: 0 12 0 12; -fx-font-size: 13;");

        formGrid.add(nomLabel, 0, row);
        formGrid.add(nomField, 1, row);
        formGrid.add(prenomLabel, 2, row);
        formGrid.add(prenomField, 3, row);

        // Email
        row++;
        Label emailLabel = new Label("Email *");
        emailLabel.setStyle("-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #34495e;");
        emailLabel.setMinWidth(70);
        emailField = new TextField();
        emailField.setPromptText("jean.dupont@email.com");
        emailField.setPrefWidth(250);
        emailField.setMaxWidth(Double.MAX_VALUE);
        emailField.setStyle("-fx-pref-height: 36; -fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-border-color: #dde1e6; -fx-border-radius: 5; -fx-border-width: 1; -fx-padding: 0 12 0 12; -fx-font-size: 13;");

        Label telephoneLabel = new Label("Téléphone *");
        telephoneLabel.setStyle("-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #34495e;");
        telephoneLabel.setMinWidth(70);
        telephoneField = new TextField();
        telephoneField.setPromptText("771234567");
        telephoneField.setPrefWidth(160);
        telephoneField.setMaxWidth(Double.MAX_VALUE);
        telephoneField.setStyle("-fx-pref-height: 36; -fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-border-color: #dde1e6; -fx-border-radius: 5; -fx-border-width: 1; -fx-padding: 0 12 0 12; -fx-font-size: 13;");

        formGrid.add(emailLabel, 0, row);
        formGrid.add(emailField, 1, row, 1, 1);
        formGrid.add(telephoneLabel, 2, row);
        formGrid.add(telephoneField, 3, row);

        // Adresse
        row++;
        Label adresseLabel = new Label("Adresse");
        adresseLabel.setStyle("-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #34495e;");
        adresseLabel.setMinWidth(70);
        adresseField = new TextField();
        adresseField.setPromptText("12 Rue de Paris, Dakar");
        adresseField.setPrefWidth(430);
        adresseField.setMaxWidth(Double.MAX_VALUE);
        adresseField.setStyle("-fx-pref-height: 36; -fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-border-color: #dde1e6; -fx-border-radius: 5; -fx-border-width: 1; -fx-padding: 0 12 0 12; -fx-font-size: 13;");

        formGrid.add(adresseLabel, 0, row);
        formGrid.add(adresseField, 1, row, 3, 1);

        // Mot de passe
        row++;
        Label passwordLabel = new Label("Mot de passe *");
        passwordLabel.setStyle("-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #34495e;");
        passwordLabel.setMinWidth(70);
        passwordField = new PasswordField();
        passwordField.setPromptText("••••••••");
        passwordField.setPrefWidth(180);
        passwordField.setMaxWidth(Double.MAX_VALUE);
        passwordField.setStyle("-fx-pref-height: 36; -fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-border-color: #dde1e6; -fx-border-radius: 5; -fx-border-width: 1; -fx-padding: 0 12 0 12; -fx-font-size: 13;");

        Label confirmLabel = new Label("Confirmer *");
        confirmLabel.setStyle("-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #34495e;");
        confirmLabel.setMinWidth(70);
        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("••••••••");
        confirmPasswordField.setPrefWidth(180);
        confirmPasswordField.setMaxWidth(Double.MAX_VALUE);
        confirmPasswordField.setStyle("-fx-pref-height: 36; -fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-border-color: #dde1e6; -fx-border-radius: 5; -fx-border-width: 1; -fx-padding: 0 12 0 12; -fx-font-size: 13;");

        formGrid.add(passwordLabel, 0, row);
        formGrid.add(passwordField, 1, row);
        formGrid.add(confirmLabel, 2, row);
        formGrid.add(confirmPasswordField, 3, row);

        // Message d'erreur / succès
        errorLabel = new Label();
        errorLabel.setMaxWidth(Double.MAX_VALUE);
        errorLabel.setAlignment(Pos.CENTER);
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);

        // Bouton d'inscription
        registerButton = new Button("📝 S'inscrire");
        registerButton.setStyle("-fx-pref-height: 40; -fx-background-color: #2ecc71; -fx-background-radius: 6; -fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0 20 0 20;");
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setPrefHeight(40);

        // Lien vers la connexion
        loginLink = new Hyperlink("🔑 Déjà un compte ? Se connecter");
        loginLink.setStyle("-fx-text-fill: #3498db; -fx-font-size: 13; -fx-underline: true; -fx-cursor: hand; -fx-padding: 8 0 0 0; -fx-alignment: center;");
        loginLink.setMaxWidth(Double.MAX_VALUE);
        loginLink.setAlignment(Pos.CENTER);

        // Pied de page
        Label footerLabel = new Label("© 2026 Agence Auto");
        footerLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #95a5a6; -fx-padding: 15 0 0 0; -fx-alignment: center;");
        footerLabel.setMaxWidth(Double.MAX_VALUE);
        footerLabel.setAlignment(Pos.CENTER);

        // Assemblage
        panel.getChildren().addAll(
                logoContainer,
                titleLabel,
                subtitleLabel,
                separator,
                formGrid,
                errorLabel,
                registerButton,
                loginLink,
                footerLabel
        );

        return wrapper;
    }

    // ===== MÉTHODES DE MESSAGE =====

    /**
     * Affiche un message d'erreur en ROUGE
     */
    public void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12; -fx-padding: 8 0 0 0; -fx-alignment: center; -fx-font-weight: 600;");
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }

    /**
     * Affiche un message de succès en VERT
     */
    public void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 12; -fx-padding: 8 0 0 0; -fx-alignment: center; -fx-font-weight: 600;");
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }

    public void clearError() {
        errorLabel.setText("");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
    }

    public void clearFields() {
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        telephoneField.clear();
        adresseField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        errorLabel.setText("");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
        registerButton.setDisable(false);
    }

    // ===== GETTERS =====

    public TextField getNomField() { return nomField; }
    public TextField getPrenomField() { return prenomField; }
    public TextField getEmailField() { return emailField; }
    public TextField getTelephoneField() { return telephoneField; }
    public TextField getAdresseField() { return adresseField; }
    public PasswordField getPasswordField() { return passwordField; }
    public PasswordField getConfirmPasswordField() { return confirmPasswordField; }
    public Button getRegisterButton() { return registerButton; }
    public Label getErrorLabel() { return errorLabel; }
    public Hyperlink getLoginLink() { return loginLink; }
    public Scene getScene() { return scene; }
    public Stage getStage() { return stage; }

    public void show() { stage.show(); }
    public void close() { stage.close(); }
}