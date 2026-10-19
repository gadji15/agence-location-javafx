package utils;


import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import org.controlsfx.control.Notifications;

import java.util.Optional;

public class NotificationManager {

    /**
     * Affiche une notification de succès (popup en bas à droite)
     */
    public static void showSuccess(String title, String message) {
        Notifications.create()
                .title(title)
                .text(message)
                .showInformation();
    }

    /**
     * Affiche une notification d'erreur
     */
    public static void showError(String title, String message) {
        Notifications.create()
                .title(title)
                .text(message)
                .showError();
    }

    /**
     * Affiche une notification d'information
     */
    public static void showInfo(String title, String message) {
        Notifications.create()
                .title(title)
                .text(message)
                .showInformation();
    }

    /**
     * Affiche une notification d'avertissement
     */
    public static void showWarning(String title, String message) {
        Notifications.create()
                .title(title)
                .text(message)
                .showWarning();
    }

    /**
     * Affiche une boîte de dialogue de confirmation
     * @return true si l'utilisateur a cliqué sur OK, false sinon
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Affiche une boîte de dialogue de confirmation avec des boutons personnalisés
     */
    public static boolean showConfirmation(String title, String message, String okText, String cancelText) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType okButton = new ButtonType(okText, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType(cancelText, ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(okButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == okButton;
    }

    /**
     * Affiche une boîte de dialogue d'information
     */
    public static void showInformation(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une boîte de dialogue d'erreur
     */
    public static void showErrorMessage(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une notification de réservation créée
     */
    public static void notifyReservationCreated(int reservationId) {
        showSuccess("✅ Réservation créée",
                "Votre réservation N°" + reservationId + " a été créée avec succès !");
    }

    /**
     * Affiche une notification de réservation annulée
     */
    public static void notifyReservationCancelled(int reservationId) {
        showInfo("🚫 Réservation annulée",
                "La réservation N°" + reservationId + " a été annulée avec succès.");
    }

    /**
     * Affiche une notification de facture générée
     */
    public static void notifyFactureGenerated(int factureId) {
        showSuccess("💰 Facture générée",
                "La facture N°" + factureId + " a été générée avec succès !");
    }

    /**
     * Affiche une notification de contrat généré
     */
    public static void notifyContratGenerated(int contratId) {
        showSuccess("📄 Contrat généré",
                "Le contrat N°" + contratId + " a été généré avec succès !");
    }
}