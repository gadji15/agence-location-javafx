package utils;



import model.Client;
import model.Contrat;
import model.Facture;
import model.Vehicule;

import java.io.*;
import java.time.format.DateTimeFormatter;

public class PDFGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String PDF_DIR = "pdfs/";

    static {
        File dir = new File(PDF_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Génère une facture PDF (version simplifiée - fichier texte)
     */
    public static String generateFacturePDF(Facture facture, Contrat contrat, Client client, Vehicule vehicule) {
        String fileName = PDF_DIR + "facture_" + facture.getIdFacture() + ".txt";

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {

            writer.println("========================================");
            writer.println("              FACTURE                  ");
            writer.println("========================================");
            writer.println();
            writer.println("Agence Auto - Location de véhicules");
            writer.println("123 Avenue de la République, Dakar");
            writer.println("Tél: +221 33 123 45 67");
            writer.println("Email: contact@agenceauto.sn");
            writer.println();
            writer.println("----------------------------------------");
            writer.println("N° Facture: " + facture.getIdFacture());
            writer.println("Date d'émission: " + facture.getDateEmission().format(DATE_FORMATTER));
            writer.println("Statut: " + facture.getStatut());
            writer.println();
            writer.println("----------------------------------------");
            writer.println("CLIENT");
            writer.println(client.getPrenom() + " " + client.getNom());
            writer.println(client.getAdresse() != null ? client.getAdresse() : "Adresse non renseignée");
            writer.println("Email: " + client.getEmail());
            writer.println("Tél: " + client.getTelephone());
            writer.println();
            writer.println("----------------------------------------");
            writer.println("VÉHICULE");
            writer.println(vehicule.getMarque() + " " + vehicule.getModele());
            writer.println("Immatriculation: " + vehicule.getImmatriculation());
            writer.println();
            writer.println("----------------------------------------");
            writer.println("DÉTAILS");

            int jours = 1;
            if (contrat.getDateDebutEffective() != null && contrat.getDateFinEffective() != null) {
                jours = (int) java.time.Duration.between(contrat.getDateDebutEffective(), contrat.getDateFinEffective()).toDays() + 1;
            }

            writer.println("Location: " + vehicule.getMarque() + " " + vehicule.getModele());
            writer.println("Durée: " + jours + " jour(s)");
            writer.println("Prix journalier: " + String.format("%.2f €", vehicule.getPrixJournalier()));
            writer.println("TOTAL: " + String.format("%.2f €", facture.getMontantTotal()));
            writer.println();
            writer.println("----------------------------------------");
            writer.println("TOTAL À PAYER: " + String.format("%.2f €", facture.getMontantTotal()));
            writer.println();
            writer.println("Merci de votre confiance !");

            System.out.println("✅ PDF généré: " + fileName);

        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la génération du PDF: " + e.getMessage());
            e.printStackTrace();
        }

        return fileName;
    }

    /**
     * Génère un contrat PDF (version simplifiée - fichier texte)
     */
    public static String generateContratPDF(Contrat contrat, Client client, Vehicule vehicule) {
        String fileName = PDF_DIR + "contrat_" + contrat.getIdContrat() + ".txt";

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {

            writer.println("========================================");
            writer.println("          CONTRAT DE LOCATION          ");
            writer.println("========================================");
            writer.println();
            writer.println("Agence Auto - Location de véhicules");
            writer.println("123 Avenue de la République, Dakar");
            writer.println("Tél: +221 33 123 45 67");
            writer.println();
            writer.println("----------------------------------------");
            writer.println("N° Contrat: " + contrat.getIdContrat());
            writer.println("Date de signature: " + contrat.getDateSignature().format(DATE_FORMATTER));
            writer.println("Statut: " + (contrat.isActif() ? "Actif" : "Clôturé"));
            writer.println();
            writer.println("----------------------------------------");
            writer.println("CLIENT");
            writer.println(client.getPrenom() + " " + client.getNom());
            writer.println(client.getAdresse() != null ? client.getAdresse() : "Adresse non renseignée");
            writer.println("Email: " + client.getEmail());
            writer.println("Tél: " + client.getTelephone());
            writer.println();
            writer.println("----------------------------------------");
            writer.println("VÉHICULE");
            writer.println(vehicule.getMarque() + " " + vehicule.getModele());
            writer.println("Immatriculation: " + vehicule.getImmatriculation());
            writer.println();
            writer.println("----------------------------------------");
            writer.println("PÉRIODE DE LOCATION");
            writer.println("Date de début: " + contrat.getDateDebutEffective().format(DATE_FORMATTER));
            writer.println("Date de fin: " + contrat.getDateFinEffective().format(DATE_FORMATTER));
            writer.println();
            writer.println("MONTANT TOTAL: " + String.format("%.2f €", contrat.getMontantTotal()));
            writer.println();
            writer.println("----------------------------------------");
            writer.println("CONDITIONS GÉNÉRALES");
            writer.println("1. Le véhicule doit être restitué dans l'état où il a été pris.");
            writer.println("2. Le kilométrage final sera contrôlé lors de la restitution.");
            writer.println("3. Toute dégradation du véhicule sera facturée au client.");
            writer.println("4. Le carburant doit être rendu avec le même niveau.");
            writer.println();
            writer.println("Signature du client: ___________________");

            System.out.println("✅ PDF généré: " + fileName);

        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la génération du PDF: " + e.getMessage());
            e.printStackTrace();
        }

        return fileName;
    }
}