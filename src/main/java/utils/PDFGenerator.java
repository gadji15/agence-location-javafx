package utils;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import model.Client;
import model.Contrat;
import model.Facture;
import model.Vehicule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class PDFGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String PDF_DIR = "pdfs";

    private PDFGenerator() {
    }

    public static String generateFacturePDF(Facture facture, Contrat contrat, Client client, Vehicule vehicule)
            throws IOException {
        Map<String, String> values = commonValues(contrat, client, vehicule);
        values.put("factureId", Integer.toString(facture.getIdFacture()));
        values.put("date", formatDate(facture.getDateEmission()));
        values.put("statut", facture.getStatut());
        values.put("montantTotal", formatCurrency(facture.getMontantTotal()));
        values.put("montantPaye", formatCurrency(facture.getMontantPaye()));
        values.put("resteAPayer", formatCurrency(facture.getResteAPayer()));

        return generate("facture_template.html", "facture_" + facture.getIdFacture() + ".pdf", values);
    }

    public static String generateContratPDF(Contrat contrat, Client client, Vehicule vehicule) throws IOException {
        Map<String, String> values = commonValues(contrat, client, vehicule);
        values.put("contratId", Integer.toString(contrat.getIdContrat()));
        values.put("dateSignature", formatDate(contrat.getDateSignature()));
        values.put("statut", contrat.isActif() ? "ACTIF" : "CLÔTURÉ");
        values.put("montantTotal", formatCurrency(contrat.getMontantTotal()));

        return generate("contrat_template.html", "contrat_" + contrat.getIdContrat() + ".pdf", values);
    }

    private static Map<String, String> commonValues(Contrat contrat, Client client, Vehicule vehicule) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("clientName", nullToEmpty(client.getPrenom()) + " " + nullToEmpty(client.getNom()));
        values.put("clientAdresse", nullToEmpty(client.getAdresse()).isBlank()
                ? "Adresse non renseignée" : client.getAdresse());
        values.put("clientEmail", nullToEmpty(client.getEmail()));
        values.put("clientPhone", nullToEmpty(client.getTelephone()));
        values.put("vehiculeMarque", nullToEmpty(vehicule.getMarque()));
        values.put("vehiculeModele", nullToEmpty(vehicule.getModele()));
        values.put("vehiculeImmatriculation", nullToEmpty(vehicule.getImmatriculation()));

        int jours = contrat.getNombreJours();
        if (jours <= 0 && contrat.getDateDebutEffective() != null && contrat.getDateFinEffective() != null) {
            jours = (int) java.time.temporal.ChronoUnit.DAYS.between(
                    contrat.getDateDebutEffective().toLocalDate(), contrat.getDateFinEffective().toLocalDate()) + 1;
        }
        values.put("jours", Integer.toString(Math.max(jours, 1)));

        double prixJournalier = contrat.getPrixJournalier() > 0
                ? contrat.getPrixJournalier() : vehicule.getPrixJournalier();
        values.put("prixJournalier", formatCurrency(prixJournalier));
        values.put("sousTotal", formatCurrency(contrat.getSousTotal()));
        values.put("fraisRetourDiffere", formatCurrency(contrat.getFraisRetourDiffere()));
        values.put("fraisCarburant", formatCurrency(contrat.getFraisCarburant()));
        values.put("fraisRetard", formatCurrency(contrat.getFraisRetard()));
        values.put("fraisDegradation", formatCurrency(contrat.getFraisDegradation()));
        values.put("remise", formatCurrency(contrat.getRemise()));
        values.put("dateDebut", formatDate(contrat.getDateDebutEffective()));
        values.put("dateFin", formatDate(contrat.getDateFinEffective()));
        return values;
    }

    private static String generate(String templateName, String fileName, Map<String, String> values)
            throws IOException {
        String resourcePath = "/templates/" + templateName;
        var templateUrl = PDFGenerator.class.getResource(resourcePath);
        if (templateUrl == null) {
            throw new IOException("Modèle PDF introuvable : " + resourcePath);
        }

        String html;
        try (InputStream input = templateUrl.openStream()) {
            html = new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
        for (Map.Entry<String, String> value : values.entrySet()) {
            html = html.replace("{{" + value.getKey() + "}}", escapeHtml(value.getValue()));
        }
        if (html.matches("(?s).*\\{\\{[^{}]+}}.*")) {
            throw new IOException("Le modèle " + templateName + " contient des champs non renseignés.");
        }

        Path outputDirectory = Path.of(PDF_DIR);
        Files.createDirectories(outputDirectory);
        Path outputFile = outputDirectory.resolve(fileName);
        Path temporaryFile = Files.createTempFile(outputDirectory, fileName, ".tmp");

        try {
            try (OutputStream output = Files.newOutputStream(temporaryFile)) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(html, templateUrl.toExternalForm());
                builder.toStream(output);
                builder.run();
            }
            try {
                Files.move(temporaryFile, outputFile, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(temporaryFile, outputFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException | RuntimeException e) {
            try {
                Files.deleteIfExists(temporaryFile);
            } catch (IOException cleanupException) {
                e.addSuppressed(cleanupException);
            }
            throw e;
        }

        return outputFile.toString();
    }

    private static String formatDate(LocalDateTime date) {
        return date == null ? "Non renseignée" : date.format(DATE_FORMATTER);
    }

    private static String formatCurrency(double amount) {
        return FormatUtil.formatFCFADecimal(amount).replace('\u202f', ' ').replace('\u00a0', ' ');
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String escapeHtml(String value) {
        return nullToEmpty(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
