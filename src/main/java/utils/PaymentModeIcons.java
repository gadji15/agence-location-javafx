package utils;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

public final class PaymentModeIcons {

    private PaymentModeIcons() {
    }

    public static Node graphic(String mode) {
        FontIcon icon = new FontIcon(iconFor(mode));
        icon.setIconSize(16);
        icon.setIconColor(javafx.scene.paint.Color.web(colorFor(mode)));

        Label label = new Label(labelFor(mode));
        label.setStyle("-fx-text-fill: #34495e;");

        HBox graphic = new HBox(8, icon, label);
        graphic.setAlignment(Pos.CENTER_LEFT);
        return graphic;
    }

    public static FontAwesomeSolid iconFor(String mode) {
        if (mode == null) {
            return FontAwesomeSolid.MONEY_BILL_ALT;
        }
        return switch (mode) {
            case "ESPECE" -> FontAwesomeSolid.MONEY_BILL_ALT;
            case "CARTE" -> FontAwesomeSolid.CREDIT_CARD;
            case "VIREMENT" -> FontAwesomeSolid.UNIVERSITY;
            case "CHEQUE" -> FontAwesomeSolid.FILE_ALT;
            case "MOBILE_MONEY", "WAVE", "ORANGE_MONEY", "FREE_MONEY" -> FontAwesomeSolid.MOBILE_ALT;
            default -> FontAwesomeSolid.MONEY_BILL_ALT;
        };
    }

    public static String labelFor(String mode) {
        if (mode == null) {
            return "Autre";
        }
        return switch (mode) {
            case "ESPECE" -> "Espèces";
            case "CARTE" -> "Carte bancaire";
            case "VIREMENT" -> "Virement";
            case "CHEQUE" -> "Chèque";
            case "MOBILE_MONEY" -> "Paiement mobile";
            case "WAVE" -> "Wave";
            case "ORANGE_MONEY" -> "Orange Money";
            case "FREE_MONEY" -> "Free Money";
            default -> mode.replace('_', ' ');
        };
    }

    private static String colorFor(String mode) {
        if (mode == null) {
            return "#64748b";
        }
        return switch (mode) {
            case "ESPECE" -> "#2e8b57";
            case "CARTE" -> "#2563eb";
            case "VIREMENT" -> "#64748b";
            case "CHEQUE" -> "#8b5cf6";
            case "WAVE" -> "#1d4ed8";
            case "ORANGE_MONEY" -> "#ea580c";
            case "FREE_MONEY" -> "#dc2626";
            case "MOBILE_MONEY" -> "#0891b2";
            default -> "#64748b";
        };
    }
}
