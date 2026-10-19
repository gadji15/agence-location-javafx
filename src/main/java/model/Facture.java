package model;

import java.time.LocalDateTime;

public class Facture {
    private int idFacture;
    private int idContrat;
    private LocalDateTime dateEmission;
    private double montantTotal;
    private String statut;
    private String pdfPath;
    private LocalDateTime datePaiement;

    // ===== NOUVEAUX CHAMPS =====
    private double montantPaye;
    private double resteAPayer;

    public Facture() {}

    public Facture(int idFacture, int idContrat, LocalDateTime dateEmission,
                   double montantTotal, String statut, String pdfPath, LocalDateTime datePaiement) {
        this.idFacture = idFacture;
        this.idContrat = idContrat;
        this.dateEmission = dateEmission;
        this.montantTotal = montantTotal;
        this.statut = statut;
        this.pdfPath = pdfPath;
        this.datePaiement = datePaiement;
    }

    public Facture(int idContrat, LocalDateTime dateEmission,
                   double montantTotal, String statut, String pdfPath) {
        this.idContrat = idContrat;
        this.dateEmission = dateEmission;
        this.montantTotal = montantTotal;
        this.statut = statut;
        this.pdfPath = pdfPath;
    }

    // Getters et Setters
    public int getIdFacture() { return idFacture; }
    public void setIdFacture(int idFacture) { this.idFacture = idFacture; }

    public int getIdContrat() { return idContrat; }
    public void setIdContrat(int idContrat) { this.idContrat = idContrat; }

    public LocalDateTime getDateEmission() { return dateEmission; }
    public void setDateEmission(LocalDateTime dateEmission) { this.dateEmission = dateEmission; }

    public double getMontantTotal() { return montantTotal; }
    public void setMontantTotal(double montantTotal) { this.montantTotal = montantTotal; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getPdfPath() { return pdfPath; }
    public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }

    public LocalDateTime getDatePaiement() { return datePaiement; }
    public void setDatePaiement(LocalDateTime datePaiement) { this.datePaiement = datePaiement; }

    // ===== GETTERS ET SETTERS POUR LES NOUVEAUX CHAMPS =====
    public double getMontantPaye() { return montantPaye; }
    public void setMontantPaye(double montantPaye) { this.montantPaye = montantPaye; }

    public double getResteAPayer() { return resteAPayer; }
    public void setResteAPayer(double resteAPayer) { this.resteAPayer = resteAPayer; }

    @Override
    public String toString() {
        return "Facture #" + idFacture + " - Contrat #" + idContrat + " - " + montantTotal + " FCFA";
    }
}