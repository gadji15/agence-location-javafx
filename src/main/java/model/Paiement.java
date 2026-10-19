package model;

import java.time.LocalDateTime;

public class Paiement {
    private int idPaiement;
    private int idFacture;
    private String mode;
    private double montantPaye;
    private String devise;
    private LocalDateTime datePaiement;
    private String referenceTransaction;
    private String notes;

    public Paiement() {}

    public Paiement(int idFacture, String mode, double montantPaye, String devise,
                    LocalDateTime datePaiement, String referenceTransaction, String notes) {
        this.idFacture = idFacture;
        this.mode = mode;
        this.montantPaye = montantPaye;
        this.devise = devise;
        this.datePaiement = datePaiement;
        this.referenceTransaction = referenceTransaction;
        this.notes = notes;
    }

    // Getters et Setters
    public int getIdPaiement() { return idPaiement; }
    public void setIdPaiement(int idPaiement) { this.idPaiement = idPaiement; }

    public int getIdFacture() { return idFacture; }
    public void setIdFacture(int idFacture) { this.idFacture = idFacture; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public double getMontantPaye() { return montantPaye; }
    public void setMontantPaye(double montantPaye) { this.montantPaye = montantPaye; }

    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }

    public LocalDateTime getDatePaiement() { return datePaiement; }
    public void setDatePaiement(LocalDateTime datePaiement) { this.datePaiement = datePaiement; }

    public String getReferenceTransaction() { return referenceTransaction; }
    public void setReferenceTransaction(String referenceTransaction) { this.referenceTransaction = referenceTransaction; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}