package utils;


import java.io.File;

public class EmailSender {

    public static boolean sendEmailWithPDF(String to, String subject, String body, String pdfPath) {
        System.out.println("========================================");
        System.out.println("📧 SIMULATION D'ENVOI D'EMAIL");
        System.out.println("   Destinataire: " + to);
        System.out.println("   Sujet: " + subject);
        System.out.println("   Corps: " + body);
        System.out.println("   Pièce jointe: " + pdfPath);

        File pdfFile = new File(pdfPath);
        if (pdfFile.exists()) {
            System.out.println("   ✅ PDF trouvé: " + pdfFile.getName());
        } else {
            System.out.println("   ⚠️ PDF non trouvé: " + pdfPath);
        }

        System.out.println("   ✅ Email envoyé avec succès (SIMULATION)");
        System.out.println("========================================");
        return true;
    }

    public static void sendReservationConfirmation(String to, String clientName, int reservationId) {
        String subject = "Confirmation de votre réservation - Agence Auto";
        String body = "Bonjour " + clientName + ",\n\n" +
                "Votre réservation N°" + reservationId + " a bien été enregistrée.\n" +
                "Vous recevrez une confirmation dès que votre demande sera traitée.\n\n" +
                "Cordialement,\n" +
                "L'équipe Agence Auto";

        sendSimpleEmail(to, subject, body);
    }

    public static boolean sendSimpleEmail(String to, String subject, String body) {
        System.out.println("========================================");
        System.out.println("📧 SIMULATION D'ENVOI D'EMAIL SIMPLE");
        System.out.println("   Destinataire: " + to);
        System.out.println("   Sujet: " + subject);
        System.out.println("   Corps: " + body);
        System.out.println("   ✅ Email envoyé avec succès (SIMULATION)");
        System.out.println("========================================");
        return true;
    }

    public static void sendFactureEmail(String to, String clientName, int factureId, String pdfPath) {
        String subject = "Votre facture N°" + factureId + " - Agence Auto";
        String body = "Bonjour " + clientName + ",\n\n" +
                "Veuillez trouver ci-joint votre facture N°" + factureId + ".\n" +
                "Montant: à consulter sur la facture.\n\n" +
                "Cordialement,\n" +
                "L'équipe Agence Auto";

        sendEmailWithPDF(to, subject, body, pdfPath);
    }

    public static void sendContratEmail(String to, String clientName, int contratId, String pdfPath) {
        String subject = "Votre contrat de location N°" + contratId + " - Agence Auto";
        String body = "Bonjour " + clientName + ",\n\n" +
                "Veuillez trouver ci-joint votre contrat de location N°" + contratId + ".\n\n" +
                "Cordialement,\n" +
                "L'équipe Agence Auto";

        sendEmailWithPDF(to, subject, body, pdfPath);
    }
}