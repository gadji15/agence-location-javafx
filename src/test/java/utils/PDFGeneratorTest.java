package utils;

import model.Client;
import model.Contrat;
import model.Facture;
import model.Vehicule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PDFGeneratorTest {

    private Path generatedFile;

    @AfterEach
    void removeGeneratedPdf() throws IOException {
        if (generatedFile != null) {
            Files.deleteIfExists(generatedFile);
        }
    }

    @Test
    void generateFactureCreatesPdfFile() throws IOException {
        int id = uniqueId();
        Facture facture = new Facture();
        facture.setIdFacture(id);
        facture.setDateEmission(LocalDateTime.of(2026, 9, 19, 15, 1));
        facture.setStatut("PAYEE");
        facture.setMontantTotal(32000);
        facture.setMontantPaye(32000);
        facture.setResteAPayer(0);

        generatedFile = Path.of(PDFGenerator.generateFacturePDF(
                facture, sampleContrat(), sampleClient(), sampleVehicule()));

        assertPdfFile(generatedFile);
    }

    @Test
    void generateContratCreatesPdfFile() throws IOException {
        Contrat contrat = sampleContrat();
        contrat.setIdContrat(uniqueId());

        generatedFile = Path.of(PDFGenerator.generateContratPDF(
                contrat, sampleClient(), sampleVehicule()));

        assertPdfFile(generatedFile);
    }

    private static void assertPdfFile(Path path) throws IOException {
        assertTrue(Files.isRegularFile(path));
        byte[] header = Files.readAllBytes(path);
        assertTrue(header.length > 5);
        assertEquals("%PDF-", new String(header, 0, 5, StandardCharsets.US_ASCII));
    }

    private static int uniqueId() {
        return (int) (System.nanoTime() & 0x7fffffff);
    }

    private static Contrat sampleContrat() {
        Contrat contrat = new Contrat();
        contrat.setDateSignature(LocalDateTime.of(2026, 9, 19, 14, 59));
        contrat.setDateDebutEffective(LocalDateTime.of(2026, 9, 20, 10, 0));
        contrat.setDateFinEffective(LocalDateTime.of(2026, 9, 25, 10, 0));
        contrat.setNombreJours(6);
        contrat.setPrixJournalier(4500);
        contrat.setSousTotal(27000);
        contrat.setFraisDegradation(5000);
        contrat.setMontantTotal(32000);
        contrat.setActif(false);
        return contrat;
    }

    private static Client sampleClient() {
        return new Client("Admin", "System", "admin@example.com", "0102030405", "Dakar", true);
    }

    private static Vehicule sampleVehicule() {
        return new Vehicule("Peugeot", "208", 2024, "AB-123-CD", "Essence",
                0, 4500, "DISPONIBLE", null, true);
    }
}
