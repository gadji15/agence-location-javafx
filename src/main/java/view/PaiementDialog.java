package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Facture;
import model.Paiement;
import utils.FormatUtil;

import java.time.LocalDateTime;

public class PaiementDialog {

    private Stage dialogStage;
    private boolean valide = false;
    private Paiement paiement;
    private Facture facture;

    // Composants
    private ComboBox<String> modeComboBox;
    private TextField montantField;
    private TextField referenceField;
    private TextArea notesArea;
    private Label infoLabel;
    private Label errorLabel;
    private Label resteLabel;

    public PaiementDialog(Facture facture) {
        this.facture = facture;
        this.paiement = new Paiement();

        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Enregistrer un paiement");

        VBox root = createUI();

        javafx.scene.Scene scene = new javafx.scene.Scene(root, 500, 620);

        // Charger le CSS (optionnel)
        try {
            String cssPath = getClass().getResource("/css/admin-dashboard.css").toExternalForm();
            if (cssPath != null) {
                scene.getStylesheets().add(cssPath);
            }
        } catch (Exception e) {
            // CSS optionnel
        }

        dialogStage.setScene(scene);
    }

    private VBox createUI() {
        VBox root = new VBox();
        root.setSpacing(15);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: white;");

        // ===== TITRE =====
        Label titleLabel = new Label("💰 Enregistrer un paiement");
        titleLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // ===== INFO FACTURE =====
        infoLabel = new Label("Facture #" + facture.getIdFacture());
        infoLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #7f8c8d;");

        // ⚠️ IMPORTANT : créer montantField AVANT createRecapBox()
        montantField = new TextField();
        montantField.setPromptText("Ex: 135000");
        montantField.setPrefWidth(300);
        montantField.setPrefHeight(38);
        montantField.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 6; " +
                "-fx-border-color: #dde1e6; -fx-border-radius: 6; -fx-border-width: 1; " +
                "-fx-padding: 0 12 0 12; -fx-font-size: 13;");

        // ===== RÉCAPITULATIF (utilise montantField) =====
        VBox recapBox = createRecapBox();

        // ===== MODE DE PAIEMENT =====
        VBox modeBox = new VBox();
        modeBox.setSpacing(5);

        Label modeLabel = new Label("Mode de paiement *");
        modeLabel.setStyle("-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #34495e;");

        modeComboBox = new ComboBox<>();
        modeComboBox.getItems().addAll(
                "💵 ESPECE",
                "💳 CARTE",
                "🏦 VIREMENT",
                "📝 CHEQUE",
                "🌊 WAVE",
                "🟠 ORANGE_MONEY",
                "🔴 FREE_MONEY"
        );
        modeComboBox.setPromptText("Sélectionner un mode...");
        modeComboBox.setPrefWidth(300);
        modeComboBox.setStyle("-fx-pref-height: 38; -fx-background-color: #f8f9fa; " +
                "-fx-background-radius: 6; -fx-border-color: #dde1e6; " +
                "-fx-border-radius: 6; -fx-border-width: 1; -fx-font-size: 13;");

        modeBox.getChildren().addAll(modeLabel, modeComboBox);

        // ===== MONTANT PAYÉ (conteneur) =====
        VBox montantBox = new VBox();
        montantBox.setSpacing(5);

        Label montantLabel = new Label("Montant payé (FCFA) *");
        montantLabel.setStyle("-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #34495e;");

        montantBox.getChildren().addAll(montantLabel, montantField);

        // ===== RÉFÉRENCE =====
        VBox refBox = new VBox();
        refBox.setSpacing(5);

        Label refLabel = new Label("Référence de transaction");
        refLabel.setStyle("-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #34495e;");

        referenceField = new TextField();
        referenceField.setPromptText("Ex: TRX-2026-001");
        referenceField.setPrefWidth(300);
        referenceField.setPrefHeight(38);
        referenceField.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 6; " +
                "-fx-border-color: #dde1e6; -fx-border-radius: 6; -fx-border-width: 1; " +
                "-fx-padding: 0 12 0 12; -fx-font-size: 13;");

        refBox.getChildren().addAll(refLabel, referenceField);

        // ===== NOTES =====
        VBox notesBox = new VBox();
        notesBox.setSpacing(5);

        Label notesLabel = new Label("Notes");
        notesLabel.setStyle("-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #34495e;");

        notesArea = new TextArea();
        notesArea.setPromptText("Remarques éventuelles...");
        notesArea.setPrefWidth(300);
        notesArea.setPrefHeight(60);
        notesArea.setWrapText(true);
        notesArea.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 6; " +
                "-fx-border-color: #dde1e6; -fx-border-radius: 6; -fx-border-width: 1; " +
                "-fx-padding: 8; -fx-font-size: 13;");

        notesBox.getChildren().addAll(notesLabel, notesArea);

        // ===== MESSAGE D'ERREUR =====
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12; -fx-font-weight: 600;");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);

        // ===== BOUTONS =====
        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button cancelButton = new Button("❌ Annuler");
        cancelButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                "-fx-pref-height: 40; -fx-padding: 0 25; -fx-background-radius: 6; " +
                "-fx-cursor: hand; -fx-font-size: 14;");
        cancelButton.setOnAction(e -> dialogStage.close());

        Button saveButton = new Button("💾 Enregistrer");
        saveButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                "-fx-pref-height: 40; -fx-padding: 0 25; -fx-background-radius: 6; " +
                "-fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 14;");
        saveButton.setOnAction(e -> enregistrerPaiement());

        buttonBox.getChildren().addAll(cancelButton, saveButton);

        // ===== ASSEMBLAGE =====
        root.getChildren().addAll(
                titleLabel,
                infoLabel,
                recapBox,
                modeBox,
                montantBox,
                refBox,
                notesBox,
                errorLabel,
                buttonBox
        );

        return root;
    }

    private VBox createRecapBox() {
        VBox recapBox = new VBox();
        recapBox.setSpacing(8);
        recapBox.setPadding(new Insets(15));
        recapBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; " +
                "-fx-border-color: #e0e5ec; -fx-border-radius: 8; -fx-border-width: 1;");

        Label recapTitle = new Label("📋 Récapitulatif");
        recapTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Montant total
        HBox totalRow = new HBox();
        totalRow.setSpacing(10);
        Label totalLabel = new Label("Montant total :");
        totalLabel.setStyle("-fx-text-fill: #7f8c8d;");
        Label totalValue = new Label(FormatUtil.formatFCFA(facture.getMontantTotal()));
        totalValue.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        totalRow.getChildren().addAll(totalLabel, spacer1, totalValue);

        // Montant payé
        HBox payeRow = new HBox();
        payeRow.setSpacing(10);
        Label payeLabel = new Label("Déjà payé :");
        payeLabel.setStyle("-fx-text-fill: #7f8c8d;");
        Label payeValue = new Label(FormatUtil.formatFCFA(facture.getMontantPaye()));
        payeValue.setStyle("-fx-font-weight: bold; -fx-text-fill: #3498db;");
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        payeRow.getChildren().addAll(payeLabel, spacer2, payeValue);

        // Reste à payer
        HBox resteRow = new HBox();
        resteRow.setSpacing(10);
        Label resteTextLabel = new Label("Reste à payer :");
        resteTextLabel.setStyle("-fx-text-fill: #7f8c8d;");
        double reste = facture.getMontantTotal() - facture.getMontantPaye();
        resteLabel = new Label(FormatUtil.formatFCFA(reste));
        resteLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c; -fx-font-size: 16;");
        Region spacer3 = new Region();
        HBox.setHgrow(spacer3, Priority.ALWAYS);
        resteRow.getChildren().addAll(resteTextLabel, spacer3, resteLabel);

        Separator sep = new Separator();

        recapBox.getChildren().addAll(recapTitle, totalRow, payeRow, sep, resteRow);

        // ✅ montantField existe déjà (créé dans createUI avant l'appel)
        montantField.setText(String.format("%.0f", reste));

        return recapBox;
    }

    private void enregistrerPaiement() {
        String modeStr = modeComboBox.getValue();
        String montantStr = montantField.getText().trim();
        String reference = referenceField.getText().trim();
        String notes = notesArea.getText().trim();

        // ===== VALIDATION =====
        if (modeStr == null || modeStr.isEmpty()) {
            showError("⚠️ Veuillez sélectionner un mode de paiement !");
            return;
        }

        if (montantStr.isEmpty()) {
            showError("⚠️ Veuillez saisir le montant payé !");
            return;
        }

        double montant;
        try {
            montant = Double.parseDouble(montantStr);
            if (montant <= 0) {
                showError("⚠️ Le montant doit être supérieur à 0 !");
                return;
            }
        } catch (NumberFormatException e) {
            showError("⚠️ Le montant doit être un nombre valide !");
            return;
        }

        // Vérifier que le montant ne dépasse pas le reste à payer
        double reste = facture.getMontantTotal() - facture.getMontantPaye();
        if (montant > reste) {
            showError("⚠️ Le montant ne peut pas dépasser le reste à payer (" +
                    FormatUtil.formatFCFA(reste) + ") !");
            return;
        }

        // Extraire le mode (enlever l'emoji + espace)
        String mode = modeStr.substring(modeStr.indexOf(" ") + 1);

        // Créer le paiement
        paiement.setIdFacture(facture.getIdFacture());
        paiement.setMode(mode);
        paiement.setMontantPaye(montant);
        paiement.setDevise("XOF");
        paiement.setDatePaiement(LocalDateTime.now());
        paiement.setReferenceTransaction(reference.isEmpty() ? null : reference);
        paiement.setNotes(notes.isEmpty() ? null : notes);

        valide = true;
        dialogStage.close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }

    public boolean isValide() {
        return valide;
    }

    public Paiement getPaiement() {
        return paiement;
    }

    public void showAndWait() {
        dialogStage.showAndWait();
    }
}