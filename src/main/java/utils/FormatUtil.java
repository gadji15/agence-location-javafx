package utils;

import java.text.NumberFormat;
import java.util.Locale;

public class FormatUtil {

    /**
     * Formate un montant en FCFA
     */
    public static String formatFCFA(double montant) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.FRANCE);
        format.setMaximumFractionDigits(0);
        return format.format(montant) + " FCFA";
    }

    /**
     * Formate un montant en FCFA avec décimales
     */
    public static String formatFCFADecimal(double montant) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.FRANCE);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return format.format(montant) + " FCFA";
    }

    /**
     * Formate un montant sans le suffixe FCFA
     */
    public static String formatMontant(double montant) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.FRANCE);
        format.setMaximumFractionDigits(0);
        return format.format(montant);
    }
}